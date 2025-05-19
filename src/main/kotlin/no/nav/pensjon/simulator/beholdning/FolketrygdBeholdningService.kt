package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertGarantipensjonNivaa
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonBeholdningPeriode
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.GarantiPensjonsnivaSatsEnum
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.uttak.UttaksdatoValidator
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.vedtak.VedtakService
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class FolketrygdBeholdningService(
    private val simulator: SimulatorCore,
    private val time: Time,
    private val vedtakService: VedtakService,
    private val generelleDataHolder: GenerelleDataHolder,
    private val validator: UttaksdatoValidator
) {
    fun simulerFolketrygdBeholdning(spec: FolketrygdBeholdningSpec): FolketrygdBeholdning {
        val beholdningSpec = spec.sanitised().validated()
        val foedselsdato = generelleDataHolder.getPerson(beholdningSpec.pid).foedselDato
        verifySpec(beholdningSpec, foedselsdato)
        val vedtakInfo = vedtakService.vedtakStatus(beholdningSpec.pid, beholdningSpec.uttakFom)
        checkForGjenlevenderettighet(vedtakInfo)

        val result: SimulatorOutput =
            simulator.simuler(
                simuleringSpec(
                    beholdningSpec,
                    foedselsdato,
                    erFoerstegangsuttak = vedtakInfo.harGjeldendeVedtak.not()
                )
            )

        return FolketrygdBeholdning(
            pensjonBeholdningPeriodeListe =
                SimulatorOutputConverter.pensjon(result, time.today()).pensjonBeholdningPeriodeListe
                    .map(::beholdningPeriode)
        )
    }

    // PEN: SimuleringRequestConverter.verifyRequest
    private fun verifySpec(spec: FolketrygdBeholdningSpec, foedselsdato: LocalDate) {
        spec.fremtidigInntektListe.forEach { verifyDateIsFirstInMonth(it.inntektFom) }
        validator.verifyUttakFom(spec.uttakFom, foedselsdato)
    }

    private companion object {

        private fun verifyDateIsFirstInMonth(date: LocalDate) {
            if (date.dayOfMonth != 1)
                throw BadSpecException("Inntekt in fremtidigInntektListe must have a 'inntektFom' date that is the first day of a month")
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
                garantitilleggBeholdning = source.garantitilleggBeholdning.toInt(),
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
