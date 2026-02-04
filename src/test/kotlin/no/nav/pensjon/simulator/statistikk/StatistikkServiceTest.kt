package no.nav.pensjon.simulator.statistikk

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.tech.time.Time
import java.time.LocalDate

class StatistikkServiceTest : FunSpec({

    context("registrer") {

        test("delegerer til statistikkRepository.update") {
            val statistikkRepository = mockk<StatistikkRepository>(relaxed = true)
            val service = StatistikkService(
                statistikkRepository = statistikkRepository,
                snapshotRepository = mockk(),
                time = mockk()
            )
            val hendelse = SimuleringHendelse(
                organisasjonsnummer = Organisasjonsnummer("123456789"),
                simuleringstype = SimuleringTypeEnum.ALDER
            )

            service.registrer(hendelse)

            verify(exactly = 1) { statistikkRepository.update(hendelse) }
        }

        test("registrerer ulike hendelser") {
            val statistikkRepository = mockk<StatistikkRepository>(relaxed = true)
            val service = StatistikkService(
                statistikkRepository = statistikkRepository,
                snapshotRepository = mockk(),
                time = mockk()
            )

            val hendelse1 = SimuleringHendelse(
                organisasjonsnummer = Organisasjonsnummer("111111111"),
                simuleringstype = SimuleringTypeEnum.ALDER
            )
            val hendelse2 = SimuleringHendelse(
                organisasjonsnummer = Organisasjonsnummer("222222222"),
                simuleringstype = SimuleringTypeEnum.AFP
            )

            service.registrer(hendelse1)
            service.registrer(hendelse2)

            verify { statistikkRepository.update(hendelse1) }
            verify { statistikkRepository.update(hendelse2) }
        }
    }

    context("hent") {

        test("returnerer data fra statistikkRepository.read") {
            val expectedStatistikk = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 100
                )
            )
            val statistikkRepository = mockk<StatistikkRepository>()
            every { statistikkRepository.read() } returns expectedStatistikk

            val service = StatistikkService(
                statistikkRepository = statistikkRepository,
                snapshotRepository = mockk(),
                time = mockk()
            )

            val result = service.hent()

            result shouldBe expectedStatistikk
        }

        test("returnerer tom liste når repository er tom") {
            val statistikkRepository = mockk<StatistikkRepository>()
            every { statistikkRepository.read() } returns emptyList()

            val service = StatistikkService(
                statistikkRepository = statistikkRepository,
                snapshotRepository = mockk(),
                time = mockk()
            )

            val result = service.hent()

            result.size shouldBe 0
        }

        test("returnerer flere elementer fra repository") {
            val expectedStatistikk = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("111111111"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 50
                ),
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("222222222"),
                        simuleringstype = SimuleringTypeEnum.AFP
                    ),
                    antall = 30
                )
            )
            val statistikkRepository = mockk<StatistikkRepository>()
            every { statistikkRepository.read() } returns expectedStatistikk

            val service = StatistikkService(
                statistikkRepository = statistikkRepository,
                snapshotRepository = mockk(),
                time = mockk()
            )

            val result = service.hent()

            result.size shouldBe 2
            result shouldBe expectedStatistikk
        }
    }

    context("takeSnapshotIfNeeded") {

        test("tar snapshot ved første kall") {
            val statistikkRepository = mockk<StatistikkRepository>()
            val snapshotRepository = mockk<SnapshotRepository>(relaxed = true)
            val time = mockk<Time>()
            val statistikk = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 10
                )
            )
            every { statistikkRepository.read() } returns statistikk
            every { time.today() } returns LocalDate.of(2024, 6, 15)

            val service = StatistikkService(
                statistikkRepository = statistikkRepository,
                snapshotRepository = snapshotRepository,
                time = time
            )

            service.takeSnapshotIfNeeded()

            verify(exactly = 1) {
                snapshotRepository.create(
                    MaanedligStatistikk(
                        aarMaaned = 202406,
                        statistikk = statistikk
                    )
                )
            }
        }

        test("tar ikke snapshot ved andre kall samme måned") {
            val statistikkRepository = mockk<StatistikkRepository>()
            val snapshotRepository = mockk<SnapshotRepository>(relaxed = true)
            val time = mockk<Time>()
            val statistikk = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 10
                )
            )
            every { statistikkRepository.read() } returns statistikk
            every { time.today() } returns LocalDate.of(2024, 6, 15)

            val service = StatistikkService(
                statistikkRepository = statistikkRepository,
                snapshotRepository = snapshotRepository,
                time = time
            )

            service.takeSnapshotIfNeeded()
            service.takeSnapshotIfNeeded()

            verify(exactly = 1) { snapshotRepository.create(any()) }
        }

        test("tar nytt snapshot når måned endres") {
            val statistikkRepository = mockk<StatistikkRepository>()
            val snapshotRepository = mockk<SnapshotRepository>(relaxed = true)
            val time = mockk<Time>()
            val statistikk = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 10
                )
            )
            every { statistikkRepository.read() } returns statistikk
            every { time.today() } returnsMany listOf(
                LocalDate.of(2024, 6, 15),
                LocalDate.of(2024, 7, 1)
            )

            val service = StatistikkService(
                statistikkRepository = statistikkRepository,
                snapshotRepository = snapshotRepository,
                time = time
            )

            service.takeSnapshotIfNeeded()
            service.takeSnapshotIfNeeded()

            verify(exactly = 2) { snapshotRepository.create(any()) }
        }

        test("tar nytt snapshot ved årskifte") {
            val statistikkRepository = mockk<StatistikkRepository>()
            val snapshotRepository = mockk<SnapshotRepository>(relaxed = true)
            val time = mockk<Time>()
            val statistikk = emptyList<SimuleringStatistikk>()
            every { statistikkRepository.read() } returns statistikk
            every { time.today() } returnsMany listOf(
                LocalDate.of(2024, 12, 31),
                LocalDate.of(2025, 1, 1)
            )

            val service = StatistikkService(
                statistikkRepository = statistikkRepository,
                snapshotRepository = snapshotRepository,
                time = time
            )

            service.takeSnapshotIfNeeded()
            service.takeSnapshotIfNeeded()

            verify { snapshotRepository.create(MaanedligStatistikk(202412, statistikk)) }
            verify { snapshotRepository.create(MaanedligStatistikk(202501, statistikk)) }
        }
    }

    context("getSnapshot") {

        test("returnerer data fra snapshotRepository.read") {
            val expectedStatistikk = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 100
                )
            )
            val snapshotRepository = mockk<SnapshotRepository>()
            every { snapshotRepository.read(202406) } returns expectedStatistikk

            val service = StatistikkService(
                statistikkRepository = mockk(),
                snapshotRepository = snapshotRepository,
                time = mockk()
            )

            val result = service.getSnapshot(202406)

            result shouldBe expectedStatistikk
        }

        test("returnerer data for ulike aarMaaned") {
            val statistikk202406 = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 50
                )
            )
            val statistikk202407 = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 75
                )
            )
            val snapshotRepository = mockk<SnapshotRepository>()
            every { snapshotRepository.read(202406) } returns statistikk202406
            every { snapshotRepository.read(202407) } returns statistikk202407

            val service = StatistikkService(
                statistikkRepository = mockk(),
                snapshotRepository = snapshotRepository,
                time = mockk()
            )

            service.getSnapshot(202406) shouldBe statistikk202406
            service.getSnapshot(202407) shouldBe statistikk202407
        }

        test("returnerer tom liste når snapshot ikke har data") {
            val snapshotRepository = mockk<SnapshotRepository>()
            every { snapshotRepository.read(any()) } returns emptyList()

            val service = StatistikkService(
                statistikkRepository = mockk(),
                snapshotRepository = snapshotRepository,
                time = mockk()
            )

            val result = service.getSnapshot(202401)

            result.size shouldBe 0
        }
    }
})
