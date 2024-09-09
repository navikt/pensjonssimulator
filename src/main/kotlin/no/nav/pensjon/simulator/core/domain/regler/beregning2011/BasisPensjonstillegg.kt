package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.MinstepenNivaCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * En versjon av Pensjonstillegg uten tilgang til brutto og netto siden Basispensjonsytelsene kun
 * er definert med årsbeløp
 */
class BasisPensjonstillegg : Pensjonstillegg {
    val pensjonstillegg: Pensjonstillegg
        get() = Pensjonstillegg(this)

    constructor() : super()

    constructor(pt: Pensjonstillegg) : super(pt) {
        // Fjerner brutto og netto
        brutto = 0
        netto = 0
    }

    constructor(bp: BasisPensjonstillegg) : super(bp)

    constructor(
            /** super Pensjonstillegg */
            forholdstall67: Double = 0.0,
            minstepensjonsnivaSats: Double = 0.0,
            minstepensjonsnivaSatsType: MinstepenNivaCti? = null,
            justertMinstePensjonsniva: JustertMinstePensjonsniva? = null,
            /** super Ytelseskomponent */
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
            formelKode: FormelKodeCti = FormelKodeCti("PenTx"),
            reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
            /** super Pensjonstillegg */
            forholdstall67 = forholdstall67,
            minstepensjonsnivaSats = minstepensjonsnivaSats,
            minstepensjonsnivaSatsType = minstepensjonsnivaSatsType,
            justertMinstePensjonsniva = justertMinstePensjonsniva,
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
