package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.normalder.Aldersgrenser
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.normalder.VerdiStatus
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonSpecValidatorV3Test : FunSpec({

    val idag = mockk<Time>().apply {
        every { today() } returns LocalDate.of(2025, 1, 1)
    }

    test("start av første uttak må være første dag i måneden") {
        shouldThrow<InvalidArgumentException> {
            AlderspensjonSpecValidatorV3(
                personService = Arrange.foedselsdato(1962, 1, 15),
                normalderService = arrangeNormalder(),
                time = idag
            ).validate(
                spec(uttakFom = LocalDate.of(2025, 2, 2))
            )
        }.message shouldBe "forsteUttak.datoFom må være første dag i måneden"
    }

    test("start av første uttak kan ikke være før måneden etter at personen oppnår nedre aldersgrense") {
        shouldThrow<InvalidArgumentException> {
            AlderspensjonSpecValidatorV3(
                personService = Arrange.foedselsdato(1970, 1, 15),
                normalderService = arrangeNormalder(), // nedre aldersgrense 62 år oppnås 2032-01-15
                time = idag
            ).validate(
                spec(uttakFom = LocalDate.of(2032, 1, 1)) // før 2032-02-01
            )
        }.message shouldBe "forsteUttak.datoFom kan ikke være før måneden etter at personen oppnår 62 år"
    }

    test("start av første uttak kan ikke være etter måneden etter at personen oppnår øvre aldersgrense") {
        shouldThrow<InvalidArgumentException> {
            AlderspensjonSpecValidatorV3(
                personService = Arrange.foedselsdato(1970, 1, 15),
                normalderService = arrangeNormalder(), // øvre aldersgrense 75 år oppnås 2045-01-15
                time = idag
            ).validate(
                spec(uttakFom = LocalDate.of(2045, 3, 1)) // etter 2045-02-01
            )
        }.message shouldBe "forsteUttak.datoFom kan ikke være etter måneden etter at personen oppnår 75 år"
    }

    test("start av første uttak må være etter dagens dato") {
        shouldThrow<InvalidArgumentException> {
            AlderspensjonSpecValidatorV3(
                personService = Arrange.foedselsdato(1962, 1, 15),
                normalderService = arrangeNormalder(),
                time = idag
            ).validate(
                spec(uttakFom = LocalDate.of(2025, 1, 1)) // = dagens dato
            )
        }.message shouldBe "forsteUttak.datoFom må være etter dagens dato"
    }

    test("når sivilstatus ved pensjonering er 'gift' må det angis om EPS har pensjon") {
        shouldThrow<InvalidArgumentException> {
            AlderspensjonSpecValidatorV3(
                personService = Arrange.foedselsdato(1963, 1, 15),
                normalderService = arrangeNormalder(),
                time = idag
            ).validate(
                spec(sivilstatus = SivilstatusSpecV3.GIFT, epsPensjon = null)
            )
        }.message shouldBe "epsPensjon må være angitt når sivilstandVedPensjonering er GIFT"
    }

    test("når sivilstatus ved pensjonering er 'samboer' må det angis om EPS har inntekt over 2G") {
        shouldThrow<InvalidArgumentException> {
            AlderspensjonSpecValidatorV3(
                personService = Arrange.foedselsdato(1963, 1, 15),
                normalderService = arrangeNormalder(),
                time = idag
            ).validate(
                spec(sivilstatus = SivilstatusSpecV3.SAMB, eps2G = null)
            )
        }.message shouldBe "eps2G må være angitt når sivilstandVedPensjonering er SAMB"
    }
})

private fun spec(
    uttakFom: LocalDate = LocalDate.of(2025, 2, 1),
    sivilstatus: SivilstatusSpecV3 = SivilstatusSpecV3.UGIF,
    epsPensjon: Boolean? = false,
    eps2G: Boolean? = false
) =
    AlderspensjonSpecV3(
        fnr = pid.value,
        sivilstandVedPensjonering = sivilstatus,
        forsteUttak = UttaksperiodeSpecV3(
            datoFom = uttakFom.toNorwegianDateAtNoon(),
            grad = 100
        ),
        heltUttak = null,
        arIUtlandetEtter16 = null,
        epsPensjon,
        eps2G,
        fremtidigInntektListe = null,
        simulerMedAfpPrivat = null
    )

private fun arrangeNormalder(): NormertPensjonsalderService =
    mockk<NormertPensjonsalderService>().apply {
        every { aldersgrenser(any()) } returns Aldersgrenser(
            aarskull = 1963,
            nedreAlder = Alder(aar = 62, maaneder = 0),
            normalder = Alder(aar = 67, maaneder = 0),
            oevreAlder = Alder(aar = 75, maaneder = 0),
            verdiStatus = VerdiStatus.FAST
        )
    }
