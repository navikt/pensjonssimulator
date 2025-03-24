package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

// no.nav.service.pensjon.simulering.ap2025.beregning.BeregningAlderspensjonCommonInput
data class AlderspensjonBeregningCommonSpec(
    val kravhode: Kravhode? = null,
    val vilkarsvedtakListe: MutableList<VilkarsVedtak> = mutableListOf(),
    val infoPavirkendeYtelse: InfoPavirkendeYtelse? = null,
    val virkFom: LocalDate? = null,
    // virkTom is always null in simulering
    val epsMottarPensjon: Boolean = false,
    val privatAfp: AfpPrivatLivsvarig? = null,
    val livsvarigOffentligAfpGrunnlag: AfpOffentligLivsvarigGrunnlag? = null
)
