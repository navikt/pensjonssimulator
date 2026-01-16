package no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag as LivsvarigOffentligAfpGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.spec.InnvilgetLivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.util.isBeforeOrOn
import no.nav.pensjon.simulator.g.GrunnbeloepService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class LivsvarigOffentligAfpGrunnlagService(private val grunnbeloepService: GrunnbeloepService) {

    fun livsvarigOffentligAfpGrunnlag(
        innvilgetAfpSpec: InnvilgetLivsvarigOffentligAfpSpec?,
        simulertAfpYtelseListe: List<LivsvarigOffentligAfpYtelseMedDelingstall>,
        kravhode: Kravhode,
        maxGjelderFom: LocalDate
    ): LivsvarigOffentligAfpGrunnlag? =
        innvilgetAfpSpec?.let(::grunnlag)
            ?: simulertGrunnlag(simulertAfpYtelseListe, maxGjelderFom)
            ?: kravhode.gjeldendeInnvilgetLivsvarigOffentligAfpGrunnlag()

    private fun simulertGrunnlag(
        ytelseListe: List<LivsvarigOffentligAfpYtelseMedDelingstall>,
        maxGjelderFom: LocalDate
    ): LivsvarigOffentligAfpGrunnlag? =
        ytelseListe
            .filter { it.gjelderFom.isBeforeOrOn(maxGjelderFom) }
            .maxByOrNull { it.gjelderFom }
            ?.let(::grunnlag)

    private fun grunnlag(ytelse: LivsvarigOffentligAfpYtelseMedDelingstall) =
        LivsvarigOffentligAfpGrunnlag(
            sistRegulertG = grunnbeloepService.naavaerendeGrunnbeloep(),
            bruttoPerAr = ytelse.afpYtelsePerAar,
            uttaksdato = ytelse.gjelderFom
            // virkTom only relevant for innvilget AFP
        )

    private fun grunnlag(ytelse: InnvilgetLivsvarigOffentligAfpSpec) =
        LivsvarigOffentligAfpGrunnlag(
            sistRegulertG = ytelse.sistRegulertGrunnbeloep ?: grunnbeloepService.naavaerendeGrunnbeloep(),
            bruttoPerAr = ytelse.aarligBruttoBeloep,
            uttaksdato = ytelse.uttakFom
        )
}
