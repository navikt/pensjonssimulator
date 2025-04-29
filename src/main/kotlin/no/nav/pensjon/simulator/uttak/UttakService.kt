package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimuleringFacade
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertUttakAlder
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.normalder.NormAlderService
import no.nav.pensjon.simulator.uttak.UttakUtil.fremtidigPensjonAlderDato
import org.springframework.stereotype.Component

@Component
class UttakService(
    private val simuleringFacade: SimuleringFacade,
    private val normAlderService: NormAlderService
) {
    fun finnTidligstMuligUttak(spec: SimuleringSpec): TidligstMuligUttak {
        val tidligstUttakAlder: Alder =
            normAlderService.normAlder(spec.foedselDato!!)
                .minusAar(ANTALL_AAR_MELLOM_TIDLIGST_OG_NORMERT_PENSJONERINGSALDER)

        val tidligstUttak = fremtidigPensjonAlderDato(spec.foedselDato, tidligstUttakAlder)
        val newSpec = spec.withFoersteUttakDato(tidligstUttak.dato)

        val result: SimulertPensjonEllerAlternativ =
            simuleringFacade.simulerAlderspensjon(newSpec, inkluderPensjonHvisUbetinget = true)

        // TMU (tidligst mulig uttak) er enten:
        // - Den lavest mulige fremtidige alder for helt uttak (hvis vilkårsprøvingen av denne gir OK), eller
        // - Den alternative alder for gradert/helt uttak som returneres av simuleringen (vilkårsprøvingen av denne har gitt OK)
        val tmuAlder: SimulertUttakAlder =
            result.alternativ?.gradertUttakAlder
                ?: result.alternativ?.heltUttakAlder
                ?: SimulertUttakAlder(tidligstUttak.alder, tidligstUttak.dato)

        return TidligstMuligUttak(
            uttakDato = tmuAlder.uttakDato,
            uttakGrad = UttakGrad.from(newSpec.uttakGrad.value.toInt()),
            feil = null
        )
    }

    companion object {
        const val ANTALL_AAR_MELLOM_TIDLIGST_OG_NORMERT_PENSJONERINGSALDER = 5
    }
}
