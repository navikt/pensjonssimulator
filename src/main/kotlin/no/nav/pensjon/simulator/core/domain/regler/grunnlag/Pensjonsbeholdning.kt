package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import java.time.LocalDate

// 2026-04-23
open class Pensjonsbeholdning : Beholdning {
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.PEN_B

    // Extra:
    @JsonIgnore
    var fomLd: LocalDate? = null

    @JsonIgnore
    var tomLd: LocalDate? = null

    constructor() : super() {
        beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
    }

    constructor(source: Pensjonsbeholdning) : super(source) {
        fomLd = source.fomLd
        tomLd = source.tomLd
    }
}
