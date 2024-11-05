package no.nav.pensjon.simulator.ytelse.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PenLoependeYtelserSpec(
    val pid: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val foersteUttakDato: LocalDate,
    val inkluderPrivatAfp: Boolean,
    val avdoedPid: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val doedDato: LocalDate?
)
