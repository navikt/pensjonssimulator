package no.nav.pensjon.simulator.core.domain.regler

// 2025-03-23
class Merknad {
    /**
     * Identifiserer merknaden. Navnekonvensjonen er:
     * Regelsettnavn.regelnavn.index, der ".index" er en opsjon.
     */
    var kode: String? = null

    /**
     * Beskrivende
     */
    var argumentListe: List<String> = mutableListOf()

    //--- Extra:
    // For choice of separator character, ref. no.nav.pensjon.pen.domain.api.Merknad in PEN
    fun asString(): String = "$kode:${argumentListe.joinToString(separator = "¤")}"
}
