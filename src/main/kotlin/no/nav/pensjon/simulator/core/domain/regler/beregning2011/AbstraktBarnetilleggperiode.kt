package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.Date

import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil

@JsonSubTypes(
    JsonSubTypes.Type(value = TidligereBarnetilleggperiode::class),
    JsonSubTypes.Type(value = FremtidigBarnetilleggperiode::class),
    JsonSubTypes.Type(value = AvkortingsinformasjonBT::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBarnetilleggperiode : Comparable<AbstraktBarnetilleggperiode> {
    /**
     * Start for periode med et antall barn.
     */
    var fomDato: Date? = null

    /**
     * Stopp for periode med et antall barn.
     */
    var tomDato: Date? = null

    /**
     * Periodens lengde i antall måneder.
     */
    var lengde: Int = 0

    /**
     * Antall barn det beregnes barnetillegg for i perioden.
     */
    var antallBarn: Int = 0

    /**
     * Fribeløpet for antall barn i perioden.
     */
    var fribelop: Int = 0

    /**
     * Brutto årlig barnetillegg, beregnet for antall barn (felles- og særkullsbarn) i perioden og eventuelt redusert mot tak.
     */
    var bruttoPerAr: Int = 0

    /**
     * Reguleringsfaktor dersom perioden gjelder for en annen G enn GvedVirk.
     */
    var reguleringsfaktor: Brok? = null

    /**
     * Halvparten av inntekt overskytende fribeløp. Fastsatt som årlig beløp, dvs oppjustert til årlig beløp dersom fribeløp og inntekt er periodisert.
     */
    var avkortingsbelopPerAr: Int = 0

    constructor() {}


    constructor(bp: AbstraktBarnetilleggperiode) {
        fomDato = bp.fomDato
        tomDato = bp.tomDato
        lengde = bp.lengde
        antallBarn = bp.antallBarn
        fribelop = bp.fribelop
        bruttoPerAr = bp.bruttoPerAr

        if (bp.reguleringsfaktor != null) {
            reguleringsfaktor = Brok(bp.reguleringsfaktor!!)
        }

        avkortingsbelopPerAr = bp.avkortingsbelopPerAr
    }

    constructor(
        fomDato: Date? = null,
        tomDato: Date? = null,
        lengde: Int = 0,
        antallBarn: Int = 0,
        fribelop: Int = 0,
        bruttoPerAr: Int = 0,
        reguleringsfaktor: Brok? = null,
        avkortingsbelopPerAr: Int = 0
    ) {
        this.fomDato = fomDato
        this.tomDato = tomDato
        this.lengde = lengde
        this.antallBarn = antallBarn
        this.fribelop = fribelop
        this.bruttoPerAr = bruttoPerAr
        this.reguleringsfaktor = reguleringsfaktor
        this.avkortingsbelopPerAr = avkortingsbelopPerAr
    }

    override fun compareTo(other: AbstraktBarnetilleggperiode): Int {
        return DateCompareUtil.compareTo(fomDato, other.fomDato)
    }
}
