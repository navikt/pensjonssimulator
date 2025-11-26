package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringstypeDeducer
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AlderspensjonSpecMapperV3(
    private val personService: GeneralPersonService,
    private val simuleringstypeDeducer: SimuleringstypeDeducer
) {
    fun fromDtoV3(source: AlderspensjonSpecV3): SimuleringSpec {
        val pid = Pid(source.fnr)
        val foersteUttak = source.forsteUttak
        val foersteUttakDato: LocalDate = foersteUttak.datoFom.toNorwegianLocalDate()
        val heltUttakDato: LocalDate? = source.heltUttak?.datoFom?.toNorwegianLocalDate()

        return SimuleringSpec(
            type = simuleringstypeDeducer.deduceSimuleringstype(
                pid,
                uttakFom = foersteUttakDato,
                inkluderPrivatAfp = source.simulerMedAfpPrivat == true
            ),
            sivilstatus = sivilstatus(source.sivilstandVedPensjonering),
            epsHarInntektOver2G = source.eps2G == true,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = heltUttakDato,
            pid = pid,
            foedselDato = personService.foedselsdato(pid),
            avdoed = null,
            isTpOrigSimulering = true, // true for samhandler
            simulerForTp = false,
            uttakGrad = uttaksgrad(foersteUttak),
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
            livsvarigOffentligAfp = null, // not supported in V3
            pre2025OffentligAfp = null, // never used in this context
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true, // true for samhandler
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // true for samhandler
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }

    private companion object {

        private fun sivilstatus(source: SivilstatusSpecV3): SivilstatusType =
            SivilstatusType.entries.firstOrNull { it.name == source.name }
                ?: SivilstatusType.UGIF

        private fun uttaksgrad(source: UttaksperiodeSpecV3): UttakGradKode =
            UttakGradKode.entries.firstOrNull { it.value == source.grad.toString() }
                ?: UttakGradKode.P_100

        private fun inntekt(source: InntektSpecV3) =
            FremtidigInntekt(
                aarligInntektBeloep = source.arligInntekt,
                fom = source.fomDato.toNorwegianLocalDate()
            )
    }
}
