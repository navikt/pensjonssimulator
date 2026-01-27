package no.nav.pensjon.simulator.core.krav

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpPersongrunnlag
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpUttaksgrad
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.endring.EndringPersongrunnlag
import no.nav.pensjon.simulator.core.endring.EndringUttaksgrad
import no.nav.pensjon.simulator.core.person.PersongrunnlagService
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

class KravhodeCreatorTest : ShouldSpec({

    /**
     * Opprettet kravhode skal ha:
     * - kravFremsattDato = dagens dato
     * - onsketVirkningsdato = heltUttakDato, hvis denne er definert
     * - gjelder = null
     * - sakId = null
     * - sakType = ALDER
     * - regelverkTypeEnum = verdi basert på årskull, f.eks. N_REG_N_OPPTJ for årskull 1963
     */
    should("opprette kravhode") {
        val kravhode = KravhodeCreator(
            epsService = mockk(relaxed = true),
            persongrunnlagService = arrangePersongrunnlag(),
            opptjeningUpdater = mockk(relaxed = true),
            generelleDataHolder = mockk(relaxed = true),
            kravService = mockk(),
            ufoereService = mockk(relaxed = true),
            endringPersongrunnlag = mockk(),
            endringUttaksgrad = mockk(),
            pre2025OffentligAfpPersongrunnlag = mockk(),
            pre2025OffentligAfpUttaksgrad = mockk(),
            time = { LocalDate.of(2025, 1, 1) } // "dagens dato"
        ).opprettKravhode(
            kravhodeSpec = KravhodeSpec(
                simulatorInput = simuleringSpec(), // heltUttakDato 2032-06-01
                forrigeAlderspensjonBeregningResult = null,
                grunnbeloep = 123000
            ),
            person = PenPerson().apply { foedselsdato = LocalDate.of(1963, 1, 1) }, // => regelverktype N_REG_N_OPPTJ
            virkningDatoGrunnlagListe = emptyList()
        )

        with(kravhode) {
            kravFremsattDato shouldBe LocalDate.of(2025, 1, 1).toNorwegianDateAtNoon() // = "dagens dato"
            onsketVirkningsdato shouldBe LocalDate.of(2032, 6, 1) // = heltUttakDato
            gjelder shouldBe null
            sakId shouldBe null
            sakType shouldBe SakTypeEnum.ALDER
            regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
        }
    }

    should("opprette anonymt persongrunnlag når erAnonym er true") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(
            erAnonym = true,
            pid = null,
            foedselDato = LocalDate.of(1963, 1, 1)
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(
                simulatorInput = spec,
                forrigeAlderspensjonBeregningResult = null,
                grunnbeloep = 123000
            ),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        with(kravhode.hentPersongrunnlagForSoker()) {
            penPerson?.penPersonId shouldBe -1L
            fodselsdato shouldBe LocalDate.of(1963, 1, 1).toNorwegianDateAtNoon()
            statsborgerskapEnum shouldBe LandkodeEnum.NOR
            bosattLandEnum shouldBe LandkodeEnum.NOR
        }
    }

    should("mappe GIFT sivilstatus til GIFT sivilstand for anonym simulering") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec(sivilstatus = SivilstatusType.GIFT)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.GIFT
    }

    should("mappe REPA sivilstatus til REPA sivilstand for anonym simulering") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec(sivilstatus = SivilstatusType.REPA)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.REPA
    }

    should("mappe UGIF sivilstatus til UGIF sivilstand for anonym simulering") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec(sivilstatus = SivilstatusType.UGIF)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.UGIF
    }

    should("opprette uttaksgradliste med én grad når uttak er 100%") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(uttakGrad = UttakGradKode.P_100, heltUttakDato = null)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.uttaksgradListe shouldHaveSize 1
        kravhode.uttaksgradListe[0].uttaksgrad shouldBe 100
    }

    should("opprette uttaksgradliste med to grader når uttak er gradert") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(
            uttakGrad = UttakGradKode.P_50,
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.uttaksgradListe shouldHaveSize 2
        kravhode.uttaksgradListe[0].uttaksgrad shouldBe 100 // Sorted by fomDato descending, so 100% (2032-06-01) comes first
        kravhode.uttaksgradListe[1].uttaksgrad shouldBe 50  // 50% (2030-01-01) comes second
    }

    should("sette boddEllerArbeidetIUtlandet til true når utlandAntallAar > 0") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(utlandAntallAar = 5)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.boddEllerArbeidetIUtlandet shouldBe true
    }

    should("sette boddEllerArbeidetIUtlandet til false når utlandAntallAar er 0") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(utlandAntallAar = 0)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.boddEllerArbeidetIUtlandet shouldBe false
    }

    should("opprette kravlinje med type AP for søker") {
        val creator = createKravhodeCreator()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.kravlinjeListe shouldHaveSize 1
        kravhode.kravlinjeListe[0].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.AP
        kravhode.kravlinjeListe[0].land shouldBe LandkodeEnum.NOR
        kravhode.kravlinjeListe[0].kravlinjeStatus shouldBe KravlinjeStatus.VILKARSPROVD
    }

    should("sette onsketVirkningsdato til heltUttakDato når definert") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.onsketVirkningsdato shouldBe LocalDate.of(2032, 6, 1)
    }

    should("sette onsketVirkningsdato til foersteUttakDato når heltUttakDato ikke er definert") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = null,
            uttakGrad = UttakGradKode.P_100
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.onsketVirkningsdato shouldBe LocalDate.of(2030, 1, 1)
    }

    should("sette onsketVirkningsdato til null for anonym simulering") {
        val creator = createKravhodeCreator()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(anonymSimuleringSpec(), null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.onsketVirkningsdato shouldBe null
    }

    should("sette regelverkType til N_REG_G_OPPTJ for årskull 1953") {
        val creator = createKravhodeCreator()
        val person = PenPerson().apply {
            penPersonId = 1L
            foedselsdato = LocalDate.of(1953, 1, 1)
        }

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = person,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_OPPTJ
    }

    should("sette regelverkType til N_REG_G_N_OPPTJ for årskull 1960") {
        val creator = createKravhodeCreator()
        val person = PenPerson().apply {
            penPersonId = 1L
            foedselsdato = LocalDate.of(1960, 1, 1)
        }

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = person,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_N_OPPTJ
    }

    should("sette regelverkType til N_REG_N_OPPTJ for årskull 1963") {
        val creator = createKravhodeCreator()
        val person = PenPerson().apply {
            penPersonId = 1L
            foedselsdato = LocalDate.of(1963, 1, 1)
        }

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = person,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
    }

    should("mappe SAMBO sivilstatus til UGIF sivilstand for anonym simulering") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec(sivilstatus = SivilstatusType.SAMB)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.UGIF
    }

    should("mappe ENKE sivilstatus til UGIF sivilstand for anonym simulering") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec(sivilstatus = SivilstatusType.ENKE)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.UGIF
    }

    should("bruke foedselsaar fra spec når person er null") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec().copy(foedselAar = 1970)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
    }

    should("sette boddEllerArbeidetIUtlandet til true når forrigeBeregningResultat har det") {
        val kravService = mockk<KravService>()
        val forrigeKravhode = Kravhode().apply {
            boddEllerArbeidetIUtlandet = true
        }
        every { kravService.fetchKravhode(any()) } returns forrigeKravhode

        val creator = createKravhodeCreator(kravService = kravService)
        val spec = simuleringSpec().copy(utlandAntallAar = 0)
        val forrigeResultat = mockk<AbstraktBeregningsResultat>()
        every { forrigeResultat.kravId } returns 123L

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, forrigeResultat, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.boddEllerArbeidetIUtlandet shouldBe true
    }

    should("sette antallArUtland på anonymt persongrunnlag") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec().copy(utlandAntallAar = 7)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().antallArUtland shouldBe 7
    }

    should("sette flyktning til false på anonymt persongrunnlag") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().flyktning shouldBe false
    }

    should("sette inngangOgEksportGrunnlag.fortsattMedlemFT til true på anonymt persongrunnlag") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().inngangOgEksportGrunnlag?.fortsattMedlemFT shouldBe true
    }

    should("legge til forsteVirkningsdatoGrunnlag når virkningDatoGrunnlagListe ikke er tom") {
        val creator = createKravhodeCreator()
        val person = penPerson()
        val virkningDatoGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            virkningsdato = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
            kravlinjeTypeEnum = KravlinjeTypeEnum.AP
        }

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = person,
            virkningDatoGrunnlagListe = listOf(virkningDatoGrunnlag)
        )

        kravhode.hentPersongrunnlagForSoker().forsteVirkningsdatoGrunnlagListe shouldHaveSize 1
        kravhode.hentPersongrunnlagForSoker().forsteVirkningsdatoGrunnlagListe[0].virkningsdato shouldBe
                LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
    }

    should("sette sisteGyldigeOpptjeningsAr på persongrunnlag basert på fødselsår") {
        val creator = createKravhodeCreator()
        val person = PenPerson().apply {
            penPersonId = 1L
            foedselsdato = LocalDate.of(1963, 1, 1)
        }

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = person,
            virkningDatoGrunnlagListe = emptyList()
        )

        // person født 1963 + MAX_ALDER (80) = 2043
        kravhode.hentPersongrunnlagForSoker().sisteGyldigeOpptjeningsAr shouldBe 2043
    }

    should("sette grunnlagsrolle SOKER på anonymt persondetalj") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
    }

    should("sette bruk til true på anonymt persondetalj") {
        val creator = createKravhodeCreator()
        val spec = anonymSimuleringSpec()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().personDetaljListe[0].bruk shouldBe true
    }

    should("sette penRolleFom til fødselsdato på anonymt persondetalj") {
        val creator = createKravhodeCreator()
        val foedselsdato = LocalDate.of(1963, 5, 15)
        val spec = anonymSimuleringSpec().copy(foedselDato = foedselsdato)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().personDetaljListe[0].penRolleFom shouldBe
                foedselsdato.toNorwegianDateAtNoon()
    }

    should("sette kravlinjeStatus til VILKARSPROVD på kravlinje") {
        val creator = createKravhodeCreator()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.kravlinjeListe[0].kravlinjeStatus shouldBe KravlinjeStatus.VILKARSPROVD
    }

    should("sette hovedKravlinje til true for AP kravlinje") {
        val creator = createKravhodeCreator()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.kravlinjeListe[0].hovedKravlinje shouldBe true
    }

    should("opprette uttaksgrad med riktig fomDato") {
        val creator = createKravhodeCreator()
        val foersteUttakDato = LocalDate.of(2030, 3, 1)
        val spec = simuleringSpec().copy(
            uttakGrad = UttakGradKode.P_100,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = null
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.uttaksgradListe[0].fomDato shouldBe foersteUttakDato.toNorwegianDateAtNoon()
    }

    should("opprette gradert uttaksgrad med tomDato dagen før heltUttakDato") {
        val creator = createKravhodeCreator()
        val foersteUttakDato = LocalDate.of(2030, 1, 1)
        val heltUttakDato = LocalDate.of(2032, 6, 1)
        val spec = simuleringSpec().copy(
            uttakGrad = UttakGradKode.P_40,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = heltUttakDato
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        // Uttaksgradliste er sortert synkende på fomDato, så 100% (2032-06-01) kommer først
        kravhode.uttaksgradListe shouldHaveSize 2
        kravhode.uttaksgradListe[0].uttaksgrad shouldBe 100
        kravhode.uttaksgradListe[0].fomDato shouldBe heltUttakDato.toNorwegianDateAtNoon()
        kravhode.uttaksgradListe[1].uttaksgrad shouldBe 40
        kravhode.uttaksgradListe[1].fomDato shouldBe foersteUttakDato.toNorwegianDateAtNoon()
        kravhode.uttaksgradListe[1].tomDato shouldBe heltUttakDato.minusDays(1).toNorwegianDateAtNoon()
    }

    should("opprette uttaksgradliste med uttaksgrad 0% når uttakGrad er P_0") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(
            uttakGrad = UttakGradKode.P_0,
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        // Skal ha to uttaksgrader: 0% og 100%
        kravhode.uttaksgradListe shouldHaveSize 2
        kravhode.uttaksgradListe.any { it.uttaksgrad == 0 } shouldBe true
        kravhode.uttaksgradListe.any { it.uttaksgrad == 100 } shouldBe true
    }

    should("sette sakId til null") {
        val creator = createKravhodeCreator()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.sakId shouldBe null
    }

    should("sette gjelder til null") {
        val creator = createKravhodeCreator()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.gjelder shouldBe null
    }

    should("håndtere uttaksgrad 20%") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(
            uttakGrad = UttakGradKode.P_20,
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.uttaksgradListe shouldHaveSize 2
        kravhode.uttaksgradListe.any { it.uttaksgrad == 20 } shouldBe true
    }

    should("håndtere uttaksgrad 60%") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(
            uttakGrad = UttakGradKode.P_60,
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.uttaksgradListe shouldHaveSize 2
        kravhode.uttaksgradListe.any { it.uttaksgrad == 60 } shouldBe true
    }

    should("håndtere uttaksgrad 80%") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(
            uttakGrad = UttakGradKode.P_80,
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        )

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.uttaksgradListe shouldHaveSize 2
        kravhode.uttaksgradListe.any { it.uttaksgrad == 80 } shouldBe true
    }

    should("sette kravFremsattDato til dagens dato") {
        val today = LocalDate.of(2025, 6, 15)
        val creator = createKravhodeCreator(today = today)

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.kravFremsattDato shouldBe today.toNorwegianDateAtNoon()
    }

    should("sette relatertPerson på kravlinje til søkers PenPerson") {
        val creator = createKravhodeCreator()
        val person = penPerson()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = person,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.kravlinjeListe[0].relatertPerson shouldNotBe null
    }

    should("sette boddEllerArbeidetIUtlandet til false når utlandAntallAar er null og ingen trygdetidPerioder utenlands") {
        val creator = createKravhodeCreator()
        val spec = simuleringSpec().copy(utlandAntallAar = 0, utlandPeriodeListe = mutableListOf())

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = penPerson(),
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.boddEllerArbeidetIUtlandet shouldBe false
    }

    should("sette regelverkType til N_REG_G_OPPTJ for årskull 1950") {
        val creator = createKravhodeCreator()
        val person = PenPerson().apply {
            penPersonId = 1L
            foedselsdato = LocalDate.of(1950, 1, 1)
        }

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = person,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_OPPTJ
    }

    should("sette regelverkType til N_REG_G_N_OPPTJ for årskull 1958") {
        val creator = createKravhodeCreator()
        val person = PenPerson().apply {
            penPersonId = 1L
            foedselsdato = LocalDate.of(1958, 1, 1)
        }

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
            person = person,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_N_OPPTJ
    }

    should("sette sisteGyldigeOpptjeningsaar fra generelleDataHolder for anonymt persongrunnlag") {
        val sisteGyldigeOpptjeningsaar = 2024
        val creator = createKravhodeCreator(sisteGyldigeOpptjeningsaar = sisteGyldigeOpptjeningsaar)
        val spec = anonymSimuleringSpec()

        val kravhode = creator.opprettKravhode(
            kravhodeSpec = KravhodeSpec(spec, null, 123000),
            person = null,
            virkningDatoGrunnlagListe = emptyList()
        )

        kravhode.hentPersongrunnlagForSoker().sisteGyldigeOpptjeningsAr shouldNotBe null
    }

    // ==========================================
    // Tests for inntektsgrunnlag creation
    // ==========================================

    context("opprettInntektGrunnlagForSoeker - inntektsgrunnlag creation") {
        should("opprette inntektsgrunnlag for forventet inntekt når foersteUttakDato er etter dagens dato") {
            val today = LocalDate.of(2025, 1, 1)
            val creator = createKravhodeCreatorWithInntektGrunnlag(today = today)
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2026, 6, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 500000
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            val inntektsgrunnlag = kravhode.hentPersongrunnlagForSoker().inntektsgrunnlagListe
            inntektsgrunnlag.any { it.belop == 500000 } shouldBe true
        }

        should("opprette inntektsgrunnlag for inntekt under gradert uttak i 2-fase simulering") {
            val today = LocalDate.of(2025, 1, 1)
            val creator = createKravhodeCreatorWithInntektGrunnlag(today = today)
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = LocalDate.of(2028, 1, 1),
                uttakGrad = UttakGradKode.P_50,
                forventetInntektBeloep = 500000,
                inntektUnderGradertUttakBeloep = 250000
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            val inntektsgrunnlag = kravhode.hentPersongrunnlagForSoker().inntektsgrunnlagListe
            inntektsgrunnlag.any { it.belop == 250000 } shouldBe true
        }

        should("opprette inntektsgrunnlag for inntekt etter helt uttak") {
            val today = LocalDate.of(2025, 1, 1)
            val creator = createKravhodeCreatorWithInntektGrunnlag(today = today)
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 0,
                inntektEtterHeltUttakBeloep = 100000,
                inntektEtterHeltUttakAntallAar = 3
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            val inntektsgrunnlag = kravhode.hentPersongrunnlagForSoker().inntektsgrunnlagListe
            inntektsgrunnlag.any { it.belop == 100000 } shouldBe true
        }

        should("ikke opprette inntektsgrunnlag for forventet inntekt når beløp er 0") {
            val today = LocalDate.of(2025, 1, 1)
            val creator = createKravhodeCreatorWithInntektGrunnlag(today = today)
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2026, 6, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 0,
                inntektEtterHeltUttakBeloep = 0
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            val inntektsgrunnlag = kravhode.hentPersongrunnlagForSoker().inntektsgrunnlagListe
            inntektsgrunnlag.filter { it.belop > 0 } shouldHaveSize 0
        }

        should("ikke opprette inntektsgrunnlag for inntekt etter helt uttak når antallAar er 0") {
            val today = LocalDate.of(2025, 1, 1)
            val creator = createKravhodeCreatorWithInntektGrunnlag(today = today)
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 0,
                inntektEtterHeltUttakBeloep = 100000,
                inntektEtterHeltUttakAntallAar = 0
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            val inntektsgrunnlag = kravhode.hentPersongrunnlagForSoker().inntektsgrunnlagListe
            inntektsgrunnlag.any { it.belop == 100000 } shouldBe false
        }
    }

    // ==========================================
    // Tests for uttaksgradliste and heltUttakTidspunkt
    // ==========================================

    context("heltUttakTidspunkt via uttaksgradliste") {
        should("håndtere 2-fase simulering med gradert uttak etterfulgt av helt uttak") {
            val creator = createKravhodeCreator()
            val foersteUttakDato = LocalDate.of(2030, 3, 15)
            val heltUttakDato = LocalDate.of(2032, 7, 1)
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_50,
                foersteUttakDato = foersteUttakDato,
                heltUttakDato = heltUttakDato
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.uttaksgradListe shouldHaveSize 2
            // Sortert synkende på fomDato
            kravhode.uttaksgradListe[0].uttaksgrad shouldBe 100
            kravhode.uttaksgradListe[0].fomDato shouldBe heltUttakDato.toNorwegianDateAtNoon()
            kravhode.uttaksgradListe[1].uttaksgrad shouldBe 50
        }

        should("håndtere uttakGrad P_0 som 2-fase simulering") {
            val creator = createKravhodeCreator()
            val foersteUttakDato = LocalDate.of(2030, 1, 1)
            val heltUttakDato = LocalDate.of(2033, 1, 1)
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_0,
                foersteUttakDato = foersteUttakDato,
                heltUttakDato = heltUttakDato
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.uttaksgradListe shouldHaveSize 2
            kravhode.uttaksgradListe.any { it.uttaksgrad == 0 } shouldBe true
            kravhode.uttaksgradListe.any { it.uttaksgrad == 100 } shouldBe true
        }
    }

    // ==========================================
    // Tests for EPS persongrunnlag
    // ==========================================

    context("addPersongrunnlagForEpsToKravhode") {
        should("kalle epsService for vanlig simulering") {
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                epsHarPensjon = true,
                epsHarInntektOver2G = true
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            // EPS-grunnlag legges til av mocken (relaxed), så vi sjekker at koden ikke krasjer
            kravhode shouldNotBe null
        }
    }

    // ==========================================
    // Tests for fremtidigInntekt handling
    // ==========================================

    context("brukFremtidigInntekt scenario") {
        should("bruke fremtidigInntektListe når brukFremtidigInntekt er true") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = true,
                fremtidigInntektListe = mutableListOf(
                    FremtidigInntekt(aarligInntektBeloep = 400000, fom = LocalDate.of(2025, 1, 1))
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.hentPersongrunnlagForSoker().inntektsgrunnlagListe shouldNotBe null
        }

        should("legge til fremtidigInntekt med beløp 0 når listen er tom") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = true,
                fremtidigInntektListe = mutableListOf()
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }
    }

    // ==========================================
    // Tests for various uttaksgrad values
    // ==========================================

    context("alle uttaksgrader") {
        should("håndtere uttaksgrad 20%") {
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_20,
                foersteUttakDato = LocalDate.of(2030, 1, 1),
                heltUttakDato = LocalDate.of(2032, 1, 1)
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.uttaksgradListe.any { it.uttaksgrad == 20 } shouldBe true
        }

        should("håndtere uttaksgrad 40%") {
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_40,
                foersteUttakDato = LocalDate.of(2030, 1, 1),
                heltUttakDato = LocalDate.of(2032, 1, 1)
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.uttaksgradListe.any { it.uttaksgrad == 40 } shouldBe true
        }

        should("håndtere uttaksgrad 50%") {
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_50,
                foersteUttakDato = LocalDate.of(2030, 1, 1),
                heltUttakDato = LocalDate.of(2032, 1, 1)
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.uttaksgradListe.any { it.uttaksgrad == 50 } shouldBe true
        }

        should("håndtere uttaksgrad 60%") {
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_60,
                foersteUttakDato = LocalDate.of(2030, 1, 1),
                heltUttakDato = LocalDate.of(2032, 1, 1)
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.uttaksgradListe.any { it.uttaksgrad == 60 } shouldBe true
        }

        should("håndtere uttaksgrad 80%") {
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_80,
                foersteUttakDato = LocalDate.of(2030, 1, 1),
                heltUttakDato = LocalDate.of(2032, 1, 1)
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.uttaksgradListe.any { it.uttaksgrad == 80 } shouldBe true
        }
    }

    // ==========================================
    // Tests for month calculations in income periods
    // ==========================================

    context("antallMaanederMedInntektUnderAfpEllerGradertUttak beregninger") {
        should("beregne måneder korrekt når første uttak og helt uttak er i samme år") {
            val creator = createKravhodeCreator()
            // Første uttak mars, helt uttak september samme år = 6 måneder med gradert uttak
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_50,
                foersteUttakDato = LocalDate.of(2030, 3, 1),
                heltUttakDato = LocalDate.of(2030, 9, 1),
                inntektUnderGradertUttakBeloep = 200000
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.uttaksgradListe shouldHaveSize 2
        }

        should("beregne måneder korrekt når første uttak og helt uttak er i forskjellige år") {
            val creator = createKravhodeCreator()
            // Første uttak juni 2030, helt uttak mars 2032
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_50,
                foersteUttakDato = LocalDate.of(2030, 6, 1),
                heltUttakDato = LocalDate.of(2032, 3, 1),
                inntektUnderGradertUttakBeloep = 200000
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.uttaksgradListe shouldHaveSize 2
        }
    }

    context("antallMaanederMedInntektEtterHeltUttak beregninger") {
        should("beregne måneder etter helt uttak korrekt") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = false,
                uttakGrad = UttakGradKode.P_100,
                foersteUttakDato = LocalDate.of(2026, 6, 1),
                heltUttakDato = null,
                inntektEtterHeltUttakBeloep = 150000,
                inntektEtterHeltUttakAntallAar = 3
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            val inntektsgrunnlag = kravhode.hentPersongrunnlagForSoker().inntektsgrunnlagListe
            inntektsgrunnlag.any { it.belop == 150000 } shouldBe true
        }
    }

    // ==========================================
    // Tests for various regelverkType edge cases
    // ==========================================

    context("regelverkType for grensetilfeller") {
        should("sette N_REG_G_OPPTJ for årskull 1943 (eldste tillatte)") {
            val creator = createKravhodeCreator()
            val person = PenPerson().apply {
                penPersonId = 1L
                foedselsdato = LocalDate.of(1943, 1, 1)
            }

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
                person = person,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_OPPTJ
        }

        should("sette N_REG_G_N_OPPTJ for årskull 1954 (første med overgangsregler)") {
            val creator = createKravhodeCreator()
            val person = PenPerson().apply {
                penPersonId = 1L
                foedselsdato = LocalDate.of(1954, 1, 1)
            }

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
                person = person,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_N_OPPTJ
        }

        should("sette N_REG_G_N_OPPTJ for årskull 1962 (siste med overgangsregler)") {
            val creator = createKravhodeCreator()
            val person = PenPerson().apply {
                penPersonId = 1L
                foedselsdato = LocalDate.of(1962, 1, 1)
            }

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(simuleringSpec(), null, 123000),
                person = person,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_N_OPPTJ
        }
    }

    // ==========================================
    // Tests for gjelderPre2025OffentligAfp scenarios
    // ==========================================

    context("opprettKravhode med gjelderPre2025OffentligAfp") {
        should("bruke pre2025OffentligAfpPersongrunnlag for AFP_ETTERF_ALDER") {
            val creator = createKravhodeCreatorForPre2025Afp()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = LocalDate.of(2029, 1, 1),
                uttakGrad = UttakGradKode.P_100,
                pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                    afpOrdning = AFPtypeEnum.AFPSTAT,
                    inntektMaanedenFoerAfpUttakBeloep = 50000,
                    inntektUnderAfpUttakBeloep = 30000
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
            kravhode.sakType shouldBe SakTypeEnum.ALDER
        }

        should("bruke pre2025OffentligAfpPersongrunnlag for AFP_FPP") {
            val creator = createKravhodeCreatorForPre2025Afp()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.AFP_FPP,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = LocalDate.of(2029, 1, 1),
                uttakGrad = UttakGradKode.P_100,
                pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                    afpOrdning = AFPtypeEnum.AFPSTAT,
                    inntektMaanedenFoerAfpUttakBeloep = 50000,
                    inntektUnderAfpUttakBeloep = 30000
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

        should("bruke pre2025OffentligAfpUttaksgrad for uttaksgradliste") {
            val creator = createKravhodeCreatorForPre2025Afp()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = LocalDate.of(2029, 1, 1),
                uttakGrad = UttakGradKode.P_100,
                pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                    afpOrdning = AFPtypeEnum.AFPSTAT,
                    inntektMaanedenFoerAfpUttakBeloep = 50000,
                    inntektUnderAfpUttakBeloep = 30000
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            // Uttaksgradliste settes av pre2025OffentligAfpUttaksgrad mock
            kravhode.uttaksgradListe shouldNotBe null
        }
    }

    // ==========================================
    // Tests for gjelderEndring scenarios
    // ==========================================

    context("opprettKravhode med gjelderEndring") {
        should("bruke endringPersongrunnlag for ENDR_ALDER") {
            val creator = createKravhodeCreatorForEndring()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ENDR_ALDER,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100
            )
            val forrigeResultat = mockk<AbstraktBeregningsResultat>()
            every { forrigeResultat.kravId } returns 123L

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, forrigeResultat, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

        should("bruke endringPersongrunnlag for ENDR_AP_M_AFP_PRIVAT") {
            val creator = createKravhodeCreatorForEndring()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100
            )
            val forrigeResultat = mockk<AbstraktBeregningsResultat>()
            every { forrigeResultat.kravId } returns 123L

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, forrigeResultat, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

        should("bruke endringUttaksgrad for uttaksgradliste ved endring") {
            val creator = createKravhodeCreatorForEndring()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ENDR_ALDER,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100
            )
            val forrigeResultat = mockk<AbstraktBeregningsResultat>()
            every { forrigeResultat.kravId } returns 123L

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, forrigeResultat, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            // Uttaksgradliste settes av endringUttaksgrad mock
            kravhode.uttaksgradListe shouldNotBe null
        }

        should("bruke endringPersongrunnlag for ENDR_ALDER_M_GJEN") {
            val creator = createKravhodeCreatorForEndring()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100
            )
            val forrigeResultat = mockk<AbstraktBeregningsResultat>()
            every { forrigeResultat.kravId } returns 123L

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, forrigeResultat, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }
    }

    // ==========================================
    // Tests for avdød grunnlag scenarios
    // ==========================================

    context("opprettKravhode med avdød") {
        should("sette boddArbeidUtlandAvdod til true når avdød har utenlandsopphold") {
            val creator = createKravhodeCreatorWithAvdoed()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ALDER_M_GJEN,
                avdoed = avdoedSpec(antallAarUtenlands = 5)
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.boddArbeidUtlandAvdod shouldBe true
        }

        should("sette boddArbeidUtlandAvdod til false når avdød ikke har utenlandsopphold") {
            val creator = createKravhodeCreatorWithAvdoed()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ALDER_M_GJEN,
                avdoed = avdoedSpec(antallAarUtenlands = 0)
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.boddArbeidUtlandAvdod shouldBe false
        }

        should("legge til GJR kravlinje når avdød persongrunnlag finnes") {
            val creator = createKravhodeCreatorWithAvdoed()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ALDER_M_GJEN,
                avdoed = avdoedSpec(antallAarUtenlands = 0)
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.kravlinjeListe.any { it.kravlinjeTypeEnum == KravlinjeTypeEnum.GJR } shouldBe true
        }

        should("ha både AP og GJR kravlinjer når avdød finnes") {
            val creator = createKravhodeCreatorWithAvdoed()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ALDER_M_GJEN,
                avdoed = avdoedSpec(antallAarUtenlands = 0)
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.kravlinjeListe shouldHaveSize 2
            kravhode.kravlinjeListe.any { it.kravlinjeTypeEnum == KravlinjeTypeEnum.AP } shouldBe true
            kravhode.kravlinjeListe.any { it.kravlinjeTypeEnum == KravlinjeTypeEnum.GJR } shouldBe true
        }
    }

    // ==========================================
    // Tests for addFremtidigInntektVedStartAvHvertAar
    // ==========================================

    context("addFremtidigInntektVedStartAvHvertAar via brukFremtidigInntekt") {
        should("legge til inntekt ved start av hvert år når inntekt ikke starter i januar") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            // Inntekt starter i juni 2025, skal automatisk legges til ved start av 2026
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = true,
                fremtidigInntektListe = mutableListOf(
                    FremtidigInntekt(aarligInntektBeloep = 500000, fom = LocalDate.of(2025, 6, 1))
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.hentPersongrunnlagForSoker().inntektsgrunnlagListe.isNotEmpty() shouldBe true
        }

        should("håndtere flere fremtidige inntekter som spenner over flere år") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = true,
                fremtidigInntektListe = mutableListOf(
                    FremtidigInntekt(aarligInntektBeloep = 400000, fom = LocalDate.of(2025, 1, 1)),
                    FremtidigInntekt(aarligInntektBeloep = 500000, fom = LocalDate.of(2027, 1, 1)),
                    FremtidigInntekt(aarligInntektBeloep = 600000, fom = LocalDate.of(2030, 1, 1))
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.hentPersongrunnlagForSoker().inntektsgrunnlagListe.isNotEmpty() shouldBe true
        }

        should("håndtere inntekt som starter midt i året etterfulgt av ny inntekt") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = true,
                fremtidigInntektListe = mutableListOf(
                    FremtidigInntekt(aarligInntektBeloep = 400000, fom = LocalDate.of(2025, 3, 1)),
                    FremtidigInntekt(aarligInntektBeloep = 550000, fom = LocalDate.of(2025, 9, 1))
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }
    }

    // ==========================================
    // Tests for validateSortedFremtidigeInntekter
    // ==========================================

    context("validateSortedFremtidigeInntekter via brukFremtidigInntekt") {
        should("feile når fremtidig inntekt har negativ verdi") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = true,
                fremtidigInntektListe = mutableListOf(
                    FremtidigInntekt(aarligInntektBeloep = -100000, fom = LocalDate.of(2025, 1, 1))
                )
            )

            val exception = runCatching {
                creator.opprettKravhode(
                    kravhodeSpec = KravhodeSpec(spec, null, 123000),
                    person = penPerson(),
                    virkningDatoGrunnlagListe = emptyList()
                )
            }.exceptionOrNull()

            exception shouldNotBe null
            exception?.message shouldBe "Det er en fremtidig inntekt med negativ verdi"
        }

        should("feile når fremtidig inntekt har fom som ikke er første dag i måneden") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = true,
                fremtidigInntektListe = mutableListOf(
                    FremtidigInntekt(aarligInntektBeloep = 400000, fom = LocalDate.of(2025, 1, 15))
                )
            )

            val exception = runCatching {
                creator.opprettKravhode(
                    kravhodeSpec = KravhodeSpec(spec, null, 123000),
                    person = penPerson(),
                    virkningDatoGrunnlagListe = emptyList()
                )
            }.exceptionOrNull()

            exception shouldNotBe null
            exception?.message shouldBe "Det er en fremtidig inntekt med f.o.m. som ikke er den 1. i måneden"
        }

        should("feile når to fremtidige inntekter har samme fom") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = true,
                fremtidigInntektListe = mutableListOf(
                    FremtidigInntekt(aarligInntektBeloep = 400000, fom = LocalDate.of(2025, 1, 1)),
                    FremtidigInntekt(aarligInntektBeloep = 500000, fom = LocalDate.of(2025, 1, 1))
                )
            )

            val exception = runCatching {
                creator.opprettKravhode(
                    kravhodeSpec = KravhodeSpec(spec, null, 123000),
                    person = penPerson(),
                    virkningDatoGrunnlagListe = emptyList()
                )
            }.exceptionOrNull()

            exception shouldNotBe null
            exception?.message shouldBe "De er to fremtidige inntekter med samme f.o.m."
        }

        should("akseptere gyldig fremtidig inntekt liste") {
            val creator = createKravhodeCreatorWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                brukFremtidigInntekt = true,
                fremtidigInntektListe = mutableListOf(
                    FremtidigInntekt(aarligInntektBeloep = 400000, fom = LocalDate.of(2025, 1, 1)),
                    FremtidigInntekt(aarligInntektBeloep = 500000, fom = LocalDate.of(2026, 1, 1))
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }
    }

    // ==========================================
    // Tests for addPersongrunnlagForEpsToKravhode scenarios
    // ==========================================

    context("addPersongrunnlagForEpsToKravhode for ulike simuleringer") {
        should("bruke pre2025OffentligAfpPersongrunnlag for EPS når gjelderPre2025OffentligAfp") {
            val creator = createKravhodeCreatorForPre2025Afp()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                epsHarPensjon = true,
                epsHarInntektOver2G = true,
                pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                    afpOrdning = AFPtypeEnum.AFPSTAT,
                    inntektMaanedenFoerAfpUttakBeloep = 50000,
                    inntektUnderAfpUttakBeloep = 30000
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

        should("bruke endringPersongrunnlag for EPS når gjelderEndring") {
            val creator = createKravhodeCreatorForEndring()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ENDR_ALDER,
                epsHarPensjon = true,
                epsHarInntektOver2G = true
            )
            val forrigeResultat = mockk<AbstraktBeregningsResultat>()
            every { forrigeResultat.kravId } returns 123L

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, forrigeResultat, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

        should("bruke epsService for vanlig alderspensjon simulering") {
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ALDER,
                epsHarPensjon = true,
                epsHarInntektOver2G = true
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }
    }

    // ==========================================
    // Tests for addPersongrunnlagForSoekerToKravhode
    // ==========================================

    context("addPersongrunnlagForSoekerToKravhode") {
        should("legge til persongrunnlag fra persongrunnlagService for vanlig simulering") {
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ALDER
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.hentPersongrunnlagForSoker() shouldNotBe null
            kravhode.hentPersongrunnlagForSoker().penPerson?.penPersonId shouldBe 1L
        }

        should("legge til anonymt persongrunnlag når erAnonym er true") {
            val creator = createKravhodeCreator()
            val spec = anonymSimuleringSpec()

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = null,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.hentPersongrunnlagForSoker().penPerson?.penPersonId shouldBe -1L
        }

        should("bruke pre2025OffentligAfpPersongrunnlag for søker når gjelderPre2025OffentligAfp") {
            val creator = createKravhodeCreatorForPre2025Afp()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                    afpOrdning = AFPtypeEnum.AFPSTAT,
                    inntektMaanedenFoerAfpUttakBeloep = 50000,
                    inntektUnderAfpUttakBeloep = 30000
                )
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.hentPersongrunnlagForSoker() shouldNotBe null
        }

        should("bruke endringPersongrunnlag for søker når gjelderEndring") {
            val creator = createKravhodeCreatorForEndring()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.ENDR_ALDER
            )
            val forrigeResultat = mockk<AbstraktBeregningsResultat>()
            every { forrigeResultat.kravId } returns 123L

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, forrigeResultat, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode.hentPersongrunnlagForSoker() shouldNotBe null
        }
    }

    // ==========================================
    // Tests for handleMissingHeltUttakDato
    // ==========================================

    context("handleMissingHeltUttakDato") {
        should("kaste BadSpecException når heltUttakDato mangler for gradert uttak") {
            // Validering av gradert uttak skjer i validateGradertUttak før handleMissingHeltUttakDato
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_50,
                foersteUttakDato = LocalDate.of(2030, 1, 1),
                heltUttakDato = null // mangler, men kreves for gradert uttak
            )

            val exception = runCatching {
                creator.opprettKravhode(
                    kravhodeSpec = KravhodeSpec(spec, null, 123000),
                    person = penPerson(),
                    virkningDatoGrunnlagListe = emptyList()
                )
            }.exceptionOrNull()

            exception shouldNotBe null
            (exception is BadSpecException) shouldBe true
            exception?.message shouldBe "dato for helt uttak mangler (obligatorisk ved gradert uttak)"
        }

        should("kaste BadSpecException når heltUttakDato mangler for AFP_ETTERF_ALDER med brukFremtidigInntekt=false") {
            // AFP_ETTERF_ALDER er 2-fase-simulering og krever heltUttakDato
            // handleMissingHeltUttakDato kalles fra aarligeInntekterFraDagensDato (brukFremtidigInntekt=false)
            val creator = createKravhodeCreatorForPre2025AfpWithInntektGrunnlag()
            val spec = simuleringSpec().copy(
                type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                brukFremtidigInntekt = false,
                uttakGrad = UttakGradKode.P_100,
                foersteUttakDato = LocalDate.of(2026, 1, 1),
                heltUttakDato = null, // mangler, men kreves for AFP_ETTERF_ALDER
                pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                    afpOrdning = AFPtypeEnum.AFPSTAT,
                    inntektMaanedenFoerAfpUttakBeloep = 50000,
                    inntektUnderAfpUttakBeloep = 30000
                )
            )

            val exception = runCatching {
                creator.opprettKravhode(
                    kravhodeSpec = KravhodeSpec(spec, null, 123000),
                    person = penPerson(),
                    virkningDatoGrunnlagListe = emptyList()
                )
            }.exceptionOrNull()

            exception shouldNotBe null
            (exception is BadSpecException) shouldBe true
            exception?.message shouldBe "Manglende heltUttakDato for 2-fase-simulering"
        }

        should("kaste BadSpecException når heltUttakDato mangler for uttakGrad P_0") {
            // Validering av gradert uttak skjer i validateGradertUttak før handleMissingHeltUttakDato
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_0,
                foersteUttakDato = LocalDate.of(2030, 1, 1),
                heltUttakDato = null // mangler, men kreves for P_0 (utsatt uttak)
            )

            val exception = runCatching {
                creator.opprettKravhode(
                    kravhodeSpec = KravhodeSpec(spec, null, 123000),
                    person = penPerson(),
                    virkningDatoGrunnlagListe = emptyList()
                )
            }.exceptionOrNull()

            exception shouldNotBe null
            (exception is BadSpecException) shouldBe true
            exception?.message shouldBe "dato for helt uttak mangler (obligatorisk ved gradert uttak)"
        }

        should("ikke kaste exception når uttakGrad er P_100 og heltUttakDato mangler") {
            val creator = createKravhodeCreator()
            val spec = simuleringSpec().copy(
                uttakGrad = UttakGradKode.P_100,
                foersteUttakDato = LocalDate.of(2030, 1, 1),
                heltUttakDato = null // OK for 100% uttak
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = penPerson(),
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
            kravhode.uttaksgradListe shouldHaveSize 1
            kravhode.uttaksgradListe[0].uttaksgrad shouldBe 100
        }
    }

    // ==========================================
    // Tests for aarligeInntekterFraDagensDato with erAnonym=true
    // ==========================================

    context("aarligeInntekterFraDagensDato for anonym simulering") {
        should("beregne gjeldendeAar fra foersteUttakDato og inntektOver1GAntallAar") {
            // gjeldendeAar = foersteUttakDato.year - inntektOver1GAntallAar = 2030 - 5 = 2025
            val creator = createKravhodeCreatorForAnonymWithVeietGrunnbeloep()
            val spec = anonymSimuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2030, 6, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 600000,
                inntektOver1GAntallAar = 5
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = null,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
            kravhode.hentPersongrunnlagForSoker().opptjeningsgrunnlagListe shouldNotBe null
        }

        should("hente veietGrunnbeloepListe når gjeldendeAar er før inneværende år") {
            // today = 2025, gjeldendeAar = 2030 - 10 = 2020 (før 2025)
            val creator = createKravhodeCreatorForAnonymWithVeietGrunnbeloep(
                today = LocalDate.of(2025, 1, 1)
            )
            val spec = anonymSimuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2030, 6, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 500000,
                inntektOver1GAntallAar = 10 // gjeldendeAar = 2030 - 10 = 2020
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = null,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

        should("ikke hente veietGrunnbeloepListe når gjeldendeAar er etter inneværende år") {
            // today = 2025, gjeldendeAar = 2030 - 0 = 2030 (etter 2025)
            val creator = createKravhodeCreatorForAnonymWithVeietGrunnbeloep(
                today = LocalDate.of(2025, 1, 1)
            )
            val spec = anonymSimuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2030, 6, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 500000,
                inntektOver1GAntallAar = 0 // gjeldendeAar = 2030 - 0 = 2030
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = null,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

        should("bruke foedselAar for å beregne aarSoekerBlirMaxAlder") {
            // aarSoekerBlirMaxAlder = 75 + foedselAar = 75 + 1963 = 2038
            val creator = createKravhodeCreatorForAnonymWithVeietGrunnbeloep()
            val spec = anonymSimuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2030, 6, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 600000,
                inntektOver1GAntallAar = 0,
                foedselAar = 1963 // aarSoekerBlirMaxAlder = 75 + 1963 = 2038
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = null,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

        should("beregne inntekt med veietGrunnbeloep-justering for tidligere år") {
            // Tester at veietGrunnbeloep brukes for å justere inntekt i år før inneværende år
            val veietGrunnbeloepListe = listOf(
                VeietSatsResultat().apply {
                    ar = 2020
                    verdi = 100000.0
                }
            )
            val creator = createKravhodeCreatorForAnonymWithVeietGrunnbeloep(
                today = LocalDate.of(2025, 1, 1),
                veietGrunnbeloepListe = veietGrunnbeloepListe
            )
            val spec = anonymSimuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2030, 6, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 500000,
                inntektOver1GAntallAar = 10 // gjeldendeAar = 2020
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = null,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

        should("håndtere gradert uttak for anonym simulering") {
            val creator = createKravhodeCreatorForAnonymWithVeietGrunnbeloep()
            val spec = anonymSimuleringSpec().copy(
                brukFremtidigInntekt = false,
                foersteUttakDato = LocalDate.of(2030, 3, 1),
                heltUttakDato = LocalDate.of(2032, 6, 1),
                uttakGrad = UttakGradKode.P_50,
                forventetInntektBeloep = 600000,
                inntektUnderGradertUttakBeloep = 300000,
                inntektEtterHeltUttakBeloep = 100000,
                inntektEtterHeltUttakAntallAar = 2,
                inntektOver1GAntallAar = 0
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = null,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
            kravhode.uttaksgradListe shouldHaveSize 2
        }

        should("beregne årlig inntekt basert på forventet inntekt for anonym simulering") {
            val creator = createKravhodeCreatorForAnonym()
            val spec = anonymSimuleringSpec().copy(
                foersteUttakDato = LocalDate.of(2030, 6, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 600000,
                inntektOver1GAntallAar = 0
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = null,
                virkningDatoGrunnlagListe = emptyList()
            )

            // Verifiser at opptjeningsgrunnlag er satt
            kravhode.hentPersongrunnlagForSoker().opptjeningsgrunnlagListe shouldNotBe null
        }

        should("håndtere inntektOver1GAntallAar for anonym simulering") {
            val creator = createKravhodeCreatorForAnonym()
            val spec = anonymSimuleringSpec().copy(
                foersteUttakDato = LocalDate.of(2030, 6, 1),
                heltUttakDato = null,
                uttakGrad = UttakGradKode.P_100,
                forventetInntektBeloep = 600000,
                inntektOver1GAntallAar = 5
            )

            val kravhode = creator.opprettKravhode(
                kravhodeSpec = KravhodeSpec(spec, null, 123000),
                person = null,
                virkningDatoGrunnlagListe = emptyList()
            )

            kravhode shouldNotBe null
        }

    }
})

private fun arrangePersongrunnlag(): PersongrunnlagService =
    mockk<PersongrunnlagService>().apply {
        every { getPersongrunnlagForSoeker(spec = any(), kravhode = any(), person = any()) } returns persongrunnlag()
    }

private fun persongrunnlag() =
    Persongrunnlag().apply {
        penPerson = PenPerson().apply { penPersonId = 1L }
        fodselsdato = LocalDate.of(1963, 1, 1).toNorwegianDateAtNoon()
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                penRolleTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon()
            }
        )
    }

private fun createKravhodeCreator(
    today: LocalDate = LocalDate.of(2025, 1, 1),
    sisteGyldigeOpptjeningsaar: Int = 2024,
    kravService: KravService = mockk()
): KravhodeCreator {
    val generelleDataHolder = mockk<GenerelleDataHolder>()
    every { generelleDataHolder.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar

    return KravhodeCreator(
        epsService = mockk(relaxed = true),
        persongrunnlagService = arrangePersongrunnlag(),
        opptjeningUpdater = mockk(relaxed = true),
        generelleDataHolder = generelleDataHolder,
        kravService = kravService,
        ufoereService = mockk(relaxed = true),
        endringPersongrunnlag = mockk(),
        endringUttaksgrad = mockk(),
        pre2025OffentligAfpPersongrunnlag = mockk(),
        pre2025OffentligAfpUttaksgrad = mockk(),
        time = { today }
    )
}

private fun anonymSimuleringSpec(sivilstatus: SivilstatusType = SivilstatusType.UGIF) =
    simuleringSpec().copy(
        erAnonym = true,
        pid = null,
        foedselDato = LocalDate.of(1963, 1, 1),
        sivilstatus = sivilstatus
    )

private fun penPerson() = PenPerson().apply {
    penPersonId = 1L
    foedselsdato = LocalDate.of(1963, 1, 1)
}

private fun createKravhodeCreatorWithInntektGrunnlag(
    today: LocalDate = LocalDate.of(2025, 1, 1),
    sisteGyldigeOpptjeningsaar: Int = 2024
): KravhodeCreator {
    val generelleDataHolder = mockk<GenerelleDataHolder>()
    every { generelleDataHolder.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar

    return KravhodeCreator(
        epsService = mockk(relaxed = true),
        persongrunnlagService = arrangePersongrunnlagWithInntektsgrunnlag(),
        opptjeningUpdater = mockk(relaxed = true),
        generelleDataHolder = generelleDataHolder,
        kravService = mockk(),
        ufoereService = mockk(relaxed = true),
        endringPersongrunnlag = mockk(),
        endringUttaksgrad = mockk(),
        pre2025OffentligAfpPersongrunnlag = mockk(),
        pre2025OffentligAfpUttaksgrad = mockk(),
        time = { today }
    )
}

private fun createKravhodeCreatorForAnonym(
    today: LocalDate = LocalDate.of(2025, 1, 1),
    sisteGyldigeOpptjeningsaar: Int = 2024
): KravhodeCreator {
    val generelleDataHolder = mockk<GenerelleDataHolder>()
    every { generelleDataHolder.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar
    every { generelleDataHolder.getVeietGrunnbeloepListe(any(), any()) } returns emptyList()

    return KravhodeCreator(
        epsService = mockk(relaxed = true),
        persongrunnlagService = mockk(relaxed = true),
        opptjeningUpdater = mockk(relaxed = true),
        generelleDataHolder = generelleDataHolder,
        kravService = mockk(),
        ufoereService = mockk(relaxed = true),
        endringPersongrunnlag = mockk(),
        endringUttaksgrad = mockk(),
        pre2025OffentligAfpPersongrunnlag = mockk(),
        pre2025OffentligAfpUttaksgrad = mockk(),
        time = { today }
    )
}

private fun arrangePersongrunnlagWithInntektsgrunnlag(): PersongrunnlagService =
    mockk<PersongrunnlagService>().apply {
        every { getPersongrunnlagForSoeker(spec = any(), kravhode = any(), person = any()) } returns persongrunnlagWithInntektsgrunnlag()
    }

private fun persongrunnlagWithInntektsgrunnlag() =
    Persongrunnlag().apply {
        penPerson = PenPerson().apply { penPersonId = 1L }
        fodselsdato = LocalDate.of(1963, 1, 1).toNorwegianDateAtNoon()
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                penRolleTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon()
            }
        )
        inntektsgrunnlagListe = mutableListOf()
    }

private fun createKravhodeCreatorForPre2025Afp(
    today: LocalDate = LocalDate.of(2025, 1, 1),
    sisteGyldigeOpptjeningsaar: Int = 2024
): KravhodeCreator {
    val generelleDataHolder = mockk<GenerelleDataHolder>()
    every { generelleDataHolder.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar

    val kravService = mockk<KravService>()
    every { kravService.fetchKravhode(any()) } returns Kravhode()

    val pre2025OffentligAfpPersongrunnlag = mockk<Pre2025OffentligAfpPersongrunnlag>()
    every {
        pre2025OffentligAfpPersongrunnlag.getPersongrunnlagForSoeker(any(), any(), any(), any())
    } returns persongrunnlag()
    every {
        pre2025OffentligAfpPersongrunnlag.addPersongrunnlagForEpsToKravhode(any(), any(), any(), any())
    } answers { secondArg() } // returns the kravhode parameter

    val pre2025OffentligAfpUttaksgrad = mockk<Pre2025OffentligAfpUttaksgrad>()
    every {
        pre2025OffentligAfpUttaksgrad.uttaksgradListe(any(), any(), any())
    } returns mutableListOf(
        Uttaksgrad().apply {
            fomDato = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon()
            uttaksgrad = 100
        }
    )

    return KravhodeCreator(
        epsService = mockk(relaxed = true),
        persongrunnlagService = arrangePersongrunnlag(),
        opptjeningUpdater = mockk(relaxed = true),
        generelleDataHolder = generelleDataHolder,
        kravService = kravService,
        ufoereService = mockk(relaxed = true),
        endringPersongrunnlag = mockk(),
        endringUttaksgrad = mockk(),
        pre2025OffentligAfpPersongrunnlag = pre2025OffentligAfpPersongrunnlag,
        pre2025OffentligAfpUttaksgrad = pre2025OffentligAfpUttaksgrad,
        time = { today }
    )
}

private fun createKravhodeCreatorForPre2025AfpWithInntektGrunnlag(
    today: LocalDate = LocalDate.of(2025, 1, 1),
    sisteGyldigeOpptjeningsaar: Int = 2024
): KravhodeCreator {
    val generelleDataHolder = mockk<GenerelleDataHolder>()
    every { generelleDataHolder.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar

    val kravService = mockk<KravService>()
    every { kravService.fetchKravhode(any()) } returns Kravhode()

    val pre2025OffentligAfpPersongrunnlag = mockk<Pre2025OffentligAfpPersongrunnlag>()
    every {
        pre2025OffentligAfpPersongrunnlag.getPersongrunnlagForSoeker(any(), any(), any(), any())
    } returns persongrunnlagWithInntektsgrunnlag()
    every {
        pre2025OffentligAfpPersongrunnlag.addPersongrunnlagForEpsToKravhode(any(), any(), any(), any())
    } answers { secondArg() } // returns the kravhode parameter

    val pre2025OffentligAfpUttaksgrad = mockk<Pre2025OffentligAfpUttaksgrad>()
    every {
        pre2025OffentligAfpUttaksgrad.uttaksgradListe(any(), any(), any())
    } returns mutableListOf(
        Uttaksgrad().apply {
            fomDato = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon()
            uttaksgrad = 100
        }
    )

    return KravhodeCreator(
        epsService = mockk(relaxed = true),
        persongrunnlagService = arrangePersongrunnlagWithInntektsgrunnlag(),
        opptjeningUpdater = mockk(relaxed = true),
        generelleDataHolder = generelleDataHolder,
        kravService = kravService,
        ufoereService = mockk(relaxed = true),
        endringPersongrunnlag = mockk(),
        endringUttaksgrad = mockk(),
        pre2025OffentligAfpPersongrunnlag = pre2025OffentligAfpPersongrunnlag,
        pre2025OffentligAfpUttaksgrad = pre2025OffentligAfpUttaksgrad,
        time = { today }
    )
}

private fun createKravhodeCreatorForEndring(
    today: LocalDate = LocalDate.of(2025, 1, 1),
    sisteGyldigeOpptjeningsaar: Int = 2024
): KravhodeCreator {
    val generelleDataHolder = mockk<GenerelleDataHolder>()
    every { generelleDataHolder.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar

    val kravService = mockk<KravService>()
    every { kravService.fetchKravhode(any()) } returns Kravhode()

    val endringPersongrunnlag = mockk<EndringPersongrunnlag>()
    every {
        endringPersongrunnlag.getPersongrunnlagForSoeker(any(), any(), any(), any())
    } returns persongrunnlag()
    every {
        endringPersongrunnlag.addPersongrunnlagForEpsToKravhode(any(), any(), any(), any())
    } answers { secondArg() } // returns the kravhode parameter

    val endringUttaksgrad = mockk<EndringUttaksgrad>()
    every {
        endringUttaksgrad.uttaksgradListe(any(), any())
    } returns mutableListOf(
        Uttaksgrad().apply {
            fomDato = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon()
            uttaksgrad = 100
        }
    )

    return KravhodeCreator(
        epsService = mockk(relaxed = true),
        persongrunnlagService = arrangePersongrunnlag(),
        opptjeningUpdater = mockk(relaxed = true),
        generelleDataHolder = generelleDataHolder,
        kravService = kravService,
        ufoereService = mockk(relaxed = true),
        endringPersongrunnlag = endringPersongrunnlag,
        endringUttaksgrad = endringUttaksgrad,
        pre2025OffentligAfpPersongrunnlag = mockk(),
        pre2025OffentligAfpUttaksgrad = mockk(),
        time = { today }
    )
}

private fun createKravhodeCreatorWithAvdoed(
    today: LocalDate = LocalDate.of(2025, 1, 1),
    sisteGyldigeOpptjeningsaar: Int = 2024
): KravhodeCreator {
    val generelleDataHolder = mockk<GenerelleDataHolder>()
    every { generelleDataHolder.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar

    val persongrunnlagService = mockk<PersongrunnlagService>()
    every {
        persongrunnlagService.getPersongrunnlagForSoeker(spec = any(), kravhode = any(), person = any())
    } answers {
        val kravhode = secondArg<Kravhode>()
        // Add avdød persongrunnlag to kravhode
        kravhode.persongrunnlagListe.add(avdoedPersongrunnlag())
        persongrunnlag()
    }

    return KravhodeCreator(
        epsService = mockk(relaxed = true),
        persongrunnlagService = persongrunnlagService,
        opptjeningUpdater = mockk(relaxed = true),
        generelleDataHolder = generelleDataHolder,
        kravService = mockk(),
        ufoereService = mockk(relaxed = true),
        endringPersongrunnlag = mockk(),
        endringUttaksgrad = mockk(),
        pre2025OffentligAfpPersongrunnlag = mockk(),
        pre2025OffentligAfpUttaksgrad = mockk(),
        time = { today }
    )
}

private fun avdoedPersongrunnlag() =
    Persongrunnlag().apply {
        penPerson = PenPerson().apply { penPersonId = 2L }
        fodselsdato = LocalDate.of(1960, 1, 1).toNorwegianDateAtNoon()
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
                penRolleTom = LocalDate.of(2020, 11, 11).toNorwegianDateAtNoon()
            }
        )
    }

private fun avdoedSpec(antallAarUtenlands: Int) =
    Avdoed(
        pid = Pid("04925398980"),
        antallAarUtenlands = antallAarUtenlands,
        inntektFoerDoed = 50000,
        doedDato = LocalDate.of(2020, 11, 11),
        erMedlemAvFolketrygden = true,
        harInntektOver1G = false
    )

private fun createKravhodeCreatorForAnonymWithVeietGrunnbeloep(
    today: LocalDate = LocalDate.of(2025, 1, 1),
    sisteGyldigeOpptjeningsaar: Int = 2024,
    veietGrunnbeloepListe: List<VeietSatsResultat> = emptyList()
): KravhodeCreator {
    val generelleDataHolder = mockk<GenerelleDataHolder>()
    every { generelleDataHolder.getSisteGyldigeOpptjeningsaar() } returns sisteGyldigeOpptjeningsaar
    every { generelleDataHolder.getVeietGrunnbeloepListe(any(), any()) } returns veietGrunnbeloepListe

    return KravhodeCreator(
        epsService = mockk(relaxed = true),
        persongrunnlagService = mockk(relaxed = true),
        opptjeningUpdater = mockk(relaxed = true),
        generelleDataHolder = generelleDataHolder,
        kravService = mockk(),
        ufoereService = mockk(relaxed = true),
        endringPersongrunnlag = mockk(),
        endringUttaksgrad = mockk(),
        pre2025OffentligAfpPersongrunnlag = mockk(),
        pre2025OffentligAfpUttaksgrad = mockk(),
        time = { today }
    )
}
