package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleDatoEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleKritEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaletypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Trygdeavtale
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Trygdeavtaledetaljer
import java.util.*

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdeavtaleFactory
object TrygdeavtaleFactory {

    private const val AVTALEDATO_NAME_PREFIX = "EOS" // EØS

    fun newTrygdeavtaleForSimuleringUtland() =
        Trygdeavtale().apply {
            avtaledatoEnum = latestAvtaleDato()?.name?.let(AvtaleDatoEnum::valueOf)
            avtaleKriterieEnum = AvtaleKritEnum.YRK_TRYGD
            avtaleTypeEnum = AvtaletypeEnum.EOS_NOR
            bostedslandEnum = LandkodeEnum.NOR
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
