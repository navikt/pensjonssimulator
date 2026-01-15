package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleDatoEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleKritEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaletypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class TrygdeavtaleFactoryTest : ShouldSpec({

    /**
     * Opprettet trygdeavtale for simulering utland skal ha:
     * - avtaledatoEnum = verdi for siste gyldige trygdeavtale med EØS
     * - avtaleKriterieEnum = YRK_TRYGD (yrkesaktiv, 1 års trygdetid)
     * - avtaleTypeEnum = EOS_NOR (EØS/Norge)
     * - bostedslandEnum = NOR (Norge)
     * - kravDatoIAvtaleland = angitt dato med klokkeslett 12
     * - omfattesavAvtalensPersonkrets = true
     */
    should("set 'kravdato i avtaleland' according to input") {
        val dato = LocalDate.of(2025, 1, 1)

        val trygdeavtale = TrygdeavtaleFactory.newTrygdeavtaleForSimuleringUtland(avtalelandKravdato = dato)

        with(trygdeavtale) {
            avtaledatoEnum shouldBe AvtaleDatoEnum.EOS1994
            avtaleKriterieEnum shouldBe AvtaleKritEnum.YRK_TRYGD
            avtaleTypeEnum shouldBe AvtaletypeEnum.EOS_NOR
            bostedslandEnum shouldBe LandkodeEnum.NOR
            kravDatoIAvtaleland shouldBe dato.toNorwegianDateAtNoon() // according to input
            omfattesavAvtalensPersonkrets shouldBe true
        }
    }
})