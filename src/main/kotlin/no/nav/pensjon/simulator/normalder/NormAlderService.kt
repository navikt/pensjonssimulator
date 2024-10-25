package no.nav.pensjon.simulator.normalder

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Simulator utilities related to "normalder".
 * The term "normalder" is defined in "NOU 2022: 7 - Et forbedret pensjonssystem"
 * (https://www.regjeringen.no/no/dokumenter/nou-2022-7/id2918654/?ch=10#kap9-1):
 * "aldersgrensen for ubetinget rett til alderspensjon som i dag (2024) er 67 år,
 *  kalles 'normert pensjoneringsalder', med 'normalderen' som kortform"
 */
@Service
class NormAlderService(
    private val generelleDataHolder: GenerelleDataHolder
) {
    fun normAlder(foedselDato: LocalDate?) = MINIMUM_NORM_ALDER

    fun normAlder(pid: Pid?): Alder =
        normAlder(pid?.let { generelleDataHolder.getPerson(it) }?.foedselDato)

    companion object {
        val MINIMUM_NORM_ALDER = Alder(aar = 67, maaneder = 0)
    }
}
