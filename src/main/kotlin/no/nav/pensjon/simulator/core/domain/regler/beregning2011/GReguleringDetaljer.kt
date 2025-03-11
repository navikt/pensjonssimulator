package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.util.*

// 2025-03-10
@Deprecated("Slettes. Ikke produsert av pensjon-regler.")
class GReguleringDetaljer : IGRegulering {
    override var justeringTomDato: Date? = null
    override var justeringsfaktor = 0.0
    override var forrigeG = 0
    override var gjeldendeG = 0
}
