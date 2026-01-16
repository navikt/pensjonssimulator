package no.nav.pensjon.simulator.tech.metric

import io.micrometer.core.instrument.Metrics
import no.nav.pensjon.simulator.tech.metric.Organisasjoner.SPK
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TPSimuleringResultatFra2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.metrics.SPKResultatKodePre2025

object Metrics {
    private const val PREFIX = "ps"

    fun countEgressCall(service: String, result: String) {
        Metrics
            .counter("${PREFIX}_egress_call", "service", service, "result", result)
            .increment()
    }

    fun countIngressCall(organisasjonId: String, callId: String) {
        Metrics
            .counter("${PREFIX}_ingress_call", "org", organisasjonId, "call", callId)
            .increment()
    }

    fun countSimuleringstype(type: String) {
        Metrics
            .counter("${PREFIX}_simuleringstype", "type", type)
            .increment()
    }

    fun countTjenestepensjonSimuleringPre2025(resultat: SPKResultatKodePre2025, org: String = Organisasjoner.navn(SPK)) {
        Metrics
            .counter("${PREFIX}_tp_simulering_pre_2025", "resultat", resultat.name, "org", org)
            .increment()
    }

    fun countTjenestepensjonSimuleringFra2025(resultat: TPSimuleringResultatFra2025, orgEllerApp: String) {
        Metrics
            .counter("${PREFIX}_tp_simulering_fra_2025", "resultat", resultat.name, "org_eller_app", orgEllerApp)
            .increment()
    }
}

object MetricResult {
    const val BAD_CLIENT = "bad-client"
    const val BAD_SERVER = "bad-server"
    const val OK = "ok"
}
