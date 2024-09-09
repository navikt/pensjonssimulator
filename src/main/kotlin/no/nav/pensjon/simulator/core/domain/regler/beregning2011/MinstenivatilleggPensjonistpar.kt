package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * Endre navn fra "SamletEktefellerGarantitillegg" til "MinstenivatilleggPensjonistpar"
 */
class MinstenivatilleggPensjonistpar : Ytelseskomponent {
    var bruker: BeregningsInformasjonMinstenivatilleggPensjonistpar? = null
    var ektefelle: BeregningsInformasjonMinstenivatilleggPensjonistpar? = null

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("MIN_NIVA_TILL_PPAR"))

    constructor(mintpp: MinstenivatilleggPensjonistpar) : super(mintpp) {
        if (mintpp.ektefelle != null) {
            ektefelle = BeregningsInformasjonMinstenivatilleggPensjonistpar(mintpp.ektefelle!!)
        }
        if (mintpp.bruker != null) {
            bruker = BeregningsInformasjonMinstenivatilleggPensjonistpar(mintpp.bruker!!)
        }
        ytelsekomponentType = YtelsekomponentTypeCti("MIN_NIVA_TILL_PPAR")
    }

    constructor(
            bruker: BeregningsInformasjonMinstenivatilleggPensjonistpar? = null,
            ektefelle: BeregningsInformasjonMinstenivatilleggPensjonistpar? = null,
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("MIN_NIVA_TILL_PPAR"),
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
        this.bruker = bruker
        this.ektefelle = ektefelle
    }
}
