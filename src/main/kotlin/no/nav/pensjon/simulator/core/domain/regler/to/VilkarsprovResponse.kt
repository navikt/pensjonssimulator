package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class VilkarsprovResponse(
    var vedtaksliste: MutableList<VilkarsVedtak> = mutableListOf(),
    override val pakkseddel: Pakkseddel = Pakkseddel()
) : ServiceResponse(pakkseddel) {
    override fun virkFom(): Date? = vedtaksliste.firstOrNull()?.virkFom

    override fun persons(): String = ""
}
