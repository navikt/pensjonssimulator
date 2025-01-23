package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class TpSimLivsvarigOffentligAfpSpec(
    val fnr: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fodselsdato: LocalDate,
    val fremtidigeInntekter: List<TpSimInntektSpec>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: LocalDate
)

data class TpSimInntektSpec(
    val belop: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fraOgMed: LocalDate
)
