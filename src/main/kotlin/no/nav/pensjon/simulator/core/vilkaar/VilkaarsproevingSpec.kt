package no.nav.pensjon.simulator.core.vilkaar

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantitilleggsbeholdningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

// PEN: no.nav.service.pensjon.simulering.abstractsimulerapfra2011.FPEN028VilkarsprovKravRequest
data class VilkaarsproevingSpec(
    val livsvarigOffentligAfpGrunnlag: AfpOffentligLivsvarigGrunnlag?,
    val privatAfp: AfpLivsvarig?,
    val virkningFom: LocalDate,
    val kravhode: Kravhode,
    val afpFoersteVirkning: LocalDate?,
    val forholdstallUtvalg: ForholdstallUtvalg,
    val delingstallUtvalg: DelingstallUtvalg,
    val sisteBeregning: SisteBeregning?,
    val forrigeVedtakListe: List<VilkarsVedtak>,
    val garantitilleggBeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag,
    val soekerFoersteVirkning: LocalDate,
    val avdoedFoersteVirkning: LocalDate?,
    val sakId: Long?,
    val ignoreAvslag: Boolean
)
