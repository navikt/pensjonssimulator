package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantitilleggsbeholdningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

// no.nav.service.pensjon.simulering.abstractsimulerapfra2011.FPEN028VilkarsprovKravRequest
data class VilkaarsproevingSpec(
    val afpOffentligLivsvarig: AfpOffentligLivsvarigGrunnlag?,
    val afpLivsvarig: AfpLivsvarig?,
    val virkFom: LocalDate,
    val kravhode: Kravhode,
    val afpForsteVirk: LocalDate?,
    val forholdstallUtvalg: ForholdstallUtvalg,
    val delingstallUtvalg: DelingstallUtvalg,
    val sisteBeregning: SisteBeregning?,
    val forrigeVilkarsvedtakList: List<VilkarsVedtak>,
    val garantitilleggsbeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag,
    val sokerForsteVirk: LocalDate,
    val avdodForsteVirk: LocalDate?,
    val sakId: Long?,
    val ignoreAvslag: Boolean = false
)
