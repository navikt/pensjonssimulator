package no.nav.pensjon.simulator.statistikk

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.tech.time.Kalendermaaned
import java.time.LocalDate

class TertialstatistikkServiceTest : ShouldSpec({

    context("årets 1. tertial") {
        should("summere månedsvis antall for Nav og totalt") {
            TertialstatistikkService(
                snapshotRepository = arrangeRepository(startMaaned = 1),
                time = mockk { every { today() } returns LocalDate.of(2026, 5, 15) }
            ).antallPerMaaned(
                tertial = 1
            ) shouldBe mapOf(
                Kalendermaaned(nummer = 1, navn = "januar") to AntallCombo(navAntall = 270, totaltAntall = 540),
                Kalendermaaned(nummer = 2, navn = "februar") to AntallCombo(navAntall = 11, totaltAntall = 111),
                Kalendermaaned(nummer = 3, navn = "mars") to AntallCombo(navAntall = 55, totaltAntall = 555),
                Kalendermaaned(nummer = 4, navn = "april") to AntallCombo(navAntall = 112, totaltAntall = 1113)
            )
        }
    }

    context("fjorårets 3. tertial") {
        should("summere månedsvis antall for Nav og totalt") {
            TertialstatistikkService(
                snapshotRepository = arrangeRepository(startMaaned = 9),
                time = mockk { every { today() } returns LocalDate.of(2027, 1, 15) }
            ).antallPerMaaned(
                tertial = 3
            ) shouldBe mapOf(
                Kalendermaaned(nummer = 9, navn = "september") to AntallCombo(navAntall = 270, totaltAntall = 540),
                Kalendermaaned(nummer = 10, navn = "oktober") to AntallCombo(navAntall = 11, totaltAntall = 111),
                Kalendermaaned(nummer = 11, navn = "november") to AntallCombo(navAntall = 55, totaltAntall = 555),
                Kalendermaaned(nummer = 12, navn = "desember") to AntallCombo(navAntall = 110, totaltAntall = 1110)
            )
        }
    }
})

private fun arrangeRepository(startMaaned: Int): SnapshotRepository = mockk {
    every { read(202600 + startMaaned) } returns listOf(
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 10
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.AFP
            ),
            antall = 20
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = annetOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 30
        )
    )
    every { read(202600 + startMaaned + 1) } returns listOf(
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 100
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.AFP
            ),
            antall = 200
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = annetOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 300
        )
    )
    every { read(202600 + startMaaned + 2) } returns listOf(
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 101
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.AFP
            ),
            antall = 210
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = annetOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 400
        )
    )
    every { read(202600 + startMaaned + 3) } returns listOf(
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 106
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.AFP
            ),
            antall = 260
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = annetOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 900
        )
    )
    every { read(202605) } returns listOf(
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.AFP
            ),
            antall = 361
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = annetOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 1901
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 117
        )
    )
    every { read(202701) } returns listOf(
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.AFP
            ),
            antall = 360
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = annetOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 1900
        ),
        SimuleringStatistikk(
            hendelse = SimuleringHendelse(
                organisasjonsnummer = navOrganisasjonsnummer,
                simuleringstype = SimuleringTypeEnum.ALDER
            ),
            antall = 116
        )
    )
}

private val navOrganisasjonsnummer = Organisasjonsnummer("889640782")
private val annetOrganisasjonsnummer = Organisasjonsnummer("234567890")

