package no.nav.pensjon.simulator.tech.env

object EnvironmentUtil {
    fun isDevelopment(): Boolean =
        System.getenv("NAIS_CLUSTER_NAME") == "dev-gcp"
}
