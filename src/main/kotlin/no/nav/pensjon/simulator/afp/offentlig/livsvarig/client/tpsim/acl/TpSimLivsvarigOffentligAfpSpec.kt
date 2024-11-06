package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class TpSimLivsvarigOffentligAfpSpec(
    val pid: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val foedselDato: LocalDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: LocalDate,
    val fremtidigInntektListe: List<TpSimInntekt>
)

data class TpSimInntekt(
    val aarligBeloep: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: LocalDate
)
