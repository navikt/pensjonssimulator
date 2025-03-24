package no.nav.pensjon.simulator.core.domain.regler

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

    constructor()
    constructor(merknad: Merknad) {
        kode = merknad.kode
        argumentListe = ArrayList(merknad.argumentListe)
    }

    fun asString(): String = "$kode:${argumentListe.joinToString(separator = ",")}"
}
