package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class FremskrevetAfpLivsvarig : AfpLivsvarig, Regulering {
    override var reguleringsfaktor: Double = 0.0
    override var gap: Int = 0
    var gjennomsnittligUttaksgradSisteAr: Double = 0.0

    constructor() {
        ytelsekomponentType = YtelsekomponentTypeCti("FREM_AFP_LIVSVARIG")
    }

    constructor(reguleringsfaktor: Double, gap: Int, gjennomsnittligUttaksgradSisteAr: Double) : this() {
        this.reguleringsfaktor = reguleringsfaktor
        this.gap = gap
        this.gjennomsnittligUttaksgradSisteAr = gjennomsnittligUttaksgradSisteAr
    }

    constructor(fremskrevetAfpLivsvarig: FremskrevetAfpLivsvarig) : this() {
        reguleringsfaktor = fremskrevetAfpLivsvarig.reguleringsfaktor
        gap = fremskrevetAfpLivsvarig.gap
        gjennomsnittligUttaksgradSisteAr = fremskrevetAfpLivsvarig.gjennomsnittligUttaksgradSisteAr
    }

    constructor(
        reguleringsfaktor: Double = 0.0,
        gap: Int = 0,
        gjennomsnittligUttaksgradSisteAr: Double = 0.0,
        /** super AfpLivsvarig */
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
        ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("FREM_AFP_LIVSVARIG"),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        fradragsTransaksjon: Boolean = false,
        opphort: Boolean = false,
        sakType: SakTypeCti? = null,
        formelKode: FormelKodeCti? = null,
        reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
            /** super AfpLivsvarig */
            justeringsbelop = justeringsbelop,
            afpProsentgrad = afpProsentgrad,
            afpForholdstall = afpForholdstall,
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
    ) {
        this.reguleringsfaktor = reguleringsfaktor
        this.gap = gap
        this.gjennomsnittligUttaksgradSisteAr = gjennomsnittligUttaksgradSisteAr
    }
}
