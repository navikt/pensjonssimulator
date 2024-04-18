package no.nav.pensjon.simulator.beholdning.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * Egress-spesifikasjon for å simulere 'folketrygdbeholdning'.
 * Must match FolketrygdbeholdningSpecV1 in pensjon-pen.
 */
data class PenFolketrygdBeholdningSpec(
    val pid: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val uttakFom: LocalDate,

    val aarUtenlandsEtter16Aar: Int,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean,
    val fremtidigInntektListe: List<PenInntektSpec>
)

data class PenInntektSpec(
    val aarligInntekt: Int,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val fom: LocalDate
)
