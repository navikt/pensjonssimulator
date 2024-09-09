package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.io.Serializable
import java.util.*

/**
 * Objektet representerer en periode som det er vurdert vilkår for barnetillegg (på uføretrygd).
 * Objektet holder på et sett av vilkår som er relevante for perioden.
 * Disse benyttes videre i vilkårsprøving av barnetillegg.
 */
class BarnetilleggVurderingsperiode(
    var fomDato: Date? = null,
    var tomDato: Date? = null,
    var btVilkarListe: MutableList<BarnetilleggVilkar> = mutableListOf()
) : Comparable<BarnetilleggVurderingsperiode>, Serializable {

    // SIMDOM-ADD
    constructor(source: BarnetilleggVurderingsperiode) : this() {
        fomDato = source.fomDato?.clone() as? Date
        tomDato = source.tomDato?.clone() as? Date
        source.btVilkarListe.forEach { btVilkarListe.add(BarnetilleggVilkar(it)) }
    }
    // end SIMDOM-ADD

    override fun compareTo(other: BarnetilleggVurderingsperiode): Int =
        DateCompareUtil.compareTo(fomDato, other.fomDato)
}
