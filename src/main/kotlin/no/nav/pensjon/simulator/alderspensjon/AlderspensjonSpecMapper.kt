package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.OffentligSimuleringstypeDeducer
import no.nav.pensjon.simulator.alderspensjon.spec.PensjonInntektSpec
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import java.time.LocalDate

/**
 * Maps from particular 'alderspensjon specification' to general 'simulering specification'.
 */
object AlderspensjonSpecMapper {

    @OptIn(ExperimentalStdlibApi::class)
    fun simuleringSpec(
        source: AlderspensjonSpec,
        foedselsdato: LocalDate,
        simuleringstypeDeducer: OffentligSimuleringstypeDeducer
    ) =
        SimuleringSpec(
            type = type(simuleringstypeDeducer, source),
            sivilstatus = sivilstatus(source),
            epsHarPensjon = source.epsHarPensjon,
            foersteUttakDato = foersteUttakFom(source),
            heltUttakDato = if (source.gradertUttak == null) null else source.heltUttakFom,
            pid = source.pid,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = true,
            simulerForTp = false, // since not set in SimulerAlderspensjonRequestV3Converter in PEN
            uttakGrad = source.gradertUttak?.uttaksgrad ?: UttakGradKode.P_100,
            forventetInntektBeloep = 0, // fremtidigInntektListe is used instead
            inntektUnderGradertUttakBeloep = 0, // fremtidigInntektListe is used instead
            inntektEtterHeltUttakBeloep = 0, // fremtidigInntektListe is used instead
            inntektEtterHeltUttakAntallAar = null, // fremtidigInntektListe is used instead
            foedselAar = foedselsdato.year,
            utlandAntallAar = source.antallAarUtenlandsEtter16,
            utlandPeriodeListe = mutableListOf(), // utenlandsopphold is in V4 specified by utlandAntallAar
            fremtidigInntektListe = source.fremtidigInntektListe.map(::fremtidigInntekt).toMutableList(),
            brukFremtidigInntekt = true,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = source.epsHarInntektOver2G,
            livsvarigOffentligAfp = source.livsvarigOffentligAfpRettFom?.let { LivsvarigOffentligAfpSpec(rettTilAfpFom = it) },
            pre2025OffentligAfp = null, // never used in this context
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // also controls whether to include 'simulert beregningsinformasjon' in the result
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // cf. SimulerAlderspensjonProviderV3.simulerAlderspensjon line 54
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

    private fun sivilstatus(spec: AlderspensjonSpec): SivilstatusType =
        if (spec.epsHarPensjon || spec.epsHarInntektOver2G) SivilstatusType.GIFT else SivilstatusType.UGIF

    private fun type(deducer: OffentligSimuleringstypeDeducer, spec: AlderspensjonSpec): SimuleringTypeEnum =
        deducer.deduceSimuleringstype(
            pid = spec.pid,
            uttakFom = spec.gradertUttak?.fom ?: spec.heltUttakFom,
            livsvarigOffentligAfpRettFom = spec.livsvarigOffentligAfpRettFom
        )

    private fun foersteUttakFom(spec: AlderspensjonSpec): LocalDate =
        spec.gradertUttak?.fom ?: spec.heltUttakFom

    private fun fremtidigInntekt(spec: PensjonInntektSpec) =
        FremtidigInntekt(
            aarligInntektBeloep = spec.aarligBeloep,
            fom = spec.fom
        )
}
