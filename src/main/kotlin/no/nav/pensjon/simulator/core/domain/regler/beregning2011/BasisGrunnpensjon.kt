package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Grunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GPSatsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid

/**
 * En versjon av Grunnpensjon uten tilgang til brutto og netto siden Basispensjonsytelsene kun
 * er definert med årsbeløp
 */
class BasisGrunnpensjon : Grunnpensjon {

    val grunnpensjon: Grunnpensjon
        get() = Grunnpensjon(this)

    constructor() : super()

    constructor(gp: Grunnpensjon) : super(gp) {
        // Fjerner brutto og netto
        super.brutto = 0
        super.netto = 0
    }

    constructor(
            /** super Grunnpensjon */
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
            formelKode: FormelKodeCti = FormelKodeCti("BasGPx"),
            reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
            /** super Grunnpensjon */
            pSats_gp = pSats_gp,
            satsType = satsType,
            ektefelleInntektOver2G = ektefelleInntektOver2G,
            anvendtTrygdetid = anvendtTrygdetid,
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
