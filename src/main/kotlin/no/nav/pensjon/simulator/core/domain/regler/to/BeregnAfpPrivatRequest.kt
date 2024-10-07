package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class BeregnAfpPrivatRequest : ServiceRequest() {

    var kravhode: Kravhode? = null
    var vilkarsvedtakListe: ArrayList<VilkarsVedtak> = ArrayList()
    var virkFom: Date? = null
    var ft = 0.0
    var justeringsbelop = 0
    var referansebelop = 0
    var ftKompensasjonstillegg = 0.0
    var sisteAfpPrivatBeregning: BeregningsResultatAfpPrivat? = null
    var virkFomAfpPrivatUttak: Date? = null
}
