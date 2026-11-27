package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isSameDay
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * PEN: SimulerAlderspensjonResponseV3Converter
 */
@Component
class AlderspensjonResultMapperV3(
    private val personService: GeneralPersonService,
    private val normertPensjonsalderService: NormertPensjonsalderService,
    private val time: Time
) {
    fun map(
        simuleringResult: SimulatorOutput,
        pid: Pid,
        foersteUttakFom: LocalDate?,
        heltUttakFom: LocalDate?
    ): AlderspensjonResultV3 {
        val alderspensjon = simuleringResult.alderspensjon
        val foedselsdato = personService.foedselsdato(pid)
        val idag = time.today()
        val uttakListe: List<Uttaksgrad> = alderspensjon?.uttakGradListe.orEmpty()
        val harUttak = uttakListe.any { it.tasUt(dato = idag) }

        return AlderspensjonResultV3(
            pensjonsperioder = alderspensjon?.pensjonPeriodeListe.orEmpty()
                .map { PensjonsperiodeResultV3.transferable(source = it, foedselsdato) },
            simuleringsdataListe = simuleringsdataListe(simuleringResult, foedselsdato, foersteUttakFom),
            pensjonsbeholdningsperioder = alderspensjon?.pensjonBeholdningListe.orEmpty()
                .map(PensjonsbeholdningPeriodeResultV3::transferable),
            alderspensjonFraFolketrygden = alderspensjonListe(
                beregningsinformasjonListe = alderspensjon?.simulertBeregningInformasjonListe.orEmpty(),
                foersteUttakFom,
                heltUttakFom
            ),
            harUttak,
            harTidligereUttak = harUttak.not() && harHattUttakFoer(uttakListe, dato = idag),
            afpPrivatBeholdningVedUttak = privatAfpBeholdningVedUttak(simuleringResult.privatAfpPeriodeListe),
            sisteGyldigeOpptjeningsAr = simuleringResult.registerData?.sisteGyldigeOpptjeningAar
        )
    }

    private fun simuleringsdataListe(
        simuleringResult: SimulatorOutput,
        foedselsdato: LocalDate,
        foersteUttakFom: LocalDate?
    ): List<SimuleringsdataResultV3> {
        val normertPensjoneringsdato: LocalDate = normertPensjonsalderService.normertPensjoneringsdato(foedselsdato)

        val foersteSimuleringsdataDato: LocalDate? =
            if (isBeforeByDay(thisDate = foersteUttakFom, thatDate = normertPensjoneringsdato, allowSameDay = true))
                normertPensjoneringsdato
            else
                foersteUttakFom

        return simuleringResult.alderspensjon?.simulertBeregningInformasjonListe.orEmpty()
            .filter { isAfterByDay(thisDate = it.datoFom, thatDate = foersteSimuleringsdataDato, allowSameDay = true) }
            .map(SimuleringsdataResultV3::transferable)
    }

    private companion object {

        private fun alderspensjonListe(
            beregningsinformasjonListe: List<SimulertBeregningInformasjon>,
            foersteUttakFom: LocalDate?,
            heltUttakFom: LocalDate?
        ): List<AlderspensjonFraFolketrygdenResultV3> {
            val alderspensjonListe: MutableList<AlderspensjonFraFolketrygdenResultV3> = mutableListOf()
            val foersteUttakInfo = elementSomStarterPaaDato(beregningsinformasjonListe, foersteUttakFom)
            alderspensjonListe.add(AlderspensjonFraFolketrygdenResultV3.transferable(foersteUttakInfo))

            heltUttakFom?.let {
                val heltUttakInfo = elementSomStarterPaaDato(beregningsinformasjonListe, dato = it)
                alderspensjonListe.add(AlderspensjonFraFolketrygdenResultV3.transferable(heltUttakInfo))
            }

            return alderspensjonListe
        }

        private fun privatAfpBeholdningVedUttak(afpPeriodeListe: List<PrivatAfpPeriode>): Int? =
            if (afpPeriodeListe.isEmpty()) null else afpPeriodeListe[0].afpOpptjening

        private fun harHattUttakFoer(uttakListe: List<Uttaksgrad>, dato: LocalDate): Boolean =
            uttakListe.none { it.tasUt(dato) } && uttakListe.any { it.tattUtFoer(dato) }

        private fun elementSomStarterPaaDato(
            beregningsinformasjonListe: List<SimulertBeregningInformasjon>,
            dato: LocalDate?
        ): SimulertBeregningInformasjon =
            beregningsinformasjonListe.firstOrNull { isSameDay(it.datoFom, dato) }
                ?: SimulertBeregningInformasjon()
    }
}