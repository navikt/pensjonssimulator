package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

class DelingstallUtvalg(
    var dt: Double = 0.0,
    @JsonIgnore var dt67soker: Double = 0.0,
    @JsonIgnore var dt67virk: Double = 0.0,
    var delingstallListe: MutableList<Delingstall> = mutableListOf()
) : Serializable {

    constructor(dt: Double, delingstallListe: MutableList<Delingstall>) : this() {
        this.dt = dt
        this.delingstallListe = delingstallListe
    }

    constructor(du: DelingstallUtvalg) : this() {
        this.dt = du.dt
        this.dt67soker = du.dt67soker
        this.dt67virk = du.dt67virk
        for (delingstall in du.delingstallListe) {
            delingstallListe.add(Delingstall(delingstall))
        }
    }

    fun sortedDelingstallListe(): MutableList<Delingstall> {
        val listecopy = delingstallListe.toMutableList()
        listecopy.sort()
        return listecopy
    }
}
