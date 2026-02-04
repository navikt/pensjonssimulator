package no.nav.pensjon.simulator.generelt

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.generelt.client.GenerelleDataClient
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

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

    should("returnere person fra client") {
        val expectedPerson = Person(statsborgerskap = LandkodeEnum.SWE)
        val holder = GenerelleDataHolder(client = arrangeGenerelleData(person = expectedPerson))

        holder.getPerson(Pid("12345678901")) shouldBe expectedPerson
    }

    should("cache person for samme pid") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)
        val pid = Pid("12345678901")

        holder.getPerson(pid)
        holder.getPerson(pid)

        verify(exactly = 1) { client.fetchGenerelleData(any()) }
    }

    should("hente person separat for ulike pid-er") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)

        holder.getPerson(Pid("12345678901"))
        holder.getPerson(Pid("98765432109"))

        verify(exactly = 2) { client.fetchGenerelleData(any()) }
    }

    should("returnere privat AFP-satser fra client") {
        val expectedSatser = PrivatAfpSatser(
            forholdstall = 1.05,
            kompensasjonstilleggForholdstall = 1.02,
            justeringsbeloep = 1000,
            referansebeloep = 50000
        )
        val holder = GenerelleDataHolder(client = arrangeGenerelleData(privatAfpSatser = expectedSatser))

        val result = holder.getPrivatAfpSatser(
            virkningFom = LocalDate.of(2024, 1, 1),
            foedselsdato = LocalDate.of(1960, 5, 15)
        )

        result shouldBe expectedSatser
    }

    should("cache privat AFP-satser for samme nøkkel") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)
        val virkningFom = LocalDate.of(2024, 1, 1)
        val foedselsdato = LocalDate.of(1960, 5, 15)

        holder.getPrivatAfpSatser(virkningFom, foedselsdato)
        holder.getPrivatAfpSatser(virkningFom, foedselsdato)

        verify(exactly = 1) { client.fetchGenerelleData(any()) }
    }

    should("hente privat AFP-satser separat for ulik virkningFom") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)
        val foedselsdato = LocalDate.of(1960, 5, 15)

        holder.getPrivatAfpSatser(LocalDate.of(2024, 1, 1), foedselsdato)
        holder.getPrivatAfpSatser(LocalDate.of(2025, 1, 1), foedselsdato)

        verify(exactly = 2) { client.fetchGenerelleData(any()) }
    }

    should("hente privat AFP-satser separat for ulik fødselsdato") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)
        val virkningFom = LocalDate.of(2024, 1, 1)

        holder.getPrivatAfpSatser(virkningFom, LocalDate.of(1960, 5, 15))
        holder.getPrivatAfpSatser(virkningFom, LocalDate.of(1965, 8, 20))

        verify(exactly = 2) { client.fetchGenerelleData(any()) }
    }

    should("returnere veiet grunnbeløp-liste fra client") {
        val expectedList = listOf(
            VeietSatsResultat().apply { ar = 2023; verdi = 118620.0 },
            VeietSatsResultat().apply { ar = 2024; verdi = 124028.0 }
        )
        val holder = GenerelleDataHolder(client = arrangeGenerelleData(satsResultatListe = expectedList))

        val result = holder.getVeietGrunnbeloepListe(fomAar = 2023, tomAar = 2024)

        result.size shouldBe 2
        result[0].ar shouldBe 2023
        result[0].verdi shouldBe 118620.0
        result[1].ar shouldBe 2024
        result[1].verdi shouldBe 124028.0
    }

    should("cache veiet grunnbeløp-liste for samme nøkkel") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)

        holder.getVeietGrunnbeloepListe(fomAar = 2023, tomAar = 2024)
        holder.getVeietGrunnbeloepListe(fomAar = 2023, tomAar = 2024)

        verify(exactly = 1) { client.fetchGenerelleData(any()) }
    }

    should("hente veiet grunnbeløp-liste separat for ulik fomAar") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)

        holder.getVeietGrunnbeloepListe(fomAar = 2022, tomAar = 2024)
        holder.getVeietGrunnbeloepListe(fomAar = 2023, tomAar = 2024)

        verify(exactly = 2) { client.fetchGenerelleData(any()) }
    }

    should("hente veiet grunnbeløp-liste separat for ulik tomAar") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)

        holder.getVeietGrunnbeloepListe(fomAar = 2023, tomAar = 2024)
        holder.getVeietGrunnbeloepListe(fomAar = 2023, tomAar = 2025)

        verify(exactly = 2) { client.fetchGenerelleData(any()) }
    }

    should("håndtere null fomAar i veiet grunnbeløp-liste") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)

        holder.getVeietGrunnbeloepListe(fomAar = null, tomAar = 2024)
        holder.getVeietGrunnbeloepListe(fomAar = null, tomAar = 2024)

        verify(exactly = 1) { client.fetchGenerelleData(any()) }
    }

    should("håndtere null tomAar i veiet grunnbeløp-liste") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)

        holder.getVeietGrunnbeloepListe(fomAar = 2023, tomAar = null)
        holder.getVeietGrunnbeloepListe(fomAar = 2023, tomAar = null)

        verify(exactly = 1) { client.fetchGenerelleData(any()) }
    }

    should("håndtere begge null-verdier i veiet grunnbeløp-liste") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)

        holder.getVeietGrunnbeloepListe(fomAar = null, tomAar = null)
        holder.getVeietGrunnbeloepListe(fomAar = null, tomAar = null)

        verify(exactly = 1) { client.fetchGenerelleData(any()) }
    }

    should("skille mellom null og ikke-null nøkler i veiet grunnbeløp-liste") {
        val client = arrangeGenerelleData()
        val holder = GenerelleDataHolder(client)

        holder.getVeietGrunnbeloepListe(fomAar = null, tomAar = 2024)
        holder.getVeietGrunnbeloepListe(fomAar = 2023, tomAar = 2024)

        verify(exactly = 2) { client.fetchGenerelleData(any()) }
    }
})

private fun arrangeGenerelleData(
    opptjeningsaar: Int = 2023,
    person: Person = Person(statsborgerskap = LandkodeEnum.NOR),
    privatAfpSatser: PrivatAfpSatser = PrivatAfpSatser(),
    satsResultatListe: List<VeietSatsResultat> = emptyList()
): GenerelleDataClient =
    mockk {
        every { fetchGenerelleData(any()) } returns GenerelleData(
            person = person,
            privatAfpSatser = privatAfpSatser,
            satsResultatListe = satsResultatListe,
            sisteGyldigeOpptjeningsaar = opptjeningsaar
        )
    }
