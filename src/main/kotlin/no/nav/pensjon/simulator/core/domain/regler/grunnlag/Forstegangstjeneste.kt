package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.io.Serializable
import java.util.*

class Forstegangstjeneste(
    var periodeListe: MutableList<ForstegangstjenestePeriode> = mutableListOf()
) : Serializable {

    val sortertPeriodeListe: MutableList<ForstegangstjenestePeriode>
        get() {
            val sortertPeriodeListe = periodeListe
            sortertPeriodeListe.sort()
            return sortertPeriodeListe
        }

    val forstegangstjenesteAr: MutableList<Int>
        get() {
            val arSet = HashSet<Int>()
            val cal = Calendar.getInstance()
            for (periode in periodeListe) {
                var startÅr = 0
                var sluttÅr = 0
                if (periode.fomDato != null) {
                    cal.time = periode.fomDato
                    startÅr = cal.get(Calendar.YEAR)
                }
                if (periode.tomDato != null) {
                    cal.time = periode.tomDato
                    sluttÅr = cal.get(Calendar.YEAR)
                }
                for (år in startÅr..sluttÅr) {
                    arSet.add(år)
                }
            }
            return arSet.toMutableList()
        }

    constructor(f: Forstegangstjeneste) : this() {
        for (p in f.periodeListe) {
            periodeListe.add(p.let { ForstegangstjenestePeriode(it) })
        }
    }
}
