package no.nav.pensjon.simulator.core.person.eps

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec

/**
 * EPS = ektefelle/partner/samboer
 */
class EpsUtilTest : FunSpec({

    test("'epsMottarPensjon' should be false for gjenlevenderett") {
        val spec = simuleringSpec(type = SimuleringTypeEnum.ALDER_M_GJEN)
        EpsUtil.epsMottarPensjon(spec) shouldBe false
    }

    test("'epsMottarPensjon' should be false for non-EPS") {
        val spec = simuleringSpec(sivilstatus = SivilstatusType.ENKE)
        EpsUtil.epsMottarPensjon(spec) shouldBe false
    }

    test("'epsMottarPensjon' should be true if EPS har pensjon") {
        val spec = simuleringSpec(sivilstatus = SivilstatusType.SAMB, epsHarPensjon = true)
        EpsUtil.epsMottarPensjon(spec) shouldBe true
    }

    test("'epsMottarPensjon' should be false if non-EPS despite 'epsHarPensjon = true'") {
        val spec = simuleringSpec(sivilstatus = SivilstatusType.UGIF, epsHarPensjon = true)
        EpsUtil.epsMottarPensjon(spec) shouldBe false
    }

    test("'erEps' should be false if non-EPS") {
        EpsUtil.erEps(SivilstatusType.GJPA) shouldBe false
    }

    test("'erEps' should be true if EPS") {
        EpsUtil.erEps(SivilstatusType.GIFT) shouldBe true
    }

    test("'gjelderGjenlevenderett' should be false if simuleringtype ikke gjelder gjenlevenderett") {
        EpsUtil.gjelderGjenlevenderett(SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG) shouldBe false
    }

    test("'gjelderGjenlevenderett' should be true if simuleringtype gjelder gjenlevenderett") {
        EpsUtil.gjelderGjenlevenderett(SimuleringTypeEnum.ENDR_ALDER_M_GJEN) shouldBe true
    }

    test("'setEpsMottarPensjon' should not set 'epsMottarPensjon' if EPS ikke mottar pensjon") {
        val resultat = BeregningsResultatAlderspensjon2011().apply {
            beregningsInformasjonKapittel19 = epsMottarIkkePensjon()
        }

        EpsUtil.setEpsMottarPensjon(
            resultat,
            spec = simuleringSpec(sivilstatus = SivilstatusType.GIFT, epsHarPensjon = false)
        )

        resultat.beregningsInformasjonKapittel19?.epsMottarPensjon shouldBe false
    }

    test("'setEpsMottarPensjon' should not set 'epsMottarPensjon' if simuleringtype gjelder gjenlevenderett") {
        val resultat = BeregningsResultatAlderspensjon2011().apply {
            beregningsInformasjonKapittel19 = epsMottarIkkePensjon()
        }

        EpsUtil.setEpsMottarPensjon(
            resultat,
            spec = simuleringSpec(
                type = SimuleringTypeEnum.ALDER_M_GJEN, // gjelder gjenlevenderett
                sivilstatus = SivilstatusType.GIFT,
                epsHarPensjon = false
            )
        )

        resultat.beregningsInformasjonKapittel19?.epsMottarPensjon shouldBe false
    }

    test("'setEpsMottarPensjon' should set 'epsMottarPensjon' if EPS mottar pensjon (kapittel 19)") {
        val resultat = BeregningsResultatAlderspensjon2011().apply {
            beregningsInformasjonKapittel19 = epsMottarIkkePensjon()
        }

        EpsUtil.setEpsMottarPensjon(
            resultat,
            spec = simuleringSpec(sivilstatus = SivilstatusType.GIFT, epsHarPensjon = true)
        )

        resultat.beregningsInformasjonKapittel19?.epsMottarPensjon shouldBe true
    }

    test("'setEpsMottarPensjon' should set 'epsMottarPensjon' if EPS mottar pensjon (kapittel 20)") {
        val resultat = BeregningsResultatAlderspensjon2025().apply {
            beregningsInformasjonKapittel20 = epsMottarIkkePensjon()
        }

        EpsUtil.setEpsMottarPensjon(
            resultat,
            spec = simuleringSpec(sivilstatus = SivilstatusType.REPA, epsHarPensjon = true)
        )

        resultat.beregningsInformasjonKapittel20?.epsMottarPensjon shouldBe true
    }

    test("'setEpsMottarPensjon' should set 'epsMottarPensjon' if EPS mottar pensjon (overgangskull)") {
        val resultat = BeregningsResultatAlderspensjon2016().apply {
            beregningsResultat2011 = BeregningsResultatAlderspensjon2011().apply {
                beregningsInformasjonKapittel19 = epsMottarIkkePensjon()
            }
            beregningsResultat2025 = BeregningsResultatAlderspensjon2025().apply {
                beregningsInformasjonKapittel20 = epsMottarIkkePensjon()
            }
        }

        EpsUtil.setEpsMottarPensjon(
            resultat,
            spec = simuleringSpec(sivilstatus = SivilstatusType.SAMB, epsHarPensjon = true)
        )

        resultat.beregningsResultat2011?.beregningsInformasjonKapittel19?.epsMottarPensjon shouldBe true
        resultat.beregningsResultat2025?.beregningsInformasjonKapittel20?.epsMottarPensjon shouldBe true
    }
})

private fun epsMottarIkkePensjon() =
    BeregningsInformasjon().apply { epsMottarPensjon = false }
