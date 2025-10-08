package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.AfpBeregningsgrunnlag
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.AlderForDelingstall
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.PensjonsbeholdningMedDelingstallAlder
import no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.to.HentDelingstallRequest
import org.springframework.stereotype.Service

@Service
class LivsvarigOffentligAfpBeregningService(val simulerAfpBeholdningGrunnlag: SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient, val simulatorContext: SimulatorContext) {
    private val log = KotlinLogging.logger {}
    fun simuler(request: LivsvarigOffentligAfpSpec): LivsvarigOffentligAfpResult {
        val aldreForDelingstall: List<AlderForDelingstall> =
            AlderForDelingstallBeregner.bestemAldreForDelingstall(request.foedselsdato, request.fom)

        val beholdningerMedAldreForDelingstall: List<PensjonsbeholdningMedDelingstallAlder> = simulerAfpBeholdningGrunnlag.simulerAfpBeholdningGrunnlag(request)
            .map { periode ->
                PensjonsbeholdningMedDelingstallAlder(
                    periode.pensjonsbeholdning,
                    aldreForDelingstall.first { it.datoVedAlder.year == periode.fom.year })
            }

        val spec = HentDelingstallRequest(
            request.foedselsdato.year,
            beholdningerMedAldreForDelingstall.map { it.alderForDelingstall.alder })
        val delingstallListe = simulatorContext.hentDelingstall(spec).delingstall

        val beregningsgrunnlag = beholdningerMedAldreForDelingstall
            .map {
                AfpBeregningsgrunnlag(
                    it.pensjonsbeholdning,
                    it.alderForDelingstall,
                    delingstallListe.first { dt -> it.alderForDelingstall.alder.let { alder -> alder.aar == dt.alder.aar && alder.maaneder == dt.alder.maaneder } }.delingstall
                )
            }
        log.info { "Request for beregning av AFP: ${request.fremtidigInntektListe} fom:${request.fom}" }
        log.info { "Beregningsgrunnlag for AFP: $beregningsgrunnlag" }

        val afpoffentligYtelser = LivsvarigOffentligAfpYtelseBeregner.beregnYtelser(beregningsgrunnlag)

        return LivsvarigOffentligAfpResult(request.pid.value, afpoffentligYtelser)
    }
}