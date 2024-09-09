package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti
import no.nav.pensjon.simulator.core.domain.regler.util.formula.Builder.Companion.kmath

/**
 * En versjon av Tilleggspensjon uten tilgang til brutto og netto siden Basispensjonsytelsene kun
 * er definert med årsbeløp
 */
class BasisTilleggspensjon : Tilleggspensjon {

    val tilleggspensjon: Tilleggspensjon
        get() = Tilleggspensjon(this)

    override fun formelPrefix(): String {
        return "BasTP"
    }

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("TP"))

    constructor(tilleggspensjon: Tilleggspensjon) : super(tilleggspensjon) {
        // Fjerner brutto og netto
        brutto = 0
        netto = 0

        kmath().felt(this::brutto).provider(this).build()
        kmath().felt(this::netto).provider(this).build()
    }

    constructor(bt: BasisTilleggspensjon) : super(bt)
    constructor(
        /** super Tilleggspensjon */
        spt: Sluttpoengtall? = null,
        ypt: Sluttpoengtall? = null,
        opt: Sluttpoengtall? = null,
        skiltesDelAvAdodesTP: Int = 0,
        uavkortetSTBrutto: Int = 0,
        uavkortetSTBruttoPerAr: Double = 0.0,
        tp_up: Int = 0,
        tp_yp: Int = 0,
        /** super Ytelseskomponent */
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
        formelKode: FormelKodeCti? = FormelKodeCti("BasTPx"),
        reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
        /** super Tilleggspensjon */
        spt = spt,
        ypt = ypt,
        opt = opt,
        skiltesDelAvAdodesTP = skiltesDelAvAdodesTP,
        uavkortetSTBrutto = uavkortetSTBrutto,
        uavkortetSTBruttoPerAr = uavkortetSTBruttoPerAr,
        tp_up = tp_up,
        tp_yp = tp_yp,
        /** super Ytelseskomponent */
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
    )

}
