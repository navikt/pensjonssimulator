package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok
import java.util.*

class FremtidigBarnetilleggperiode : AbstraktBarnetilleggperiode {

    /**
     * Hva perioden skal avkortes med. Årlig beløp.
     */
    var fradragPerAr: Double = 0.0
    /**
     * Årlig netto uten hensyn til justeringsbeløp
     */
    var nettoPerArForJustering: Int = 0
    /**
     * Årlig netto hensyntatt justeringsbeløp
     */
    var nettoPerAr: Double = 0.0
    /**
     * Månedlig nettobeløp
     */
    var nettoPerMnd: Int = 0
    /**
     * Hva gjenstår å utbetale i perioden
     */
    var restTilUtbetaling: Int = 0
    /**
     * Hva ville ha gjenstått å utbetale i perioden gitt ingen justering
     */
    var restTilUtbetalingForJustering: Double = 0.0
    /**
     * Nødvendig justering av avkortingsbeløp for perioden
     */
    var justeringsbelopUbegrensetPerAr: Double = 0.0
    /**
     * Hva må avkortingsbeløpet justeres med for å oppnå riktig fradrag for perioden
     */
    var justeringsbelopPerAr: Double = 0.0
    /**
     * Månedlig justeringsbeløp
     */
    var justeringsbelopPerMnd: Int = 0

    constructor() {}

    constructor(fbtp: FremtidigBarnetilleggperiode) : super(fbtp) {
        this.fradragPerAr = fbtp.fradragPerAr
        this.nettoPerArForJustering = fbtp.nettoPerArForJustering
        this.nettoPerAr = fbtp.nettoPerAr
        this.nettoPerMnd = fbtp.nettoPerMnd
        this.restTilUtbetaling = fbtp.restTilUtbetaling
        this.restTilUtbetalingForJustering = fbtp.restTilUtbetalingForJustering
        this.justeringsbelopUbegrensetPerAr = fbtp.justeringsbelopUbegrensetPerAr
        this.justeringsbelopPerAr = fbtp.justeringsbelopPerAr
        this.justeringsbelopPerMnd = fbtp.justeringsbelopPerMnd
    }

    constructor(
            fradragPerAr: Double = 0.0,
            nettoPerArForJustering: Int = 0,
            nettoPerAr: Double = 0.0,
            nettoPerMnd: Int = 0,
            restTilUtbetaling: Int = 0,
            restTilUtbetalingForJustering: Double = 0.0,
            justeringsbelopUbegrensetPerAr: Double = 0.0,
            justeringsbelopPerAr: Double = 0.0,
            justeringsbelopPerMnd: Int = 0,
            /** super AbstraktBarnetilleggperiode */
            fomDato: Date? = null,
            tomDato: Date? = null,
            lengde: Int = 0,
            antallBarn: Int = 0,
            fribelop: Int = 0,
            bruttoPerAr: Int = 0,
            reguleringsfaktor: Brok? = null,
            avkortingsbelopPerAr: Int = 0
    ) : super(
            fomDato = fomDato,
            tomDato = tomDato,
            lengde = lengde,
            antallBarn = antallBarn,
            fribelop = fribelop,
            bruttoPerAr = bruttoPerAr,
            reguleringsfaktor = reguleringsfaktor,
            avkortingsbelopPerAr = avkortingsbelopPerAr
    ) {
        this.fradragPerAr = fradragPerAr
        this.nettoPerArForJustering = nettoPerArForJustering
        this.nettoPerAr = nettoPerAr
        this.nettoPerMnd = nettoPerMnd
        this.restTilUtbetaling = restTilUtbetaling
        this.restTilUtbetalingForJustering = restTilUtbetalingForJustering
        this.justeringsbelopUbegrensetPerAr = justeringsbelopUbegrensetPerAr
        this.justeringsbelopPerAr = justeringsbelopPerAr
        this.justeringsbelopPerMnd = justeringsbelopPerMnd
    }

}
