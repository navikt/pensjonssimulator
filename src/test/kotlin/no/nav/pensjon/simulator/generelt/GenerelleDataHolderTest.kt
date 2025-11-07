package no.nav.pensjon.simulator.generelt

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.generelt.client.GenerelleDataClient

class GenerelleDataHolderTest : ShouldSpec({

    should("returnere siste gyldige opptjeningsår") {
        GenerelleDataHolder(
            client = arrangeGenerelleData()
        ).getSisteGyldigeOpptjeningsaar() shouldBe 2023
    }

    should("cache siste gyldige opptjeningsår") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)

        holder.getSisteGyldigeOpptjeningsaar() shouldBe 2023 // value from the client
        holder.getSisteGyldigeOpptjeningsaar() shouldBe 2023 // cached value

        verify(exactly = 1) { client.fetchGenerelleData(any()) }
    }
})

private fun arrangeGenerelleData(): GenerelleDataClient =
    mockk<GenerelleDataClient> {
        every {
            fetchGenerelleData(any())
        } returns GenerelleData(
            person = Person(statsborgerskap = LandkodeEnum.NOR),
            privatAfpSatser = PrivatAfpSatser(),
            satsResultatListe = emptyList(),
            sisteGyldigeOpptjeningsaar = 2023
        )
    }
