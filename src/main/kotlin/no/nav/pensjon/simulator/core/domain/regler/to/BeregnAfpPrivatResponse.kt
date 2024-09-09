package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import java.util.*

class BeregnAfpPrivatResponse(
    var beregningsResultatAfpPrivat: BeregningsResultatAfpPrivat? = null,
    override val pakkseddel: Pakkseddel = Pakkseddel()
) : ServiceResponse(pakkseddel) {
    override fun virkFom(): Date? = beregningsResultatAfpPrivat?.virkFom

    override fun persons(): String = ""
}
