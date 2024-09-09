package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BasisGrunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GPSatsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid

@JsonSubTypes(
    JsonSubTypes.Type(value = BasisGrunnpensjon::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")

open class Grunnpensjon : Ytelseskomponent {
    var pSats_gp: Double = 0.0
    var satsType: GPSatsTypeCti? = null
    var ektefelleInntektOver2G: Boolean = false
    var anvendtTrygdetid: AnvendtTrygdetid? = null

    /**
     * Denne er beholdt av hensyn til bakoverkompatibilitet med xml filer.
     * Skal ikke ha get/set og skal ikke brukes fra regelkoden.
     */
    @JsonIgnore
    var prorata_gp: Double = 0.0

    constructor() : super(
            ytelsekomponentType = YtelsekomponentTypeCti("GP"),
            formelKode = FormelKodeCti("GPx")
    )

    constructor(
            pSats_gp: Double = 0.0,
            satsType: GPSatsTypeCti? = null,
            ektefelleInntektOver2G: Boolean = false,
            anvendtTrygdetid: AnvendtTrygdetid? = null,
            /** super Ytelseskomponent */
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("GP"),
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti  = FormelKodeCti("GPx"),
            reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
            brutto = brutto,
            netto = netto,
            fradrag = fradrag,
            bruttoPerAr = bruttoPerAr,
            nettoPerAr = nettoPerAr,
            fradragPerAr = fradragPerAr,
            ytelsekomponentType = ytelsekomponentType,
            merknadListe = merknadListe,
            fradragsTransaksjon = fradragsTransaksjon,
            opphort = opphort,
            sakType = sakType,
            formelKode = formelKode,
            reguleringsInformasjon = reguleringsInformasjon
    ) {
        this.pSats_gp = pSats_gp
        this.satsType = satsType
        this.ektefelleInntektOver2G = ektefelleInntektOver2G
        this.anvendtTrygdetid = anvendtTrygdetid
    }

    constructor(gp: Grunnpensjon) : super(gp) {
        this.pSats_gp = gp.pSats_gp
        this.satsType = gp.satsType
        this.ektefelleInntektOver2G = gp.ektefelleInntektOver2G
        this.anvendtTrygdetid = gp.anvendtTrygdetid
    }

}
