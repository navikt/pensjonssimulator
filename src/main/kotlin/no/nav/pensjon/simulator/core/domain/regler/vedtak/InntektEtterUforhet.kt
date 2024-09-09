package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import java.util.*

class InntektEtterUforhet : AbstraktBeregningsvilkar {
    var inntekt = 0
    var ieuDato: Date? = null

    constructor() : super()

    constructor(inntektEtterUforhet: InntektEtterUforhet) : super(inntektEtterUforhet) {
        this.inntekt = inntektEtterUforhet.inntekt
        this.ieuDato = inntektEtterUforhet.ieuDato
    }

    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),
        /** Interne felt */
        inntekt: Int = 0,
        ieuDato: Date? = null
    ) : super(merknadListe) {
        this.inntekt = inntekt
        this.ieuDato = ieuDato
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var ieu: InntektEtterUforhet? = null
        if (abstraktBeregningsvilkar.javaClass == InntektEtterUforhet::class.java) {
            ieu = InntektEtterUforhet(abstraktBeregningsvilkar as InntektEtterUforhet)
        }
        return ieu
    }

}
