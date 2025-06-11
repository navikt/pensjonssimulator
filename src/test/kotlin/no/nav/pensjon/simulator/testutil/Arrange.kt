package no.nav.pensjon.simulator.testutil

import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

object Arrange {

    fun foedselsdato(dato: LocalDate): GeneralPersonService =
        mockk<GeneralPersonService>().apply {
            every { foedselsdato(pid) } returns dato
        }

    fun normalder(foedselsdato: LocalDate): NormertPensjonsalderService =
        mockk<NormertPensjonsalderService>().apply {
            every { normalder(foedselsdato) } returns Alder(67, 0)
        }

    fun organisasjonsnummer(nummer: String): OrganisasjonsnummerProvider =
        mockk<OrganisasjonsnummerProvider>().apply {
            every { provideOrganisasjonsnummer() } returns Organisasjonsnummer(nummer)
        }
}
