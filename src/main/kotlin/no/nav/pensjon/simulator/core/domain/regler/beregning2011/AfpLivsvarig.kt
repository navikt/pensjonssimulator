package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti
import java.io.Serializable

@JsonSubTypes(
    JsonSubTypes.Type(value = FremskrevetAfpLivsvarig::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class AfpLivsvarig : Serializable, Ytelseskomponent {
    var justeringsbelop: Int = 0
    var afpProsentgrad: Double = 0.0
    var afpForholdstall: Double = 0.0

    constructor() : super(
            ytelsekomponentType = YtelsekomponentTypeCti("AFP_LIVSVARIG"),
            formelKode = FormelKodeCti("AFPx")
    )

    constructor(o: AfpLivsvarig) : super(o) {
        afpForholdstall = o.afpForholdstall
        afpProsentgrad = o.afpProsentgrad
        justeringsbelop = o.justeringsbelop
    }

    constructor(
            justeringsbelop: Int = 0,
            afpProsentgrad: Double = 0.0,
            afpForholdstall: Double = 0.0,
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("AFP_LIVSVARIG"),
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti? = FormelKodeCti("AFPx"),
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
        this.justeringsbelop = justeringsbelop
        this.afpProsentgrad = afpProsentgrad
        this.afpForholdstall = afpForholdstall
    }
}
