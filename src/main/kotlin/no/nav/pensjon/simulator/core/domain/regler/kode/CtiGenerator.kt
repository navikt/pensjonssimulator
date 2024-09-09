package no.nav.pensjon.simulator.core.domain.regler.kode

interface CtiGenerator<out TypeCti> {
    fun navn(): String
    fun cti(): TypeCti
}
