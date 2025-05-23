package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimuleringFacade
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertUttakAlder
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.isBeforeOrOn
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class UttakService(
    private val simuleringFacade: SimuleringFacade,
    private val normalderService: NormertPensjonsalderService,
    private val time: Time
) {
    fun finnTidligstMuligUttak(spec: SimuleringSpec): TidligstMuligUttak {
        val tidligstUttakAlder: Alder = normalderService.nedreAlder(spec.foedselDato!!)
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
            uttaksgrad = Uttaksgrad.from(newSpec.uttakGrad.value.toInt()),
            feil = null
        )
    }

    private fun fremtidigPensjonAlderDato(foedselsdato: LocalDate, alder: Alder): PensjonAlderDato {
        val alderDato = PensjonAlderDato(foedselsdato, alder)

        // Sjekk at dato er i fremtid:
        return time.today().let {
            if (alderDato.dato.isBeforeOrOn(it))
                PensjonAlderDato(foedselsdato, dato = it.withDayOfMonth(1).plusMonths(1))
            else
                alderDato
        }
    }
}
