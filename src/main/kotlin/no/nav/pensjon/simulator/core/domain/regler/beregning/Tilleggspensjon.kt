package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BasisTilleggspensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti
import no.nav.pensjon.simulator.core.domain.regler.util.formula.*

@JsonSubTypes(
    JsonSubTypes.Type(value = BasisTilleggspensjon::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class Tilleggspensjon : Ytelseskomponent, FormelProvider {

    /**
     * Det ordinære sluttpoengtallet.
     */
    var spt: Sluttpoengtall? = null

    /**
     * Sluttpoengtallet for yrkesskaden.Denne blir utfylt dersom det foreligger
     * yrkesskadegrunnlag i persongrunnlaget. ypt.pt er beregnet på grunnlag av
     * a) tilhørende poengtall (ypt.poengrekke.poengtallListe ) eller b) paa (
     * poeng etter antatt årlig inntekt ). Poengtall-listen er da tom. I alle
     * tilfeller er YPT.pt >= SPT.pt. Det vanligste tilfellet hvor YPT.pt >
     * SPT.pt skyldes yrkessadegrunnlag.antattArligInntekt.
     */
    var ypt: Sluttpoengtall? = null

    /**
     * Sluttpoengtallet for overkompensasjon.
     */
    var opt: Sluttpoengtall? = null

    /**
     * Den skiltes del av avdødes tilleggspensjon. Angis i prosent.
     */
    var skiltesDelAvAdodesTP: Int = 0

    /**
     * Bevarer ST brutto i tilfelle Sertillegg objektet avkortes bort.
     * Verdien er nødvendig dersom Tilleggspensjonen skal ytterligere avkorte ST i evt SAMBER.
     */
    @JsonIgnore
    var uavkortetSTBrutto: Int = 0

    /**
     * Bevarer ST brutto per år i tilfelle Sertillegg objektet avkortes bort.
     * Verdien er nødvendig dersom Tilleggspensjonen skal ytterligere avkorte ST i evt SAMBER.
     */
    @JsonIgnore
    var uavkortetSTBruttoPerAr: Double = 0.0

    /**
     * Andel av tilleggspensjon beregnet med restgrad uføre (UFG - YUG).
     */
    @JsonIgnore
    var tp_up: Int = 0

    /**
     * Andel av tilleggspensjon beregnet med yrkesskadegrad.
     */
    @JsonIgnore
    var tp_yp: Int = 0

    override fun formelPrefix(): String {
        return "TP"
    }

    /**
     * Map av formler brukt i beregning av Tilleggspensjon.
     */
    final override val formelMap: HashMap<String, Formel> = hashMapOf()

    constructor() : super(
        ytelsekomponentType = YtelsekomponentTypeCti("TP"),
        formelKode = FormelKodeCti("TPx")
    )

    constructor(tilleggspensjon: Tilleggspensjon) : super(tilleggspensjon) {
        if (tilleggspensjon.spt != null) {
            spt = Sluttpoengtall(tilleggspensjon.spt!!)
        }
        if (tilleggspensjon.ypt != null) {
            ypt = Sluttpoengtall(tilleggspensjon.ypt!!)
        }
        if (tilleggspensjon.opt != null) {
            opt = Sluttpoengtall(tilleggspensjon.opt!!)
        }
        skiltesDelAvAdodesTP = tilleggspensjon.skiltesDelAvAdodesTP

        for ((key, value) in tilleggspensjon.formelMap) {
            formelMap[key] = Formel(value)
        }

        uavkortetSTBrutto = tilleggspensjon.uavkortetSTBrutto
        uavkortetSTBruttoPerAr = tilleggspensjon.uavkortetSTBruttoPerAr
    }

    constructor(
        spt: Sluttpoengtall? = null,
        ypt: Sluttpoengtall? = null,
        opt: Sluttpoengtall? = null,
        skiltesDelAvAdodesTP: Int = 0,
        uavkortetSTBrutto: Int = 0,
        uavkortetSTBruttoPerAr: Double = 0.0,
        tp_up: Int = 0,
        tp_yp: Int = 0,
        /** super Ytelseskomponent*/
        brutto: Int = 0,
        netto: Int = 0,
        fradrag: Int = 0,
        bruttoPerAr: Double = 0.0,
        nettoPerAr: Double = 0.0,
        fradragPerAr: Double = 0.0,
        ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("TP"),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        fradragsTransaksjon: Boolean = false,
        opphort: Boolean = false,
        sakType: SakTypeCti? = null,
        formelKode: FormelKodeCti? = FormelKodeCti("TPx"),
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
        this.spt = spt
        this.ypt = ypt
        this.opt = opt
        this.skiltesDelAvAdodesTP = skiltesDelAvAdodesTP
        this.uavkortetSTBrutto = uavkortetSTBrutto
        this.uavkortetSTBruttoPerAr = uavkortetSTBruttoPerAr
        this.tp_up = tp_up
        this.tp_yp = tp_yp
    }
}

