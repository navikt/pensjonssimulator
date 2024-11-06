package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpYtelseMedDelingstall

object TpSimLivsvarigOffentligAfpResultMapper {

    fun fromDto(source: TpSimLivsvarigOffentligAfpResult) =
        LivsvarigOffentligAfpResult(
            pid = source.pid,
            afpYtelseListe = source.afpYtelseListe.map(::ytelse)
        )

    private fun ytelse(source: TpSimLivsvarigOffentligAfpYtelseMedDelingstall) =
        LivsvarigOffentligAfpYtelseMedDelingstall(
            pensjonBeholdning = source.pensjonBeholdning,
            afpYtelsePerAar = source.afpYtelsePerAar,
            delingstall = source.delingstall,
            gjelderFom = source.gjelderFom,
            gjelderFomAlder = alder(source.gjelderFomAlder)
        )

    private fun alder(source: TpSimAlder) =
        Alder(
            aar = source.aar,
            maaneder = source.maaneder
        )
}
