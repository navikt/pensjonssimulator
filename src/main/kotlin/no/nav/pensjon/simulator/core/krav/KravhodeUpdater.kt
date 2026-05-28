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
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.trygdetid.*
import no.nav.pensjon.simulator.trygdetid.InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland
import no.nav.pensjon.simulator.trygdetid.Kapittel19TrygdetidsgrunnlagCreator.kapittel19TrygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.Kapittel20TrygdetidsgrunnlagCreator.kapittel20TrygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.TrygdeavtaleFactory.newTrygdeavtaleForSimuleringUtland
import no.nav.pensjon.simulator.trygdetid.TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode
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
        val simuleringSpec = spec.simuleringSpec
        val erFoerstegangsberegning = spec.erFoerstegangsberegning
        val initieltSoekerGrunnlag = kravhode.hentPersongrunnlagForSoker()
        val avdoedGrunnlag =
            kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.AVDOD, checkBruk = false)

        log.debug { "STEP 4.1 - Sett trygdetidsgrunnlag for søker" }
        val soekerGrunnlag: Persongrunnlag =
            if (simuleringSpec.erAnonym)
                settAnonymTrygdetid(
                    spec = TrygdetidsgrunnlagAnonymSpec(
                        antallAarUtenlands = simuleringSpec.utlandAntallAar,
                        foedselsaar = simuleringSpec.foedselAar
                    ),
                    persongrunnlag = initieltSoekerGrunnlag
                )
            else
                settSoekersTrygdetid(
                    spec = TrygdetidsgrunnlagPeriodebasertSpec(
                        simuleringSpec,
                        brukSoekersUtenlandsperioder = simuleringSpec.brukSoekersUtenlandsperioder,
                        regelverkType = kravhode.regelverkTypeEnum!!,
                        erFoerstegangsberegning
                    ),
                    kravhode, // mutable
                    persongrunnlag = initieltSoekerGrunnlag // mutable
                )

        log.debug { "STEP 4.2 - Sett pensjonsbeholdning for søker" }
        settPensjonsbeholdning(
            spec = simuleringSpec,
            persongrunnlag = soekerGrunnlag,
            erEndringsberegning = erFoerstegangsberegning.not()
        )

        log.debug { "STEP 4.3 - Sett uførehistorikk" }
        val ufoerePeriodeTom: LocalDate =
            ufoereperiodeService.ufoereperiodeTom(simuleringSpec, persongrunnlag = soekerGrunnlag)
        soekerGrunnlag.terminerUfoereperioder(ufoerePeriodeTom)

        avdoedGrunnlag?.let {
            log.debug { "STEP 4.4 - Sett trygdetidsgrunnlag for avdød" }
            updateAvdoed(
                avdoedGrunnlag = it,
                antallAarUtenlands = simuleringSpec.avdoed?.antallAarUtenlands ?: 0,
                foersteUttakDato = simuleringSpec.foersteUttakDato!!,
                erFoerstegangsberegning
            )
        }

        return kravhode
    }

    private fun settPensjonsbeholdning(
        spec: SimuleringSpec,
        persongrunnlag: Persongrunnlag,
        erEndringsberegning: Boolean
    ) {
        when {
            spec.gjelderPre2025OffentligAfp() -> tidsbegrensetOffentligAfpBeholdning
                .setPensjonsbeholdning(persongrunnlag, erEndringsberegning)

            spec.gjelderEndring() -> {} // ref. SimulerEndringAvAPCommand.settPensjonsbeholdning

            else -> persongrunnlag.replaceBeholdninger(
                fetchBeholdninger(persongrunnlag, foersteUttakDato = spec.foersteUttakDato)
            )
        }
    }

    private fun updateAvdoed(
        avdoedGrunnlag: Persongrunnlag,
        antallAarUtenlands: Int,
        foersteUttakDato: LocalDate,
        erFoerstegangsberegning: Boolean
    ) {
        val doedsdato = avdoedGrunnlag.dodsdatoLd!!

        val spec = TrygdetidsgrunnlagAarsbasertSpec(
            antallAarUtenlands,
            tom = sisteDagAaretFoer(doedsdato),
            erFoerstegangsberegning,
            foersteUttakDato
        )

        trygdetidSetter.settTrygdetid(spec, persongrunnlag = avdoedGrunnlag)
        avdoedGrunnlag.terminerUfoereperioder(tom = doedsdato)
    }

    // SimulerFleksibelAPCommand.settPensjonsbeholdning (part of)
    private fun fetchBeholdninger(persongrunnlag: Persongrunnlag, foersteUttakDato: LocalDate?) =
        try {
            context.beregnOpptjening(beholdningTom = foersteUttakDato, persongrunnlag)
                .filter { BeholdningtypeEnum.PEN_B == it.beholdningsTypeEnum }
        } catch (e: RegelmotorValideringException) {
            handle(e)
        }

    private fun settAnonymTrygdetid(
        spec: TrygdetidsgrunnlagAnonymSpec,
        persongrunnlag: Persongrunnlag
    ): Persongrunnlag {
        with(anonymSimuleringTrygdetidPeriode(spec)) {
            persongrunnlag.trygdetidPerioder.add(this)
            persongrunnlag.trygdetidPerioderKapittel20.add(this)
        }

        return persongrunnlag
    }

    // SimulerFleksibelAPCommand.settTrygdetid
    /**
     * Setter trygdetid på persongrunnlaget (muterer 'persongrunnlag'- og 'kravhode'-objektene).
     */
    private fun settSoekersTrygdetid(
        spec: TrygdetidsgrunnlagPeriodebasertSpec,
        kravhode: Kravhode,
        persongrunnlag: Persongrunnlag
    ): Persongrunnlag {
        val simuleringSpec = spec.simuleringSpec
        val regelverkType = spec.regelverkType

        if (spec.brukSoekersUtenlandsperioder) {
            kravhode.boddEllerArbeidetIUtlandet = true

            if (regelverkType.isAlderspensjon2011) {
                addKapittel19Trygdetid(
                    persongrunnlag,
                    utlandPeriodeListe = simuleringSpec.utlandPeriodeListe,
                    foersteUttakDato = simuleringSpec.foersteAlderspensjonUttaksdato()!!
                )
            }

            if (regelverkType.isAlderspensjon2016) {
                addKapittel19Trygdetid(
                    persongrunnlag,
                    utlandPeriodeListe = simuleringSpec.utlandPeriodeListe,
                    foersteUttakDato = simuleringSpec.foersteAlderspensjonUttaksdato()!!
                )
                addKapittel20Trygdetid(
                    persongrunnlag,
                    utlandPeriodeListe = simuleringSpec.utlandPeriodeListe,
                    foersteUttakDato = simuleringSpec.foersteAlderspensjonUttaksdato()!!
                )
            }

            if (regelverkType.isAlderspensjon2025) {
                addKapittel20Trygdetid(
                    persongrunnlag,
                    utlandPeriodeListe = simuleringSpec.utlandPeriodeListe,
                    foersteUttakDato = simuleringSpec.foersteAlderspensjonUttaksdato()!!
                )
            }

            persongrunnlag.trygdeavtale = newTrygdeavtaleForSimuleringUtland(avtalelandKravdato = time.today())
            persongrunnlag.trygdeavtaledetaljer = newTrygdeavtaledetaljerForSimuleringUtland()

            persongrunnlag.inngangOgEksportGrunnlag =
                newInngangOgEksportGrunnlagForSimuleringUtland(persongrunnlag, regelverkType)

            return persongrunnlag
        } else {
            // Bruk søkers 'antall år utenlands':
            val setterSpec = TrygdetidsgrunnlagAarsbasertSpec(
                antallAarUtenlands = simuleringSpec.utlandAntallAar,
                tom = null,
                erFoerstegangsberegning = spec.erFoerstegangsberegning,
                foersteUttakDato = simuleringSpec.foersteAlderspensjonUttaksdato()!!
            )

            return trygdetidSetter.settTrygdetid(setterSpec, persongrunnlag)
        }
    }

    // SimulerFleksibelAPCommand.setTrygetidKap19
    private fun addKapittel19Trygdetid(
        persongrunnlag: Persongrunnlag,
        utlandPeriodeListe: MutableList<UtlandPeriode>,
        foersteUttakDato: LocalDate
    ) {
        val periodeListe = kapittel19TrygdetidsperiodeListe(
            opptjeningsgrunnlagListe = persongrunnlag.opptjeningsgrunnlagListe,
            utlandPeriodeListe,
            foedselsdato = persongrunnlag.fodselsdatoLd!!,
            foersteUttakDato
        )

        periodeListe.forEach { persongrunnlag.trygdetidPerioder.add(it) }
    }

    // SimulerFleksibelAPCommand.setTrygdetidKap20
    private fun addKapittel20Trygdetid(
        persongrunnlag: Persongrunnlag,
        utlandPeriodeListe: MutableList<UtlandPeriode>,
        foersteUttakDato: LocalDate
    ) {
        val periodeListe = kapittel20TrygdetidsperiodeListe(
            utlandPeriodeListe,
            foedselsdato = persongrunnlag.fodselsdatoLd!!,
            foersteUttakDato
        )

        periodeListe.forEach { persongrunnlag.trygdetidPerioderKapittel20.add(it) }
    }

    private companion object {

        //TODO: Should this value be 17 instead?
        // According to documentation it is 16: https://pensjon-dokumentasjon.intern.dev.nav.no/pen/Fellestjenester/FPEN028_abstraktSimulerAPFra2011.html#_setttrygdetidsgrunnlag
        // but that may be an outdated fact
        private const val AGE_ADULTHOOD_OPPTJENING = 16

        // SimulerFleksibelAPCommand.createTrygdetidsgrunnlagForenkletSimulering
        private fun anonymSimuleringTrygdetidPeriode(spec: TrygdetidsgrunnlagAnonymSpec): TTPeriode =
            anonymSimuleringTrygdetidPeriode(
                fom = LocalDate.of(spec.foedselsaar + AGE_ADULTHOOD_OPPTJENING + spec.antallAarUtenlands, 1, 1),
                tom = null // ingen sluttdato for anonym simulering
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

        private fun sisteDagAaretFoer(dato: LocalDate) =
            LocalDate.of(dato.year, 1, 1).minusDays(1)
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
