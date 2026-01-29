package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.Trygdetid

data class TrygdetidCombo(
    val kapittel19: Trygdetid?,
    val kapittel20: Trygdetid?
)
