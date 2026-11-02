package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.nav

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.beregning.LivsvarigOffentligAfpBeregningService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class SammenlignAfpService(
    private val navBeregnetAfpService: LivsvarigOffentligAfpBeregningService,
    private val featureToggleService: FeatureToggleService,
) {
    private val log = KotlinLogging.logger {}

    @Async("loggingExecutor")
    fun sammenlignOgLoggAfp(
        request: OffentligTjenestepensjonFra2025SimuleringSpec,
        simulertTjenestepensjon: SimulertTjenestepensjon,
        tpOrdning: String
    ) {

        featureToggleService.isEnabled("SAMMENLIGN_NAV_BEREGNET_AFP_MED_$tpOrdning") || return

        val livsvarigOffentligAfpSpec = AfpYtelseMapper.toLivsvarigOffentligAfpSpec(request)

        val navResultat = navBeregnetAfpService.simuler(livsvarigOffentligAfpSpec).afpYtelseListe
        val hentetResultat = simulertTjenestepensjon.utbetalingsperioder.filter { it.ytelseType == "OAFP" }

        val sammenligningsresultat = navResultat.map { it.gjelderFom }.toSet()
            .union(simulertTjenestepensjon.utbetalingsperioder.map { it.fom }.toSet())
            .map { AfpYtelseSammenligningsresultat(it) }
            .map { resultat ->
                resultat.apply {
                    navAfp = navResultat.find { it.gjelderFom == resultat.fom }?.afpYtelsePerAar?.toInt()
                    hentetAfp = hentetResultat.find { it.fom == resultat.fom }?.maanedligBelop?.times(12)
                }
            }.toSortedSet( compareBy { it.fom })

        //vi sjekker kun første periode, siden de fleste tpLeverandørerne ikke støtter flere perioder
        if (sammenligningsresultat.firstOrNull()?.afpTallErLike() == true){
            log.info { "Ingen forskjell i AFP-beregning mellom Nav og TP-leverandør $tpOrdning for " +
                    "foedselsdato=${request.foedselsdato}. Sammenligningsresultat: $sammenligningsresultat" }
        } else {
            log.warn { "Forskjell i AFP-beregning mellom Nav og TP-leverandør $tpOrdning for " +
                    "foedselsdato=${request.foedselsdato}. Sammenligningsresultat: $sammenligningsresultat" }
        }

    }
}
