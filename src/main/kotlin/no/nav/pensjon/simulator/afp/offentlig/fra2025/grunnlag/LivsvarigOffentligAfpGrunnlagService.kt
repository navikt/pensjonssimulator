package no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.util.isBeforeOrOn
import no.nav.pensjon.simulator.g.GrunnbeloepService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class LivsvarigOffentligAfpGrunnlagService(private val grunnbeloepService: GrunnbeloepService) {

    fun livsvarigOffentligAfpGrunnlag(
        afpResult: LivsvarigOffentligAfpResult,
        kravhode: Kravhode,
        maxGjelderFom: LocalDate
    ): AfpOffentligLivsvarigGrunnlag? =
        afpResult.afpYtelseListe
            .filter { it.gjelderFom.isBeforeOrOn(maxGjelderFom) }
            .maxByOrNull { it.gjelderFom }
            ?.let(::grunnlag)
            ?: kravhode.gjeldendeInnvilgetLivsvarigOffentligAfpGrunnlag()

    private fun grunnlag(ytelse: LivsvarigOffentligAfpYtelseMedDelingstall) =
        AfpOffentligLivsvarigGrunnlag(
            sistRegulertG = grunnbeloepService.naavaerendeGrunnbeloep(),
            bruttoPerAr = ytelse.afpYtelsePerAar,
            uttaksdato = ytelse.gjelderFom
            // virkTom only relevant for innvilget AFP
        )
}
