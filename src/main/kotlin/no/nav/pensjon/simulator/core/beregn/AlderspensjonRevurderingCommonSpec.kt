package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

// no.nav.service.pensjon.simulering.ap2025.beregning.RevurderingAlderspensjonCommonInput
data class AlderspensjonRevurderingCommonSpec(
    val kravhode: Kravhode? = null,
    val vilkarsvedtakListe: MutableList<VilkarsVedtak> = mutableListOf(),
    val infoPavirkendeYtelse: InfoPavirkendeYtelse? = null,
    val epsMottarPensjon: Boolean = false,
    val virkFom: LocalDate? = null,
    val forrigeAldersberegning: SisteBeregning? = null,
    val privatAfp: AfpPrivatLivsvarig? = null,
    val livsvarigOffentligAfpGrunnlag: AfpOffentligLivsvarigGrunnlag? = null
)
