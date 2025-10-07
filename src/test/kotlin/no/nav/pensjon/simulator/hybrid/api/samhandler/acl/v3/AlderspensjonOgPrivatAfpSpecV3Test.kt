package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonOgPrivatAfpSpecV3Test : FunSpec({

    test("toString should produce JSON with redacted person ID") {
        AlderspensjonOgPrivatAfpSpecV3(
            personident = pid.value,
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
