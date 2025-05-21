package no.nav.pensjon.simulator.core.trygd

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleDatoEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleKritEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaletypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class TrygdeavtaleFactoryTest : FunSpec({

    /**
     * Opprettet trygdeavtale for simulering utland skal ha:
     * - avtaledatoEnum = verdi for siste gyldige trygdeavtale med EØS
     * - avtaleKriterieEnum = YRK_TRYGD (yrkesaktiv, 1 års trygdetid)
     * - avtaleTypeEnum = EOS_NOR (EØS/Norge)
     * - bostedslandEnum = NOR (Norge)
     * - kravDatoIAvtaleland = angitt dato med klokkeslett 12
     * - omfattesavAvtalensPersonkrets = true
     */
    test("newTrygdeavtaleForSimuleringUtland should set kravDatoIAvtaleland according to input") {
        val trygdeavtale =
            TrygdeavtaleFactory.newTrygdeavtaleForSimuleringUtland(avtalelandKravdato = LocalDate.of(2025, 1, 1))

        with(trygdeavtale) {
            avtaledatoEnum shouldBe AvtaleDatoEnum.EOS1994
            avtaleKriterieEnum shouldBe AvtaleKritEnum.YRK_TRYGD
            avtaleTypeEnum shouldBe AvtaletypeEnum.EOS_NOR
            bostedslandEnum shouldBe LandkodeEnum.NOR
            kravDatoIAvtaleland shouldBe dateAtNoon(2025, Calendar.JANUARY, 1) // according to input
            omfattesavAvtalensPersonkrets shouldBe true
        }
    }
})
