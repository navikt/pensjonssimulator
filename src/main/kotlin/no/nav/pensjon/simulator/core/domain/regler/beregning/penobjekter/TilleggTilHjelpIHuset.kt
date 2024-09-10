package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * Brukes kun av BEF270 til G-omregning.
 */
class TilleggTilHjelpIHuset : Ytelseskomponent {

    var grunnlagForUtbetaling: Int = 0

    constructor(tilleggTilHjelpIHuset: TilleggTilHjelpIHuset) : super(tilleggTilHjelpIHuset) {
        grunnlagForUtbetaling = tilleggTilHjelpIHuset.grunnlagForUtbetaling
    }

    constructor(grunnlagForUtbetaling: Int) : super(ytelsekomponentType = YtelsekomponentTypeCti("HJELP_I_HUS")) {
        this.grunnlagForUtbetaling = grunnlagForUtbetaling
        ytelsekomponentType = YtelsekomponentTypeCti("HJELP_I_HUS")
    }

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("HJELP_I_HUS"))

    constructor(
        grunnlagForUtbetaling: Int = 0,
        /** super */
            brutto: Int = 0,
        netto: Int = 0,
        fradrag: Int = 0,
        bruttoPerAr: Double = 0.0,
        nettoPerAr: Double = 0.0,
        fradragPerAr: Double = 0.0,
        ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("HJELP_I_HUS"),
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
        this.grunnlagForUtbetaling = grunnlagForUtbetaling
    }
}