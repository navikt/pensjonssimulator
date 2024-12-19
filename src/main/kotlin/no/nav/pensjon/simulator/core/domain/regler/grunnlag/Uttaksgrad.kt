package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.io.Serializable
import java.util.*

class Uttaksgrad(
    var fomDato: Date? = null,
    var tomDato: Date? = null,
    var uttaksgrad: Int = 0
) : Comparable<Uttaksgrad>, Serializable {
    @JsonIgnore var rawFomDato: Date? = null
    @JsonIgnore var rawTomDato: Date? = null

    /**
     * Backs up original "raw" date values before setting their time part to noon.
     * (Ref. PEN: CommonToReglerMapper.mapUttaksgradToRegler)
     */
    fun finishInit() {
        rawFomDato = fomDato
        rawTomDato = tomDato
        fomDato = rawFomDato?.noon()
        tomDato = rawTomDato?.noon()
    }

    constructor(source: Uttaksgrad) : this() {
        fomDato = source.fomDato
        tomDato = source.tomDato
        rawFomDato = source.rawFomDato
        rawTomDato = source.rawTomDato
        uttaksgrad = source.uttaksgrad
    }

    override fun compareTo(other: Uttaksgrad): Int =
        DateCompareUtil.compareTo(fomDato, other.fomDato)
}
