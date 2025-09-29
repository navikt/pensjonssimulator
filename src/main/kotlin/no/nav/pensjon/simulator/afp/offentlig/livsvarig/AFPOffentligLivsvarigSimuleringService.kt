package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.AFPBeholdningClient
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.to.HentDelingstallRequest

import no.nav.tjenestepensjon.simulering.v2025.afp.v1.AlderForDelingstallBeregner.bestemAldreForDelingstall
import no.nav.tjenestepensjon.simulering.v2025.afp.v1.OffentligAFPYtelseBeregner.beregnAfpOffentligLivsvarigYtelser
import org.springframework.stereotype.Service

@Service
class AFPOffentligLivsvarigSimuleringService(val simulerAFPBeholdningGrunnlag: AFPBeholdningClient, val simulatorContext: SimulatorContext) {
    private val log = KotlinLogging.logger {}
    fun simuler(request: LivsvarigOffentligAfpSpec): List<AfpOffentligLivsvarigYtelseMedDelingstall> {
        val aldreForDelingstall: List<AlderForDelingstall> = bestemAldreForDelingstall(request.foedselsdato, request.fom)

        val requestToAFPBeholdninger = SimulerAFPBeholdningGrunnlagRequest(request.pid, request.fom, request.fremtidigInntektListe.map { InntektPeriode(it.fom, it.aarligBeloep) })

        val beholdningerMedAldreForDelingstall: List<PensjonsbeholdningMedDelingstallAlder> = simulerAFPBeholdningGrunnlag.simulerAFPBeholdningGrunnlag(requestToAFPBeholdninger)
            .map { periode -> PensjonsbeholdningMedDelingstallAlder(periode.pensjonsBeholdning, aldreForDelingstall.first { it.datoVedAlder.year == periode.fom.year }) }

        val spec = HentDelingstallRequest(request.foedselsdato.year, beholdningerMedAldreForDelingstall.map { it.alderForDelingstall.alder })
        val delingstallListe = simulatorContext.hentDelingstall(spec).delingstall

        val beregningsgrunnlag = beholdningerMedAldreForDelingstall
            .map {
                AfpBeregningsgrunnlag(
                    it.pensjonsbeholdning,
                    it.alderForDelingstall,
                    delingstallListe.first { dt -> dt.alder == it.alderForDelingstall.alder }.delingstall
                )
            }
        log.info { "Request for beregning av AFP: ${request.fremtidigInntektListe}\n" +
                "${request.fom}" }
        log.info { "Beregningsgrunnlag for AFP: $beregningsgrunnlag" }

        return beregnAfpOffentligLivsvarigYtelser(beregningsgrunnlag)
    }
}
