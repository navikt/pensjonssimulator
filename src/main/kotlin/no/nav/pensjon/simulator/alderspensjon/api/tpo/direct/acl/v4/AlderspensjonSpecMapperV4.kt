package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4


import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import java.time.LocalDate

/**
 * Maps from V4 data transfer objects (received from the API)
 * to domain objects that represent specification for 'simuler alderspensjon'
 */
object AlderspensjonSpecMapperV4 {

    @OptIn(ExperimentalStdlibApi::class)
    fun fromSpecV4(source: AlderspensjonSpecV4, foedselsdato: LocalDate) =
        SimuleringSpec(
            type = source.rettTilAfpOffentligDato?.let { SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG }
                ?: SimuleringType.ALDER,
            sivilstatus = if (source.epsPensjon == true || source.eps2G == true) SivilstatusType.GIFT else SivilstatusType.UGIF,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = (source.gradertUttak?.fraOgMedDato ?: source.heltUttakFraOgMedDato)
                ?.let(LocalDate::parse) ?: missing("heltUttakFraOgMedDato"),
            heltUttakDato = if (source.gradertUttak == null) null else
                source.heltUttakFraOgMedDato?.let(LocalDate::parse).also { validate(source.gradertUttak) }
                    ?: missing("heltUttakFraOgMedDato"),
            pid = source.personId?.let(::Pid),
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = true,
            simulerForTp = false, // since not set in SimulerAlderspensjonRequestV3Converter in PEN
            uttakGrad = UttakGradKode.entries.firstOrNull { it.value.toInt() == source.gradertUttak?.uttaksgrad }
                ?: UttakGradKode.P_100,
            forventetInntektBeloep = 0, // fremtidigInntektListe is used instead
            inntektUnderGradertUttakBeloep = 0, // fremtidigInntektListe is used instead
            inntektEtterHeltUttakBeloep = 0, // fremtidigInntektListe is used instead
            inntektEtterHeltUttakAntallAar = null, // fremtidigInntektListe is used instead
            foedselAar = foedselsdato.year,
            utlandAntallAar = source.aarIUtlandetEtter16 ?: 0,
            utlandPeriodeListe = mutableListOf(), // utenlandsopphold is in V4 specified by utlandAntallAar
            fremtidigInntektListe = source.fremtidigInntektListe.orEmpty().map(::fremtidigInntekt).toMutableList(),
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = source.eps2G == true,
            rettTilOffentligAfpFom = source.rettTilAfpOffentligDato?.let(LocalDate::parse),
            afpOrdning = null,
            afpInntektMaanedFoerUttak = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // also controls whether to include 'simulert beregningsinformasjon' in result
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // cf. SimulerAlderspensjonProviderV3.simulerAlderspensjon line 54
        )

    private fun validate(uttak: GradertUttakSpecV4) {
        if (uttak.fraOgMedDato == null) missing("gradertUttak.fraOgMedDato")
        if (uttak.uttaksgrad == null) missing("gradertUttak.uttaksgrad")
    }

    private fun fremtidigInntekt(source: PensjonInntektSpecV4) =
        FremtidigInntekt(
            aarligInntektBeloep = source.aarligInntekt ?: 0,
            fom = source.fraOgMedDato?.let(LocalDate::parse) ?: missing("fremtidigInntekt.fraOgMedDato")
        )

    private fun missing(valueName: String): Nothing {
        throw BadRequestException("$valueName missing")
    }
}
