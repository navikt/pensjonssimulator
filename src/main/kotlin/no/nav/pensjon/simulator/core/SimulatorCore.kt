package no.nav.pensjon.simulator.core

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpService
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpPeriodeConverter
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpBeregner
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpEndringBeregner
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpResult
import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpBeregner
import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpResult
import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSpec
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.beregn.AlderspensjonBeregnerResult
import no.nav.pensjon.simulator.core.beregn.AlderspensjonVilkaarsproeverBeregnerSpec
import no.nav.pensjon.simulator.core.beregn.AlderspensjonVilkaarsproeverOgBeregner
import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.endring.EndringValidator
import no.nav.pensjon.simulator.core.exception.BeregningsmotorValidereException
import no.nav.pensjon.simulator.core.exception.ForLavtTidligUttakException
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktAarsak
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktFinder
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktSpec
import no.nav.pensjon.simulator.core.krav.*
import no.nav.pensjon.simulator.core.result.ResultPreparerSpec
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimuleringResultPreparer
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.ForKortTrygdetidException
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoRepopulator
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.normalder.NormAlderService
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.sak.SakService
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import no.nav.pensjon.simulator.ytelse.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.System.currentTimeMillis
import java.time.LocalDate
import java.time.Period
import java.util.*
import java.util.stream.Stream

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
    private val generelleDataHolder: GenerelleDataHolder,
    private val personService: PersonService,
    private val sakService: SakService,
    private val ytelseService: YtelseService,
    private val livsvarigOffentligAfpService: LivsvarigOffentligAfpService,
    private val normAlderService: NormAlderService
) : UttakAlderDiscriminator {

    private val logger = LoggerFactory.getLogger(SimulatorCore::class.java)

    // AbstraktSimulerAPFra2011Command.execute + overrides in SimulerFleksibelAPCommand & SimulerAFPogAPCommand & SimulerEndringAvAPCommand
    @Throws(
        BeregningsmotorValidereException::class,
        ForKortTrygdetidException::class,
        ForLavtTidligUttakException::class
    )
    override fun simuler(initialSpec: SimuleringSpec, flags: SimulatorFlags): SimulatorOutput {
        val gjelderEndring = initialSpec.gjelderEndring()

        if (gjelderEndring) {
            EndringValidator.validate(initialSpec)
        }

        val grunnbeloep: Int = fetchGrunnbeloep()

        logger.info("Simulator steg 1 - Hent løpende ytelser")

        val personVirkningDatoCombo: FoersteVirkningDatoCombo? =
            initialSpec.pid?.let(sakService::personVirkningDato) // null if forenklet simulering
        val person: PenPerson? = initialSpec.pid?.let(personService::person)
        val foedselsdato: LocalDate? = person?.fodselsdato?.toLocalDate()
        val ytelser: LoependeYtelser = fetchLoependeYtelser(initialSpec)

        val spec: SimuleringSpec =
            if (initialSpec.gjelderPre2025OffentligAfp())
            // Ref. SimulerAFPogAPCommand.hentLopendeYtelser
                initialSpec.withHeltUttakDato(foedselsdato?.let {
                    uttakDato(it, normAlderService.normAlder(it))
                })
            else
                initialSpec


        if (gjelderEndring) {
            EndringValidator.validateRequestBasedOnLoependeYtelser(spec, ytelser.forrigeAlderspensjonBeregningResultat)
        }

        logger.info("Simulator steg 2 - Opprett kravhode")

        var kravhode: Kravhode = opprettKravhode(
            spec = KravhodeSpec(
                simulatorInput = spec,
                forrigeAlderspensjonBeregningResult = ytelser.forrigeAlderspensjonBeregningResultat,
                grunnbeloep = grunnbeloep
            ),
            person = person,
            virkningDatoGrunnlagListe = personVirkningDatoCombo?.foersteVirkningDatoGrunnlagListe.orEmpty()
        )

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        logger.info("Simulator steg 3 - Beregn AFP Privat")

        var privatAfpBeregningResultatListe: MutableList<BeregningsResultatAfpPrivat> = mutableListOf()
        var gjeldendePrivatAfpBeregningResultat: BeregningsResultatAfpPrivat? = null

        if (ytelser.privatAfpVirkningFom != null) {
            val response = beregnPrivatAfp(
                PrivatAfpSpec(
                    simulering = spec,
                    kravhode = kravhode,
                    virkningFom = ytelser.privatAfpVirkningFom,
                    forrigePrivatAfpBeregningResult = null, // forrigeAfpPrivatBeregningsresultat is null for SimulerFleksibelAPCommand & SimulerAFPogAPCommand (ref. line 129 in SimulerAFPogAPCommand)
                    gjelderOmsorg = kravhode.hentPersongrunnlagForSoker().gjelderOmsorg,
                    sakId = kravhode.sakId
                )
            )

            gjeldendePrivatAfpBeregningResultat = response.gjeldendeBeregningsresultatAfpPrivat
            privatAfpBeregningResultatListe = response.afpPrivatBeregningsresultatListe
        }

        logger.info("Simulator steg 4 - Oppdater kravhode før første knekkpunkt")

        kravhode = oppdaterKravhodeForFoersteKnekkpunkt(
            KravhodeUpdateSpec(
                kravhode = kravhode,
                simulering = spec,
                forrigeAlderspensjonBeregningResult = ytelser.forrigeAlderspensjonBeregningResultat
            )
        )

        logger.info("Simulator steg 5 - Finn knekkpunkter")

        val knekkpunktMap = finnKnekkpunkter(
            KnekkpunktSpec(
                kravhode = kravhode,
                simulering = spec,
                soekerVirkningFom = ytelser.soekerVirkningFom,
                avdoedVirkningFom = ytelser.avdoedVirkningFom,
                forrigeAlderspensjonBeregningResultatVirkningFom =
                    ytelser.forrigeAlderspensjonBeregningResultat?.virkFom?.toLocalDate(),
                sakId = kravhode.sakId
            )
        )

        logger.info("Simulator steg 6 - Beregn AFP i offentlig sektor")

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
        } else if (gjelderEndring) {
            pre2025OffentligAfpResult =
                spec.foersteUttakDato?.let { pre2025OffentligAfpEndringBeregner.beregnAfp(kravhode, it) }
            livsvarigOffentligAfpResult = null
        } else {
            pre2025OffentligAfpResult = null
            livsvarigOffentligAfpResult =
                if (flags.inkluderLivsvarigOffentligAfp) //TODO fremtidige inntekter
                    foedselsdato?.let {
                        beregnLivsvarigOffentligAfp(
                            pid = person.pid!!,
                            foedselDato = it,
                            forventetAarligInntektBeloep = spec.forventetInntektBeloep,
                            virkningDato = spec.rettTilOffentligAfpFom ?: spec.foersteUttakDato
                            ?: throw RuntimeException("Ingen virkningsdato angitt for livsvarig offentlig AFP")
                        )
                    }
                else
                    null
        }

        logger.info("Simulator steg 7 - Vilkårsprøv og beregn alderspensjon")

        val vilkaarsproevOgBeregnAlderspensjonResult = vilkaarsproevOgBeregnAlderspensjon(
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
                ignoreAvslag = flags.ignoreAvslag
            )
        )

        logger.info("Simulator steg 8 - Opprett output")

        val output: SimulatorOutput = opprettOutput(
            ResultPreparerSpec(
                simuleringSpec = spec,
                kravhode = kravhode,
                alderspensjonBeregningResultatListe = vilkaarsproevOgBeregnAlderspensjonResult.beregningsresultater,
                privatAfpBeregningResultatListe = privatAfpBeregningResultatListe,
                forrigeAlderspensjonBeregningResultat = ytelser.forrigeAlderspensjonBeregningResultat,
                forrigePrivatAfpBeregningResultat = null, // forrigeAfpPrivatBeregningsresultat is null for SimulerFleksibelAPCommand & SimulerAFPogAPCommand
                pre2025OffentligAfpBeregningResultat = pre2025OffentligAfpResult?.simuleringResult,
                livsvarigOffentligAfpBeregningResultatListe =
                    LivsvarigOffentligAfpPeriodeConverter.konverterTilArligeAfpOffentligLivsvarigPerioder(
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
                this.foedselDato = foedselDato
                this.persongrunnlag = kravhode.hentPersongrunnlagForSoker()
            }
    }

    private fun fetchLoependeYtelser(spec: SimuleringSpec): LoependeYtelser {
        if (spec.gjelderPre2025OffentligAfp()) {
            // SimulerAFPogAPCommand
            val ytelser: LoependeYtelserResult = ytelseService.getLoependeYtelser(
                LoependeYtelserSpec(
                    pid = spec.pid!!,
                    foersteUttakDato = spec.foersteUttakDato!!,
                    avdoed = spec.avdoed,
                    alderspensjonFlags = null,
                    endringAlderspensjonFlags = null,
                    pre2025OffentligAfpYtelserFlags = Pre2025OffentligAfpYtelserFlags(
                        gjelderFpp = spec.type == SimuleringType.AFP_FPP,
                        sivilstatusUdefinert = false //TODO check if this can happen: spec.sivilstatus == null
                    )
                )
            )

            return LoependeYtelser(
                soekerVirkningFom = ytelser.alderspensjon?.sokerVirkningFom!!,
                avdoedVirkningFom = ytelser.alderspensjon.avdodVirkningFom,
                privatAfpVirkningFom = ytelser.afpPrivat?.virkningFom,
                sisteBeregning = ytelser.alderspensjon.sisteBeregning,
                forrigeAlderspensjonBeregningResultat = ytelser.alderspensjon.forrigeBeregningsresultat,
                forrigePrivatAfpBeregningResultat = ytelser.afpPrivat?.forrigeBeregningsresultat,
                forrigeVedtakListe = norskeVedtak(ytelser.alderspensjon.forrigeVilkarsvedtakListe)
            )
        }

        if (spec.gjelderEndring()) {
            // SimulerEndringAvAPCommand
            val ytelser: LoependeYtelserResult = ytelseService.getLoependeYtelser(
                LoependeYtelserSpec(
                    pid = spec.pid!!,
                    foersteUttakDato = spec.foersteUttakDato!!,
                    avdoed = spec.avdoed,
                    alderspensjonFlags = null,
                    endringAlderspensjonFlags = EndringAlderspensjonYtelserFlags(
                        inkluderPrivatAfp = spec.type == SimuleringType.ENDR_AP_M_AFP_PRIVAT
                    ),
                    pre2025OffentligAfpYtelserFlags = null
                )
            )

            return LoependeYtelser(
                soekerVirkningFom = ytelser.alderspensjon?.sokerVirkningFom!!,
                avdoedVirkningFom = ytelser.alderspensjon.avdodVirkningFom,
                privatAfpVirkningFom = ytelser.afpPrivat?.virkningFom,
                sisteBeregning = ytelser.alderspensjon.sisteBeregning,
                forrigeAlderspensjonBeregningResultat = ytelser.alderspensjon.forrigeBeregningsresultat,
                forrigePrivatAfpBeregningResultat = ytelser.afpPrivat?.forrigeBeregningsresultat,
                forrigeVedtakListe = norskeVedtak(ytelser.alderspensjon.forrigeVilkarsvedtakListe)
            )
        }

        // SimulerFleksibelAPCommand
        val ytelser: LoependeYtelserResult = ytelseService.getLoependeYtelser(
            LoependeYtelserSpec(
                pid = spec.pid,
                foersteUttakDato = spec.foersteUttakDato!!,
                avdoed = spec.avdoed,
                alderspensjonFlags = AlderspensjonYtelserFlags(
                    inkluderPrivatAfp = spec.type == SimuleringType.ENDR_AP_M_AFP_PRIVAT
                ),
                endringAlderspensjonFlags = null,
                pre2025OffentligAfpYtelserFlags = null
            )
        )

        return LoependeYtelser(
            soekerVirkningFom = ytelser.alderspensjon?.sokerVirkningFom!!,
            avdoedVirkningFom = ytelser.alderspensjon.avdodVirkningFom,
            privatAfpVirkningFom = ytelser.afpPrivat?.virkningFom,
            sisteBeregning = null,
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            forrigeVedtakListe = mutableListOf() //TODO use value in ytelser?
        )
    }

    //TODO change according to PEN fix in PEK-782
    private fun beregnLivsvarigOffentligAfp(
        pid: Pid,
        foedselDato: LocalDate,
        forventetAarligInntektBeloep: Int,
        virkningDato: LocalDate,
    ): LivsvarigOffentligAfpResult {
        val fom: LocalDate = foersteAarMedUregistrertInntekt()
        val til: LocalDate = sisteAarMedAfpOpptjeningInntekt(foedselDato)

        val fremtidigInntektListe: List<Inntekt> =
            if (fom.isBefore(til))
                aarligInntektListe(fom, til, forventetAarligInntektBeloep)
            else
                emptyList()

        return livsvarigOffentligAfpService.simuler(
            LivsvarigOffentligAfpSpec(
                pid,
                foedselDato,
                fom = virkningDato,
                fremtidigInntektListe
            )
        )
    }

    override fun fetchFoedselDato(pid: Pid): LocalDate =
        generelleDataHolder.getPerson(pid).foedselDato

    private fun fetchGrunnbeloep(): Int {
        val grunnbeloepListe: List<SatsResultat> = context.fetchGrunnbeloepListe(LocalDate.now()).satsResultater
        return grunnbeloepListe.firstOrNull()?.verdi?.toInt() ?: 0
    }

    private fun opprettKravhode(
        spec: KravhodeSpec,
        person: PenPerson?,
        virkningDatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ): Kravhode {
        val start = currentTimeMillis()
        val kravhode = kravhodeCreator.opprettKravhode(spec, person, virkningDatoGrunnlagListe)
        logger.info("opprettKravhode tok {} ms", currentTimeMillis() - start)
        return kravhode
    }

    private fun oppdaterKravhodeForFoersteKnekkpunkt(spec: KravhodeUpdateSpec): Kravhode {
        val start = currentTimeMillis()
        val response = kravhodeUpdater.updateKravhodeForFoersteKnekkpunkt(spec)
        logger.info("oppdaterKravhodeForForsteKnekkpunkt tok {} ms", currentTimeMillis() - start)
        return response
    }

    private fun finnKnekkpunkter(spec: KnekkpunktSpec): SortedMap<LocalDate, MutableList<KnekkpunktAarsak>> {
        val start = currentTimeMillis()
        val response = knekkpunktFinder.finnKnekkpunkter(spec)
        logger.info("finnKnekkpunkter tok {} ms", currentTimeMillis() - start)
        return response
    }

    private fun beregnPrivatAfp(spec: PrivatAfpSpec): PrivatAfpResult {
        val start = currentTimeMillis()
        val response = privatAfpBeregner.beregnPrivatAfp(spec)
        logger.info("beregnAfpPrivat tok {} ms", currentTimeMillis() - start)
        return response
    }

    private fun vilkaarsproevOgBeregnAlderspensjon(request: AlderspensjonVilkaarsproeverBeregnerSpec): AlderspensjonBeregnerResult {
        val start = currentTimeMillis()
        val response = alderspensjonVilkaarsproeverOgBeregner.vilkaarsproevOgBeregnAlder(request)
        logger.info("vilkarsprovOgBeregnAlder tok {} ms", currentTimeMillis() - start)
        return response
    }

    private fun opprettOutput(request: ResultPreparerSpec): SimulatorOutput {
        val start = currentTimeMillis()
        val response = SimuleringResultPreparer.opprettOutput(request)
        logger.info("opprettOutput tok {} ms", currentTimeMillis() - start)
        return response
    }

    private companion object {

        private fun foersteAarMedUregistrertInntekt(): LocalDate =
            LocalDate.now().minusYears(1)

        private fun sisteAarMedAfpOpptjeningInntekt(foedselDato: LocalDate): LocalDate =
            foedselDato.plusYears(LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR)

        private fun aarligInntektListe(fom: LocalDate, til: LocalDate, aarligBeloep: Int): List<Inntekt> =
            aarligeDatoer(fom, til)
                .map { inntektVedAaretsStart(it, aarligBeloep) }
                .toList()

        private fun aarligeDatoer(fom: LocalDate, til: LocalDate): Stream<LocalDate> =
            fom.datesUntil(til, Period.ofYears(1))

        private fun inntektVedAaretsStart(dato: LocalDate, aarligBeloep: Int) =
            Inntekt(
                aarligBeloep,
                fom = dato.withMonth(1).withDayOfMonth(1)
            )

        // AbstraktSimulerAPFra2011Command.filterVilkarsVedtakListOnNOR
        private fun norskeVedtak(vedtakListe: List<VilkarsVedtak>): MutableList<VilkarsVedtak> =
            vedtakListe.filter { Land.NOR == it.kravlinje?.land }.toMutableList()
    }
}
