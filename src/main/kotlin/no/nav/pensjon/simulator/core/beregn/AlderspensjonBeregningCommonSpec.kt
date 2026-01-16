package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag as LivsvarigOffentligAfpGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig as PrivatAfp

data class AlderspensjonBeregningCommonSpec(
    val kravhode: Kravhode? = null,
    val vilkarsvedtakListe: MutableList<VilkarsVedtak> = mutableListOf(),
    val infoPavirkendeYtelse: InfoPavirkendeYtelse? = null,
    val virkFom: LocalDate? = null,
    // virkTom is always null in simulering
    val epsMottarPensjon: Boolean = false,
    val privatAfp: PrivatAfp? = null,
    val livsvarigOffentligAfpGrunnlag: LivsvarigOffentligAfpGrunnlag? = null
)
