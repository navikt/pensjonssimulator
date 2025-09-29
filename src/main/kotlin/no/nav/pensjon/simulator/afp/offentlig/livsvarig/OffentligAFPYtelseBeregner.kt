package no.nav.tjenestepensjon.simulering.v2025.afp.v1

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.AfpBeregningsgrunnlag
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpYtelseMedDelingstall

object OffentligAFPYtelseBeregner {
    private val opptjeningssatsAFPBeholdning = 0.0421
    private val opptjeningssatsPensjonsbeholdning = 0.181

    fun beregnAfpOffentligLivsvarigYtelser(
        grunnlag: List<AfpBeregningsgrunnlag>
    ): List<LivsvarigOffentligAfpYtelseMedDelingstall> {
        val afpBeregningsgrunnlagVedUttak = grunnlag[0]
        val ytelseFraOnsketUttaksdato = LivsvarigOffentligAfpYtelseMedDelingstall(
            pensjonBeholdning = afpBeregningsgrunnlagVedUttak.pensjonsbeholdning,
            afpYtelsePerAar = beregn(afpBeregningsgrunnlagVedUttak.pensjonsbeholdning, afpBeregningsgrunnlagVedUttak.delingstall),
            delingstall = afpBeregningsgrunnlagVedUttak.delingstall,
            gjelderFom = afpBeregningsgrunnlagVedUttak.alderForDelingstall.datoVedAlder,
            gjelderFomAlder = afpBeregningsgrunnlagVedUttak.alderForDelingstall.alder
        )

        if (grunnlag.size == 2) {
            val afpBeregningsgrunnlagEtterAarskifteTil63 = grunnlag[1]
            val andreArsYtelse = LivsvarigOffentligAfpYtelseMedDelingstall(
                pensjonBeholdning = afpBeregningsgrunnlagEtterAarskifteTil63.pensjonsbeholdning,
                afpYtelsePerAar = beregn(afpBeregningsgrunnlagEtterAarskifteTil63.pensjonsbeholdning - afpBeregningsgrunnlagVedUttak.pensjonsbeholdning, afpBeregningsgrunnlagEtterAarskifteTil63.delingstall) + ytelseFraOnsketUttaksdato.afpYtelsePerAar,
                delingstall = afpBeregningsgrunnlagEtterAarskifteTil63.delingstall,
                gjelderFom = afpBeregningsgrunnlagEtterAarskifteTil63.alderForDelingstall.datoVedAlder,
                gjelderFomAlder = afpBeregningsgrunnlagEtterAarskifteTil63.alderForDelingstall.alder
            )
            return listOf(ytelseFraOnsketUttaksdato, andreArsYtelse)
        }

        return listOf(ytelseFraOnsketUttaksdato)
    }

    fun beregn(pensjonsBeholdning: Int, delingstall: Double): Double {
        return (pensjonsBeholdning / opptjeningssatsPensjonsbeholdning) * opptjeningssatsAFPBeholdning / delingstall
    }
}