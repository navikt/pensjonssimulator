package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning

import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.AfpBeregningsgrunnlag
import no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag.LivsvarigOffentligAfpYtelseMedDelingstall

object LivsvarigOffentligAfpYtelseBeregner {
    private const val OPPTJENINGSSATS_AFP_BEHOLDNING = 0.0421
    private const val OPPTJENINGSSATS_PENSJONSBEHOLDNING = 0.181

    fun beregnYtelser(
        grunnlag: List<AfpBeregningsgrunnlag>
    ): List<LivsvarigOffentligAfpYtelseMedDelingstall> {
        val afpBeregningsgrunnlagVedUttak = grunnlag[0]
        val ytelseFraOnsketUttaksdato = LivsvarigOffentligAfpYtelseMedDelingstall(
            pensjonBeholdning = afpBeregningsgrunnlagVedUttak.pensjonsbeholdning,
            afpYtelsePerAar = beregn(
                afpBeregningsgrunnlagVedUttak.pensjonsbeholdning,
                afpBeregningsgrunnlagVedUttak.delingstall
            ),
            delingstall = afpBeregningsgrunnlagVedUttak.delingstall,
            gjelderFom = afpBeregningsgrunnlagVedUttak.alderForDelingstall.datoVedAlder,
            gjelderFomAlder = afpBeregningsgrunnlagVedUttak.alderForDelingstall.alder
        )

        if (grunnlag.size == 2) {
            val afpBeregningsgrunnlagEtterAarskifteTil63 = grunnlag[1]
            val andreAarsYtelse = LivsvarigOffentligAfpYtelseMedDelingstall(
                pensjonBeholdning = afpBeregningsgrunnlagEtterAarskifteTil63.pensjonsbeholdning,
                afpYtelsePerAar = beregn(
                    afpBeregningsgrunnlagEtterAarskifteTil63.pensjonsbeholdning - afpBeregningsgrunnlagVedUttak.pensjonsbeholdning,
                    afpBeregningsgrunnlagEtterAarskifteTil63.delingstall
                ) + ytelseFraOnsketUttaksdato.afpYtelsePerAar,
                delingstall = afpBeregningsgrunnlagEtterAarskifteTil63.delingstall,
                gjelderFom = afpBeregningsgrunnlagEtterAarskifteTil63.alderForDelingstall.datoVedAlder,
                gjelderFomAlder = afpBeregningsgrunnlagEtterAarskifteTil63.alderForDelingstall.alder
            )
            return listOf(ytelseFraOnsketUttaksdato, andreAarsYtelse)
        }

        return listOf(ytelseFraOnsketUttaksdato)
    }

    fun beregn(pensjonsBeholdning: Int, delingstall: Double): Double {
        return (pensjonsBeholdning / OPPTJENINGSSATS_PENSJONSBEHOLDNING) * OPPTJENINGSSATS_AFP_BEHOLDNING / delingstall
    }
}