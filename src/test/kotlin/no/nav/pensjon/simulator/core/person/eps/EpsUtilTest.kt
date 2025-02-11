package no.nav.pensjon.simulator.core.person.eps

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.testutil.TestObjects

/**
 * EPS = ektefelle/partner/samboer
 */
class EpsUtilTest : FunSpec({

    test("epsMottarPensjon shouldBe false for gjenlevenderett") {
        val spec = TestObjects.simuleringSpec(type = SimuleringType.ALDER_M_GJEN)
        EpsUtil.epsMottarPensjon(spec) shouldBe false
    }

    test("epsMottarPensjon shouldBe false for non-EPS") {
        val spec = TestObjects.simuleringSpec(sivilstatus = SivilstatusType.ENKE)
        EpsUtil.epsMottarPensjon(spec) shouldBe false
    }

    test("epsMottarPensjon shouldBe true if EPS har pensjon") {
        val spec = TestObjects.simuleringSpec(sivilstatus = SivilstatusType.SAMB, epsHarPensjon = true)
        EpsUtil.epsMottarPensjon(spec) shouldBe true
    }

    test("epsMottarPensjon shouldBe false if non-EPS despite 'epsHarPensjon = true'") {
        val spec = TestObjects.simuleringSpec(sivilstatus = SivilstatusType.UGIF, epsHarPensjon = true)
        EpsUtil.epsMottarPensjon(spec) shouldBe false
    }

    test("erEps shouldBe false if non-EPS") {
        EpsUtil.erEps(SivilstatusType.GJPA) shouldBe false
    }

    test("erEps shouldBe true if EPS") {
        EpsUtil.erEps(SivilstatusType.GIFT) shouldBe true
    }

    test("gjelderGjenlevenderett shouldBe false if simuleringtype ikke gjelder gjenlevenderett") {
        EpsUtil.gjelderGjenlevenderett(SimuleringType.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG) shouldBe false
    }

    test("gjelderGjenlevenderett shouldBe true if simuleringtype gjelder gjenlevenderett") {
        EpsUtil.gjelderGjenlevenderett(SimuleringType.ENDR_ALDER_M_GJEN) shouldBe true
    }
})
