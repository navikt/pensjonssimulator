package no.nav.pensjon.simulator.api.samhandler.np.v2.acl.spec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestObjects
import java.time.LocalDate

class SimuleringSpecDtoTest : FunSpec({

    test("toString should produce JSON with redacted person ID") {
        SimuleringSpecDto(
            personident = TestObjects.pid.value,
            sivilstatusVedPensjonering = ApOgPrivatAfpSivilstatusSpecV3.UGIF,
            foersteUttak = ApOgPrivatAfpUttakSpecV3(
                fomDato = LocalDate.of(2025, 1, 1),
                grad = 100,
                aarligInntekt = null
            ),
            heltUttak = null,
            aarligInntektFoerUttak = null,
            antallInntektsaarEtterHeltUttak = null,
            aarIUtlandetEtter16 = null,
            harEpsPensjon = null,
            harEpsPensjonsgivendeInntektOver2G = null,
            simulerPrivatAfp = null
        ).toString() shouldBe """{
    "personident": "123456*****",
    "sivilstatusVedPensjonering": "UGIF",
    "foersteUttak": {
        "fomDato": "2025-01-01",
        "grad": 100,
        "aarligInntekt": null
    },
    "heltUttak": null,
    "aarligInntektFoerUttak": null,
    "antallInntektsaarEtterHeltUttak": null,
    "aarIUtlandetEtter16": null,
    "harEpsPensjon": null,
    "harEpsPensjonsgivendeInntektOver2G": null,
    "simulerPrivatAfp": null
}"""
    }
})