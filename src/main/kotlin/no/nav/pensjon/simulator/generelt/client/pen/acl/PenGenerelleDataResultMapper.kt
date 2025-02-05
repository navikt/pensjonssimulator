package no.nav.pensjon.simulator.generelt.client.pen.acl

import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Delingstall
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Forholdstall
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.generelt.GenerelleData
import no.nav.pensjon.simulator.generelt.Person
import java.time.LocalDate

object PenGenerelleDataResultMapper {

    private val defaultPerson = Person(LocalDate.MIN, LandkodeEnum.NOR)

    fun fromDto(source: PenGenerelleDataResult) =
        GenerelleData(
            person = source.person?.let(::person) ?: defaultPerson,
            privatAfpSatser = source.privatAfpSatser?.let(::privatAfpSatser) ?: PrivatAfpSatser(),
            delingstallUtvalg = source.delingstallUtvalg?.let(::delingstallUtvalg) ?: DelingstallUtvalg(),
            forholdstallUtvalg = source.forholdstallUtvalg?.let(::forholdstallUtvalg) ?: ForholdstallUtvalg(),
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

    private fun delingstallUtvalg(source: PenDelingstallUtvalg) =
        DelingstallUtvalg(
            dt = source.dt,
            // dt67soker, dt67virk are marked to be ignored
            delingstallListe = source.delingstallListe.map(::delingstall).toMutableList()
        )

    private fun forholdstallUtvalg(source: PenForholdstallUtvalg) =
        ForholdstallUtvalg(
            ft = source.ft,
            forholdstallListe = source.forholdstallListe.map(::forholdstall).toMutableList()
        )

    private fun veietSatsResultat(source: PenVeietSatsResultat) =
        VeietSatsResultat(
            ar = source.aar,
            verdi = source.verdi
        )

    private fun delingstall(source: PenAarskullTall) =
        Delingstall(
            arskull = source.aarskull ?: 0L,
            alder = source.alderAar ?: 0L,
            maned = source.maaneder ?: 0L,
            delingstall = source.tall
        )

    private fun forholdstall(source: PenAarskullTall) =
        Forholdstall(
            arskull = source.aarskull,
            alder = source.alderAar,
            maned = source.maaneder,
            forholdstall = source.tall
        )
}
