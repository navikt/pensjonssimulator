package no.nav.pensjon.simulator.normalder.client.pen.acl

/**
 * Corresponds to NormertPensjonsalderResponse and NormertPensionsalderErrorResponse in PEN
 */
data class PenNormalderResult(
    // Normal response:
    val normertPensjonsalderListe: List<PenNormertPensjonsalder>? = null,
    // Error response:
    val message: String? = null,
    val aarskull: Int? = null
)

/**
 * Corresponds to NormertPensjonsalder in PEN
 */
data class PenNormertPensjonsalder(
    val aarskull: Int,
    val aar: Int,
    val maaned: Int,
    val nedreAar: Int,
    val nedreMaaned: Int,
    val oevreAar: Int,
    val oevreMaaned: Int,
    val type: String // PEN: PensjonsalderType
)
