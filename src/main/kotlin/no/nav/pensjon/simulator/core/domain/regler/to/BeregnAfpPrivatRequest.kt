package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class BeregnAfpPrivatRequest(
    var kravhode: Kravhode? = null,
    var vilkarsvedtakListe: ArrayList<VilkarsVedtak> = arrayListOf(),
    var virkFom: Date? = null,
    var ft: Double = 0.0,
    var justeringsbelop: Int = 0,
    var referansebelop: Int = 0,
    var ftKompensasjonstillegg: Double = 0.0,
    var sisteAfpPrivatBeregning: BeregningsResultatAfpPrivat? = null,
    var virkFomAfpPrivatUttak: Date? = null
) : ServiceRequest() {
    override fun virkFom(): Date? = this.virkFom

    override fun persons(): String = ""
}
