package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Trygdeavtale
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Trygdeavtaledetaljer
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtaleDatoCti
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtaleKritCti
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtaleTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import java.util.*

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdeavtaleFactory
object TrygdeavtaleFactory {

    private const val AVTALEDATO_NAME_PREFIX = "EOS" // EØS

    fun newTrygdeavtaleForSimuleringUtland() =
        Trygdeavtale().apply {
            avtaledato = latestAvtaleDato()?.let { AvtaleDatoCti(it.name) }
            avtaleKriterie = AvtaleKritCti(AvtaleKrit.YRK_TRYGD.name)
            avtaleType = AvtaleTypeCti(AvtaleType.EOS_NOR.name)
            bostedsland = LandCti(Land.NOR.name)
            kravDatoIAvtaleland = Date()
            omfattesavAvtalensPersonkrets = true
            //TODO minst12MndMedlemskapFolketrygden = true
        }

    fun newTrygdeavtaledetaljerForSimuleringUtland() =
        Trygdeavtaledetaljer().apply {
            erArt10BruktGP = false
            erArt10BruktTP = false
            fpa_nordisk = 0
        }

    @OptIn(ExperimentalStdlibApi::class)
    private fun latestAvtaleDato() =
        AvtaleDatoKode.entries
            .filter { it.name.startsWith(AVTALEDATO_NAME_PREFIX) }
            .minByOrNull { it.avtaledato }
}
