package no.nav.pensjon.domain

data class EvaluationResult(
    val responseIsAsExpected: Boolean,
    val path: String,
    val expectedResponsePath: String,
    val actualResponse: String,
)