package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonPensjonsbeholdning
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

class RegulerPensjonsbeholdningResponse(
    var regulertBeregningsgrunnlagForPensjonsbeholdning: ArrayList<PersonPensjonsbeholdning> = arrayListOf()
) : ServiceResponse() {
    override fun virkFom(): Date? {
        val sisteBeholdningAr = regulertBeregningsgrunnlagForPensjonsbeholdning.maxOfOrNull { it.pensjonsbeholdning?.ar ?: 0 }

        return sisteBeholdningAr?.let {
            val localDato = LocalDate.of(sisteBeholdningAr, 1, 1)
            Date.from(localDato.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant())
        }
    }

    override fun persons(): String {
        return ""
    }
}
