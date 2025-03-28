package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertGarantipensjonNivaa
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonBeholdningPeriode
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.beregn.Tuple2
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.GarantiPensjonsnivaSatsEnum
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.vedtak.VedtakService
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.Period

@Component
class FolketrygdBeholdningService(
    private val simulator: SimulatorCore,
    private val vedtakService: VedtakService,
    private val generelleDataHolder: GenerelleDataHolder
) {
    fun simulerFolketrygdBeholdning(spec: FolketrygdBeholdningSpec): FolketrygdBeholdning {
        val beholdningSpec = spec.sanitised().validated()
        val foedselsdato = generelleDataHolder.getPerson(beholdningSpec.pid).foedselDato
        verifySpec(beholdningSpec, foedselsdato)
        val vedtakInfo = vedtakService.vedtakStatus(beholdningSpec.pid, beholdningSpec.uttakFom)
        checkForGjenlevenderettighet(vedtakInfo)

        val result: SimulatorOutput =
            simulator.simuler(
                simuleringSpec( // PEN: simdomSimulerAlderspensjonForFolketrygdbeholdning
                    beholdningSpec = beholdningSpec,
                    foedselsdato,
                    erFoerstegangsuttak = vedtakInfo.harGjeldendeVedtak.not()
                )
            )

        return FolketrygdBeholdning(
            pensjonBeholdningPeriodeListe =
                SimulatorOutputConverter.pensjon(result).pensjonBeholdningPeriodeListe
                    .map(::beholdningPeriode)
        )
    }

    private companion object {
        private const val MIN_ALDER_AAR = 62 //TODO normert?
        private const val MAX_ALDER_AAR = 75

        // PEN: SimuleringRequestConverter.verifyRequest
        private fun verifySpec(spec: FolketrygdBeholdningSpec, fodselsdato: LocalDate) {
            spec.fremtidigInntektListe.forEach { verifyDateIsFirstInMonth(it.inntektFom) }
            verifyUttakFom(spec.uttakFom, fodselsdato)
        }

        private fun verifyDateIsFirstInMonth(date: LocalDate) {
            if (date.dayOfMonth != 1) {
                throw BadSpecException("Inntekt in fremtidigInntektListe must have a 'inntektFom' date that is the first day of a month")
            }
        }

        private fun verifyUttakFom(uttakFom: LocalDate, foedselsdato: LocalDate) {
            if (uttakFom.dayOfMonth != 1) {
                throw BadSpecException("uttakFom must be the first day in a month")
            }

            val alder = convertDatoFomToAlderTuple(uttakFom, foedselsdato)

            if (alder.first < MIN_ALDER_AAR) {
                throw BadSpecException("uttakFom cannot be earlier than first month after user turns $MIN_ALDER_AAR")
            }

            if (alder.first > MAX_ALDER_AAR || (alder.first == MAX_ALDER_AAR && alder.second > 1)) {
                throw BadSpecException("uttakFom cannot be later than first month after user turns $MAX_ALDER_AAR")
            }

            if (isBeforeByDay(uttakFom, LocalDate.now(), false)) {
                throw BadSpecException("uttakFom must be after today")
            }
        }

        private fun convertDatoFomToAlderTuple(fom: LocalDate, fodselsdato: LocalDate): Tuple2<Int, Int> {
            val firstDayOfLastMonth = fom.minusMonths(1L).withDayOfMonth(1)
            val period = Period.between(fodselsdato.withDayOfMonth(1), firstDayOfLastMonth)
            return Tuple2(period.years, period.months + 1) //TODO Use Alder
        }

        private fun simuleringSpec(
            beholdningSpec: FolketrygdBeholdningSpec,
            foedselsdato: LocalDate,
            erFoerstegangsuttak: Boolean
        ) =
            SimuleringSpec(
                pid = beholdningSpec.pid,
                foersteUttakDato = beholdningSpec.uttakFom,
                uttakGrad = UttakGradKode.P_100,
                heltUttakDato = null,
                utlandAntallAar = beholdningSpec.antallAarUtenlandsEtter16Aar,
                sivilstatus = sivilstatus(beholdningSpec),
                epsHarPensjon = beholdningSpec.epsHarPensjon,
                epsHarInntektOver2G = beholdningSpec.epsHarInntektOver2G,
                fremtidigInntektListe = beholdningSpec.fremtidigInntektListe.map(::fremtidigInntekt).toMutableList(),
                brukFremtidigInntekt = true,
                type = if (erFoerstegangsuttak) SimuleringType.ALDER else SimuleringType.ENDR_ALDER, // inkluderAfpPrivat = false
                foedselAar = 0, // only for anonym
                forventetInntektBeloep = 0, // inntekt instead given by fremtidigInntektListe
                inntektOver1GAntallAar = 0, // only for anonym
                inntektUnderGradertUttakBeloep = 0, // inntekt instead given by fremtidigInntektListe
                inntektEtterHeltUttakBeloep = 0, // inntekt instead given by fremtidigInntektListe
                inntektEtterHeltUttakAntallAar = 0, // inntekt instead given by fremtidigInntektListe
                foedselDato = foedselsdato,
                avdoed = null,
                isTpOrigSimulering = true,
                simulerForTp = false,
                utlandPeriodeListe = mutableListOf(),
                flyktning = null,
                rettTilOffentligAfpFom = null,
                pre2025OffentligAfp = null, // never used in this context
                erAnonym = false,
                ignoreAvslag = true, // true for folketrygdbeholdning
                isHentPensjonsbeholdninger = true, // true for TPO
                isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // true for TPO
                onlyVilkaarsproeving = false,
                epsKanOverskrives = false
            )

        private fun fremtidigInntekt(source: InntektSpec) =
            FremtidigInntekt(
                aarligInntektBeloep = source.inntektAarligBeloep,
                fom = source.inntektFom
            )

        private fun sivilstatus(source: FolketrygdBeholdningSpec) =
            if (source.epsHarPensjon == true || source.epsHarInntektOver2G == true)
                SivilstatusType.GIFT
            else
                SivilstatusType.UGIF

        private fun beholdningPeriode(source: SimulertPensjonBeholdningPeriode) =
            BeholdningPeriode(
                pensjonBeholdning = source.pensjonBeholdning.toInt(),
                garantipensjonBeholdning = source.garantipensjonBeholdning.toInt(),
                garantipensjonNivaa = garantipensjonNivaa(source.garantipensjonNivaa),
                fom = source.datoFom
            )

        private fun garantipensjonNivaa(source: SimulertGarantipensjonNivaa) =
            GarantipensjonNivaa(
                beloep = source.beloep?.toInt() ?: 0,
                satsType = source.satsType?.let(GarantiPensjonsnivaSatsEnum::valueOf)
                    ?: GarantiPensjonsnivaSatsEnum.ORDINAER,
                sats = source.sats?.toInt() ?: 0,
                anvendtTrygdetid = source.anvendtTrygdetid ?: 0,
            )

        // PEN: SimuleringServiceBase.checkForGjenlevenderettighet
        private fun checkForGjenlevenderettighet(vedtakInfo: VedtakStatus) {
            if (vedtakInfo.harGjenlevenderettighet) {
                throw FeilISimuleringsgrunnlagetException("Kan ikke simulere bruker med gjenlevenderettigheter")
            }
        }
    }
}
