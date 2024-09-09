package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.RegelendringTypeCti
import java.util.*

class Regelendring : Comparable<Regelendring> {
    /**
     * Datoen en regel- eller satsendring har virkningsdato.
     */
    var endringsdato: Date? = null

    /**
     * Tekst som beskriver typen endring, ref. kodeverk.
     */
    var endringstype: RegelendringTypeCti? = null

    constructor() : super()

    constructor(regelendring: Regelendring) : super() {
        if (regelendring.endringsdato != null) {
            endringsdato = regelendring.endringsdato!!.clone() as Date
        }
        if (regelendring.endringstype != null) {
            endringstype = RegelendringTypeCti(regelendring.endringstype)
        }
    }

    constructor(
        endringsdato: Date? = null,
        endringstype: RegelendringTypeCti? = null
    ) {
        this.endringsdato = endringsdato
        this.endringstype = endringstype
    }

    override fun compareTo(other: Regelendring): Int {
        //Sorterer primært på dato
        if (other.endringsdato != null && endringsdato != null) {
            val dateCompare = endringsdato!!.compareTo(other.endringsdato!!)
            if (dateCompare != 0) {
                return dateCompare
            }
        }
        //Sorterer sekundært alfabetisk på regelendringstype
        return if (other.endringstype != null && endringstype != null) {
            endringstype!!.kode.compareTo(other.endringstype!!.kode)
        } else {
            0
        }//Nullpointer på dato eller regelendringtype, eller helt like endringer.
    }
}
