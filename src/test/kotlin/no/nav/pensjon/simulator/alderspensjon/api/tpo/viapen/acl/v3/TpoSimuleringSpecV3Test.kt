package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class TpoSimuleringSpecV3Test : FunSpec({
    test("toString without nulls") {
        TpoSimuleringSpecV3(
            pid = pid.value,
            sivilstatus = SivilstatusType.SAMB,
            epsPensjon = true,
            eps2G = false,
            utenlandsopphold = 1,
            simuleringType = SimuleringType.ALDER,
            fremtidigInntektList = listOf(
                InntektSpecLegacyV3(arligInntekt = 2, fomDato = LocalDate.of(2021, 1, 1))
            ),
            foersteUttakDato = LocalDate.of(2022, 2, 2),
            uttakGrad = UttakGradKode.P_50,
            heltUttakDato = LocalDate.of(2023, 3, 3)
        ).toString() shouldBe """{ "pid": "123456*****", "sivilstatus": "SAMB", "epsPensjon": true, "eps2G": false, "utenlandsopphold": 1, "simuleringType": "ALDER", "fremtidigInntektList": [{ "arligInntekt": 2, "fomDato": "2021-01-01" }], "foersteUttakDato": "2022-02-02", "uttakGrad": "P_50", "heltUttakDato": "2023-03-03" }"""
    }

    test("toString with nulls") {
        TpoSimuleringSpecV3(
            pid = null,
            sivilstatus = null,
            epsPensjon = null,
            eps2G = null,
            utenlandsopphold = null,
            simuleringType = null,
            fremtidigInntektList = null,
            foersteUttakDato = null,
            uttakGrad = null,
            heltUttakDato = null,
        ).toString() shouldBe """{ "pid": "<null>", "sivilstatus": null, "epsPensjon": null, "eps2G": null, "utenlandsopphold": null, "simuleringType": null, "fremtidigInntektList": null, "foersteUttakDato": null, "uttakGrad": null, "heltUttakDato": null }"""
    }
})
