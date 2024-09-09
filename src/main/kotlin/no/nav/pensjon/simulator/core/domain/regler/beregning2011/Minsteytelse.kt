package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid

/**
 * @author Steinar Hjellvik (Decisive) - PKPYTON-1746
 * @author Frederik Rønnevig (Decisive) - PK-18954 (Nytt felt eksportforbud)
 */
class Minsteytelse {
    var formelKode: FormelKodeCti? = null
    var merknadListe: MutableList<Merknad> = mutableListOf()
    var satsMinsteytelse: SatsMinsteytelse? = null
    var arsbelop: Int = 0
    var eksportforbud: Boolean = false

    /**
     * Trygdetid som er brukt ved beregning av minsteytelsen.
     */
    var anvendtTrygdetid: AnvendtTrygdetid? = null

    /**
     * Angir hvorvidt flyktningfordelen er anvendt ved fastsettelse av minsteytelsens trygdetid.
     */
    //SIMDOM-MOD @JsonIgnore var anvendtFlyktningFaktum: Faktum<UtfallTypeCti> = Faktum("Anvendt flyktning", UtfallEnum.IKKE_RELEVANT.cti())

    //SIMDOM-MOD @JsonGetter fun anvendtFlyktning(): UtfallTypeCti = anvendtFlyktningFaktum.value

    constructor() : super() {
        merknadListe = mutableListOf()
    }

    constructor(minsteytelse: Minsteytelse) : super() {
        if (minsteytelse.formelKode != null) {
            formelKode = FormelKodeCti(minsteytelse.formelKode!!)
        }
        merknadListe = mutableListOf()
        for (merknad in minsteytelse.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }

        if (minsteytelse.satsMinsteytelse != null) {
            satsMinsteytelse = SatsMinsteytelse(minsteytelse.satsMinsteytelse!!)
        }
        arsbelop = minsteytelse.arsbelop
        if (minsteytelse.anvendtTrygdetid != null) {
            anvendtTrygdetid = AnvendtTrygdetid(minsteytelse.anvendtTrygdetid!!)
        }

        eksportforbud = minsteytelse.eksportforbud

        // SIMDOM-MOD anvendtFlyktningFaktum = Faktum(minsteytelse.anvendtFlyktningFaktum.name,minsteytelse.anvendtFlyktningFaktum.value)
            //.apply { children.addAll(minsteytelse.anvendtFlyktningFaktum.children) }
    }

    constructor(
        formelKode: FormelKodeCti? = null,
        merknadListe: MutableList<Merknad> = mutableListOf(),
        satsMinsteytelse: SatsMinsteytelse? = null,
        arsbelop: Int = 0,
        eksportforbud: Boolean = false,
        anvendtTrygdetid: AnvendtTrygdetid? = null
    ) {
        this.formelKode = formelKode
        this.merknadListe = merknadListe
        this.satsMinsteytelse = satsMinsteytelse
        this.arsbelop = arsbelop
        this.eksportforbud = eksportforbud
        this.anvendtTrygdetid = anvendtTrygdetid
    }

}

