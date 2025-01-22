package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class TpSimLivsvarigOffentligAfpSpec(
    val fnr: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fodselsdato: LocalDate,
    val fremtidigeInntekter: List<TpSimInntekt>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: LocalDate
)

data class TpSimInntekt(
    val belop: Int,
    val fraOgMed: LocalDate
)
