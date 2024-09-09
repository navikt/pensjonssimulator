package no.nav.pensjon.simulator.core.domain.regler.util

import no.nav.pensjon.simulator.core.domain.regler.IBeregning

interface IBeregningVisitor {
    fun visit(beregning:IBeregning)
}
