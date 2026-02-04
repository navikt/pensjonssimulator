package no.nav.pensjon.simulator.vedtak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.vedtak.client.VedtakClient
import java.time.LocalDate

class VedtakServiceTest : FunSpec({

    lateinit var client: VedtakClient
    lateinit var service: VedtakService

    beforeTest {
        client = mockk()
        service = VedtakService(client)
    }

    context("tidligsteKapittel20VedtakGjelderFom") {

        test("returnerer dato fra client") {
            val pid = Pid("12345678901")
            val sakType = SakTypeEnum.ALDER
            val expectedDate = LocalDate.of(2024, 1, 15)

            every { client.tidligsteKapittel20VedtakGjelderFom(pid, sakType) } returns expectedDate

            val result = service.tidligsteKapittel20VedtakGjelderFom(pid, sakType)

            result shouldBe expectedDate
        }

        test("returnerer null når client returnerer null") {
            val pid = Pid("12345678901")
            val sakType = SakTypeEnum.ALDER

            every { client.tidligsteKapittel20VedtakGjelderFom(pid, sakType) } returns null

            val result = service.tidligsteKapittel20VedtakGjelderFom(pid, sakType)

            result shouldBe null
        }

        test("kaller client med korrekte parametere") {
            val pid = Pid("98765432109")
            val sakType = SakTypeEnum.UFOREP

            every { client.tidligsteKapittel20VedtakGjelderFom(pid, sakType) } returns null

            service.tidligsteKapittel20VedtakGjelderFom(pid, sakType)

            verify { client.tidligsteKapittel20VedtakGjelderFom(pid, sakType) }
        }

        test("håndterer ALDER saktype") {
            val pid = Pid("12345678901")
            val sakType = SakTypeEnum.ALDER
            val expectedDate = LocalDate.of(2020, 6, 1)

            every { client.tidligsteKapittel20VedtakGjelderFom(pid, sakType) } returns expectedDate

            val result = service.tidligsteKapittel20VedtakGjelderFom(pid, sakType)

            result shouldBe expectedDate
        }

        test("håndterer UFOREP saktype") {
            val pid = Pid("12345678901")
            val sakType = SakTypeEnum.UFOREP
            val expectedDate = LocalDate.of(2019, 3, 1)

            every { client.tidligsteKapittel20VedtakGjelderFom(pid, sakType) } returns expectedDate

            val result = service.tidligsteKapittel20VedtakGjelderFom(pid, sakType)

            result shouldBe expectedDate
        }

        test("håndterer GJENLEV saktype") {
            val pid = Pid("12345678901")
            val sakType = SakTypeEnum.GJENLEV
            val expectedDate = LocalDate.of(2022, 12, 1)

            every { client.tidligsteKapittel20VedtakGjelderFom(pid, sakType) } returns expectedDate

            val result = service.tidligsteKapittel20VedtakGjelderFom(pid, sakType)

            result shouldBe expectedDate
        }

        test("håndterer ulike pids") {
            val pids = listOf(
                Pid("11111111111"),
                Pid("22222222222"),
                Pid("33333333333")
            )
            val sakType = SakTypeEnum.ALDER

            pids.forEachIndexed { index, pid ->
                val expectedDate = LocalDate.of(2020 + index, 1, 1)
                every { client.tidligsteKapittel20VedtakGjelderFom(pid, sakType) } returns expectedDate

                val result = service.tidligsteKapittel20VedtakGjelderFom(pid, sakType)

                result shouldBe expectedDate
            }
        }
    }

    context("vedtakStatus") {

        test("returnerer VedtakStatus fra client") {
            val pid = Pid("12345678901")
            val uttakFom = LocalDate.of(2024, 6, 1)
            val expectedStatus = VedtakStatus(
                harGjeldendeVedtak = true,
                harGjenlevenderettighet = false
            )

            every { client.fetchVedtakStatus(pid, uttakFom) } returns expectedStatus

            val result = service.vedtakStatus(pid, uttakFom)

            result shouldBe expectedStatus
            result.harGjeldendeVedtak shouldBe true
            result.harGjenlevenderettighet shouldBe false
        }

        test("håndterer null uttakFom") {
            val pid = Pid("12345678901")
            val expectedStatus = VedtakStatus(
                harGjeldendeVedtak = false,
                harGjenlevenderettighet = false
            )

            every { client.fetchVedtakStatus(pid, null) } returns expectedStatus

            val result = service.vedtakStatus(pid, null)

            result shouldBe expectedStatus
        }

        test("kaller client med korrekte parametere") {
            val pid = Pid("98765432109")
            val uttakFom = LocalDate.of(2025, 1, 1)
            val expectedStatus = VedtakStatus(harGjeldendeVedtak = false, harGjenlevenderettighet = false)

            every { client.fetchVedtakStatus(pid, uttakFom) } returns expectedStatus

            service.vedtakStatus(pid, uttakFom)

            verify { client.fetchVedtakStatus(pid, uttakFom) }
        }

        test("returnerer harGjeldendeVedtak=true når bruker har gjeldende vedtak") {
            val pid = Pid("12345678901")
            val uttakFom = LocalDate.of(2024, 1, 1)
            val expectedStatus = VedtakStatus(
                harGjeldendeVedtak = true,
                harGjenlevenderettighet = false
            )

            every { client.fetchVedtakStatus(pid, uttakFom) } returns expectedStatus

            val result = service.vedtakStatus(pid, uttakFom)

            result.harGjeldendeVedtak shouldBe true
        }

        test("returnerer harGjenlevenderettighet=true når bruker har gjenlevenderettighet") {
            val pid = Pid("12345678901")
            val uttakFom = LocalDate.of(2024, 1, 1)
            val expectedStatus = VedtakStatus(
                harGjeldendeVedtak = false,
                harGjenlevenderettighet = true
            )

            every { client.fetchVedtakStatus(pid, uttakFom) } returns expectedStatus

            val result = service.vedtakStatus(pid, uttakFom)

            result.harGjenlevenderettighet shouldBe true
        }

        test("returnerer begge flagg som true") {
            val pid = Pid("12345678901")
            val uttakFom = LocalDate.of(2024, 1, 1)
            val expectedStatus = VedtakStatus(
                harGjeldendeVedtak = true,
                harGjenlevenderettighet = true
            )

            every { client.fetchVedtakStatus(pid, uttakFom) } returns expectedStatus

            val result = service.vedtakStatus(pid, uttakFom)

            result.harGjeldendeVedtak shouldBe true
            result.harGjenlevenderettighet shouldBe true
        }

        test("returnerer begge flagg som false") {
            val pid = Pid("12345678901")
            val uttakFom = LocalDate.of(2024, 1, 1)
            val expectedStatus = VedtakStatus(
                harGjeldendeVedtak = false,
                harGjenlevenderettighet = false
            )

            every { client.fetchVedtakStatus(pid, uttakFom) } returns expectedStatus

            val result = service.vedtakStatus(pid, uttakFom)

            result.harGjeldendeVedtak shouldBe false
            result.harGjenlevenderettighet shouldBe false
        }

        test("håndterer ulike uttakFom datoer") {
            val pid = Pid("12345678901")
            val dates = listOf(
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2024, 6, 15),
                LocalDate.of(2030, 12, 31)
            )

            dates.forEach { uttakFom ->
                val expectedStatus = VedtakStatus(
                    harGjeldendeVedtak = true,
                    harGjenlevenderettighet = false
                )
                every { client.fetchVedtakStatus(pid, uttakFom) } returns expectedStatus

                val result = service.vedtakStatus(pid, uttakFom)

                result shouldBe expectedStatus
            }
        }
    }
})
