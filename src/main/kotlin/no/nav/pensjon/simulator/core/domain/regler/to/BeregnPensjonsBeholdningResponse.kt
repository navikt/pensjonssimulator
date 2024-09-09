package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

class BeregnPensjonsBeholdningResponse(
    var beholdninger: ArrayList<Pensjonsbeholdning> = arrayListOf(),
    override val pakkseddel: Pakkseddel = Pakkseddel()
) : ServiceResponse(pakkseddel) {
    override fun virkFom(): Date? {
        val sisteBeholdningAr = beholdninger.maxOfOrNull { it.ar }

        return sisteBeholdningAr?.let {
            val localDato = LocalDate.of(sisteBeholdningAr, 1, 1)
            Date.from(localDato.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant())
        }
    }

    override fun persons(): String = ""
}
