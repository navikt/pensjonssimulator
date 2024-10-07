package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.Vector

class VilkarsprovResponse : ServiceResponse() {

    var vedtaksliste: MutableList<VilkarsVedtak> = Vector() //TODO mutableListOf()?
}
