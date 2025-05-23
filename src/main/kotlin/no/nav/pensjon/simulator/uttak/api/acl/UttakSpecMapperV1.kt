package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component

@Component
class UttakSpecMapperV1(val personService: GeneralPersonService) {

    // PEN: SimuleringUttaksalderSpecToInputMapper.mapSpecToInput
    @OptIn(ExperimentalStdlibApi::class)
    fun fromSpecV1(source: TidligstMuligUttakSpecV1): SimuleringSpec {
        val pid = Pid(source.personId)
        val foedselsdato = personService.foedselsdato(pid)

        return SimuleringSpec(
            type = source.rettTilAfpOffentligDato?.let { SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG }
                ?: SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = null, // ukjent; det er verdien vi ønsker å finne
            heltUttakDato = source.heltUttakFraOgMedDato,
            pid = pid,
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
            utlandAntallAar = source.arIUtlandetEtter16 ?: 0,
            utlandPeriodeListe = mutableListOf(), // not taken into account
            fremtidigInntektListe = source.fremtidigInntektListe.orEmpty().map(::inntekt).toMutableList(),
            brukFremtidigInntekt = true,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            rettTilOffentligAfpFom = source.rettTilAfpOffentligDato,
            pre2025OffentligAfp = null, // never used in this context
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // also controls whether to include 'simulert beregningsinformasjon' in result
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // cf. SimulerAlderspensjonProviderV3.simulerAlderspensjon line 54
            onlyVilkaarsproeving = true,
            epsKanOverskrives = false
        )
    }

    private fun inntekt(source: UttakInntektSpecV1) =
        FremtidigInntekt(
            aarligInntektBeloep = source.arligInntekt ?: 0,
            fom = source.fraOgMedDato
        )
}
