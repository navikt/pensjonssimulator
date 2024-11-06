package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class TpSimLivsvarigOffentligAfpResult(
    val pid: String,
    val afpYtelseListe: List<TpSimLivsvarigOffentligAfpYtelseMedDelingstall>
)

data class TpSimLivsvarigOffentligAfpYtelseMedDelingstall(
    val pensjonBeholdning: Int,
    val afpYtelsePerAar: Double,
    val delingstall: Double,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val gjelderFom: LocalDate,
    val gjelderFomAlder: TpSimAlder
)

data class TpSimAlder(
    val aar: Int,
    val maaneder: Int
)
