package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

class IkkeSisteOrdningException(val tpOrdning: String) : RuntimeException()  {
        override val message: String
            get() = "$tpOrdning er ikke siste ordning"
}