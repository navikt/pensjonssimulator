package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class TpSimLivsvarigOffentligAfpResult(
    val fnr: String,
    val afpYtelser: List<TpSimAfpYtelse>
)

data class TpSimAfpYtelse(
    val pensjonsbeholdning: Int,
    val afpYtelsePerAar: Double,
    val delingstall: Double,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val gjelderFraOgMed: LocalDate,
    val gjelderFraOgMedAlder: TpSimAlder
)

data class TpSimAlder(
    val aar: Int,
    val maaneder: Int
)
