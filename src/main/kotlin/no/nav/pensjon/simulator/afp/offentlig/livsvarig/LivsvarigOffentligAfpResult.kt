package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import no.nav.pensjon.simulator.alder.Alder
import java.time.LocalDate

// no.nav.consumer.pensjon.pen.simuleroffentligtjenestepensjon.to.afpoffentliglivsvarig.response.SimulerAFPOffentligLivsvarigResponse
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
