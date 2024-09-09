package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class ArbeidsavklaringspengerUT : MotregningYtelseskomponent {

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("UT_AAP"))
    constructor(arbeidsavklaringspengerUT: ArbeidsavklaringspengerUT) : super(arbeidsavklaringspengerUT)

    constructor(
            dagsats: Int = 0,
            antallDager: Int = 0,
            /** super BeregningYtelseskomponent */
            ytelseKomponentTypeName: String? = null,
            beregning: Beregning? = null,
            /** super Ytelseskomponent */
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("UT_AAP"),
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti? = null,
            reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(dagsats, antallDager, ytelseKomponentTypeName, beregning, brutto, netto, fradrag, bruttoPerAr, nettoPerAr, fradragPerAr, ytelsekomponentType, merknadListe, fradragsTransaksjon, opphort
    , sakType, formelKode, reguleringsInformasjon)
}
