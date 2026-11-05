package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.nav

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.beregning.LivsvarigOffentligAfpBeregningService
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.grunnlag.LivsvarigOffentligAfpYtelseMedDelingstall
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import org.slf4j.LoggerFactory
import java.time.LocalDate

class SammenlignAfpServiceTest : FunSpec({
    val navBeregnetAfpService = mockk<LivsvarigOffentligAfpBeregningService>()
    val featureToggleService = mockk<FeatureToggleService>()
    val service = SammenlignAfpService(navBeregnetAfpService, featureToggleService)
    val logger = LoggerFactory.getLogger(SammenlignAfpService::class.java) as Logger
    val originalLogLevel = logger.level
    val appender = ListAppender<ILoggingEvent>()
    val fom = LocalDate.of(2026, 3, 1)
    val senereFom = LocalDate.of(2027, 1, 1)
    val request = OffentligTjenestepensjonFra2025SimuleringSpec(
        pid = Pid("12345678910"),
        foedselsdato = LocalDate.of(1963, 2, 5),
        uttaksdato = fom,
        sisteInntekt = 500000,
        utlandAntallAar = 0,
        afpErForespurt = true,
        epsHarPensjon = false,
        epsHarInntektOver2G = false,
        fremtidigeInntekter = listOf(TjenestepensjonInntektSpec(fom, 300000)),
        gjelderApoteker = false
    )
    val expectedSpec = LivsvarigOffentligAfpSpec(
        pid = request.pid,
        foedselsdato = request.foedselsdato,
        fom = fom,
        fremtidigInntektListe = listOf(FremtidigInntekt(aarligInntektBeloep = 300000, fom = fom))
    )

    beforeSpec {
        logger.level = Level.INFO
        appender.start()
        logger.addAppender(appender)
    }

    beforeTest {
        clearMocks(navBeregnetAfpService, featureToggleService)
        appender.list.clear()
    }

    afterSpec {
        logger.detachAppender(appender)
        appender.stop()
        logger.level = originalLogLevel
    }

    fun navYtelse(aarligBeloep: Double, gjelderFom: LocalDate = fom) =
        LivsvarigOffentligAfpYtelseMedDelingstall(
            pensjonBeholdning = 1000000,
            afpYtelsePerAar = aarligBeloep,
            delingstall = 20.0,
            gjelderFom = gjelderFom,
            gjelderFomAlder = Alder(63, 0)
        )

    fun sammenlign(
        navYtelser: List<LivsvarigOffentligAfpYtelseMedDelingstall>,
        tpYtelser: List<Utbetalingsperiode>,
        tpOrdning: String = "SPK"
    ) {
        every { featureToggleService.isEnabled("SAMMENLIGN_NAV_BEREGNET_AFP_MED_$tpOrdning") } returns true
        every { navBeregnetAfpService.simuler(expectedSpec) } returns
                LivsvarigOffentligAfpResult(request.pid.value, navYtelser)

        service.sammenlignOgLoggAfp(
            request,
            { expectedSpec.fremtidigInntektListe },
            SimulertTjenestepensjon(
                tpLeverandoer = tpOrdning,
                utbetalingsperioder = tpYtelser,
                betingetTjenestepensjonErInkludert = false
            ),
            tpOrdning
        )

        verify(exactly = 1) { navBeregnetAfpService.simuler(expectedSpec) }
    }

    listOf("SPK", "KLP", "Storebrand").forEach { tpOrdning ->
        test("skips comparison when the feature toggle for $tpOrdning is disabled") {
            every { featureToggleService.isEnabled("SAMMENLIGN_NAV_BEREGNET_AFP_MED_$tpOrdning") } returns false

            service.sammenlignOgLoggAfp(request, { error("Income function must not be called") }, mockk(), tpOrdning)

            verify(exactly = 0) { navBeregnetAfpService.simuler(any()) }
            appender.list.size shouldBe 0
        }

        test("logs INFO for matching annual AFP from $tpOrdning") {
            sammenlign(
                listOf(navYtelse(24000.0)),
                listOf(Utbetalingsperiode(fom, 2000, "OAFP")),
                tpOrdning
            )

            with(appender.list.single()) {
                level shouldBe Level.INFO
                formattedMessage shouldContain "Ingen forskjell"
                formattedMessage shouldContain tpOrdning
            }
        }
    }

    test("logs WARN when annual AFP amounts differ") {
        sammenlign(listOf(navYtelse(24000.0)), listOf(Utbetalingsperiode(fom, 1000, "OAFP")))

        with(appender.list.single()) {
            level shouldBe Level.WARN
            formattedMessage shouldContain "Forskjell i AFP-beregning"
        }
    }

    test("excludes other benefit types from the AFP comparison") {
        sammenlign(
            listOf(navYtelse(24000.0)),
            listOf(
                Utbetalingsperiode(fom, 1000, "AP"),
                Utbetalingsperiode(fom, 2000, "OAFP")
            )
        )

        appender.list.single().level shouldBe Level.INFO
    }

    test("logs WARN when the provider has no AFP even if another benefit matches the amount") {
        sammenlign(listOf(navYtelse(24000.0)), listOf(Utbetalingsperiode(fom, 2000, "AP")))

        appender.list.single().level shouldBe Level.WARN
    }

    test("logs WARN when NAV has no AFP periods") {
        sammenlign(emptyList(), listOf(Utbetalingsperiode(fom, 2000, "OAFP")))

        appender.list.single().level shouldBe Level.WARN
    }

    test("logs WARN when the provider has no payment periods") {
        sammenlign(listOf(navYtelse(24000.0)), emptyList())

        appender.list.single().level shouldBe Level.WARN
    }

    test("logs WARN when neither result contains periods") {
        sammenlign(emptyList(), emptyList())

        appender.list.single().level shouldBe Level.WARN
    }

    test("logs WARN when AFP periods start on different dates") {
        sammenlign(listOf(navYtelse(24000.0)), listOf(Utbetalingsperiode(senereFom, 2000, "OAFP")))

        appender.list.single().level shouldBe Level.WARN
    }

    test("compares the earliest period even when input is unsorted and later amounts differ") {
        sammenlign(
            listOf(navYtelse(36000.0, senereFom), navYtelse(24000.0)),
            listOf(Utbetalingsperiode(senereFom, 1000, "OAFP"), Utbetalingsperiode(fom, 2000, "OAFP"))
        )

        appender.list.single().level shouldBe Level.INFO
    }

    test("logs WARN when the earliest period differs even if a later period matches") {
        sammenlign(
            listOf(navYtelse(36000.0, senereFom), navYtelse(24000.0)),
            listOf(Utbetalingsperiode(senereFom, 3000, "OAFP"), Utbetalingsperiode(fom, 1000, "OAFP"))
        )

        appender.list.single().level shouldBe Level.WARN
    }
})
