package no.nav.pensjon.simulator.core.domain.regler.beregning2011

open class BeregningsResultatAfpPrivat : AbstraktBeregningsResultat() {
    var afpPrivatBeregning: AfpPrivatBeregning? = null

    override fun hentBeregningsinformasjon(): BeregningsInformasjon? = null // SIMDOM-ADD; no beregningsinformasjon for AFP privat
}
