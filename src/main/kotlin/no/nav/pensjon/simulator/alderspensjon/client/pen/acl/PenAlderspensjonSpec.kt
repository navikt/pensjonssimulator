package no.nav.pensjon.simulator.alderspensjon.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PenAlderspensjonSpec(
    val personId: String,
    val gradertUttak: PenGradertUttakSpec?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val heltUttakFraOgMedDato: LocalDate,
    val aarIUtlandetEtter16: Int,
    val epsPensjon: Boolean,
    val eps2G: Boolean,
    val fremtidigInntektListe: List<PenInntektSpec>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val rettTilAfpOffentligDato: LocalDate?
)

data class PenGradertUttakSpec(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
    val uttaksgrad: Int
)

data class PenInntektSpec(
    val aarligInntekt: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate
)
