package no.nav.pensjon.simulator.testutil

import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

object Arrange {

    fun foedselsdato(year: Int, month: Int, dayOfMonth: Int): GeneralPersonService =
        mockk<GeneralPersonService>().apply {
            every { foedselsdato(pid) } returns LocalDate.of(year, month, dayOfMonth)
        }

    fun normalder(foedselsdato: LocalDate): NormertPensjonsalderService =
        mockk<NormertPensjonsalderService>().apply {
            every { normalder(foedselsdato) } returns Alder(67, 0)
        }
}
