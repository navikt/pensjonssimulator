package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag as LivsvarigOffentligAfpGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig as PrivatAfp

data class AlderspensjonRevurderingCommonSpec(
    val kravhode: Kravhode? = null,
    val vilkaarsvedtakListe: MutableList<VilkarsVedtak> = mutableListOf(),
    val paavirkendeYtelseInfo: InfoPavirkendeYtelse? = null,
    val epsMottarPensjon: Boolean = false,
    val virkFom: LocalDate? = null,
    val forrigeAldersberegning: SisteBeregning? = null,
    val privatAfp: PrivatAfp? = null,
    val livsvarigOffentligAfpGrunnlag: LivsvarigOffentligAfpGrunnlag? = null
)
