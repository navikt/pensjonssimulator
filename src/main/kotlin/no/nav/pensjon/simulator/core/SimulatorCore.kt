package no.nav.pensjon.simulator.core

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpBeregner
import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpPeriodeConverter
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.grunnlag.LivsvarigOffentligAfpYtelseMedDelingstall
import no.nav.pensjon.simulator.afp.privat.PrivatAfpBeregner
import no.nav.pensjon.simulator.afp.privat.PrivatAfpSpec
import no.nav.pensjon.simulator.alder.Alder
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
import no.nav.pensjon.simulator.core.result.RegisterData
import no.nav.pensjon.simulator.core.result.ResultPreparerSpec
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimuleringResultPreparer
import no.nav.pensjon.simulator.core.spec.InnvilgetLivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.spec.SpecOverrider
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoRepopulator
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.sak.SakService
import no.nav.pensjon.simulator.tech.metric.Metrics
import no.nav.pensjon.simulator.ytelse.YtelseService
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Corresponds to AbstraktSimulerAPFra2011Command, SimulerFleksibelAPCommand, SimulerAFPogAPCommand, SimulerEndringAvAPCommand
 */
@Component
class SimulatorCore(
    private val kravhodeCreator: KravhodeCreator,
    private val kravhodeUpdater: KravhodeUpdater,
    private val knekkpunktFinder: KnekkpunktFinder,
    private val alderspensjonVilkaarsproeverOgBeregner: AlderspensjonVilkaarsproeverOgBeregner,
    private val privatAfpBeregner: PrivatAfpBeregner,
    private val generalPersonService: GeneralPersonService,
    private val personService: PersonService, // pensjonsrelaterte persondata
    private val specOverrider: SpecOverrider,
    private val sakService: SakService,
    private val ytelseService: YtelseService,
    private val offentligAfpBeregner: OffentligAfpBeregner,
    private val grunnbeloepService: GrunnbeloepService,
    private val generelleDataHolder: GenerelleDataHolder,
    private val resultPreparer: SimuleringResultPreparer
) : UttakAlderDiscriminator {

    private val log = KotlinLogging.logger {}

    // AbstraktSimulerAPFra2011Command.execute + overrides in SimulerFleksibelAPCommand & SimulerAFPogAPCommand & SimulerEndringAvAPCommand
    override fun simuler(initialSpec: SimuleringSpec): SimulatorOutput {
        Metrics.countSimuleringstype(type = initialSpec.type.toString())
        val gjelderEndring = initialSpec.gjelderEndring()

        if (gjelderEndring) {
            EndringValidator.validate(initialSpec)
        }

        log.debug { "Simulator steg 1 - Hent løpende ytelser" }

        val personVirkningDatoCombo: FoersteVirkningDatoCombo? =
            initialSpec.pid?.let(sakService::personVirkningDato) // null if forenklet simulering

        val person: PenPerson? = initialSpec.pid?.let(personService::person)
        //?.also { validateUfoeregrad(it, initialSpec) } <--- awaiting introducing this - plus logic needs to be refined

        val foedselsdato: LocalDate? = person?.foedselsdato
        val ytelser: LoependeYtelser = ytelseService.getLoependeYtelser(initialSpec)
        val spec: SimuleringSpec = specOverrider.behandleSpec(initialSpec, ytelser, foedselsdato)

        if (gjelderEndring) {
            EndringValidator.validateRequestBasedOnLoependeYtelser(spec, ytelser.forrigeAlderspensjonBeregningResultat)
        }

        log.debug { "Simulator steg 2 - Opprett kravhode" }

        val grunnbeloep: Int = grunnbeloepService.naavaerendeGrunnbeloep()

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
                simuleringSpec = spec,
                erFoerstegangsberegning = ytelser.forrigeAlderspensjonBeregningResultat == null
            )
        )

        log.debug { "Simulator steg 5 - Finn knekkpunkter" }

        val knekkpunktMap = knekkpunktFinder.finnKnekkpunkter(
            KnekkpunktSpec(
                kravhode,
                simulering = spec,
                soekerVirkningFom = ytelser.soekerVirkningFom,
                avdoedVirkningFom = ytelser.avdoed?.foersteVirkningsdato,
                forrigeAlderspensjonBeregningResultatVirkningFom =
                    ytelser.forrigeAlderspensjonBeregningResultat?.virkFomLd,
                sakId = kravhode.sakId
            )
        )

        log.debug { "Simulator steg 6 - Beregn offentlig AFP" }
        val simulertOffentligAfp: OffentligAfpResult =
            if (spec.livsvarigOffentligAfp?.innvilgetAfp == null)
                offentligAfpBeregner.beregnAfp(spec, kravhode, ytelser, foedselsdato, pid = person?.pid)
            else
                OffentligAfpResult(tidsbegrenset = null, livsvarig = null, kravhode)

        kravhode = simulertOffentligAfp.kravhode // NB: kravhode reassigned (but only for pre-2025)

        log.debug { "Simulator steg 7 - Vilkårsprøv og beregn alderspensjon" }

        val vilkaarsproevOgBeregnAlderspensjonResult =
            alderspensjonVilkaarsproeverOgBeregner.vilkaarsproevOgBeregnAlder(
                AlderspensjonVilkaarsproeverBeregnerSpec(
                    kravhode = kravhode,
                    knekkpunkter = knekkpunktMap,
                    simulering = spec,
                    sokerForsteVirk = ytelser.soekerVirkningFom,
                    avdodForsteVirk = ytelser.avdoed?.foersteVirkningsdato,
                    forrigeVilkarsvedtakListe = ytelser.forrigeVedtakListe,
                    forrigeAlderBeregningsresultat = ytelser.forrigeAlderspensjonBeregningResultat,
                    sisteBeregning = ytelser.sisteBeregning,
                    afpPrivatBeregningsresultater = privatAfpBeregningResultatListe,
                    gjeldendeAfpPrivatBeregningsresultat = gjeldendePrivatAfpBeregningResultat,
                    forsteVirkAfpPrivat = ytelser.privatAfpVirkningFom,
                    afpOffentligLivsvarigBeregningsresultat = simulertOffentligAfp.livsvarig,
                    isHentPensjonsbeholdninger = spec.isHentPensjonsbeholdninger,
                    kravGjelder = kravhode.gjelder ?: KravGjelder.FORSTEG_BH,
                    sakId = kravhode.sakId,
                    sakType = kravhode.sakType,
                    ignoreAvslag = spec.ignoreAvslag,
                    onlyVilkaarsproeving = spec.onlyVilkaarsproeving
                )
            )

        log.debug { "Simulator steg 8 - Opprett output" }

        val registerData = RegisterData(
            sisteLignetInntektAar = initialSpec.registerData?.sisteLignetInntektAar,
            sisteGyldigeOpptjeningAar = generelleDataHolder.getSisteGyldigeOpptjeningsaar(),
            soekerFoedselsdato = foedselsdato,
            grunnbeloep = grunnbeloep
        )

        val output: SimulatorOutput =
            if (spec.type == SimuleringTypeEnum.AFP_FPP) // ref. PEN: SimulerAFPogAPCommand.opprettOutput
                SimulatorOutput().apply {
                    tidsbegrensetOffentligAfp = simulertOffentligAfp.tidsbegrenset?.simuleringResult
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
                        tidsbegrensetOffentligAfpBeregningResultat = simulertOffentligAfp.tidsbegrenset?.simuleringResult,
                        livsvarigOffentligAfpBeregningResultatListe =
                            LivsvarigOffentligAfpPeriodeConverter.aarligePerioder(
                                result = innvilgetLivsvarigOffentligAfp(spec, foedselsdato)
                                    ?: simulertOffentligAfp.livsvarig,
                                foedselMaaned = foedselsdato?.monthValue
                            ),
                        pensjonBeholdningPeriodeListe = vilkaarsproevOgBeregnAlderspensjonResult.pensjonsbeholdningPerioder,
                        outputSimulertBeregningsInformasjonForAllKnekkpunkter = spec.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter,
                        registerData
                    )
                )

        return if (spec.erAnonym)
            output
        else
            output.apply {
                this.persongrunnlag = kravhode.hentPersongrunnlagForSoker()
                this.heltUttakDato = spec.heltUttakDato
            }
    }

    override fun fetchFoedselsdato(pid: Pid): LocalDate =
        generalPersonService.foedselsdato(pid)

    private companion object {

        private fun innvilgetLivsvarigOffentligAfp(
            spec: SimuleringSpec,
            foedselsdato: LocalDate?
        ): LivsvarigOffentligAfpResult? =
            spec.livsvarigOffentligAfp?.innvilgetAfp?.let {
                livsvarigOffentligAfpResult(innvilgetAfp = it, pid = spec.pid, foedselsdato)
            }

        private fun livsvarigOffentligAfpResult(
            innvilgetAfp: InnvilgetLivsvarigOffentligAfpSpec,
            pid: Pid?,
            foedselsdato: LocalDate?
        ) =
            LivsvarigOffentligAfpResult(
                pid = pid?.value ?: "",
                afpYtelseListe = listOf(
                    LivsvarigOffentligAfpYtelseMedDelingstall(
                        pensjonBeholdning = 0, // not used
                        afpYtelsePerAar = innvilgetAfp.aarligBruttoBeloep,
                        delingstall = 0.0, // not used
                        gjelderFom = innvilgetAfp.uttakFom,
                        gjelderFomAlder = foedselsdato?.let {
                            Alder.from(foedselsdato = it, dato = innvilgetAfp.uttakFom)
                        } ?: Alder(aar = 0, maaneder = 0) // cannot deduce alder without fødselsdato
                    ))
            )
    }
}