package no.nav.pensjon.simulator.core.krav

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpBeholdning
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.ufoere.UfoereperiodeService
import no.nav.pensjon.simulator.tech.time.DateUtil.sisteDag
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.trygdetid.InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland
import no.nav.pensjon.simulator.trygdetid.Kapittel19TrygdetidsgrunnlagCreator.kapittel19TrygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.Kapittel20TrygdetidsgrunnlagCreator.kapittel20TrygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.TrygdeavtaleFactory.newTrygdeavtaleForSimuleringUtland
import no.nav.pensjon.simulator.trygdetid.TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagSpec
import no.nav.pensjon.simulator.trygdetid.TrygdetidSetter
import no.nav.pensjon.simulator.validity.InternDataInkonsistensException
import org.springframework.stereotype.Component
import java.time.LocalDate

// PEN:
// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OppdaterKravhodeForForsteKnekkpunktHelper
@Component
class KravhodeUpdater(
    private val context: SimulatorContext,
    private val ufoereperiodeService: UfoereperiodeService,
    private val tidsbegrensetOffentligAfpBeholdning: Pre2025OffentligAfpBeholdning,
    private val trygdetidSetter: TrygdetidSetter,
    private val time: Time
) {
    private val log = KotlinLogging.logger {}

    // OppdaterKravhodeForForsteKnekkpunktHelper.oppdaterKravHodeForForsteKnekkpunkt
    fun updateKravhodeForFoersteKnekkpunkt(spec: KravhodeUpdateSpec): Kravhode {
        val kravhode = spec.kravhode
        val simuleringSpec = spec.simulering
        val forrigeAlderspensjonBeregningResult = spec.forrigeAlderspensjonBeregningResult
        var soekerGrunnlag = kravhode.hentPersongrunnlagForSoker()
        var avdoedGrunnlag =
            kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.AVDOD, checkBruk = false)

        log.debug { "STEP 4.1 - Sett trygdetidsgrunnlag" }
        soekerGrunnlag = setTrygdetid(
            spec = TrygdetidGrunnlagSpec(
                persongrunnlag = soekerGrunnlag,
                utlandAntallAar = simuleringSpec.utlandAntallAar,
                tom = null,
                forrigeAlderspensjonBeregningResultat = forrigeAlderspensjonBeregningResult,
                simuleringSpec
            ),
            kravhode
        )

        log.debug { "STEP 4.2 - Sett pensjonsbeholdning for bruker" }
        when {
            simuleringSpec.gjelderPre2025OffentligAfp() -> tidsbegrensetOffentligAfpBeholdning.setPensjonsbeholdning(
                soekerGrunnlag,
                forrigeAlderspensjonBeregningResult
            )

            simuleringSpec.gjelderEndring() -> {} // ref. SimulerEndringAvAPCommand.settPensjonsbeholdning
            else -> soekerGrunnlag.replaceBeholdninger(
                fetchBeholdninger(
                    soekerGrunnlag,
                    simuleringSpec.foersteUttakDato
                )
            )
        }

        log.debug { "STEP 4.3 - Sett uførehistorikk" }
        val ufoerePeriodeTom: LocalDate = ufoereperiodeService.ufoereperiodeTom(simuleringSpec, soekerGrunnlag)
        soekerGrunnlag.terminerUfoereperioder(ufoerePeriodeTom)

        if (avdoedGrunnlag != null) {
            log.debug { "STEP 4.4 - Sett trygdetidsgrunnlag for avdød" }
            // Dodsdato set to Dec 31 the previous year
            val lastDayOfYearBeforeDoedDato = sisteDag(avdoedGrunnlag.dodsdatoLd!!.year - 1)

            avdoedGrunnlag = setTrygdetid(
                TrygdetidGrunnlagSpec(
                    persongrunnlag = avdoedGrunnlag,
                    utlandAntallAar = simuleringSpec.avdoed?.antallAarUtenlands,
                    tom = lastDayOfYearBeforeDoedDato,
                    forrigeAlderspensjonBeregningResultat = forrigeAlderspensjonBeregningResult,
                    simuleringSpec = simuleringSpec
                ),
                kravhode
            )

            log.debug { "STEP 4.5 - Sett uførehistorikk for avdød" }
            avdoedGrunnlag.dodsdatoLd?.let(avdoedGrunnlag::terminerUfoereperioder)
        }

        return kravhode
    }

    // SimulerFleksibelAPCommand.settPensjonsbeholdning (part of)
    private fun fetchBeholdninger(grunnlag: Persongrunnlag, foersteUttakDato: LocalDate?) =
        try {
            context.beregnOpptjening(foersteUttakDato, grunnlag)
                .filter { BeholdningtypeEnum.PEN_B == it.beholdningsTypeEnum }
        } catch (e: RegelmotorValideringException) {
            handle(e)
        }

    // SimulerFleksibelAPCommand.settTrygdetid
    private fun setTrygdetid(spec: TrygdetidGrunnlagSpec, kravhode: Kravhode): Persongrunnlag {
        val persongrunnlag = spec.persongrunnlag
        val simuleringSpec = spec.simuleringSpec

        if (simuleringSpec.erAnonym) {
            with(anonymSimuleringTrygdetidPeriode(spec)) {
                persongrunnlag.trygdetidPerioder.add(this)
                persongrunnlag.trygdetidPerioderKapittel20.add(this)
            }
            return persongrunnlag
        }

        if (simuleringSpec.boddUtenlands) {
            kravhode.boddEllerArbeidetIUtlandet = true
            val regelverkType = kravhode.regelverkTypeEnum!!

            if (regelverkType.isAlderspensjon2011) {
                addKapittel19Trygdetid(persongrunnlag, simuleringSpec)
            }

            if (regelverkType.isAlderspensjon2016) {
                addKapittel19Trygdetid(persongrunnlag, simuleringSpec)
                addKapittel20Trygdetid(persongrunnlag, simuleringSpec)
            }

            if (regelverkType.isAlderspensjon2025) {
                addKapittel20Trygdetid(persongrunnlag, simuleringSpec)
            }

            persongrunnlag.trygdeavtale = newTrygdeavtaleForSimuleringUtland(avtalelandKravdato = time.today())
            persongrunnlag.trygdeavtaledetaljer = newTrygdeavtaledetaljerForSimuleringUtland()

            persongrunnlag.inngangOgEksportGrunnlag =
                newInngangOgEksportGrunnlagForSimuleringUtland(persongrunnlag, kravhode)

            return persongrunnlag
        }

        return trygdetidSetter.settTrygdetid(spec)
    }

    // SimulerFleksibelAPCommand.setTrygetidKap19
    private fun addKapittel19Trygdetid(persongrunnlag: Persongrunnlag, spec: SimuleringSpec) {
        val periodeListe = kapittel19TrygdetidsperiodeListe(
            opptjeningsgrunnlagListe = persongrunnlag.opptjeningsgrunnlagListe,
            utlandPeriodeListe = spec.utlandPeriodeListe,
            foedselsdato = persongrunnlag.fodselsdatoLd!!,
            foersteUttakDato = spec.foersteAlderspensjonUttaksdato()
        )

        periodeListe.forEach { persongrunnlag.trygdetidPerioder.add(it) }
    }

    // SimulerFleksibelAPCommand.setTrygdetidKap20
    private fun addKapittel20Trygdetid(persongrunnlag: Persongrunnlag, spec: SimuleringSpec) {
        val periodeListe = kapittel20TrygdetidsperiodeListe(
            utlandPeriodeListe = spec.utlandPeriodeListe,
            foedselsdato = persongrunnlag.fodselsdatoLd!!,
            foersteUttakDato = spec.foersteAlderspensjonUttaksdato()
        )

        periodeListe.forEach { persongrunnlag.trygdetidPerioderKapittel20.add(it) }
    }

    private companion object {

        //TODO: Should this value be 17 instead?
        // According to documentation it is 16: https://pensjon-dokumentasjon.intern.dev.nav.no/pen/Fellestjenester/FPEN028_abstraktSimulerAPFra2011.html#_setttrygdetidsgrunnlag
        // but that may be an outdated fact
        private const val AGE_ADULTHOOD_OPPTJENING = 16

        // SimulerFleksibelAPCommand.createTrygdetidsgrunnlagForenkletSimulering
        private fun anonymSimuleringTrygdetidPeriode(spec: TrygdetidGrunnlagSpec): TTPeriode =
            anonymSimuleringTrygdetidPeriode(
                fom = LocalDate.of(
                    spec.simuleringSpec.foedselAar + AGE_ADULTHOOD_OPPTJENING + (spec.utlandAntallAar ?: 0),
                    1,
                    1
                ),
                tom = spec.tom
            )

        private fun handle(e: RegelmotorValideringException): Nothing {
            throw if (indikererYrkesskadegradFeil(e.merknadListe))
                InternDataInkonsistensException(message = "En yrkesskadegrad kan ikke være høyere enn uføregraden i en uføreperiode", e)
            else
                e
        }

        private fun indikererYrkesskadegradFeil(merknadListe: List<Merknad>): Boolean =
            merknadListe.any { it.kode == "UFOREGRUNNLAG_GeneriskKontrollpunkt" && gjelderYrkesskadegrad(merknad = it) }

        private fun gjelderYrkesskadegrad(merknad: Merknad): Boolean =
            merknad.argumentListe.any { it.startsWith("En yrkesskadegrad kan ikke") }
    }
}

//TODO move this into Enum?
val RegelverkTypeEnum.isAlderspensjon2011: Boolean
    get() {
        return this.kap19 && !this.kap20
    }

val RegelverkTypeEnum.isAlderspensjon2016: Boolean
    get() {
        return this.kap19 && this.kap20
    }

val RegelverkTypeEnum.isAlderspensjon2025: Boolean
    get() {
        return !this.kap19 && this.kap20
    }

private val RegelverkTypeEnum.kap19: Boolean
    get() {
        return this == RegelverkTypeEnum.N_REG_G_N_OPPTJ
                || this == RegelverkTypeEnum.N_REG_G_OPPTJ
    }

private val RegelverkTypeEnum.kap20: Boolean
    get() {
        return this == RegelverkTypeEnum.N_REG_G_N_OPPTJ
                || this == RegelverkTypeEnum.N_REG_N_OPPTJ
    }
