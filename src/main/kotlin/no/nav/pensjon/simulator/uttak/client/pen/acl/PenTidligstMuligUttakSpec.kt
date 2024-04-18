package no.nav.pensjon.simulator.uttak.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * Egress-spesifikasjon for å finne 'tidligst mulig uttak' (TMU).
 * Must match DatobasertTidligstMuligUttakSpecV1 in pensjon-pen.
 */
data class PenTidligstMuligUttakSpec(
    val pid: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val foedselsdato: LocalDate,

    val gradertUttak: PenTmuGradertUttakSpec?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val rettTilOffentligAfpFom: LocalDate?,

    val antallAarUtenlandsEtter16Aar: Int,
    val fremtidigInntektListe: List<PenTmuInntektSpec>,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean
)

data class PenTmuGradertUttakSpec(
    val grad: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val heltUttakFom: LocalDate
)

data class PenTmuInntektSpec(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val fom: LocalDate,

    val aarligBeloep: Int
)
