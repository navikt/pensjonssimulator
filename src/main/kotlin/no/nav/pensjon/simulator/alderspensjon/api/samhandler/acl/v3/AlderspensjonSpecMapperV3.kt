package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringstypeDeducer
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AlderspensjonSpecMapperV3(
    private val personService: GeneralPersonService,
    private val simuleringstypeDeducer: SimuleringstypeDeducer
) {
    fun fromDtoV3(source: AlderspensjonSpecV3): SimuleringSpec {
        val pid: Pid = source.fnr?.let(::Pid) ?: missing("fnr")
        val foersteUttakDato: LocalDate? = source.forsteUttak?.datoFom?.toNorwegianLocalDate()
        val heltUttakDato: LocalDate? = source.heltUttak?.datoFom?.toNorwegianLocalDate()

        return SimuleringSpec(
            type = simuleringstypeDeducer.deduceSimuleringstype(
                pid,
                uttakFom = foersteUttakDato ?: heltUttakDato ?: missing("datoFom in forsteUttak and heltUttak"),
                inkluderPrivatAfp = true
            ),
            sivilstatus = sivilstatus(source),
            epsHarInntektOver2G = source.eps2G == true,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = heltUttakDato,
            pid = pid,
            foedselDato = personService.foedselsdato(pid),
            avdoed = null,
            isTpOrigSimulering = true, // true for TPO
            simulerForTp = false,
            uttakGrad = uttaksgrad(source),
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = null,
            foedselAar = 0,
            utlandAntallAar = source.arIUtlandetEtter16 ?: 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = source.fremtidigInntektListe.orEmpty().map(::inntekt).toMutableList(),
            brukFremtidigInntekt = true,
            inntektOver1GAntallAar = 0,
            flyktning = null,
            rettTilOffentligAfpFom = null,
            pre2025OffentligAfp = null, // never used in this context
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // true for TPO
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // true for TPO
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }

    private fun sivilstatus(source: AlderspensjonSpecV3): SivilstatusType =
        SivilstatusType.entries.firstOrNull { it.name == source.sivilstandVedPensjonering?.name }
            ?: SivilstatusType.UGIF

    private fun uttaksgrad(source: AlderspensjonSpecV3): UttakGradKode =
        UttakGradKode.entries.firstOrNull { it.value == source.forsteUttak?.grad?.toString() }
            ?: UttakGradKode.P_100

    private fun inntekt(source: InntektSpecV3) =
        FremtidigInntekt(
            aarligInntektBeloep = source.arligInntekt ?: missing("arligInntekt in fremtidigInntektListe"),
            fom = source.fomDato?.toNorwegianLocalDate()
                ?: missing("fomDato in fremtidigInntektListe"),
        )

    private fun missing(something: String): Nothing {
        throw BadRequestException("missing $something")
    }
}
