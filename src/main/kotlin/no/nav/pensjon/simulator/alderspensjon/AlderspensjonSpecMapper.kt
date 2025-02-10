package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.PensjonInntektSpec
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import java.time.LocalDate

/**
 * Maps from particular 'alderspensjon specification'
 * to general 'simulering specification'.
 */
object AlderspensjonSpecMapper {

    @OptIn(ExperimentalStdlibApi::class)
    fun simuleringSpec(
        source: AlderspensjonSpec,
        foedselsdato: LocalDate,
        erFoerstegangsuttak: Boolean
    ) =
        SimuleringSpec(
            type = simuleringType(source.livsvarigOffentligAfpRettFom, erFoerstegangsuttak),
            sivilstatus = if (source.epsHarPensjon || source.epsHarInntektOver2G) SivilstatusType.GIFT else SivilstatusType.UGIF,
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
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = source.epsHarInntektOver2G,
            rettTilOffentligAfpFom = source.livsvarigOffentligAfpRettFom,
            afpOrdning = null,
            afpInntektMaanedFoerUttak = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // also controls whether to include 'simulert beregningsinformasjon' in result
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // cf. SimulerAlderspensjonProviderV3.simulerAlderspensjon line 54
            onlyVilkaarsproeving = false
        )

    private fun foersteUttakFom(source: AlderspensjonSpec): LocalDate =
        source.gradertUttak?.fom ?: source.heltUttakFom

    private fun simuleringType(
        livsvarigOffentligAfpRettDato: LocalDate?,
        erFoerstegangsuttak: Boolean
    ): SimuleringType =
        livsvarigOffentligAfpRettDato?.let {
            if (erFoerstegangsuttak)
                SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG
            else
                SimuleringType.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG
        } ?: if (erFoerstegangsuttak)
            SimuleringType.ALDER
        else
            SimuleringType.ENDR_ALDER

    private fun fremtidigInntekt(source: PensjonInntektSpec) =
        FremtidigInntekt(
            aarligInntektBeloep = source.aarligBeloep,
            fom = source.fom
        )
}
