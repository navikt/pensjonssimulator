package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantitilleggsbeholdningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate
import java.util.Date

// no.nav.service.pensjon.simulering.ap2025.beregning.RevurderingAlderspensjonCommonInput
data class AlderspensjonRevurderingCommonSpec(
    val kravhode: Kravhode? = null,
    val vilkarsvedtakListe: MutableList<VilkarsVedtak> = mutableListOf(),
    val infoPavirkendeYtelse: InfoPavirkendeYtelse? = null,
    val epsMottarPensjon: Boolean = false,
    val forholdstallUtvalg: ForholdstallUtvalg? = null,
    val delingstallUtvalg: DelingstallUtvalg? = null,
    val virkFom: LocalDate? = null,
    val forrigeAldersberegning: SisteBeregning? = null,
    val afpLivsvarig: AfpLivsvarig? = null,
    val garantitilleggsbeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag? = null
)
