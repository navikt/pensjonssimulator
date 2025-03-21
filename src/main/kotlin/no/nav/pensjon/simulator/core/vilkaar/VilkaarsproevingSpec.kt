package no.nav.pensjon.simulator.core.vilkaar

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

// PEN: no.nav.service.pensjon.simulering.abstractsimulerapfra2011.FPEN028VilkarsprovKravRequest
data class VilkaarsproevingSpec(
    val livsvarigOffentligAfpGrunnlag: AfpOffentligLivsvarigGrunnlag?,
    val privatAfp: AfpPrivatLivsvarig?,
    val virkningFom: LocalDate,
    val kravhode: Kravhode,
    val afpFoersteVirkning: LocalDate?,
    val sisteBeregning: SisteBeregning?,
    val forrigeVedtakListe: List<VilkarsVedtak>,
    val soekerFoersteVirkning: LocalDate,
    val avdoedFoersteVirkning: LocalDate?,
    val sakId: Long?,
    val ignoreAvslag: Boolean
)
