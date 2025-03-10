package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import java.util.*

// Checked 2025-02-28
open class Pensjonsbeholdning : Beholdning {
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.PEN_B

    @JsonIgnore
    var fom: Date? = null // SIMDOM-ADD
    @JsonIgnore
    var tom: Date? = null // SIMDOM-ADD

    constructor() : super() {
        beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
    }

    constructor(source: Pensjonsbeholdning) : super(source) {
        fom = source.fom?.clone() as? Date
        tom = source.tom?.clone() as? Date
    }
}
