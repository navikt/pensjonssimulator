package no.nav.pensjon.simulator.core.domain.regler.util.formula

// 2026-05-05
class Formel() {
    var emne: String = ""
    var navn: String = ""
    var prefix: String = ""
    var resultat: Number? = null
    var notasjon: String = ""
    var innhold: String = ""
    var subFormelList: ArrayList<Formel> = ArrayList()

    /** Copy constructor */
    constructor(f: Formel) : this() {
        emne = f.emne
        navn = f.navn
        prefix = f.prefix
        resultat = f.resultat
        notasjon = f.notasjon
        innhold = f.innhold
        subFormelList = ArrayList(f.subFormelList)
    }
}