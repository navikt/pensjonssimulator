package no.nav.pensjon.simulator.generelt.client.pen.acl

import no.nav.pensjon.simulator.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.generelt.GenerelleData
import no.nav.pensjon.simulator.generelt.Person

object PenGenerelleDataResultMapper {

    private val defaultPerson = Person( LandkodeEnum.NOR)

    fun fromDto(source: PenGenerelleDataResult) =
        GenerelleData(
            person = source.person?.let(::person) ?: defaultPerson,
            privatAfpSatser = source.privatAfpSatser?.let(::privatAfpSatser) ?: PrivatAfpSatser(),
            satsResultatListe = source.satsResultatListe.orEmpty().map(::veietSatsResultat),
            sisteGyldigeOpptjeningsaar = source.sisteGyldigeOpptjeningsaar
        )

    private fun person(source: PenPersonData) =
        Person(
            statsborgerskap = source.statsborgerskap?.let(LandkodeEnum::valueOf) ?: defaultPerson.statsborgerskap
        )

    private fun privatAfpSatser(source: PenPrivatAfpSatser) =
        PrivatAfpSatser(
            forholdstall = source.forholdstall ?: 0.0,
            kompensasjonstilleggForholdstall = source.kompensasjonstilleggForholdstall ?: 0.0,
            justeringsbeloep = source.justeringsbeloep?.toInt() ?: 0,
            referansebeloep = source.referansebeloep?.toInt() ?: 0
        )

    private fun veietSatsResultat(source: PenVeietSatsResultat) =
        VeietSatsResultat().apply {
            ar = source.aar
            verdi = source.verdi
        }
}
