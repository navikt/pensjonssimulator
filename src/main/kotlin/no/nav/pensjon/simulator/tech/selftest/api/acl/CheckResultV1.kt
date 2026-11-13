package no.nav.pensjon.simulator.tech.selftest.api.acl

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.pensjon.simulator.tech.selftest.PingResult
import no.nav.pensjon.simulator.tech.selftest.ServiceStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CheckResultV1(
    val endpoint: String,
    val description: String,
    val errorMessage: String?,
    val result: Int
) {
    companion object {
        /**
         * Maps from domain representation to transferable format for the API.
         */
        fun transferable(source: PingResult) =
            CheckResultV1(
                endpoint = source.endpoint,
                description = source.service.description,
                errorMessage = if (source.status == ServiceStatus.DOWN) source.message else null,
                result = source.status.code
            )
    }
}