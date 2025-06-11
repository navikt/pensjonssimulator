package no.nav.pensjon.simulator.generelt.client.pen.acl

import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.generelt.GenerelleData
import no.nav.pensjon.simulator.generelt.Person
import java.time.LocalDate

object PenGenerelleDataResultMapper {

    private val defaultPerson = Person(LocalDate.MIN, LandkodeEnum.NOR)

    fun fromDto(source: PenGenerelleDataResult) =
        GenerelleData(
            person = source.person?.let(::person) ?: defaultPerson,
            privatAfpSatser = source.privatAfpSatser?.let(::privatAfpSatser) ?: PrivatAfpSatser(),
            satsResultatListe = source.satsResultatListe.orEmpty().map(::veietSatsResultat)
        )

    private fun person(source: PenPersonData) =
        Person(
            foedselDato = source.foedselDato ?: defaultPerson.foedselDato,
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
        VeietSatsResultat(
            ar = source.aar,
            verdi = source.verdi
        )
}
