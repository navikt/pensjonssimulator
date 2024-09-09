package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning

object BeholdningUtil {
    // Updated annually. In PEN stored in T_APPL_PARAMETER; context.getSisteGyldigeOpptjeningsAr()
    const val SISTE_GYLDIGE_OPPTJENING_AAR = 2022

    // TypedInformationListeUtils.findElementOfType
    fun findElementOfType(list: List<Beholdning>, type: BeholdningType) =
        list.find { type.name == it.beholdningsType?.kode } as? Pensjonsbeholdning

    // ArligInformasjonListeUtils.sortedSubset
    fun sortedSubset(list: List<Pensjonsbeholdning>, year: Int) =
        list.filter { it.ar == year }
            .toMutableList()
            .sortedBy { it.ar } // ArligInformasjonAscendingComparator
}
