package no.nav.pensjon.simulator.core

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpPeriodeConverter
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpService
import no.nav.pensjon.simulator.core.afp.offentlig.OffentligAfpConstants.OVERGANG_PRE2025_TIL_LIVSVARIG_OFFENTLIG_AFP_FOEDSEL_AAR
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpBeregner
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpEndringBeregner
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpResult
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpTerminator.terminatePre2025OffentligAfp
import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpBeregner
import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSpec
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.beregn.AlderspensjonVilkaarsproeverBeregnerSpec
import no.nav.pensjon.simulator.core.beregn.AlderspensjonVilkaarsproeverOgBeregner
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.endring.EndringValidator
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktFinder
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktSpec
import no.nav.pensjon.simulator.core.krav.*
import no.nav.pensjon.simulator.core.result.ResultPreparerSpec
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimuleringResultPreparer
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoRepopulator
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.sak.SakService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import no.nav.pensjon.simulator.ytelse.YtelseService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

/**
 * Corresponds to AbstraktSimulerAPFra2011Command, SimulerFleksibelAPCommand, SimulerAFPogAPCommand, SimulerEndringAvAPCommand
 */
@Component
class SimulatorCore(
    private val context: SimulatorContext,
    private val kravhodeCreator: KravhodeCreator,
    private val kravhodeUpdater: KravhodeUpdater,
    private val knekkpunktFinder: KnekkpunktFinder,
    private val alderspensjonVilkaarsproeverOgBeregner: AlderspensjonVilkaarsproeverOgBeregner,
    private val privatAfpBeregner: PrivatAfpBeregner,
    private val pre2025OffentligAfpBeregner: Pre2025OffentligAfpBeregner,
    private val pre2025OffentligAfpEndringBeregner: Pre2025OffentligAfpEndringBeregner,
    private val generalPersonService: GeneralPersonService,
    private val personService: PersonService,
    private val sakService: SakService,
    private val ytelseService: YtelseService,
    private val livsvarigOffentligAfpService: LivsvarigOffentligAfpService,
    private val normalderService: NormertPensjonsalderService,
    private val resultPreparer: SimuleringResultPreparer,
    private val time: Time
) : UttakAlderDiscriminator {

    private val log = KotlinLogging.logger {}

    // AbstraktSimulerAPFra2011Command.execute + overrides in SimulerFleksibelAPCommand & SimulerAFPogAPCommand & SimulerEndringAvAPCommand
    override fun simuler(initialSpec: SimuleringSpec): SimulatorOutput {
        val gjelderEndring = initialSpec.gjelderEndring()

        if (gjelderEndring) {
            EndringValidator.validate(initialSpec)
        }

        val grunnbeloep: Int = fetchGrunnbeloep()

        log.debug { "Simulator steg 1 - Hent løpende ytelser" }

        val personVirkningDatoCombo: FoersteVirkningDatoCombo? =
            initialSpec.pid?.let(sakService::personVirkningDato) // null if forenklet simulering

        val person: PenPerson? = initialSpec.pid
            ?.let(personService::person)
        //?.also { validateUfoeregrad(it, initialSpec) } <--- awaiting introducing this - plus logic needs to be refined

        val foedselsdato: LocalDate? = person?.foedselsdato
        val ytelser: LoependeYtelser = ytelseService.getLoependeYtelser(initialSpec)

        val spec: SimuleringSpec =
            if (initialSpec.gjelderPre2025OffentligAfp())
            // Ref. SimulerAFPogAPCommand.hentLopendeYtelser
                initialSpec.withHeltUttakDato(foedselsdato?.let {
                    uttakDato(foedselsdato = it, uttakAlder = normalderService.normalder(it))
                })
            else
                initialSpec


        if (gjelderEndring) {
            EndringValidator.validateRequestBasedOnLoependeYtelser(spec, ytelser.forrigeAlderspensjonBeregningResultat)
        }

        log.debug { "Simulator steg 2 - Opprett kravhode" }

        var kravhode: Kravhode = kravhodeCreator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(
                simulatorInput = spec,
                forrigeAlderspensjonBeregningResult = ytelser.forrigeAlderspensjonBeregningResultat,
                grunnbeloep
            ),
            person,
            virkningDatoGrunnlagListe = personVirkningDatoCombo?.foersteVirkningDatoGrunnlagListe.orEmpty()
        )

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        log.debug { "Simulator steg 3 - Beregn privat AFP" }

        var privatAfpBeregningResultatListe: MutableList<BeregningsResultatAfpPrivat> = mutableListOf()
        var gjeldendePrivatAfpBeregningResultat: BeregningsResultatAfpPrivat? = null

        if (ytelser.privatAfpVirkningFom != null) {
            val response = privatAfpBeregner.beregnPrivatAfp(
                PrivatAfpSpec(
                    kravhode,
                    virkningFom = ytelser.privatAfpVirkningFom,
                    foersteUttakDato = spec.foersteUttakDato,
                    forrigePrivatAfpBeregningResult = ytelser.forrigePrivatAfpBeregningResultat as? BeregningsResultatAfpPrivat,
                    gjelderOmsorg = kravhode.hentPersongrunnlagForSoker().gjelderOmsorg,
                    sakId = kravhode.sakId
                )
            )

            gjeldendePrivatAfpBeregningResultat = response.gjeldendeBeregningsresultatAfpPrivat
            privatAfpBeregningResultatListe = response.afpPrivatBeregningsresultatListe
        }

        log.debug { "Simulator steg 4 - Oppdater kravhode før første knekkpunkt" }

        kravhode = kravhodeUpdater.updateKravhodeForFoersteKnekkpunkt(
            KravhodeUpdateSpec(
                kravhode,
                simulering = spec,
                forrigeAlderspensjonBeregningResult = ytelser.forrigeAlderspensjonBeregningResultat
            )
        )

        log.debug { "Simulator steg 5 - Finn knekkpunkter" }

        val knekkpunktMap = knekkpunktFinder.finnKnekkpunkter(
            KnekkpunktSpec(
                kravhode,
                simulering = spec,
                soekerVirkningFom = ytelser.soekerVirkningFom,
                avdoedVirkningFom = ytelser.avdoedVirkningFom,
                forrigeAlderspensjonBeregningResultatVirkningFom =
                    ytelser.forrigeAlderspensjonBeregningResultat?.virkFom?.toNorwegianLocalDate(),
                sakId = kravhode.sakId
            )
        )

        log.debug { "Simulator steg 6 - Beregn offentlig AFP" }

        val pre2025OffentligAfpResult: Pre2025OffentligAfpResult?
        val livsvarigOffentligAfpResult: LivsvarigOffentligAfpResult?

        if (spec.gjelderPre2025OffentligAfp()) {
            pre2025OffentligAfpResult = pre2025OffentligAfpBeregner.beregnAfp(
                spec,
                kravhode,
                ytelser.forrigeAlderspensjonBeregningResultat,
                grunnbeloep
            )
            kravhode = pre2025OffentligAfpResult.kravhode
            livsvarigOffentligAfpResult = null
        } else if (gjelderEndring && spec.type != SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG) {
            pre2025OffentligAfpResult =
                spec.foersteUttakDato?.let { pre2025OffentligAfpEndringBeregner.beregnAfp(kravhode, it) }
            livsvarigOffentligAfpResult = null
        } else if (simuleringTyperSomKreverTermineringAvPre2025OffentligAfp.contains(spec.type) &&
            mayHavePre2025OffentligAfp(foedselsdato)
        ) {
            pre2025OffentligAfpResult = terminatePre2025OffentligAfp(kravhode, spec.foersteUttakDato)
            livsvarigOffentligAfpResult = null
        } else {
            pre2025OffentligAfpResult = null
            livsvarigOffentligAfpResult =
                if (spec.type == SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG ||
                    spec.type == SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG) // github.com/navikt/pensjon-pen/pull/14792
                    foedselsdato?.let {
                        livsvarigOffentligAfpService.beregnAfp(
                            pid = person.pid!!,
                            foedselsdato = it,
                            forventetAarligInntektBeloep = spec.forventetInntektBeloep,
                            fremtidigeInntekter = spec.fremtidigInntektListe,
                            brukFremtidigInntekt = spec.brukFremtidigInntekt,
                            virkningDato = spec.rettTilOffentligAfpFom ?: spec.foersteUttakDato!!
                        )
                    }
                else
                    null
        }

        log.debug { "Simulator steg 7 - Vilkårsprøv og beregn alderspensjon" }

        val vilkaarsproevOgBeregnAlderspensjonResult =
            alderspensjonVilkaarsproeverOgBeregner.vilkaarsproevOgBeregnAlder(
                AlderspensjonVilkaarsproeverBeregnerSpec(
                    kravhode = kravhode,
                    knekkpunkter = knekkpunktMap,
                    simulering = spec,
                    sokerForsteVirk = ytelser.soekerVirkningFom,
                    avdodForsteVirk = ytelser.avdoedVirkningFom,
                    forrigeVilkarsvedtakListe = ytelser.forrigeVedtakListe,
                    forrigeAlderBeregningsresultat = ytelser.forrigeAlderspensjonBeregningResultat,
                    sisteBeregning = ytelser.sisteBeregning,
                    afpPrivatBeregningsresultater = privatAfpBeregningResultatListe,
                    gjeldendeAfpPrivatBeregningsresultat = gjeldendePrivatAfpBeregningResultat,
                    forsteVirkAfpPrivat = ytelser.privatAfpVirkningFom,
                    afpOffentligLivsvarigBeregningsresultat = livsvarigOffentligAfpResult,
                    isHentPensjonsbeholdninger = spec.isHentPensjonsbeholdninger,
                    kravGjelder = kravhode.gjelder ?: KravGjelder.FORSTEG_BH,
                    sakId = kravhode.sakId,
                    sakType = kravhode.sakType,
                    ignoreAvslag = spec.ignoreAvslag,
                    onlyVilkaarsproeving = spec.onlyVilkaarsproeving
                )
            )

        log.debug { "Simulator steg 8 - Opprett output" }

        val output: SimulatorOutput =
            if (spec.type == SimuleringTypeEnum.AFP_FPP) // ref. PEN: SimulerAFPogAPCommand.opprettOutput
                SimulatorOutput().apply {
                    pre2025OffentligAfp = pre2025OffentligAfpResult?.simuleringResult
                }
            else
                resultPreparer.opprettOutput(
                    ResultPreparerSpec(
                        simuleringSpec = spec,
                        kravhode = kravhode,
                        alderspensjonBeregningResultatListe = vilkaarsproevOgBeregnAlderspensjonResult.beregningsresultater,
                        privatAfpBeregningResultatListe = privatAfpBeregningResultatListe,
                        forrigeAlderspensjonBeregningResultat = ytelser.forrigeAlderspensjonBeregningResultat,
                        forrigePrivatAfpBeregningResultat = ytelser.forrigePrivatAfpBeregningResultat as? BeregningsResultatAfpPrivat,
                        pre2025OffentligAfpBeregningResultat = pre2025OffentligAfpResult?.simuleringResult,
                        livsvarigOffentligAfpBeregningResultatListe =
                            LivsvarigOffentligAfpPeriodeConverter.aarligePerioder(
                                result = livsvarigOffentligAfpResult,
                                foedselMaaned = foedselsdato?.monthValue
                            ),
                        grunnbeloep = grunnbeloep,
                        pensjonBeholdningPeriodeListe = vilkaarsproevOgBeregnAlderspensjonResult.pensjonsbeholdningPerioder,
                        outputSimulertBeregningsInformasjonForAllKnekkpunkter = spec.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter,
                        sisteGyldigeOpptjeningAar = SISTE_GYLDIGE_OPPTJENING_AAR
                    )
                )

        return if (spec.erAnonym)
            output
        else
            output.apply {
                this.foedselDato = foedselsdato
                this.persongrunnlag = kravhode.hentPersongrunnlagForSoker()
                this.heltUttakDato = spec.heltUttakDato
            }
    }

    override fun fetchFoedselsdato(pid: Pid): LocalDate =
        generalPersonService.foedselsdato(pid)

    private fun fetchGrunnbeloep(): Int =
        context.fetchGrunnbeloepListe(time.today()).satsResultater.firstOrNull()?.verdi?.toInt() ?: 0

    private companion object {
        private val simuleringTyperSomKreverTermineringAvPre2025OffentligAfp =
            EnumSet.of(SimuleringTypeEnum.ALDER, SimuleringTypeEnum.ALDER_M_AFP_PRIVAT, SimuleringTypeEnum.ALDER_M_GJEN)

        private fun mayHavePre2025OffentligAfp(foedselsdato: LocalDate?): Boolean =
            foedselsdato?.let { it.year < OVERGANG_PRE2025_TIL_LIVSVARIG_OFFENTLIG_AFP_FOEDSEL_AAR } == true
    }
}
