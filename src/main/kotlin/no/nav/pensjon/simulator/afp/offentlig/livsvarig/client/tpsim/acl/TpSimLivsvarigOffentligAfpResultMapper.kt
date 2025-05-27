package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpYtelseMedDelingstall
import no.nav.pensjon.simulator.alder.Alder

object TpSimLivsvarigOffentligAfpResultMapper {

    fun fromDto(source: TpSimLivsvarigOffentligAfpResult) =
        LivsvarigOffentligAfpResult(
            pid = source.fnr,
            afpYtelseListe = source.afpYtelser.map(::ytelse)
        )

    private fun ytelse(source: TpSimAfpYtelse) =
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
