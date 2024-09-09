package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

class Merknad(var kode: String = "", var argumentListe: MutableList<String> = mutableListOf()) : Serializable {

    /**
     * Copy Constructor
     */
    constructor(merknad: Merknad) : this() {
        this.kode = merknad.kode
        this.argumentListe = ArrayList(merknad.argumentListe)
    }

    init {
        this.kode = kode.replace("__", ".")
    }

    fun addArgument(arg: String?) {
        if (arg != null) {
            argumentListe.add(arg)
        }
    }

    @JsonIgnore
    val tekst = "$kode:${argumentListe.joinToString(separator = ",")}"
}
