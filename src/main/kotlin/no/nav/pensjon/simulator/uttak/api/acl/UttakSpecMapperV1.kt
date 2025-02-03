package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

object UttakSpecMapperV1 {

    // PEN: SimuleringUttaksalderSpecToInputMapper.mapSpecToInput
    @OptIn(ExperimentalStdlibApi::class)
    fun fromSpecV1(source: TidligstMuligUttakSpecV1, foedselsdato: LocalDate) =
        SimuleringSpec(
            type = source.rettTilAfpOffentligDato?.let { SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG }
                ?: SimuleringType.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = null, // ukjent; det er verdien vi ønsker å finne
            heltUttakDato = source.heltUttakFraOgMedDato,
            pid = Pid(source.personId),
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = true,
            simulerForTp = false, // since not set in SimulerAlderspensjonRequestV3Converter in PEN
            uttakGrad = UttakGradKode.entries.firstOrNull { it.value.toInt() == source.uttaksgrad }
                ?: UttakGradKode.P_100,
            forventetInntektBeloep = 0, // fremtidigInntektListe is used instead
            inntektUnderGradertUttakBeloep = 0, // fremtidigInntektListe is used instead
            inntektEtterHeltUttakBeloep = 0, // fremtidigInntektListe is used instead
            inntektEtterHeltUttakAntallAar = null, // fremtidigInntektListe is used instead
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0, // not taken into account
            utlandPeriodeListe = mutableListOf(), // not taken into account
            fremtidigInntektListe = source.fremtidigInntektListe.orEmpty().map(::inntekt).toMutableList(),
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            rettTilOffentligAfpFom = source.rettTilAfpOffentligDato,
            afpOrdning = null,
            afpInntektMaanedFoerUttak = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // also controls whether to include 'simulert beregningsinformasjon' in result
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // cf. SimulerAlderspensjonProviderV3.simulerAlderspensjon line 54
            onlyVilkaarsproeving = true
        )

    private fun inntekt(source: UttakInntektSpecV1) =
        FremtidigInntekt(
            aarligInntektBeloep = source.arligInntekt ?: 0,
            fom = source.fraOgMedDato
        )
}
