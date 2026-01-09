package no.nav.pensjon.simulator.tech.selftest.api.acl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

data class SelfTestResultV1(
    val application: String,
    val timestamp: String,
    val aggregateResult: Int,
    val checks: List<CheckResultV1>
)

@JsonInclude(NON_NULL)
data class CheckResultV1(
    val endpoint: String,
    val description: String,
    val errorMessage: String?,
    val result: Int
)
