package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning

import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.AfpBeregningsgrunnlag
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.PensjonsbeholdningMedDelingstallAlder
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.sats.Delingstall

object AfpBeregningsgrunnlagAggregator {
    val HOYESTE_ALDER_FOR_DELINGSTALL = Alder(70, 0)

    fun aggregate(
        beholdningerMedAldreForDelingstall: List<PensjonsbeholdningMedDelingstallAlder>,
        delingstallListe: List<Delingstall>
    ): List<AfpBeregningsgrunnlag> {
        return beholdningerMedAldreForDelingstall
            .map {
                AfpBeregningsgrunnlag(
                    it.pensjonsbeholdning,
                    it.alderForDelingstall,
                    delingstallListe
                        .firstOrNull { dt -> haveEqualAlder(it.alderForDelingstall.alder, dt) }?.delingstall
                        ?: delingstallListe.first { dt -> haveEqualAlder(HOYESTE_ALDER_FOR_DELINGSTALL,dt) }.delingstall
                )
            }
    }

    fun haveEqualAlder(alder: Alder, delingstall: Delingstall): Boolean =
        alder.aar == delingstall.alder.aar && alder.maaneder == delingstall.alder.maaneder
}