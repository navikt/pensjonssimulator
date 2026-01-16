package no.nav.pensjon.simulator.core.vilkaar

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag as LivsvarigOffentligAfpGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig as PrivatAfp

data class VilkaarsproevingSpec(
    val livsvarigOffentligAfpGrunnlag: LivsvarigOffentligAfpGrunnlag?,
    val privatAfp: PrivatAfp?,
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
