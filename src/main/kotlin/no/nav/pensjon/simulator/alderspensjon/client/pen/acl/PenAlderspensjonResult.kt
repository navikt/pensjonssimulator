package no.nav.pensjon.simulator.alderspensjon.client.pen.acl

/**
 * Ref. API specification:
 * https://confluence.adeo.no/pages/viewpage.action?pageId=583317319
 */
data class PenAlderspensjonResult(
    val simuleringSuksess: Boolean?,
    val aarsakListeIkkeSuksess: List<PenPensjonSimuleringStatus>?,
    val alderspensjon: List<PenAlderspensjonFraFolketrygden>?,
    val alternativerVedForLavOpptjening: PenPensjonAlternativerVedForLavOpptjening?,
    val harUttak: Boolean?
)

data class PenAlderspensjonFraFolketrygden(
    val fraOgMedDato: String?,
    val delytelseListe: List<PenPensjonDelytelse>?,
    val uttaksgrad: Int?
)

data class PenPensjonAlternativerVedForLavOpptjening(
    val alderspensjonVedTidligstMuligUttak: List<PenAlderspensjonFraFolketrygden>?,
    val alderspensjonVedHoyestMuligGrad: List<PenAlderspensjonFraFolketrygden>?
)

data class PenPensjonSimuleringStatus(
    val statusKode: String?,
    val statusBeskrivelse: String?
)

data class PenPensjonDelytelse(
    val pensjonsType: String?,
    val belop: Int?
)

