package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringstypeDeducer
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Anti-corruption layer (ACL).
 * Maps from version 3 of the externally constrained data transfer object to the 'free' internal domain object.
 * The object represents a specification for the 'simuler alderspensjon & privat AFP' service.
 * ----------
 * PEN: no.nav.pensjon.pen_app.provider.ws.simulerepensjon.v1.converter.HentSimulertPensjonRequestConverter
 */
@Component
class AlderspensjonOgPrivatAfpSpecMapperV3(
    private val personService: GeneralPersonService,
    private val simuleringstypeDeducer: SimuleringstypeDeducer
) {
    /**
     * Takes a specification in the form of a data transfer object (DTO) and maps it to a domain object.
     */
    fun fromDto(source: AlderspensjonOgPrivatAfpSpecV3): SimuleringSpec {
        val pid: Pid = source.personident.let(::Pid)
        val foersteUttak = source.foersteUttak
        val foersteUttakFom: LocalDate = foersteUttak.fomDato
        val heltUttak = source.heltUttak
        val gradertUttak = if (heltUttak == null) null else foersteUttak

        return SimuleringSpec(
            type = simuleringstypeDeducer.deduceSimuleringstype(
                pid,
                uttakFom = foersteUttakFom,
                inkluderPrivatAfp = source.simulerPrivatAfp == true
            ),
            sivilstatus = sivilstatus(source),
            epsHarInntektOver2G = source.harEpsPensjonsgivendeInntektOver2G == true,
            epsHarPensjon = source.harEpsPensjon == true,
            foersteUttakDato = foersteUttakFom,
            heltUttakDato = heltUttak?.fomDato,
            pid = pid,
            foedselDato = personService.foedselsdato(pid),
            avdoed = null,
            isTpOrigSimulering = true, // true for samhandler
            simulerForTp = false,
            uttakGrad = uttaksgrad(source),
            forventetInntektBeloep = source.aarligInntektFoerUttak ?: 0,
            inntektUnderGradertUttakBeloep = gradertUttak?.aarligInntekt ?: 0,
            inntektEtterHeltUttakBeloep = heltUttak?.aarligInntekt ?: foersteUttak.aarligInntekt ?: 0,
            inntektEtterHeltUttakAntallAar = source.antallInntektsaarEtterHeltUttak,
            foedselAar = 0,
            utlandAntallAar = source.aarIUtlandetEtter16 ?: 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(), //source.fremtidigInntektListe.orEmpty().map(::inntekt).toMutableList(),
            brukFremtidigInntekt = true,
            inntektOver1GAntallAar = 0,
            flyktning = null,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null, // never used in this context
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // true for samhandler
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // true for samhandler
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }

    private fun sivilstatus(source: AlderspensjonOgPrivatAfpSpecV3): SivilstatusType =
        SivilstatusType.entries.firstOrNull { it.name == source.sivilstatusVedPensjonering.name }
            ?: SivilstatusType.UGIF

    private fun uttaksgrad(source: AlderspensjonOgPrivatAfpSpecV3): UttakGradKode =
        UttakGradKode.entries.firstOrNull { it.value == source.foersteUttak.grad.toString() }
            ?: UttakGradKode.P_100
}
