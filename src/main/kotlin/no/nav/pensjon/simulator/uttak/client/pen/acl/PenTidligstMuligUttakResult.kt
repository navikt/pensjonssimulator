package no.nav.pensjon.simulator.uttak.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PenTidligstMuligUttakResult(
    val alder: PenUttakAlder,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo") val dato: LocalDate
)

data class PenUttakAlder(
    val aar: Int,
    val maaneder: Int
)
