package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

class ForholdstallUtvalg(
    var ft: Double = 0.0,
    var forholdstallListe: MutableList<Forholdstall> = mutableListOf(),
    @JsonIgnore var ft67soker: Double = 0.0,
    @JsonIgnore var ft67virk: Double = 0.0,
    @JsonIgnore var reguleringsfaktor: Double = 0.0

) : Serializable {

    constructor(ft: Double, forholdstallListe: MutableList<Forholdstall>) : this() {
        this.ft = ft
        this.forholdstallListe = forholdstallListe
    }

    constructor(fu: ForholdstallUtvalg) : this() {
        this.ft = fu.ft
        /**
         * Pensjon-Regler variable
         */
        this.ft67soker = fu.ft67soker
        this.ft67virk = fu.ft67virk
        this.reguleringsfaktor = fu.reguleringsfaktor

        for (forholdstall in fu.forholdstallListe) {
            forholdstallListe.add(forholdstall.let { Forholdstall(it) })
        }
    }

    fun sortedForholdstallListe(): MutableList<Forholdstall> {
        val forholdstallListeCopy = mutableListOf<Forholdstall>()
        forholdstallListeCopy.addAll(forholdstallListe)
        forholdstallListeCopy.sort()
        return forholdstallListeCopy
    }
}
