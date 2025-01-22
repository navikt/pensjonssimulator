package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import java.time.LocalDate

data class TpSimLivsvarigOffentligAfpResult(
    val fnr: String,
    val afpYtelser: List<TpSimAfpOffentligLivsvarigYtelseMedDelingstall>
)

data class TpSimAfpOffentligLivsvarigYtelseMedDelingstall(
    val pensjonsbeholdning: Int,
    val afpYtelsePerAar: Double,
    val delingstall: Double,
    val gjelderFraOgMed: LocalDate,
    val gjelderFraOgMedAlder: TpSimAlder,
)

data class TpSimAlder(
    val aar: Int,
    val maaneder: Int
)
