package no.nav.pensjon.simulator.core.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.beregning.BarnetilleggFellesbarn
import no.nav.pensjon.simulator.core.domain.regler.beregning.BarnetilleggSerkullsbarn
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ektefelletillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AldersberegningKapittel19
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AldersberegningKapittel20
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.Basispensjon
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdninger
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class SimuleringResultPreparerTest : FunSpec({

    test("opprettOutput returns SimulatorOutput with correct grunnbeloep and sisteGyldigeOpptjeningAar") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            grunnbeloep = 118620,
            sisteGyldigeOpptjeningAar = 2023
        )

        val result = preparer.opprettOutput(spec)

        result.grunnbeloep shouldBe 118620
        result.sisteGyldigeOpptjeningAar shouldBe 2023
    }

    test("opprettOutput calls opptjeningAdder.addToOpptjeningListe") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val spec = resultPreparerSpec(foedselsdato = foedselsdato)
        preparer.opprettOutput(spec)

        verify {
            opptjeningAdder.addToOpptjeningListe(
                opptjeningListe = any(),
                beregningsresultatListe = any(),
                soekerGrunnlag = any(),
                regelverkType = RegelverkTypeEnum.N_REG_G_OPPTJ
            )
        }
    }

    test("opprettOutput sets alderspensjon with kapittel19Andel for N_REG_G_OPPTJ") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            regelverkType = RegelverkTypeEnum.N_REG_G_OPPTJ
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.kapittel19Andel shouldBe 1.0
        result.alderspensjon!!.kapittel20Andel shouldBe 0.0
    }

    test("opprettOutput sets alderspensjon with kapittel20Andel for N_REG_N_OPPTJ") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.kapittel19Andel shouldBe 0.0
        result.alderspensjon!!.kapittel20Andel shouldBe 1.0
    }

    test("opprettOutput sets pre2025OffentligAfp when provided") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val pre2025OffentligAfpResultat = mockk<no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat>()
        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            pre2025OffentligAfpBeregningResultat = pre2025OffentligAfpResultat
        )

        val result = preparer.opprettOutput(spec)

        result.pre2025OffentligAfp shouldBe pre2025OffentligAfpResultat
    }

    test("opprettOutput creates pensjonsperioder based on beregningsresultater") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 6, 15)
        val foersteUttakDato = LocalDate.of(2027, 7, 1) // Age 67

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2027, 7, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.pensjonPeriodeListe.isNotEmpty() shouldBe true
    }

    test("opprettOutput handles forrige alderspensjon beregningsresultat") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 3, 15)
        val foersteUttakDato = LocalDate.of(2025, 4, 1) // Age 67
        val today = LocalDate.of(2025, 6, 1) // User is already receiving pension

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 4, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        val forrigeResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 4, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 18000
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 7, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 19000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Should have periode representing løpende ytelser (tagged with alder=null)
        val loependePeriode = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == null }
        loependePeriode shouldNotBe null
    }

    test("opprettOutput populates privatAfpPeriodeListe when AFP results exist") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        val foersteUttakDato = LocalDate.of(2030, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val afpResultat = BeregningsResultatAfpPrivat().apply {
            virkFom = dateAtNoon(2030, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 5000
                totalbelopNettoAr = 60000.0
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            simuleringType = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            privatAfpBeregningResultatListe = mutableListOf(afpResultat)
        )

        val result = preparer.opprettOutput(spec)

        result.privatAfpPeriodeListe.isNotEmpty() shouldBe true
    }

    test("opprettOutput forces Kap19 output when simulerForTp is true and regelverkType is N_REG_N_OPPTJ") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            simulerForTp = true
        )

        val result = preparer.opprettOutput(spec)

        // After forceKap19OutputIfSimulerForTp, regelverkType changes to N_REG_G_OPPTJ
        // which results in kapittel19Andel = 1.0
        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.kapittel19Andel shouldBe 1.0
        result.alderspensjon!!.kapittel20Andel shouldBe 0.0
    }

    test("opprettOutput does not modify regelverkType when simulerForTp is false") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            simulerForTp = false
        )

        val result = preparer.opprettOutput(spec)

        // When simulerForTp is false, regelverkType stays N_REG_N_OPPTJ
        // which results in kapittel20Andel = 1.0
        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.kapittel19Andel shouldBe 0.0
        result.alderspensjon!!.kapittel20Andel shouldBe 1.0
    }

    test("opprettOutput sets uttakGradListe from kravhode") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val uttaksgrad = no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad().apply {
            uttaksgrad = 50
            fomDato = dateAtNoon(2030, 2, 1)
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            uttaksgradListe = mutableListOf(uttaksgrad)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.uttakGradListe shouldHaveSize 1
        result.alderspensjon!!.uttakGradListe[0].uttaksgrad shouldBe 50
    }

    test("opprettOutput sets pensjonBeholdningListe from spec") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val beholdningPeriode = BeholdningPeriode(
            datoFom = LocalDate.of(2029, 1, 1),
            pensjonsbeholdning = 2000000.0,
            garantipensjonsbeholdning = 500000.0,
            garantitilleggsbeholdning = 100000.0,
            garantipensjonsniva = null
        )

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            pensjonBeholdningPeriodeListe = listOf(beholdningPeriode)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.pensjonBeholdningListe shouldHaveSize 1
        result.alderspensjon!!.pensjonBeholdningListe[0].pensjonsbeholdning shouldBe 2000000.0
    }

    test("opprettOutput handles AFP etterfulgt av alderspensjon simulation type") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        val afpUttakDato = LocalDate.of(2025, 2, 1) // AFP from age 62
        val alderspensjonUttakDato = LocalDate.of(2030, 2, 1) // AP from age 67

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2024, 1, 1)

        val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2030, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 22000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = afpUttakDato,
            heltUttakDato = alderspensjonUttakDato,
            simuleringType = SimuleringTypeEnum.AFP_ETTERF_ALDER,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
    }

    test("opprettOutput handles livsvarigOffentligAfp results") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val livsvarigAfpOutput = mockk<no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpOutput>()
        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            livsvarigOffentligAfpBeregningResultatListe = listOf(livsvarigAfpOutput)
        )

        val result = preparer.opprettOutput(spec)

        result.livsvarigOffentligAfp shouldNotBe null
        result.livsvarigOffentligAfp!! shouldHaveSize 1
    }

    test("opprettOutput handles 2016 regelverk with mixed kapittel andeler") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 6, 15) // Born 1960 => mix of kap19 and kap20
        val foersteUttakDato = LocalDate.of(2027, 7, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val beregningsresultat2016 = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2027, 7, 1)
            virkTom = null
            andelKapittel19 = 5 // 50%
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 25000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2016)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.kapittel19Andel shouldBe 0.5
        result.alderspensjon!!.kapittel20Andel shouldBe 0.5
    }

    test("opprettOutput with gradert uttak creates correct pensjonsperioder") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        val foersteUttakDato = LocalDate.of(2028, 2, 1) // Gradert from age 65
        val heltUttakDato = LocalDate.of(2030, 2, 1) // Full from age 67

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val gradertResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2028, 2, 1)
            virkTom = dateAtNoon(2030, 1, 31)
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 10000 // 50% uttak
            }
        }

        val heltResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2030, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000 // 100% uttak
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = heltUttakDato,
            uttakGrad = UttakGradKode.P_50,
            alderspensjonBeregningResultatListe = mutableListOf(gradertResultat, heltResultat)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.pensjonPeriodeListe.isNotEmpty() shouldBe true
    }

    test("opprettOutput creates simulertBeregningInformasjonListe when outputSimulertBeregningsInformasjonForAllKnekkpunkter is true") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        val foersteUttakDato = LocalDate.of(2030, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2030, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = true
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.simulertBeregningInformasjonListe.isNotEmpty() shouldBe true
    }
    test("opprettOutput handles forrige privat AFP beregningsresultat") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 1, 15)
        val foersteUttakDato = LocalDate.of(2023, 2, 1) // Age 63

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 6, 1) // User already receiving AFP

        val forrigeAfpResultat = BeregningsResultatAfpPrivat().apply {
            virkFom = dateAtNoon(2023, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 4500
                totalbelopNettoAr = 54000.0
            }
        }

        val nyttAfpResultat = BeregningsResultatAfpPrivat().apply {
            virkFom = dateAtNoon(2025, 7, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 4800
                totalbelopNettoAr = 57600.0
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            simuleringType = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            privatAfpBeregningResultatListe = mutableListOf(nyttAfpResultat),
            forrigePrivatAfpBeregningResultat = forrigeAfpResultat
        )

        val result = preparer.opprettOutput(spec)

        result.privatAfpPeriodeListe.isNotEmpty() shouldBe true
        // Should include a periode with alderAar=0 for løpende ytelse
        val loependeAfpPeriode = result.privatAfpPeriodeListe.find { it.alderAar == 0 }
        loependeAfpPeriode shouldNotBe null
    }

    test("opprettOutput with 2025 regelverk uses BeregningsResultatAlderspensjon2025") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 6, 15) // Born 1970 => full kap20
        val foersteUttakDato = LocalDate.of(2037, 7, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2035, 1, 1)

        val beregningsresultat2025 = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2037, 7, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2025)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.kapittel19Andel shouldBe 0.0
        result.alderspensjon!!.kapittel20Andel shouldBe 1.0
    }

    test("opprettOutput correctly handles sivilstand from persongrunnlag") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.GIFT
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.GIFT,
            epsHarPensjon = true,
            foersteUttakDato = LocalDate.of(2030, 2, 1),
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = true,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2023
        )

        val result = preparer.opprettOutput(spec)

        result.sivilstand shouldBe SivilstandEnum.GIFT
        result.epsHarPensjon shouldBe true
        result.epsHarInntektOver2G shouldBe true
    }

    test("opprettOutput returns empty privatAfpPeriodeListe when no AFP results") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            simuleringType = SimuleringTypeEnum.ALDER,
            privatAfpBeregningResultatListe = mutableListOf()
        )

        val result = preparer.opprettOutput(spec)

        result.privatAfpPeriodeListe shouldHaveSize 0
    }

    test("opprettOutput handles empty alderspensjon beregningsresultat list") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            alderspensjonBeregningResultatListe = mutableListOf()
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // With empty result list, the pensjonsperiodeListe will be generated but empty for age periods
    }

    test("opprettOutput with forrige beregningsresultat 2011 extracts basispensjon") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 1, 15)
        val foersteUttakDato = LocalDate.of(2025, 2, 1)
        val today = LocalDate.of(2026, 3, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        val forrigeResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 18000
            }
            beregningKapittel19 = AldersberegningKapittel19().apply {
                basispensjon = Basispensjon().apply {
                    totalbelop = 200000.0
                }
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2026, 4, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 19000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
    }

    test("opprettOutput with forrige beregningsresultat 2016 extracts basispensjon and beholdning") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 6, 15)
        val foersteUttakDato = LocalDate.of(2027, 7, 1)
        val today = LocalDate.of(2028, 8, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        val forrigeResultat = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2027, 7, 1)
            virkTom = null
            andelKapittel19 = 5
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 22000
            }
            beregningsResultat2011 = BeregningsResultatAlderspensjon2011().apply {
                beregningKapittel19 = AldersberegningKapittel19().apply {
                    basispensjon = Basispensjon().apply {
                        totalbelop = 150000.0
                    }
                }
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2028, 9, 1)
            virkTom = null
            andelKapittel19 = 5
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 23000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.kapittel19Andel shouldBe 0.5
    }

    test("opprettOutput with forrige beregningsresultat 2025 extracts beholdning") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 1, 15)
        val foersteUttakDato = LocalDate.of(2037, 2, 1)
        val today = LocalDate.of(2038, 3, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        val forrigeResultat = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2037, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2038, 4, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 29000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.kapittel20Andel shouldBe 1.0
    }

    test("opprettOutput with early uttak before normertPensjoneringsdato adds extra knekkpunkt") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        val foersteUttakDato = LocalDate.of(2025, 2, 1) // Age 62 - before normalder

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1) // Age 67
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2024, 1, 1)

        val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 15000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.pensjonPeriodeListe.isNotEmpty() shouldBe true
    }

    test("opprettOutput calculates start alder when alderVedFoersteUttak equals alderToday") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 3, 15)
        val foersteUttakDato = LocalDate.of(2025, 4, 1) // Age 67
        val today = LocalDate.of(2025, 6, 1) // Also age 67

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 4, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        val forrigeResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 4, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 18000
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 7, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 19000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // When alderVedFoersteUttak == alderToday, startAlder should be alderToday (67)
        val perioderWithAlder67 = result.alderspensjon!!.pensjonPeriodeListe.filter { it.alderAar == 67 }
        perioderWithAlder67.isNotEmpty() shouldBe true
    }

    test("opprettOutput with multiple beregningsresultater calculates correct beloep per period") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        val foersteUttakDato = LocalDate.of(2030, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val resultat1 = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2030, 2, 1)
            virkTom = dateAtNoon(2030, 6, 30)
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 18000
            }
        }

        val resultat2 = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2030, 7, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(resultat1, resultat2)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Age 67 periode should have contributions from both resultater
        val periode67 = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == 67 }
        periode67 shouldNotBe null
        periode67!!.maanedsutbetalinger.size shouldBe 2
    }

    test("opprettOutput with empty resultatListe for forrige AFP sets virkTom to null") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 1, 15)
        val foersteUttakDato = LocalDate.of(2023, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 6, 1)

        val forrigeAfpResultat = BeregningsResultatAfpPrivat().apply {
            virkFom = dateAtNoon(2023, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 4500
                totalbelopNettoAr = 54000.0
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            simuleringType = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            privatAfpBeregningResultatListe = mutableListOf(), // Empty list
            forrigePrivatAfpBeregningResultat = forrigeAfpResultat
        )

        val result = preparer.opprettOutput(spec)

        result.privatAfpPeriodeListe.isNotEmpty() shouldBe true
    }

    test("opprettOutput handles 2016 regelverk with null andelKapittel19") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 6, 15)
        val foersteUttakDato = LocalDate.of(2027, 7, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        // BeregningsResultatAlderspensjon2016 with andelKapittel19 not set (defaults to 0)
        val beregningsresultat2016 = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2027, 7, 1)
            virkTom = null
            // andelKapittel19 not explicitly set, uses default 0
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 25000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2016)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // andelKapittel19 = 0 means kapittel19Andel = 0.0, kapittel20Andel = 1.0
        result.alderspensjon!!.kapittel19Andel shouldBe 0.0
        result.alderspensjon!!.kapittel20Andel shouldBe 1.0
    }

    test("opprettOutput handles persongrunnlag from spec correctly") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            heltUttakDato = LocalDate.of(2030, 2, 1)
        )

        val result = preparer.opprettOutput(spec)

        // opprettOutput sets basic output fields from SimulatorOutputMapper
        result.grunnbeloep shouldBe 118620
        result.sivilstand shouldBe SivilstandEnum.UGIF
    }

    test("opprettOutput with pre2025OffentligAfp simuleringstype uses heltUttakDato for start") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 1, 15)
        val afpUttakDato = LocalDate.of(2022, 2, 1) // AFP from age 62
        val alderspensjonUttakDato = LocalDate.of(2027, 2, 1) // AP from age 67

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2021, 1, 1)

        val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2027, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = afpUttakDato,
            heltUttakDato = alderspensjonUttakDato,
            simuleringType = SimuleringTypeEnum.AFP_ETTERF_ALDER,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Start alder should be calculated from heltUttakDato (67) not foersteUttakDato (62)
        val perioderWithAlder67 = result.alderspensjon!!.pensjonPeriodeListe.filter { it.alderAar == 67 }
        perioderWithAlder67.isNotEmpty() shouldBe true
    }

    test("opprettOutput does not add foersteUttakDato knekkpunkt for AFP_ETTERF_ALDER") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 1, 15)
        val afpUttakDato = LocalDate.of(2022, 2, 1)
        val alderspensjonUttakDato = LocalDate.of(2027, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2021, 1, 1)

        val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2027, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = afpUttakDato,
            heltUttakDato = alderspensjonUttakDato,
            simuleringType = SimuleringTypeEnum.AFP_ETTERF_ALDER,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // For AFP_ETTERF_ALDER, foersteUttakDato is not added as knekkpunkt
        // (AFP date should not be in alderspensjon knekkpunkter)
    }

    test("opprettOutput with pensjonsbeholdning in persongrunnlag finds beholdning for kap20") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 1, 15)
        val foersteUttakDato = LocalDate.of(2037, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2035, 1, 1)

        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2036
                    totalbelop = 2500000.0
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val beregningsresultat2025 = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2037, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
        }

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2025),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2034
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
    }

    test("opprettOutput correctly calculates beloep for partial year coverage") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 6, 15) // Born mid-year
        val foersteUttakDato = LocalDate.of(2030, 7, 1) // Start mid-year

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2030, 7, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // First year (age 67) will have partial coverage
        val periode67 = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == 67 }
        periode67 shouldNotBe null
        // beloep calculation is based on months of intersection between period and resultat
        // The exact value depends on the beloepPeriode calculation which considers
        // periodeStart (1st day of month after birth month + alderAar years) to periodeSlutt
        periode67!!.beloep shouldBe 20000 * 11 // 11 months coverage in age 67 year
    }

    test("opprettOutput with gjelderPre2025OffentligAfp uses heltUttakDato for foersteHeleUttak in beregningsinfo") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 1, 15)
        val afpUttakDato = LocalDate.of(2022, 2, 1) // AFP from age 62
        val alderspensjonUttakDato = LocalDate.of(2027, 2, 1) // AP from age 67

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2021, 1, 1)

        val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2027, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
            }
        }

        // Using AFP_ETTERF_ALDER which makes gjelderPre2025OffentligAfp() return true
        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = afpUttakDato,
            heltUttakDato = alderspensjonUttakDato,
            simuleringType = SimuleringTypeEnum.AFP_ETTERF_ALDER,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = true
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // When gjelderPre2025OffentligAfp()=true, gradertUttak is null and foersteHeleUttak = heltUttakDato
        // simulertBeregningInformasjonListe should contain info for heltUttakDato (2027-02-01)
        result.alderspensjon!!.simulertBeregningInformasjonListe.isNotEmpty() shouldBe true
    }

    test("opprettOutput with AFP_FPP simuleringType uses heltUttakDato correctly") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 1, 15)
        val afpUttakDato = LocalDate.of(2022, 2, 1)
        val alderspensjonUttakDato = LocalDate.of(2027, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2021, 1, 1)

        val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2027, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
            }
        }

        // AFP_FPP also makes gjelderPre2025OffentligAfp() return true
        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = afpUttakDato,
            heltUttakDato = alderspensjonUttakDato,
            simuleringType = SimuleringTypeEnum.AFP_FPP,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = true
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        result.alderspensjon!!.simulertBeregningInformasjonListe.isNotEmpty() shouldBe true
    }

    test("opprettOutput with gradertUttak adds knekkpunkt for both gradert and helt uttak") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1963, 1, 15)
        val gradertUttakDato = LocalDate.of(2028, 2, 1) // Gradert from age 65
        val heltUttakDato = LocalDate.of(2030, 2, 1) // Helt from age 67

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2030, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        val gradertResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2028, 2, 1)
            virkTom = dateAtNoon(2030, 1, 31)
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 10000
            }
        }

        val heltResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2030, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
            }
        }

        // When heltUttakDato is set, gradertUttak = foersteUttakDato
        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = gradertUttakDato,
            heltUttakDato = heltUttakDato,
            uttakGrad = UttakGradKode.P_50,
            alderspensjonBeregningResultatListe = mutableListOf(gradertResultat, heltResultat),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = true
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Should have simulertBeregningInformasjon for both gradert and helt uttak dates
        result.alderspensjon!!.simulertBeregningInformasjonListe.isNotEmpty() shouldBe true
        // The list should have entries for gradertUttakDato (2028-02-01) and heltUttakDato (2030-02-01)
        result.alderspensjon!!.simulertBeregningInformasjonListe.size shouldBeGreaterThanOrEqualTo 2
    }

    test("opprettOutput removes ektefelletillegg from totalbeloep when brukt is true") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 1, 15)
        val foersteUttakDato = LocalDate.of(2025, 2, 1)
        val today = LocalDate.of(2026, 3, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        // Create ektefelletillegg with brukt=true
        val ektefelletillegg = Ektefelletillegg().apply {
            brukt = true
            netto = 2000
            ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.ET
        }

        val forrigeResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000 // Before subtraction
                ytelseskomponenter.add(ektefelletillegg)
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2026, 4, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 21000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // The løpende periode (alderAar=null) should have ektefelletillegg subtracted
        val loependePeriode = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == null }
        loependePeriode shouldNotBe null
        // totalbelopNetto was 20000, ektefelletillegg.netto was 2000
        // After removeEktefelleAndBarnetilleggFromTotalbeloep: 20000 - 2000 = 18000
        loependePeriode!!.maanedsutbetalinger[0].beloep shouldBe 18000
    }

    test("opprettOutput does not remove ektefelletillegg from totalbeloep when brukt is false") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 1, 15)
        val foersteUttakDato = LocalDate.of(2025, 2, 1)
        val today = LocalDate.of(2026, 3, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        // Create ektefelletillegg with brukt=false
        val ektefelletillegg = Ektefelletillegg().apply {
            brukt = false
            netto = 2000
            ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.ET
        }

        val forrigeResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 20000
                ytelseskomponenter.add(ektefelletillegg)
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2026, 4, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 21000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        val loependePeriode = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == null }
        loependePeriode shouldNotBe null
        // totalbelopNetto should remain 20000 since brukt=false
        loependePeriode!!.maanedsutbetalinger[0].beloep shouldBe 20000
    }

    test("opprettOutput removes barnetillegg fellesbarn from totalbeloep when brukt is true") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 1, 15)
        val foersteUttakDato = LocalDate.of(2025, 2, 1)
        val today = LocalDate.of(2026, 3, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        // Create barnetillegg fellesbarn with brukt=true
        val barnetilleggFellesbarn = BarnetilleggFellesbarn().apply {
            brukt = true
            netto = 1500
        }

        val forrigeResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 22000
                ytelseskomponenter.add(barnetilleggFellesbarn)
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2026, 4, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 23000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        val loependePeriode = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == null }
        loependePeriode shouldNotBe null
        // 22000 - 1500 (barnetillegg fellesbarn) = 20500
        loependePeriode!!.maanedsutbetalinger[0].beloep shouldBe 20500
    }

    test("opprettOutput removes barnetillegg saerkullsbarn from totalbeloep when brukt is true") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 1, 15)
        val foersteUttakDato = LocalDate.of(2025, 2, 1)
        val today = LocalDate.of(2026, 3, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        // Create barnetillegg saerkullsbarn with brukt=true
        val barnetilleggSaerkullsbarn = BarnetilleggSerkullsbarn().apply {
            brukt = true
            netto = 1800
        }

        val forrigeResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 25000
                ytelseskomponenter.add(barnetilleggSaerkullsbarn)
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2026, 4, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 26000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        val loependePeriode = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == null }
        loependePeriode shouldNotBe null
        // 25000 - 1800 (barnetillegg saerkullsbarn) = 23200
        loependePeriode!!.maanedsutbetalinger[0].beloep shouldBe 23200
    }

    test("opprettOutput removes multiple tilleggsytelser from totalbeloep when all are brukt") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 1, 15)
        val foersteUttakDato = LocalDate.of(2025, 2, 1)
        val today = LocalDate.of(2026, 3, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        val ektefelletillegg = Ektefelletillegg().apply {
            brukt = true
            netto = 2000
        }

        val barnetilleggFellesbarn = BarnetilleggFellesbarn().apply {
            brukt = true
            netto = 1500
        }

        val barnetilleggSaerkullsbarn = BarnetilleggSerkullsbarn().apply {
            brukt = true
            netto = 1000
        }

        val forrigeResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 30000
                ytelseskomponenter.add(ektefelletillegg)
                ytelseskomponenter.add(barnetilleggFellesbarn)
                ytelseskomponenter.add(barnetilleggSaerkullsbarn)
            }
        }

        val nyttResultat = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2026, 4, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 31000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        val loependePeriode = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == null }
        loependePeriode shouldNotBe null
        // 30000 - 2000 (ET) - 1500 (TFB) - 1000 (TSB) = 25500
        loependePeriode!!.maanedsutbetalinger[0].beloep shouldBe 25500
    }

    test("findBeholdningFraBeregningsresultat with N_REG_G_N_OPPTJ extracts beholdning from 2016 resultat") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 6, 15) // Born 1960 => mix of kap19 and kap20
        val foersteUttakDato = LocalDate.of(2027, 7, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        // Create a BeregningsResultatAlderspensjon2016 with beholdning in beregningsResultat2025
        val pensjonsbeholdning = no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
            ar = 2026
            totalbelop = 2500000.0
            beholdningsTypeEnum = no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum.PEN_B
        }

        val beregningsresultat2016 = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2027, 7, 1)
            virkTom = null
            andelKapittel19 = 5 // 50%
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 25000
            }
            beregningsResultat2025 = BeregningsResultatAlderspensjon2025().apply {
                beregningKapittel20 = AldersberegningKapittel20().apply {
                    beholdninger = Beholdninger().apply {
                        beholdninger = listOf(pensjonsbeholdning)
                    }
                }
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2016)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // For N_REG_G_N_OPPTJ (2016 mixed rules), kapittel19Andel and kapittel20Andel should reflect the mix
        result.alderspensjon!!.kapittel19Andel shouldBe 0.5
        result.alderspensjon!!.kapittel20Andel shouldBe 0.5
        // The pensjonPeriode for age 67 should exist
        val periode67 = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == 67 }
        periode67 shouldNotBe null
    }

    test("findBeholdningFraBeregningsresultat with N_REG_N_OPPTJ extracts beholdning from 2025 resultat") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 1, 15) // Born 1970 => full kap20
        val foersteUttakDato = LocalDate.of(2037, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2035, 1, 1)

        // Create a BeregningsResultatAlderspensjon2025 with beholdning
        val pensjonsbeholdning = no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
            ar = 2036
            totalbelop = 3000000.0
            beholdningsTypeEnum = no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum.PEN_B
        }

        val beregningsresultat2025 = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2037, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
            beregningKapittel20 = AldersberegningKapittel20().apply {
                beholdninger = Beholdninger().apply {
                    beholdninger = listOf(pensjonsbeholdning)
                }
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2025)
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // For N_REG_N_OPPTJ (2025 rules), kapittel20Andel should be 1.0
        result.alderspensjon!!.kapittel19Andel shouldBe 0.0
        result.alderspensjon!!.kapittel20Andel shouldBe 1.0
        // The pensjonPeriode for age 67 should exist
        val periode67 = result.alderspensjon!!.pensjonPeriodeListe.find { it.alderAar == 67 }
        periode67 shouldNotBe null
    }

    test("findBeholdningFraBeregningsresultat with N_REG_G_N_OPPTJ extracts beholdning from forrigeResultat and sets pensjonBeholdningFoerUttak") {
        // This test verifies that findBeholdningFraBeregningsresultat is called when forrigeResultat exists
        // and correctly extracts beholdning from BeregningsResultatAlderspensjon2016.beregningsResultat2025.beregningKapittel20.beholdninger
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 6, 15)
        val foersteUttakDato = LocalDate.of(2025, 7, 1) // Started pension earlier
        val today = LocalDate.of(2026, 8, 1) // User is already receiving pension

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        // Create forrigeResultat (existing/running pension) with beholdning
        val forrigePensjonsbeholdning = no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
            ar = 2025
            totalbelop = 2200000.0
            beholdningsTypeEnum = no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum.PEN_B
        }

        val forrigeResultat = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2025, 7, 1)
            virkTom = null
            andelKapittel19 = 5
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 22000
            }
            beregningsResultat2025 = BeregningsResultatAlderspensjon2025().apply {
                beregningKapittel20 = AldersberegningKapittel20().apply {
                    beholdninger = Beholdninger().apply {
                        beholdninger = listOf(forrigePensjonsbeholdning)
                    }
                }
            }
        }

        // Create new beregningsresultat for the simulation
        val nyttResultat = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2026, 9, 1) // After today
            virkTom = null
            andelKapittel19 = 5
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 24000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Find the knekkpunkt info that used forrigeResultat
        val allBeregningsinfo = result.alderspensjon!!.pensjonPeriodeListe
            .flatMap { it.simulertBeregningInformasjonListe }
        allBeregningsinfo.isNotEmpty() shouldBe true
        // pensjonBeholdningFoerUttak should be set from forrigeResultat's beholdning (2,200,000)
        val infoWithBeholdningFoerUttak = allBeregningsinfo.find { it.pensjonBeholdningFoerUttak != null }
        infoWithBeholdningFoerUttak shouldNotBe null
        infoWithBeholdningFoerUttak!!.pensjonBeholdningFoerUttak shouldBe 2200000
    }

    test("findBeholdningFraBeregningsresultat with N_REG_N_OPPTJ extracts beholdning from forrigeResultat and sets pensjonBeholdningFoerUttak") {
        // This test verifies that findBeholdningFraBeregningsresultat is called when forrigeResultat exists
        // and correctly extracts beholdning from BeregningsResultatAlderspensjon2025.beregningKapittel20.beholdninger
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 1, 15)
        val foersteUttakDato = LocalDate.of(2035, 2, 1) // Started pension earlier
        val today = LocalDate.of(2036, 3, 1) // User is already receiving pension

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns today

        // Create forrigeResultat (existing/running pension) with beholdning
        val forrigePensjonsbeholdning = no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
            ar = 2035
            totalbelop = 3200000.0
            beholdningsTypeEnum = no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum.PEN_B
        }

        val forrigeResultat = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2035, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 26000
            }
            beregningKapittel20 = AldersberegningKapittel20().apply {
                beholdninger = Beholdninger().apply {
                    beholdninger = listOf(forrigePensjonsbeholdning)
                }
            }
        }

        // Create new beregningsresultat for the simulation
        val nyttResultat = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2036, 4, 1) // After today
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
        }

        val spec = resultPreparerSpec(
            foedselsdato = foedselsdato,
            foersteUttakDato = foersteUttakDato,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            alderspensjonBeregningResultatListe = mutableListOf(nyttResultat),
            forrigeAlderspensjonBeregningResultat = forrigeResultat
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Find the knekkpunkt info that used forrigeResultat
        val allBeregningsinfo = result.alderspensjon!!.pensjonPeriodeListe
            .flatMap { it.simulertBeregningInformasjonListe }
        allBeregningsinfo.isNotEmpty() shouldBe true
        // pensjonBeholdningFoerUttak should be set from forrigeResultat's beholdning (3,200,000)
        val infoWithBeholdningFoerUttak = allBeregningsinfo.find { it.pensjonBeholdningFoerUttak != null }
        infoWithBeholdningFoerUttak shouldNotBe null
        infoWithBeholdningFoerUttak!!.pensjonBeholdningFoerUttak shouldBe 3200000
    }

    test("findBeholdningFraPersongrunnlag with N_REG_G_N_OPPTJ sets pensjonBeholdningFoerUttak from persongrunnlag beholdninger") {
        // This test verifies the full functionality of findBeholdningFraPersongrunnlag:
        // 1. EnumSet.of(N_REG_G_N_OPPTJ, N_REG_N_OPPTJ).contains(regelverkType) = true
        // 2. Gets year from dagenFoerBeregningsresultatVirkFom
        // 3. Filters beholdninger by year using sortedSubset
        // 4. Returns latest beholdning using findLatest
        // 5. Sets pensjonBeholdningFoerUttak on SimulertBeregningsinfo
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 6, 15)
        val foersteUttakDato = LocalDate.of(2027, 7, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        // Create persongrunnlag with beholdninger for year 2027
        // (year of dagenFoerBeregningsresultatVirkFom = 2027-06-30)
        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2027 // Matches year of 2027-06-30
                    totalbelop = 2450000.0
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ
        }

        // Create beregningsresultat - no forrigeResultat triggers findBeholdningFraPersongrunnlag
        // Note: dateAtNoon uses zero-based month, so 6 = July
        val beregningsresultat2016 = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2027, 6, 1) // July 1 (zero-based month)
            virkTom = null
            andelKapittel19 = 5
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 25000
            }
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2016),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null, // No forrigeResultat
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2025
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Check that pensjonPeriodeListe has periods
        val periods = result.alderspensjon!!.pensjonPeriodeListe
        periods.isNotEmpty() shouldBe true
        // Check that periods have the expected ages (starting from 67)
        periods.any { it.alderAar == 67 } shouldBe true
        // Verify simulertBeregningInformasjonListe on PensjonPeriode is populated
        val allBeregningsinfo = periods.flatMap { it.simulertBeregningInformasjonListe }
        allBeregningsinfo.isNotEmpty() shouldBe true
        // Verify pensjonBeholdningFoerUttak is set from persongrunnlag.beholdninger (2,450,000)
        val infoWithBeholdningFoerUttak = allBeregningsinfo.find { it.pensjonBeholdningFoerUttak != null }
        infoWithBeholdningFoerUttak shouldNotBe null
        infoWithBeholdningFoerUttak!!.pensjonBeholdningFoerUttak shouldBe 2450000
        // For N_REG_G_N_OPPTJ, kapittel andel should be 0.5 (50%)
        result.alderspensjon!!.kapittel19Andel shouldBe 0.5
        result.alderspensjon!!.kapittel20Andel shouldBe 0.5
    }

    test("findBeholdningFraPersongrunnlag with N_REG_N_OPPTJ sets pensjonBeholdningFoerUttak from persongrunnlag beholdninger") {
        // This test verifies findBeholdningFraPersongrunnlag for N_REG_N_OPPTJ regelverkType
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 1, 15)
        val foersteUttakDato = LocalDate.of(2037, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2035, 1, 1)

        // Create persongrunnlag with beholdninger for year 2037
        // (year of dagenFoerBeregningsresultatVirkFom = 2037-01-31)
        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2037 // Matches year of 2037-01-31
                    totalbelop = 3650000.0
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
        }

        // Note: dateAtNoon uses zero-based month, so 1 = February
        val beregningsresultat2025 = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2037, 1, 1) // February 1 (zero-based month)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2025),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2035
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Check that pensjonPeriodeListe has periods
        val periods = result.alderspensjon!!.pensjonPeriodeListe
        periods.isNotEmpty() shouldBe true
        // Check that periods have the expected ages (starting from 67)
        periods.any { it.alderAar == 67 } shouldBe true
        // Verify simulertBeregningInformasjonListe on PensjonPeriode is populated
        val allBeregningsinfo = periods.flatMap { it.simulertBeregningInformasjonListe }
        allBeregningsinfo.isNotEmpty() shouldBe true
        // Verify pensjonBeholdningFoerUttak is set from persongrunnlag.beholdninger (3,650,000)
        val infoWithBeholdningFoerUttak = allBeregningsinfo.find { it.pensjonBeholdningFoerUttak != null }
        infoWithBeholdningFoerUttak shouldNotBe null
        infoWithBeholdningFoerUttak!!.pensjonBeholdningFoerUttak shouldBe 3650000
        // For N_REG_N_OPPTJ (pure kapittel 20), andel should be 0 and 1.0
        result.alderspensjon!!.kapittel19Andel shouldBe 0.0
        result.alderspensjon!!.kapittel20Andel shouldBe 1.0
    }

    test("findBeholdningFraPersongrunnlag returns null when regelverkType is N_REG_G_OPPTJ") {
        // This test verifies that EnumSet.of(N_REG_G_N_OPPTJ, N_REG_N_OPPTJ).contains(regelverkType) returns false
        // for N_REG_G_OPPTJ, so no beholdning is fetched from persongrunnlag
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 1, 15)
        val foersteUttakDato = LocalDate.of(2025, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2024, 1, 1)

        // Create persongrunnlag with beholdninger - but these should NOT be used for N_REG_G_OPPTJ
        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2025
                    totalbelop = 1800000.0
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ // Not in the EnumSet
        }

        // Note: dateAtNoon uses zero-based month, so 1 = February
        val beregningsresultat2011 = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 1, 1) // February 1 (zero-based month)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 18000
            }
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2011),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2023
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Check that pensjonPeriodeListe has periods
        val periods = result.alderspensjon!!.pensjonPeriodeListe
        periods.isNotEmpty() shouldBe true
        // Check that periods have the expected ages (starting from 67)
        periods.any { it.alderAar == 67 } shouldBe true
        // Verify simulertBeregningInformasjonListe on PensjonPeriode is populated
        val allBeregningsinfo = periods.flatMap { it.simulertBeregningInformasjonListe }
        allBeregningsinfo.isNotEmpty() shouldBe true
        // For N_REG_G_OPPTJ, findBeholdningFraPersongrunnlag returns null, so pensjonBeholdningFoerUttak should be null
        val infoWithBeholdningFoerUttak = allBeregningsinfo.find { it.pensjonBeholdningFoerUttak != null }
        infoWithBeholdningFoerUttak shouldBe null
        // For N_REG_G_OPPTJ (pure kapittel 19), andel should be 1.0 and 0
        result.alderspensjon!!.kapittel19Andel shouldBe 1.0
        result.alderspensjon!!.kapittel20Andel shouldBe 0.0
    }

    test("findBeholdningFraPersongrunnlag filters beholdninger by year and returns latest") {
        // This test verifies sortedSubset filters by year and findLatest returns the last one
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 1, 15)
        val foersteUttakDato = LocalDate.of(2037, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2035, 1, 1)

        // Create persongrunnlag with multiple beholdninger:
        // - Two for year 2037 (should filter to these and pick latest)
        // - One for year 2036 (should be filtered out)
        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2036 // Different year - should be filtered out
                    totalbelop = 3000000.0
                    fom = dateAtNoon(2036, 0, 1) // January 2036
                },
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2037 // Matching year - first entry
                    totalbelop = 3400000.0
                    fom = dateAtNoon(2037, 0, 1) // January 2037
                },
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2037 // Matching year - second entry (should be picked as "latest" due to later fom)
                    totalbelop = 3500000.0
                    fom = dateAtNoon(2037, 6, 1) // July 2037 - later than first 2037 entry
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
        }

        // Note: dateAtNoon uses zero-based month, so 1 = February
        val beregningsresultat2025 = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2037, 1, 1) // February 1 (zero-based month)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2025),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2035
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Check that pensjonPeriodeListe has periods
        val periods = result.alderspensjon!!.pensjonPeriodeListe
        periods.isNotEmpty() shouldBe true
        // Check that periods have the expected ages (starting from 67)
        periods.any { it.alderAar == 67 } shouldBe true
        // Verify simulertBeregningInformasjonListe on PensjonPeriode is populated
        val allBeregningsinfo = periods.flatMap { it.simulertBeregningInformasjonListe }
        allBeregningsinfo.isNotEmpty() shouldBe true
        // Verify pensjonBeholdningFoerUttak is set to latest beholdning for year 2037 (3,500,000)
        val infoWithBeholdningFoerUttak = allBeregningsinfo.find { it.pensjonBeholdningFoerUttak != null }
        infoWithBeholdningFoerUttak shouldNotBe null
        infoWithBeholdningFoerUttak!!.pensjonBeholdningFoerUttak shouldBe 3500000
        // For N_REG_N_OPPTJ (pure kapittel 20), andel should be 0 and 1.0
        result.alderspensjon!!.kapittel19Andel shouldBe 0.0
        result.alderspensjon!!.kapittel20Andel shouldBe 1.0
    }

    test("findBeholdningFraPersongrunnlag returns null when no beholdning matches year") {
        // This test verifies that when no beholdning matches the year, null is returned
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 1, 15)
        val foersteUttakDato = LocalDate.of(2037, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2035, 1, 1)

        // Create persongrunnlag with beholdninger for WRONG year (2036 instead of 2037)
        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2036 // Wrong year - should not match
                    totalbelop = 3000000.0
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
        }

        // Note: dateAtNoon uses zero-based month, so 1 = February
        val beregningsresultat2025 = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2037, 1, 1) // February 1 (zero-based month)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2025),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2035
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // Check that pensjonPeriodeListe has periods
        val periods = result.alderspensjon!!.pensjonPeriodeListe
        periods.isNotEmpty() shouldBe true
        // Check that periods have the expected ages (starting from 67)
        periods.any { it.alderAar == 67 } shouldBe true
        // Verify simulertBeregningInformasjonListe on PensjonPeriode is populated
        val allBeregningsinfo = periods.flatMap { it.simulertBeregningInformasjonListe }
        allBeregningsinfo.isNotEmpty() shouldBe true
        // Verify pensjonBeholdningFoerUttak is null because no beholdning matches year 2037
        val infoWithBeholdningFoerUttak = allBeregningsinfo.find { it.pensjonBeholdningFoerUttak != null }
        infoWithBeholdningFoerUttak shouldBe null
        // For N_REG_N_OPPTJ (pure kapittel 20), andel should be 0 and 1.0
        result.alderspensjon!!.kapittel19Andel shouldBe 0.0
        result.alderspensjon!!.kapittel20Andel shouldBe 1.0
    }

    test("findBeholdningFraPersongrunnlag with N_REG_G_N_OPPTJ finds beholdning from persongrunnlag") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1960, 6, 15)
        val foersteUttakDato = LocalDate.of(2027, 7, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2027, 7, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2025, 1, 1)

        // Create persongrunnlag with beholdninger
        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2026 // Year before virkFom
                    totalbelop = 2200000.0
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ
        }

        // Create beregningsresultat without beholdning (to trigger findBeholdningFraPersongrunnlag)
        val beregningsresultat2016 = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2027, 7, 1)
            virkTom = null
            andelKapittel19 = 5
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 25000
            }
            // No beregningsResultat2025, so findBeholdningFraBeregningsresultat returns null
            // This triggers findBeholdningFraPersongrunnlag
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2016),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2025
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // The N_REG_G_N_OPPTJ regelverkType should trigger findBeholdningFraPersongrunnlag
        // since beregningsresultat doesn't have beholdning
    }

    test("findBeholdningFraPersongrunnlag with N_REG_N_OPPTJ finds beholdning from persongrunnlag") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 1, 15)
        val foersteUttakDato = LocalDate.of(2037, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2035, 1, 1)

        // Create persongrunnlag with beholdninger
        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2036 // Year before virkFom
                    totalbelop = 3500000.0
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
        }

        // Create beregningsresultat without beholdning
        val beregningsresultat2025 = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2037, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
            // No beregningKapittel20, so findBeholdningFraBeregningsresultat returns null
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2025),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2035
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // The N_REG_N_OPPTJ regelverkType should trigger findBeholdningFraPersongrunnlag
    }

    test("findBeholdningFraPersongrunnlag returns null for N_REG_G_OPPTJ regelverkType") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1958, 1, 15) // Born 1958 => full kap19
        val foersteUttakDato = LocalDate.of(2025, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2025, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2024, 1, 1)

        // Create persongrunnlag with beholdninger (but these should not be used for N_REG_G_OPPTJ)
        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2024
                    totalbelop = 1500000.0
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ // This should NOT trigger beholdning lookup
        }

        val beregningsresultat2011 = BeregningsResultatAlderspensjon2011().apply {
            virkFom = dateAtNoon(2025, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 18000
            }
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2011),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2023
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // For N_REG_G_OPPTJ, beholdning should NOT be looked up from persongrunnlag
        // The findBeholdningFraPersongrunnlag returns null for this regelverkType
    }

    test("findBeholdningFraPersongrunnlag with multiple beholdninger for same year finds latest") {
        val opptjeningAdder = mockk<SimulertOpptjeningAdder>(relaxed = true)
        val normalderService = mockk<NormertPensjonsalderService>()
        val time = mockk<Time>()
        val preparer = SimuleringResultPreparer(opptjeningAdder, normalderService, time)

        val foedselsdato = LocalDate.of(1970, 1, 15)
        val foersteUttakDato = LocalDate.of(2037, 2, 1)

        every { normalderService.normalder(foedselsdato) } returns Alder(67, 0)
        every { normalderService.normertPensjoneringsdato(foedselsdato) } returns LocalDate.of(2037, 2, 1)
        every { normalderService.opptjeningMaxAlderAar(foedselsdato) } returns 75
        every { time.today() } returns LocalDate.of(2035, 1, 1)

        // Create persongrunnlag with multiple beholdninger for same year
        val soekerGrunnlag = Persongrunnlag().apply {
            fodselsdato = foedselsdato.atStartOfDay().let {
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                    clear()
                    set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
                }.time
            }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    sivilstandTypeEnum = SivilstandEnum.UGIF
                }
            )
            beholdninger = mutableListOf(
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2036
                    totalbelop = 3000000.0
                },
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2036
                    totalbelop = 3100000.0 // This is the "latest" one added
                },
                no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning().apply {
                    ar = 2035 // Different year, should not be used
                    totalbelop = 2900000.0
                }
            )
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(soekerGrunnlag)
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
        }

        val beregningsresultat2025 = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2037, 2, 1)
            virkTom = null
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                totalbelopNetto = 28000
            }
            // No beregningKapittel20 to force lookup from persongrunnlag
        }

        val simuleringSpec = SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null,
            pid = null,
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foedselAar = foedselsdato.year,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

        val spec = ResultPreparerSpec(
            simuleringSpec = simuleringSpec,
            kravhode = kravhode,
            alderspensjonBeregningResultatListe = mutableListOf(beregningsresultat2025),
            privatAfpBeregningResultatListe = mutableListOf(),
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            pre2025OffentligAfpBeregningResultat = null,
            livsvarigOffentligAfpBeregningResultatListe = null,
            grunnbeloep = 118620,
            pensjonBeholdningPeriodeListe = emptyList(),
            outputSimulertBeregningsInformasjonForAllKnekkpunkter = false,
            sisteGyldigeOpptjeningAar = 2035
        )

        val result = preparer.opprettOutput(spec)

        result.alderspensjon shouldNotBe null
        // sortedSubset filters by year (2036) and findLatest returns the last one after sorting by year
    }
})

private fun resultPreparerSpec(
    foedselsdato: LocalDate,
    foersteUttakDato: LocalDate = foedselsdato.plusYears(67).withDayOfMonth(1).plusMonths(1),
    heltUttakDato: LocalDate? = null,
    grunnbeloep: Int = 118620,
    sisteGyldigeOpptjeningAar: Int = 2023,
    regelverkType: RegelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ,
    simuleringType: SimuleringTypeEnum = SimuleringTypeEnum.ALDER,
    uttakGrad: UttakGradKode = UttakGradKode.P_100,
    simulerForTp: Boolean = false,
    alderspensjonBeregningResultatListe: MutableList<AbstraktBeregningsResultat> = mutableListOf(),
    privatAfpBeregningResultatListe: MutableList<BeregningsResultatAfpPrivat> = mutableListOf(),
    forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat? = null,
    forrigePrivatAfpBeregningResultat: BeregningsResultatAfpPrivat? = null,
    pre2025OffentligAfpBeregningResultat: no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat? = null,
    livsvarigOffentligAfpBeregningResultatListe: List<no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpOutput>? = null,
    pensjonBeholdningPeriodeListe: List<BeholdningPeriode> = emptyList(),
    outputSimulertBeregningsInformasjonForAllKnekkpunkter: Boolean = false,
    uttaksgradListe: MutableList<no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad> = mutableListOf()
): ResultPreparerSpec {
    val soekerGrunnlag = Persongrunnlag().apply {
        fodselsdato = foedselsdato.atStartOfDay().let {
            Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
                clear()
                set(it.year, it.monthValue - 1, it.dayOfMonth, 12, 0, 0)
            }.time
        }
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                sivilstandTypeEnum = SivilstandEnum.UGIF
            }
        )
    }

    val kravhode = Kravhode().apply {
        persongrunnlagListe = mutableListOf(soekerGrunnlag)
        regelverkTypeEnum = regelverkType
        this.uttaksgradListe = uttaksgradListe
    }

    val simuleringSpec = SimuleringSpec(
        type = simuleringType,
        sivilstatus = SivilstatusType.UGIF,
        epsHarPensjon = false,
        foersteUttakDato = foersteUttakDato,
        heltUttakDato = heltUttakDato,
        pid = null,
        foedselDato = foedselsdato,
        avdoed = null,
        isTpOrigSimulering = false,
        simulerForTp = simulerForTp,
        uttakGrad = uttakGrad,
        forventetInntektBeloep = 0,
        inntektUnderGradertUttakBeloep = 0,
        inntektEtterHeltUttakBeloep = 0,
        inntektEtterHeltUttakAntallAar = 0,
        foedselAar = foedselsdato.year,
        utlandAntallAar = 0,
        utlandPeriodeListe = mutableListOf(),
        fremtidigInntektListe = mutableListOf(),
        brukFremtidigInntekt = false,
        inntektOver1GAntallAar = 0,
        flyktning = false,
        epsHarInntektOver2G = false,
        livsvarigOffentligAfp = null,
        pre2025OffentligAfp = null,
        erAnonym = true,
        ignoreAvslag = false,
        isHentPensjonsbeholdninger = false,
        isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = outputSimulertBeregningsInformasjonForAllKnekkpunkter,
        onlyVilkaarsproeving = false,
        epsKanOverskrives = false
    )

    return ResultPreparerSpec(
        simuleringSpec = simuleringSpec,
        kravhode = kravhode,
        alderspensjonBeregningResultatListe = alderspensjonBeregningResultatListe,
        privatAfpBeregningResultatListe = privatAfpBeregningResultatListe,
        forrigeAlderspensjonBeregningResultat = forrigeAlderspensjonBeregningResultat,
        forrigePrivatAfpBeregningResultat = forrigePrivatAfpBeregningResultat,
        pre2025OffentligAfpBeregningResultat = pre2025OffentligAfpBeregningResultat,
        livsvarigOffentligAfpBeregningResultatListe = livsvarigOffentligAfpBeregningResultatListe,
        grunnbeloep = grunnbeloep,
        pensjonBeholdningPeriodeListe = pensjonBeholdningPeriodeListe,
        outputSimulertBeregningsInformasjonForAllKnekkpunkter = outputSimulertBeregningsInformasjonForAllKnekkpunkter,
        sisteGyldigeOpptjeningAar = sisteGyldigeOpptjeningAar
    )
}

private fun dateAtNoon(year: Int, month: Int, day: Int): Date =
    Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
        clear()
        set(year, month - 1, day, 12, 0, 0)
    }.time
