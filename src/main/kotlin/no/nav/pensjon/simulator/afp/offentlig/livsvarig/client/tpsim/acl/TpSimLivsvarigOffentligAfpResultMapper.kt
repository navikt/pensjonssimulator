package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpYtelseMedDelingstall

object TpSimLivsvarigOffentligAfpResultMapper {

    fun fromDto(source: TpSimLivsvarigOffentligAfpResult) =
        LivsvarigOffentligAfpResult(
            pid = source.fnr,
            afpYtelseListe = source.afpYtelser.map(::ytelse)
        )

    private fun ytelse(source: TpSimAfpOffentligLivsvarigYtelseMedDelingstall) =
        LivsvarigOffentligAfpYtelseMedDelingstall(
            pensjonBeholdning = source.pensjonsbeholdning,
            afpYtelsePerAar = source.afpYtelsePerAar,
            delingstall = source.delingstall,
            gjelderFom = source.gjelderFraOgMed,
            gjelderFomAlder = alder(source.gjelderFraOgMedAlder)
        )

    private fun alder(source: TpSimAlder) =
        Alder(
            aar = source.aar,
            maaneder = source.maaneder
        )
}
