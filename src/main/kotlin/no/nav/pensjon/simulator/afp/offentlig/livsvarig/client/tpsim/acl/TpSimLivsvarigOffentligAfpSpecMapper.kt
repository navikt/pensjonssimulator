package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.inntekt.Inntekt

object TpSimLivsvarigOffentligAfpSpecMapper {

    fun toDto(source: LivsvarigOffentligAfpSpec) =
        TpSimLivsvarigOffentligAfpSpec(
            pid = source.pid.value,
            foedselDato = source.foedselDato,
            fom = source.fom,
            fremtidigInntektListe = source.fremtidigInntektListe.map(::inntekt)
        )

    private fun inntekt(source: Inntekt) =
        TpSimInntekt(
            aarligBeloep = source.aarligBeloep,
            fom = source.fom
        )
}
