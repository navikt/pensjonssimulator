package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class RegulerBeregningGrunnlag(
        var beregning1967: Beregning? = null,
        var virkFom: Date? = null,
        var uttaksgradListe: MutableList<Uttaksgrad> = mutableListOf(),
        var brukersVilkarsvedtakListe: MutableList<VilkarsVedtak> = mutableListOf(),
        var sokersPersongrunnlag: Persongrunnlag? = null,
        var epsPersongrunnlag: Persongrunnlag? = null,
        var pakkseddel: Pakkseddel? = null
) {

    constructor(g: RegulerBeregningGrunnlag) : this() {
        if (g.beregning1967 != null) {
            this.beregning1967 = Beregning(g.beregning1967)
        }
        if (g.virkFom != null) {
            this.virkFom = g.virkFom!!.clone() as Date
        }
        if (g.uttaksgradListe.size > 0) {
            for (u in g.uttaksgradListe) {
                this.uttaksgradListe.add(Uttaksgrad(u))
            }
        }
        if (g.brukersVilkarsvedtakListe.size > 0) {
            for (v in g.brukersVilkarsvedtakListe) {
                this.brukersVilkarsvedtakListe.add(VilkarsVedtak(v))
            }
        }
        if (g.sokersPersongrunnlag != null) {
            this.sokersPersongrunnlag = Persongrunnlag(g.sokersPersongrunnlag!!)
        }
        if (g.epsPersongrunnlag != null) {
            this.epsPersongrunnlag = Persongrunnlag(g.epsPersongrunnlag!!)
        }
        if (g.pakkseddel != null) {
            this.pakkseddel = Pakkseddel(g.pakkseddel!!)
        }
    }
}
