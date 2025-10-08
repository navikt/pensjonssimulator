package no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag

import no.nav.pensjon.simulator.alder.Alder
import java.time.LocalDate

data class LivsvarigOffentligAfpResult(
    val pid: String,
    val afpYtelseListe: List<LivsvarigOffentligAfpYtelseMedDelingstall>
)

data class LivsvarigOffentligAfpYtelseMedDelingstall(
    val pensjonBeholdning: Int,
    val afpYtelsePerAar: Double,
    val delingstall: Double,
    val gjelderFom: LocalDate,
    val gjelderFomAlder: Alder
)
