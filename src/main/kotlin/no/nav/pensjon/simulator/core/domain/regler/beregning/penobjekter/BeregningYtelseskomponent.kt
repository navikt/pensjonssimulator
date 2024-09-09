package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

@JsonSubTypes(
    JsonSubTypes.Type(value = MotregningYtelseskomponent::class),
    JsonSubTypes.Type(value = SkattefriGrunnpensjon::class),
    JsonSubTypes.Type(value = SkattefriUforetrygdOrdiner::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class BeregningYtelseskomponent : Ytelseskomponent {
    var ytelseKomponentTypeName: String? = null
    var beregning: Beregning? = null

    constructor() : super(
            ytelsekomponentType = YtelsekomponentTypeCti()
    )

    constructor(beregningYtelseskomponent: BeregningYtelseskomponent) : super(beregningYtelseskomponent) {
        ytelseKomponentTypeName = beregningYtelseskomponent.ytelseKomponentTypeName
    }

    constructor(
            ytelseKomponentTypeName: String? = null,
            beregning: Beregning? = null,
            /** super */
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti(),
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti? = null,
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
        this.ytelseKomponentTypeName = ytelseKomponentTypeName
        this.beregning = beregning
    }
}
