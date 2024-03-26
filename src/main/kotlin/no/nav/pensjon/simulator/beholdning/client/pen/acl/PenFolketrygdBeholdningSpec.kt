package no.nav.pensjon.simulator.beholdning.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * Egress-spesifikasjon for å simulere 'folketrygdbeholdning'.
 */
data class PenFolketrygdBeholdningSpec(
    val personId: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val uttaksdato: LocalDate,
    val fremtidigInntektListe: List<PenInntektSpec>,
    val arIUtlandetEtter16: Int,
    val epsPensjon: Boolean,
    val eps2G: Boolean
)

data class PenInntektSpec(
    val arligInntekt: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate
)
