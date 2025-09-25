package no.nav.tjenestepensjon.simulering.v2025.afp.v1

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.AfpBeregningsgrunnlag
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.AfpOffentligLivsvarigYtelseMedDelingstall

object OffentligAFPYtelseBeregner {
    private val opptjeningssatsAFPBeholdning = 0.0421
    private val opptjeningssatsPensjonsbeholdning = 0.181

    fun beregnAfpOffentligLivsvarigYtelser(
        grunnlag: List<AfpBeregningsgrunnlag>
    ): List<AfpOffentligLivsvarigYtelseMedDelingstall> {
        val afpBeregningsgrunnlagVedUttak = grunnlag[0]
        val ytelseFraOnsketUttaksdato = AfpOffentligLivsvarigYtelseMedDelingstall(
            pensjonsbeholdning = afpBeregningsgrunnlagVedUttak.pensjonsbeholdning,
            afpYtelsePerAar = beregn(afpBeregningsgrunnlagVedUttak.pensjonsbeholdning, afpBeregningsgrunnlagVedUttak.delingstall),
            delingstall = afpBeregningsgrunnlagVedUttak.delingstall,
            gjelderFraOgMed = afpBeregningsgrunnlagVedUttak.alderForDelingstall.datoVedAlder,
            gjelderFraOgMedAlder = afpBeregningsgrunnlagVedUttak.alderForDelingstall.alder
        )

        if (grunnlag.size == 2) {
            val afpBeregningsgrunnlagEtterAarskifteTil63 = grunnlag[1]
            val andreArsYtelse = AfpOffentligLivsvarigYtelseMedDelingstall(
                pensjonsbeholdning = afpBeregningsgrunnlagEtterAarskifteTil63.pensjonsbeholdning,
                afpYtelsePerAar = beregn(afpBeregningsgrunnlagEtterAarskifteTil63.pensjonsbeholdning - afpBeregningsgrunnlagVedUttak.pensjonsbeholdning, afpBeregningsgrunnlagEtterAarskifteTil63.delingstall) + ytelseFraOnsketUttaksdato.afpYtelsePerAar,
                delingstall = afpBeregningsgrunnlagEtterAarskifteTil63.delingstall,
                gjelderFraOgMed = afpBeregningsgrunnlagEtterAarskifteTil63.alderForDelingstall.datoVedAlder,
                gjelderFraOgMedAlder = afpBeregningsgrunnlagEtterAarskifteTil63.alderForDelingstall.alder
            )
            return listOf(ytelseFraOnsketUttaksdato, andreArsYtelse)
        }

        return listOf(ytelseFraOnsketUttaksdato)
    }

    fun beregn(pensjonsBeholdning: Int, delingstall: Double): Double {
        return (pensjonsBeholdning / opptjeningssatsPensjonsbeholdning) * opptjeningssatsAFPBeholdning / delingstall
    }
}