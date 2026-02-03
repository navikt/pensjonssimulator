package no.nav.pensjon.simulator.sak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.sak.client.SakClient
import java.util.*

class SakServiceTest : FunSpec({

    context("personVirkningDato") {

        test("kaller client med korrekt pid") {
            val client = mockk<SakClient>()
            val service = SakService(client)
            val pid = Pid("12345678901")

            every { client.fetchPersonVirkningDato(pid) } returns FoersteVirkningDatoCombo(emptyList())

            service.personVirkningDato(pid)

            verify { client.fetchPersonVirkningDato(pid) }
        }

        test("returnerer resultat fra client") {
            val client = mockk<SakClient>()
            val service = SakService(client)
            val pid = Pid("12345678901")
            val expectedResult = FoersteVirkningDatoCombo(
                foersteVirkningDatoGrunnlagListe = listOf(
                    ForsteVirkningsdatoGrunnlag().apply {
                        virkningsdato = Date()
                        kravlinjeTypeEnum = KravlinjeTypeEnum.AP
                        sakType = SakTypeEnum.ALDER
                    }
                )
            )

            every { client.fetchPersonVirkningDato(pid) } returns expectedResult

            val result = service.personVirkningDato(pid)

            result shouldBe expectedResult
        }

        test("returnerer tom liste når client returnerer tom liste") {
            val client = mockk<SakClient>()
            val service = SakService(client)
            val pid = Pid("98765432109")
            val expectedResult = FoersteVirkningDatoCombo(emptyList())

            every { client.fetchPersonVirkningDato(pid) } returns expectedResult

            val result = service.personVirkningDato(pid)

            result.foersteVirkningDatoGrunnlagListe shouldHaveSize 0
        }

        test("returnerer flere virkningsdatoer når client returnerer flere") {
            val client = mockk<SakClient>()
            val service = SakService(client)
            val pid = Pid("12345678901")
            val expectedResult = FoersteVirkningDatoCombo(
                foersteVirkningDatoGrunnlagListe = listOf(
                    ForsteVirkningsdatoGrunnlag().apply {
                        virkningsdato = Date()
                        kravlinjeTypeEnum = KravlinjeTypeEnum.AP
                        sakType = SakTypeEnum.ALDER
                    },
                    ForsteVirkningsdatoGrunnlag().apply {
                        virkningsdato = Date()
                        kravlinjeTypeEnum = KravlinjeTypeEnum.UP
                        sakType = SakTypeEnum.UFOREP
                    },
                    ForsteVirkningsdatoGrunnlag().apply {
                        virkningsdato = Date()
                        kravlinjeTypeEnum = KravlinjeTypeEnum.GJP
                        sakType = SakTypeEnum.GJENLEV
                    }
                )
            )

            every { client.fetchPersonVirkningDato(pid) } returns expectedResult

            val result = service.personVirkningDato(pid)

            result.foersteVirkningDatoGrunnlagListe shouldHaveSize 3
            result.foersteVirkningDatoGrunnlagListe[0].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.AP
            result.foersteVirkningDatoGrunnlagListe[1].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.UP
            result.foersteVirkningDatoGrunnlagListe[2].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.GJP
        }

        test("returnerer virkningsdato med bruker og annenPerson") {
            val client = mockk<SakClient>()
            val service = SakService(client)
            val pid = Pid("12345678901")
            val bruker = PenPerson(123L)
            val annenPerson = PenPerson(456L)
            val expectedResult = FoersteVirkningDatoCombo(
                foersteVirkningDatoGrunnlagListe = listOf(
                    ForsteVirkningsdatoGrunnlag().apply {
                        virkningsdato = Date()
                        kravlinjeTypeEnum = KravlinjeTypeEnum.ET
                        sakType = SakTypeEnum.ALDER
                        this.bruker = bruker
                        this.annenPerson = annenPerson
                    }
                )
            )

            every { client.fetchPersonVirkningDato(pid) } returns expectedResult

            val result = service.personVirkningDato(pid)

            result.foersteVirkningDatoGrunnlagListe shouldHaveSize 1
            result.foersteVirkningDatoGrunnlagListe[0].bruker shouldBe bruker
            result.foersteVirkningDatoGrunnlagListe[0].annenPerson shouldBe annenPerson
        }

        test("håndterer ulike pid-verdier") {
            val client = mockk<SakClient>()
            val service = SakService(client)

            val pid1 = Pid("11111111111")
            val pid2 = Pid("22222222222")
            val result1 = FoersteVirkningDatoCombo(
                listOf(ForsteVirkningsdatoGrunnlag().apply { sakType = SakTypeEnum.ALDER })
            )
            val result2 = FoersteVirkningDatoCombo(
                listOf(ForsteVirkningsdatoGrunnlag().apply { sakType = SakTypeEnum.UFOREP })
            )

            every { client.fetchPersonVirkningDato(pid1) } returns result1
            every { client.fetchPersonVirkningDato(pid2) } returns result2

            val actualResult1 = service.personVirkningDato(pid1)
            val actualResult2 = service.personVirkningDato(pid2)

            actualResult1.foersteVirkningDatoGrunnlagListe[0].sakType shouldBe SakTypeEnum.ALDER
            actualResult2.foersteVirkningDatoGrunnlagListe[0].sakType shouldBe SakTypeEnum.UFOREP
        }
    }
})
