package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.inntekt.Inntekt

object TpSimLivsvarigOffentligAfpSpecMapper {

    fun toDto(source: LivsvarigOffentligAfpSpec) =
        TpSimLivsvarigOffentligAfpSpec(
            fnr = source.pid.value,
            fodselsdato = source.foedselsdato,
            fom = source.fom,
            fremtidigeInntekter = source.fremtidigInntektListe.map(::inntekt)
        )

    private fun inntekt(source: Inntekt) =
        TpSimInntekt(
            belop = source.aarligBeloep,
            fraOgMed = source.fom
        )
}
