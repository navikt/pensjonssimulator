package no.nav.pensjon.simulator.generelt.client.pen.acl

import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Delingstall
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Forholdstall
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.generelt.GenerelleData
import java.time.LocalDate

object PenGenerelleDataResultMapper {

    fun fromDto(source: PenGenerelleDataResult) =
        GenerelleData(
            foedselDato = source.foedselDato ?: LocalDate.MIN,
            privatAfpSatser = source.privatAfpSatser?.let(::privatAfpSatser) ?: PrivatAfpSatser(),
            delingstallUtvalg = source.delingstallUtvalg?.let(::delingstallUtvalg) ?: DelingstallUtvalg(),
            forholdstallUtvalg = source.forholdstallUtvalg?.let(::forholdstallUtvalg) ?: ForholdstallUtvalg(),
            satsResultatListe = source.satsResultatListe.orEmpty().map(::veietSatsResultat)
        )

    private fun privatAfpSatser(source: PenPrivatAfpSatser) =
        PrivatAfpSatser(ft = source.forholdstall?.let(::forholdstall))

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
        VeietSatsResultat(ar = source.aar)

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
