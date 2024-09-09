package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantitilleggsbeholdningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class RevurderingAlderspensjon2025Request(
    var kravhode: Kravhode? = null,
    var vilkarsvedtakListe: ArrayList<VilkarsVedtak> = arrayListOf(),
    var infoPavirkendeYtelse: InfoPavirkendeYtelse? = null,
    var epsMottarPensjon: Boolean = false,
    var forholdstallUtvalg: ForholdstallUtvalg? = null,
    var delingstallUtvalg: DelingstallUtvalg? = null,
    var virkFom: Date? = null,
    var sisteAldersBeregning2011: SisteAldersberegning2011? = null,
    var afpLivsvarig: AfpLivsvarig? = null,
    var garantitilleggsbeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag? = null
) : ServiceRequest() {
    constructor(request: RevurderingAlderspensjon2025Request) : this() {
        kravhode = request.kravhode?.let { Kravhode(it) }
        vilkarsvedtakListe =
            request.vilkarsvedtakListe.map { VilkarsVedtak(it) }.toMutableList() as ArrayList<VilkarsVedtak>
        infoPavirkendeYtelse = request.infoPavirkendeYtelse?.let { InfoPavirkendeYtelse(it) }
        epsMottarPensjon = request.epsMottarPensjon
        forholdstallUtvalg = request.forholdstallUtvalg?.let { ForholdstallUtvalg(it) }
        delingstallUtvalg = request.delingstallUtvalg?.let { DelingstallUtvalg(it) }
        virkFom = request.virkFom
        sisteAldersBeregning2011 = request.sisteAldersBeregning2011?.let { SisteAldersberegning2011(it) }
        afpLivsvarig = request.afpLivsvarig?.let { AfpLivsvarig(it) }
        garantitilleggsbeholdningGrunnlag = request.garantitilleggsbeholdningGrunnlag?.let {
            GarantitilleggsbeholdningGrunnlag(
                it
            )
        }
    }

    override fun virkFom(): Date? = this.virkFom

    override fun persons(): String = ""
}
