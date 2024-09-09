package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.MinstepenNivaCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

@JsonSubTypes(
    JsonSubTypes.Type(value = BasisPensjonstillegg::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class Pensjonstillegg : Ytelseskomponent {
    var forholdstall67: Double = 0.0
    var minstepensjonsnivaSats: Double = 0.0
    var minstepensjonsnivaSatsType: MinstepenNivaCti? = null
    var justertMinstePensjonsniva: JustertMinstePensjonsniva? = null

    constructor() : super(
            ytelsekomponentType = YtelsekomponentTypeCti("PT"),
            formelKode = FormelKodeCti("PenTx")
    )

    constructor(pt: Pensjonstillegg) : super(pt) {
        forholdstall67 = pt.forholdstall67
        minstepensjonsnivaSats = pt.minstepensjonsnivaSats
        if (pt.minstepensjonsnivaSatsType != null) {
            minstepensjonsnivaSatsType = MinstepenNivaCti(pt.minstepensjonsnivaSatsType)
        }
        if (pt.justertMinstePensjonsniva != null) {
            justertMinstePensjonsniva = JustertMinstePensjonsniva(pt.justertMinstePensjonsniva!!)
        }
    }

    constructor(
            forholdstall67: Double = 0.0,
            minstepensjonsnivaSats: Double = 0.0,
            minstepensjonsnivaSatsType: MinstepenNivaCti? = null,
            justertMinstePensjonsniva: JustertMinstePensjonsniva? = null,
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("PT"),
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti? = FormelKodeCti("PenTx"),
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
        this.forholdstall67 = forholdstall67
        this.minstepensjonsnivaSats = minstepensjonsnivaSats
        this.minstepensjonsnivaSatsType = minstepensjonsnivaSatsType
        this.justertMinstePensjonsniva = justertMinstePensjonsniva
    }

}
