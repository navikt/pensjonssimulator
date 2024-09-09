package no.nav.pensjon.simulator.core.domain.regler.util

import no.nav.pensjon.simulator.core.domain.regler.IBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import java.util.*

class FindBeregningVisitor(
    private val query: (b11: IBeregning) -> Boolean,
    private val beregningRelasjonFilter: (br: BeregningRelasjon) -> Boolean = { true }
):IBeregningVisitor {
    var result: Optional<IBeregning> = Optional.empty()

    override fun visit(beregning2011: IBeregning) {
        if (query.invoke(beregning2011)) {
            result = Optional.of(beregning2011)
        } else {
            beregning2011.delberegningsListe
                .filter(beregningRelasjonFilter)
                .forEach { br ->
                    if (this.result.isPresent) return@forEach
                    br.beregning2011?.accept(this)
                }
        }
    }
}
