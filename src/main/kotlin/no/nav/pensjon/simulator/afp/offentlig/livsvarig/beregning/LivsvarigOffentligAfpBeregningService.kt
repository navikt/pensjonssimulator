package no.nav.pensjon.simulator.afp.offentlig.livsvarig.beregning

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.beregning.domain.AfpBeregningsgrunnlag
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.beregning.domain.AfpBeregningsgrunnlagBuilder
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.SimulatorContext
import org.springframework.stereotype.Service

@Service
class LivsvarigOffentligAfpBeregningService(
    val afpBeholdningClient: SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient,
    val simulatorContext: SimulatorContext
) {
    private val log = KotlinLogging.logger {}

    fun simuler(spec: LivsvarigOffentligAfpSpec): LivsvarigOffentligAfpResult {
        val beregningsgrunnlag: List<AfpBeregningsgrunnlag> = AfpBeregningsgrunnlagBuilder()
            .medSpec(spec)
            .leggTilAlderForDelingstall()
            .hentSimulerteAfpBeholdninger(afpBeholdningClient::simuler)
            .hentRelevanteDelingstall(simulatorContext::hentDelingstall)
            .build()

        val ytelser = LivsvarigOffentligAfpYtelseBeregner.beregnYtelser(beregningsgrunnlag)

        return LivsvarigOffentligAfpResult(spec.pid.value, ytelser)
    }
}