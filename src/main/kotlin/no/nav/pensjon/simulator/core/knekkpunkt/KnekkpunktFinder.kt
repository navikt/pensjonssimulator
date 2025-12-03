package no.nav.pensjon.simulator.core.knekkpunkt

import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.createDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.findLatestDateByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.trygdetid.TrygdetidBeregnerProxy
import no.nav.pensjon.simulator.trygdetid.TrygdetidCombo
import no.nav.pensjon.simulator.trygdetid.TrygdetidUtil.trygdetidSpec
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// Corresponds to FinnKnekkpunkterHelper + FastsettTrygdetidCache
@Component
class KnekkpunktFinder(
    private val trygdetidBeregner: TrygdetidBeregnerProxy,
    private val normalderService: NormertPensjonsalderService,
    private val time: Time
) {

    // FinnKnekkpunkterHelper.finnKnekkpunkter
    fun finnKnekkpunkter(knekkpunktSpec: KnekkpunktSpec): SortedMap<LocalDate, MutableList<KnekkpunktAarsak>> {
        val kravhode = knekkpunktSpec.kravhode
        val simuleringSpec = knekkpunktSpec.simulering
        val soekerForsteVirkning = knekkpunktSpec.soekerVirkningFom
        val avdoedFoersteVirkning = knekkpunktSpec.avdoedVirkningFom
        val foersteUttakDato = simuleringSpec.foersteUttakDato
        val soekerGrunnlag = kravhode.hentPersongrunnlagForSoker()
        val avdoedGrunnlag = kravhode.hentPersongrunnlagForRolle(GrunnlagsrolleEnum.AVDOD, false)
        var knekkpunktMap: SortedMap<LocalDate, MutableList<KnekkpunktAarsak>> = TreeMap()

        val normalderDato: LocalDate =
            normalderService.normalderDato(soekerGrunnlag.fodselsdato!!.toNorwegianLocalDate())

        // STEP 1 - Calculate forsteBerDato
        val foersteBeregningDato =
            knekkpunktSpec.forrigeAlderspensjonBeregningResultatVirkningFom?.let {
                calculateFoersteBeregningDato(
                    normalderDato,
                    foersteUttakDato!!,
                    forrigeBeregningResultatVirkning = it
                )
            }
                ?: forsteBeregningsdato(simuleringSpec)

        // STEP 2 - Add knekkpunkter based the opptjeningsgrunnlag for SOKER (and AVDOD if GJR)
        addKnekkpunkterBasedOnOpptjeningsgrunnlag(
            knekkpunktMap = knekkpunktMap,
            opptjeningGrunnlagListe = soekerGrunnlag.opptjeningsgrunnlagListe,
            aarsak = KnekkpunktAarsak.OPPTJBRUKER
        )

        if (avdoedGrunnlag != null) {
            addKnekkpunkterBasedOnOpptjeningsgrunnlag(
                knekkpunktMap = knekkpunktMap,
                opptjeningGrunnlagListe = avdoedGrunnlag.opptjeningsgrunnlagListe,
                aarsak = KnekkpunktAarsak.OPPTJAVDOD
            )
        }

        // STEP 3 - Add knekkpunkter based the trygdetid for SOKER (and AVDOD if GJR)
        addKnekkpunkterBasedOnTrygdetid(
            knekkpunktMap = knekkpunktMap,
            foersteBeregningDato = foersteBeregningDato!!,
            foersteVirkning = soekerForsteVirkning,
            kravhode = kravhode,
            aarsak = KnekkpunktAarsak.TTBRUKER,
            sakId = knekkpunktSpec.sakId
        )

        if (avdoedGrunnlag != null) {
            addKnekkpunkterBasedOnTrygdetid(
                knekkpunktMap,
                foersteBeregningDato,
                foersteVirkning = avdoedFoersteVirkning,
                kravhode,
                aarsak = KnekkpunktAarsak.TTAVDOD,
                sakId = knekkpunktSpec.sakId
            )
        }

        // STEP 4 - Add knekkpunkter based the uttaksgradliste
        addKnekkpunkterBasedOnUttaksgrader(knekkpunktMap, kravhode.uttaksgradListe)

        // STEP 5 - Add a knekkpunkt the 1st of the month after bruker turns 67 years old
        addKnekkpunkt(
            knekkpunktMap = knekkpunktMap,
            knekkpunktDato = normalderDato,
            aarsak = KnekkpunktAarsak.OPPTJBRUKER
        )

        // Make sure knekkpunkter before forsteBerDato are all stripped away
        knekkpunktMap = knekkpunktMap.tailMap(foersteBeregningDato)

        // When simulation is called from eksterne ordninger for a 2025-bruker, then we should
        // force a Kap 19 simulation even for pure Kap 20 brukere (2025) since eksterne ordninger currently aren't able to parse
        // a Kap 20 result. The following is part of that "logic". This "hack" should be removed as soon as they're able to
        // receive Kap 20 results.
        if (simuleringSpec.simulerForTp) {
            knekkpunktMap = trimKnekkpunkterInCaseOfSimulerForTp(normalderDato, knekkpunktMap)
        }

        return knekkpunktMap
    }

    // FastsettTrygdetidCache.fastsettTrygdetid + fastsettTrygdetidInPreg + updateSisteGyldigeOpptjeningsaar
    private fun fastsettTrygdetid(
        aarsak: KnekkpunktAarsak,
        kravhode: Kravhode,
        foersteBeregningDato: LocalDate,
        foersteVirkning: LocalDate?, // NB: nullable at least for avdød
        sakId: Long?
    ): TrygdetidCombo {
        if (aarsak == KnekkpunktAarsak.TTBRUKER) { // from FinnKnekkpunkterHelper.addKnekkpunkterBasedOnTrygdetid
            val persongrunnlag =
                kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.SOKER, checkBruk = false)!!

            return trygdetidBeregner.fastsettTrygdetidForPeriode(
                spec = trygdetidSpec(
                    kravhode,
                    persongrunnlag,
                    knekkpunktDato = foersteBeregningDato,
                    soekerFoersteVirkningFom = foersteVirkning,
                    ytelseType = KravlinjeTypeEnum.AP,
                    boddEllerArbeidetUtenlands = kravhode.boddEllerArbeidetIUtlandet
                ),
                rolle = GrunnlagsrolleEnum.SOKER,
                kravIsUforetrygd = persongrunnlag.gjelderUforetrygd,
                sakId
            )
        }

        val persongrunnlag =
            kravhode.hentPersongrunnlagForRolle(rolle = GrunnlagsrolleEnum.AVDOD, checkBruk = false)!!

        return trygdetidBeregner.fastsettTrygdetidForPeriode(
            spec = trygdetidSpec(
                kravhode,
                persongrunnlag,
                knekkpunktDato = foersteBeregningDato,
                soekerFoersteVirkningFom = foersteVirkning,
                ytelseType = KravlinjeTypeEnum.GJR,
                boddEllerArbeidetUtenlands = kravhode.boddArbeidUtlandAvdod
            ),
            rolle = GrunnlagsrolleEnum.AVDOD,
            kravIsUforetrygd = persongrunnlag.gjelderUforetrygd,
            sakId
        )
    }

    // FinnKnekkpunkterHelper.addKnekkpunkterBasedOnTrygdetid
    private fun addKnekkpunkterBasedOnTrygdetid(
        knekkpunktMap: SortedMap<LocalDate, MutableList<KnekkpunktAarsak>>,
        foersteBeregningDato: LocalDate,
        foersteVirkning: LocalDate?, // NB: nullable at least for avdød
        kravhode: Kravhode,
        aarsak: KnekkpunktAarsak,
        sakId: Long?
    ) {
        val gjelderSoeker = aarsak == KnekkpunktAarsak.TTBRUKER
        val grunnlagRolle = if (gjelderSoeker) GrunnlagsrolleEnum.SOKER else GrunnlagsrolleEnum.AVDOD
        val persongrunnlag = kravhode.hentPersongrunnlagForRolle(grunnlagRolle, false)!!
        val boddEllerArbeidetUtenlands =
            if (gjelderSoeker) kravhode.boddEllerArbeidetIUtlandet else kravhode.boddArbeidUtlandAvdod
        val kravlinjeType = if (gjelderSoeker) KravlinjeTypeEnum.AP else KravlinjeTypeEnum.GJR

        val sisteRelevanteAar =
            if (gjelderSoeker)
                yearUserTurnsGivenAge(persongrunnlag.fodselsdato!!, MAX_RELEVANTE_TRYGDETID_ALDER)
            else
                persongrunnlag.dodsdato!!.toNorwegianLocalDate().plusYears( ANTALL_RELEVANTE_AAR_ETTER_DOED.toLong()).year

        addKnekkpunkt(knekkpunktMap, foersteBeregningDato, aarsak)
        val forrigeTrygdetid = fastsettTrygdetid(aarsak, kravhode, foersteBeregningDato, foersteVirkning, sakId)
        var forrigeTrygdetidKap19 = forrigeTrygdetid.kapittel19
        var forrigeTrygdetidKap20 = forrigeTrygdetid.kapittel20

        for (kalenderAar in foersteBeregningDato.year + 1..sisteRelevanteAar) {
            val virkDato = LocalDate.of(kalenderAar, 1, 1)

            val trygdetidSpec: TrygdetidRequest = trygdetidSpec(
                kravhode,
                persongrunnlag,
                knekkpunktDato = virkDato,
                soekerFoersteVirkningFom = foersteVirkning,
                ytelseType = kravlinjeType,
                boddEllerArbeidetUtenlands
            )

            val trygdetid: TrygdetidCombo = trygdetidBeregner.fastsettTrygdetidForPeriode(
                spec = trygdetidSpec,
                rolle = grunnlagRolle,
                kravIsUforetrygd = persongrunnlag.gjelderUforetrygd,
                sakId
            )

            val trygdetidKap19 = trygdetid.kapittel19
            val trygdetidKap20 = trygdetid.kapittel20

            if (areDifferent(trygdetidKap19, forrigeTrygdetidKap19) ||
                areDifferent(trygdetidKap20, forrigeTrygdetidKap20)
            ) {
                addKnekkpunkt(knekkpunktMap, virkDato, aarsak)
            }

            if (erFull(trygdetidKap19) && erFull(trygdetidKap20)) {
                break
            }

            forrigeTrygdetidKap19 = trygdetidKap19
            forrigeTrygdetidKap20 = trygdetidKap20
        }
    }

    // FinnKnekkpunkterHelper.calculateForsteBeregningDato
    private fun calculateFoersteBeregningDato(
        normalderDato: LocalDate,
        foersteUttakDato: LocalDate,
        forrigeBeregningResultatVirkning: LocalDate
    ): LocalDate {
        // Citation from design:
        // SETT forsteBerDato = den første av følgende datoer
        // - 1.1 neste kalenderår etter den siste av dagens dato og forrigeBerResAP.virkDato
        // - forsteUttaksdato
        // - den 1. i måneden etter at bruker blir 67 år, dersom denne er etter dagens dato og forrigeBerResAP.virkDato
        val latestOfTodayAndForrigeBeregningResultatVirkning: Date? =
            findLatestDateByDay(
                first = time.today().toNorwegianDateAtNoon(),
                second = forrigeBeregningResultatVirkning.toNorwegianDateAtNoon()
            )

        val sortedDates: SortedSet<Date> = TreeSet()
        sortedDates.add(
            createDate(
                getYear(latestOfTodayAndForrigeBeregningResultatVirkning!!) + 1,
                Calendar.JANUARY,
                1
            )
        )
        sortedDates.add(foersteUttakDato.toNorwegianDateAtNoon())

        if (isAfterByDay(normalderDato, latestOfTodayAndForrigeBeregningResultatVirkning, false)) {
            sortedDates.add(normalderDato.toNorwegianDateAtNoon())
        }

        return sortedDates.iterator().next().toNorwegianLocalDate()
    }

    private companion object {
        private const val MAX_RELEVANTE_TRYGDETID_ALDER = 76
        private const val FULL_TRYGDETID_ANTALL_AAR = 40
        private const val ANTALL_RELEVANTE_AAR_ETTER_DOED = 2

        // FinnKnekkpunkterHelper.addKnekkpunkt
        private fun addKnekkpunkt(
            knekkpunktMap: SortedMap<LocalDate, MutableList<KnekkpunktAarsak>>,
            knekkpunktDato: LocalDate,
            aarsak: KnekkpunktAarsak
        ) {
            var aarsaker = knekkpunktMap[knekkpunktDato]

            if (aarsaker == null) {
                aarsaker = mutableListOf()
                knekkpunktMap[knekkpunktDato] = aarsaker
            }

            aarsaker.add(aarsak)
        }

        // FinnKnekkpunkterHelper.addKnekkpunkterBasedOnOpptjeningsgrunnlagListe
        private fun addKnekkpunkterBasedOnOpptjeningsgrunnlag(
            knekkpunktMap: SortedMap<LocalDate, MutableList<KnekkpunktAarsak>>,
            opptjeningGrunnlagListe: MutableList<Opptjeningsgrunnlag>,
            aarsak: KnekkpunktAarsak
        ) {
            for (grunnlag in opptjeningGrunnlagListe) {
                if (grunnlag.pi > 0) {
                    addKnekkpunkt(
                        knekkpunktMap,
                        LocalDate.of(grunnlag.ar + OPPTJENING_ETTERSLEP_ANTALL_AAR, 1, 1),
                        aarsak
                    )
                }
            }
        }

        // FinnKnekkpunkterHelper.addKnekkpunkterBasedOnUttaksgradListe
        private fun addKnekkpunkterBasedOnUttaksgrader(
            knekkpunktMap: SortedMap<LocalDate, MutableList<KnekkpunktAarsak>>,
            uttaksgradListe: MutableList<Uttaksgrad>
        ) {
            for (uttaksgrad in uttaksgradListe) {
                addKnekkpunkt(
                    knekkpunktMap,
                    knekkpunktDato = uttaksgrad.fomDato!!.toNorwegianLocalDate(),
                    aarsak = KnekkpunktAarsak.UTG
                )
            }
        }

        // FinnKnekkpunkterHelper.trimKnekkpunktListeInCaseOfSimulerForTp
        private fun trimKnekkpunkterInCaseOfSimulerForTp(
            normalderDato: LocalDate,
            knekkpunkter: SortedMap<LocalDate, MutableList<KnekkpunktAarsak>>
        ): SortedMap<LocalDate, MutableList<KnekkpunktAarsak>> {
            var latestRelevantKnekkpunkt: LocalDate = normalderDato

            // Uttaksgrad-knekkpunkt etter normalder brukes som siste relevante knekkpunkt:
            for ((key, value) in knekkpunkter) {
                if (value.contains(KnekkpunktAarsak.UTG)) {
                    if (isAfterByDay(key, latestRelevantKnekkpunkt, false)) {
                        latestRelevantKnekkpunkt = key
                    }
                }
            }

            // Fjern alle knekkpunkter etter siste relevante knekkpunkt:
            return knekkpunkter.headMap(latestRelevantKnekkpunkt.plusDays(1))
        }

        private fun forsteBeregningsdato(spec: SimuleringSpec) =
            if (spec.gjelderPre2025OffentligAfp())
                spec.heltUttakDato
            else
                spec.foersteUttakDato

        private fun areDifferent(a: Trygdetid?, b: Trygdetid?): Boolean {
            if (a == null != (b == null)) {
                return true
            }

            return b != null && a!!.tt != b.tt
        }

        /**
         * NB: Udefinert trygdetid anses som full trygdetid.
         */
        private fun erFull(trygdetid: Trygdetid?) =
            trygdetid?.let { it.tt == FULL_TRYGDETID_ANTALL_AAR } != false
    }
}
