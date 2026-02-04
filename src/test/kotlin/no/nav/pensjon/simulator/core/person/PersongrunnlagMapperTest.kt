package no.nav.pensjon.simulator.core.person

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GenerellHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.generelt.Person
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

class PersongrunnlagMapperTest : FunSpec({

    // ===========================================
    // Tests for mapToPersongrunnlag - basic fields
    // ===========================================

    test("mapToPersongrunnlag should set penPerson from input") {
        val mapper = createMapper()
        val person = PenPerson().apply {
            penPersonId = 123L
            foedselsdato = LocalDate.of(1963, 1, 1)
        }

        val result = mapper.mapToPersongrunnlag(person, simuleringSpec)

        result.penPerson shouldBe person
    }

    test("mapToPersongrunnlag should set fodselsdato from person") {
        val mapper = createMapper()
        val person = PenPerson().apply {
            foedselsdato = LocalDate.of(1965, 5, 15)
        }

        val result = mapper.mapToPersongrunnlag(person, simuleringSpec)

        result.fodselsdato shouldNotBe null
    }

    test("mapToPersongrunnlag should set gjelderOmsorg to false") {
        val mapper = createMapper()

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.gjelderOmsorg shouldBe false
    }

    test("mapToPersongrunnlag should set gjelderUforetrygd to false") {
        val mapper = createMapper()

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.gjelderUforetrygd shouldBe false
    }

    test("mapToPersongrunnlag should set bosattLandEnum to NOR") {
        val mapper = createMapper()

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.bosattLandEnum shouldBe LandkodeEnum.NOR
    }

    test("mapToPersongrunnlag should set statsborgerskapEnum from generelleDataHolder") {
        val pid = Pid("12345678901")
        val generelleDataHolder = mockk<GenerelleDataHolder>(relaxed = true) {
            every { getPerson(pid) } returns Person(statsborgerskap = LandkodeEnum.SWE)
        }
        val mapper = PersongrunnlagMapper(
            generelleDataHolder = generelleDataHolder,
            personService = mockk(),
            time = { LocalDate.of(2025, 1, 15) }
        )
        val person = PenPerson().apply { this.pid = pid }

        val result = mapper.mapToPersongrunnlag(person, simuleringSpec)

        result.statsborgerskapEnum shouldBe LandkodeEnum.SWE
    }

    test("mapToPersongrunnlag should set over60ArKanIkkeForsorgesSelv to false") {
        val mapper = createMapper()

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.over60ArKanIkkeForsorgesSelv shouldBe false
    }

    test("mapToPersongrunnlag should set sisteGyldigeOpptjeningsAr from generelleDataHolder") {
        val generelleDataHolder = mockk<GenerelleDataHolder>(relaxed = true) {
            every { getSisteGyldigeOpptjeningsaar() } returns 2022
        }
        val mapper = PersongrunnlagMapper(
            generelleDataHolder = generelleDataHolder,
            personService = mockk(),
            time = { LocalDate.of(2025, 1, 15) }
        )

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.sisteGyldigeOpptjeningsAr shouldBe 2022
    }

    test("mapToPersongrunnlag should set inngangOgEksportGrunnlag with fortsattMedlemFT true") {
        val mapper = createMapper()

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.inngangOgEksportGrunnlag shouldNotBe null
        result.inngangOgEksportGrunnlag?.fortsattMedlemFT shouldBe true
    }

    test("mapToPersongrunnlag should set medlemIFolketrygdenSiste3Ar to true") {
        val mapper = createMapper()

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.medlemIFolketrygdenSiste3Ar shouldBe true
    }

    test("mapToPersongrunnlag should set flyktning from spec") {
        val mapper = createMapper()
        val spec = createSimuleringSpec(flyktning = true)

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.flyktning shouldBe true
    }

    // ===========================================
    // Tests for mapToPersongrunnlag - AFP historikk
    // ===========================================

    test("mapToPersongrunnlag should take only first AFP historikk item") {
        val mapper = createMapper()
        val person = PenPerson().apply {
            afpHistorikkListe = mutableListOf(
                AfpHistorikk().apply { afpPensjonsgrad = 50 },
                AfpHistorikk().apply { afpPensjonsgrad = 100 }
            )
        }

        val result = mapper.mapToPersongrunnlag(person, simuleringSpec)

        result.afpHistorikkListe shouldHaveSize 1
        result.afpHistorikkListe[0].afpPensjonsgrad shouldBe 50
    }

    test("mapToPersongrunnlag should handle empty AFP historikk") {
        val mapper = createMapper()
        val person = PenPerson().apply {
            afpHistorikkListe = mutableListOf()
        }

        val result = mapper.mapToPersongrunnlag(person, simuleringSpec)

        result.afpHistorikkListe.shouldBeEmpty()
    }

    test("mapToPersongrunnlag should handle null AFP historikk") {
        val mapper = createMapper()
        val person = PenPerson().apply {
            afpHistorikkListe = null
        }

        val result = mapper.mapToPersongrunnlag(person, simuleringSpec)

        result.afpHistorikkListe.shouldBeEmpty()
    }

    // ===========================================
    // Tests for mapToPersongrunnlag - historikk from person
    // ===========================================

    test("mapToPersongrunnlag should set uforeHistorikk from person") {
        val mapper = createMapper()
        val uforehistorikk = Uforehistorikk()
        val person = PenPerson().apply {
            this.uforehistorikk = uforehistorikk
        }

        val result = mapper.mapToPersongrunnlag(person, simuleringSpec)

        result.uforeHistorikk shouldBe uforehistorikk
    }

    test("mapToPersongrunnlag should set generellHistorikk from person") {
        val mapper = createMapper()
        val generellHistorikk = GenerellHistorikk()
        val person = PenPerson().apply {
            this.generellHistorikk = generellHistorikk
        }

        val result = mapper.mapToPersongrunnlag(person, simuleringSpec)

        result.generellHistorikk shouldBe generellHistorikk
    }

    // ===========================================
    // Tests for mapToPersongrunnlag - utland
    // ===========================================

    test("mapToPersongrunnlag should set antallArUtland from spec") {
        val mapper = createMapper()
        val spec = createSimuleringSpec(utlandAntallAar = 5)

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.antallArUtland shouldBe 5
    }

    test("mapToPersongrunnlag should use utlandPeriodeListe to calculate antallArUtland") {
        val mapper = createMapper()
        val spec = simuleringSpec(
            utlandAntallAar = 0,
            utlandPeriodeListe = listOf(
                UtlandPeriode(
                    fom = LocalDate.of(2020, 1, 1),
                    tom = LocalDate.of(2022, 12, 31),
                    land = LandkodeEnum.SWE,
                    arbeidet = false
                )
            ),
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.antallArUtland shouldBe 3
    }

    // ===========================================
    // Tests for mapToPersongrunnlag - PersonDetalj
    // ===========================================

    test("mapToPersongrunnlag should add PersonDetalj with grunnlagsrolle SOKER") {
        val mapper = createMapper()

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.personDetaljListe shouldHaveSize 1
        result.personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
    }

    test("mapToPersongrunnlag should set bruk to true on PersonDetalj") {
        val mapper = createMapper()

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.personDetaljListe[0].bruk shouldBe true
    }

    test("mapToPersongrunnlag should set grunnlagKildeEnum to BRUKER on PersonDetalj") {
        val mapper = createMapper()

        val result = mapper.mapToPersongrunnlag(PenPerson(), simuleringSpec)

        result.personDetaljListe[0].grunnlagKildeEnum shouldBe GrunnlagkildeEnum.BRUKER
    }

    // ===========================================
    // Tests for mapToPersongrunnlag - sivilstand mapping
    // ===========================================

    test("mapToPersongrunnlag should map sivilstatus GIFT to sivilstand GIFT") {
        val mapper = createMapper()
        val spec = createSimuleringSpec(sivilstatus = SivilstatusType.GIFT)

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.GIFT
    }

    test("mapToPersongrunnlag should map sivilstatus REPA to sivilstand REPA") {
        val mapper = createMapper()
        val spec = createSimuleringSpec(sivilstatus = SivilstatusType.REPA)

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.REPA
    }

    test("mapToPersongrunnlag should map sivilstatus SAMB to sivilstand UGIF") {
        val mapper = createMapper()
        val spec = createSimuleringSpec(sivilstatus = SivilstatusType.SAMB)

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.UGIF
    }

    test("mapToPersongrunnlag should map sivilstatus UGIF to sivilstand UGIF") {
        val mapper = createMapper()
        val spec = createSimuleringSpec(sivilstatus = SivilstatusType.UGIF)

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.UGIF
    }

    test("mapToPersongrunnlag should set sivilstand to ENKE for ALDER_M_GJEN") {
        val mapper = createMapper()
        val spec = createSimuleringSpec(type = SimuleringTypeEnum.ALDER_M_GJEN)

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.ENKE
    }

    test("mapToPersongrunnlag should set sivilstand to ENKE for ENDR_ALDER_M_GJEN") {
        val mapper = createMapper()
        val spec = createSimuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN)

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.personDetaljListe[0].sivilstandTypeEnum shouldBe SivilstandEnum.ENKE
    }

    // ===========================================
    // Tests for mapToPersongrunnlag - rolleFom
    // ===========================================

    test("mapToPersongrunnlag should set penRolleFom to today for non-AFP_FPP types") {
        val today = LocalDate.of(2025, 6, 15)
        val mapper = PersongrunnlagMapper(
            generelleDataHolder = mockk(relaxed = true),
            personService = mockk(),
            time = { today }
        )
        val spec = createSimuleringSpec(type = SimuleringTypeEnum.ALDER)

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        result.personDetaljListe[0].penRolleFom shouldNotBe null
    }

    test("mapToPersongrunnlag should set penRolleFom to last day of previous month for AFP_FPP") {
        val mapper = createMapper()
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.AFP_FPP,
            foersteUttakDato = LocalDate.of(2025, 6, 15)
        )

        val result = mapper.mapToPersongrunnlag(PenPerson(), spec)

        // For AFP_FPP, rolleFom should be last day of previous month (May 31, 2025)
        result.personDetaljListe[0].penRolleFom shouldNotBe null
    }

    // ===========================================
    // Tests for mapToEpsPersongrunnlag
    // ===========================================

    test("mapToEpsPersongrunnlag should set penPersonId to -2") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.penPerson?.penPersonId shouldBe -2L
    }

    test("mapToEpsPersongrunnlag should set fodselsdato") {
        val mapper = createMapper()
        val foedselsdato = LocalDate.of(1965, 3, 20)

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, foedselsdato)

        result.fodselsdato shouldNotBe null
    }

    test("mapToEpsPersongrunnlag should set gjelderOmsorg to false") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.gjelderOmsorg shouldBe false
    }

    test("mapToEpsPersongrunnlag should set gjelderUforetrygd to false") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.gjelderUforetrygd shouldBe false
    }

    test("mapToEpsPersongrunnlag should set antallArUtland to 0") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.antallArUtland shouldBe 0
    }

    test("mapToEpsPersongrunnlag should set dodsdato to null") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.dodsdato shouldBe null
    }

    test("mapToEpsPersongrunnlag should set statsborgerskapEnum to NOR") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.statsborgerskapEnum shouldBe LandkodeEnum.NOR
    }

    test("mapToEpsPersongrunnlag should set flyktning to false") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.flyktning shouldBe false
    }

    test("mapToEpsPersongrunnlag should set bosattLandEnum to NOR") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.bosattLandEnum shouldBe LandkodeEnum.NOR
    }

    test("mapToEpsPersongrunnlag should set empty afpHistorikkListe") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.afpHistorikkListe.shouldBeEmpty()
    }

    test("mapToEpsPersongrunnlag should set uforeHistorikk to null") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.uforeHistorikk shouldBe null
    }

    test("mapToEpsPersongrunnlag should set generellHistorikk to null") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.generellHistorikk shouldBe null
    }

    // ===========================================
    // Tests for mapToEpsPersongrunnlag - PersonDetalj for GIFT
    // ===========================================

    test("mapToEpsPersongrunnlag should set grunnlagsrolle to EKTEF for GIFT") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.personDetaljListe shouldHaveSize 1
        result.personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.EKTEF
    }

    test("mapToEpsPersongrunnlag should set borMedEnum to J_EKTEF for GIFT") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.personDetaljListe[0].borMedEnum shouldBe BorMedTypeEnum.J_EKTEF
    }

    // ===========================================
    // Tests for mapToEpsPersongrunnlag - PersonDetalj for REPA
    // ===========================================

    test("mapToEpsPersongrunnlag should set grunnlagsrolle to PARTNER for REPA") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.REPA, LocalDate.of(1965, 3, 20))

        result.personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.PARTNER
    }

    test("mapToEpsPersongrunnlag should set borMedEnum to J_PARTNER for REPA") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.REPA, LocalDate.of(1965, 3, 20))

        result.personDetaljListe[0].borMedEnum shouldBe BorMedTypeEnum.J_PARTNER
    }

    // ===========================================
    // Tests for mapToEpsPersongrunnlag - PersonDetalj for SAMB
    // ===========================================

    test("mapToEpsPersongrunnlag should set grunnlagsrolle to SAMBO for SAMB") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.SAMB, LocalDate.of(1965, 3, 20))

        result.personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SAMBO
    }

    test("mapToEpsPersongrunnlag should set borMedEnum to SAMBOER1_5 for SAMB") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.SAMB, LocalDate.of(1965, 3, 20))

        result.personDetaljListe[0].borMedEnum shouldBe BorMedTypeEnum.SAMBOER1_5
    }

    // ===========================================
    // Tests for mapToEpsPersongrunnlag - PersonDetalj common fields
    // ===========================================

    test("mapToEpsPersongrunnlag should set bruk to true on PersonDetalj") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.personDetaljListe[0].bruk shouldBe true
    }

    test("mapToEpsPersongrunnlag should set grunnlagKildeEnum to BRUKER on PersonDetalj") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.personDetaljListe[0].grunnlagKildeEnum shouldBe GrunnlagkildeEnum.BRUKER
    }

    test("mapToEpsPersongrunnlag should set penRolleFom from foedselsdato") {
        val mapper = createMapper()

        val result = mapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, LocalDate.of(1965, 3, 20))

        result.personDetaljListe[0].penRolleFom shouldNotBe null
    }

    // ===========================================
    // Tests for avdoedPersongrunnlag
    // ===========================================

    test("avdoedPersongrunnlag should set penPerson from avdoedPerson") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            penPersonId = 999L
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.penPerson shouldBe avdoedPerson
    }

    test("avdoedPersongrunnlag should set dodsdato from avdoed") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed(doedDato = LocalDate.of(2020, 11, 25))

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.dodsdato shouldNotBe null
    }

    test("avdoedPersongrunnlag should set dodAvYrkesskade to false") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.dodAvYrkesskade shouldBe false
    }

    test("avdoedPersongrunnlag should set arligPGIMinst1G from avdoed.harInntektOver1G") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed(harInntektOver1G = true)

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.arligPGIMinst1G shouldBe true
    }

    test("avdoedPersongrunnlag should set antallArUtland from avdoed") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed(antallAarUtenlands = 7)

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.antallArUtland shouldBe 7
    }

    test("avdoedPersongrunnlag should set medlemIFolketrygdenSiste3Ar from avdoed.erMedlemAvFolketrygden") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed(erMedlemAvFolketrygden = true)

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.medlemIFolketrygdenSiste3Ar shouldBe true
    }

    test("avdoedPersongrunnlag should set flyktning to false") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.flyktning shouldBe false
    }

    // ===========================================
    // Tests for avdoedPersongrunnlag - PersonDetalj
    // ===========================================

    test("avdoedPersongrunnlag should set grunnlagsrolle to AVDOD on PersonDetalj") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.personDetaljListe shouldHaveSize 1
        result.personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.AVDOD
    }

    test("avdoedPersongrunnlag should set grunnlagKildeEnum to BRUKER on PersonDetalj") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.personDetaljListe[0].grunnlagKildeEnum shouldBe GrunnlagkildeEnum.BRUKER
    }

    test("avdoedPersongrunnlag should set bruk to true on PersonDetalj") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.personDetaljListe[0].bruk shouldBe true
    }

    test("avdoedPersongrunnlag should set borMedEnum to null on PersonDetalj") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.personDetaljListe[0].borMedEnum shouldBe null
    }

    test("avdoedPersongrunnlag should set penRolleFom from soeker foedselsdato when soekerPid provided") {
        val soekerPid = Pid("12345678901")
        val soekerFoedselsdato = LocalDate.of(1963, 1, 1)
        val personService = mockk<GeneralPersonService> {
            every { foedselsdato(soekerPid) } returns soekerFoedselsdato
        }
        val mapper = PersongrunnlagMapper(
            generelleDataHolder = mockk(relaxed = true),
            personService = personService,
            time = { LocalDate.of(2025, 1, 15) }
        )
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid)

        result.personDetaljListe[0].penRolleFom shouldNotBe null
    }

    test("avdoedPersongrunnlag should set penRolleFom to null when soekerPid is null") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.personDetaljListe[0].penRolleFom shouldBe null
    }

    // ===========================================
    // Tests for avdoedPersongrunnlag - gjelderOmsorg and gjelderUforetrygd
    // ===========================================

    test("avdoedPersongrunnlag should set gjelderOmsorg to false") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.gjelderOmsorg shouldBe false
    }

    test("avdoedPersongrunnlag should set gjelderUforetrygd to false") {
        val mapper = createMapper()
        val avdoedPerson = PenPerson().apply {
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val avdoed = createAvdoed()

        val result = mapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid = null)

        result.gjelderUforetrygd shouldBe false
    }
})

// ===========================================
// Helper functions
// ===========================================

private fun createMapper(): PersongrunnlagMapper = PersongrunnlagMapper(
    generelleDataHolder = mockk(relaxed = true),
    personService = mockk(),
    time = { LocalDate.of(2025, 1, 15) }
)

private fun createSimuleringSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ALDER,
    sivilstatus: SivilstatusType = SivilstatusType.UGIF,
    flyktning: Boolean = false,
    utlandAntallAar: Int = 0,
    foersteUttakDato: LocalDate? = LocalDate.of(2029, 1, 1)
) = SimuleringSpec(
    type = type,
    sivilstatus = sivilstatus,
    epsHarPensjon = false,
    foersteUttakDato = foersteUttakDato,
    heltUttakDato = LocalDate.of(2032, 6, 1),
    pid = Pid("12345678901"),
    foedselDato = LocalDate.of(1963, 1, 1),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = UttakGradKode.P_100,
    forventetInntektBeloep = 250000,
    inntektUnderGradertUttakBeloep = 125000,
    inntektEtterHeltUttakBeloep = 67500,
    inntektEtterHeltUttakAntallAar = 5,
    foedselAar = 1963,
    utlandAntallAar = utlandAntallAar,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = flyktning,
    epsHarInntektOver2G = false,
    livsvarigOffentligAfp = null,
    pre2025OffentligAfp = null,
    erAnonym = false,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = true,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false
)

private fun createAvdoed(
    pid: Pid = Pid("98765432109"),
    antallAarUtenlands: Int = 0,
    inntektFoerDoed: Int = 500000,
    doedDato: LocalDate = LocalDate.of(2020, 5, 15),
    erMedlemAvFolketrygden: Boolean = false,
    harInntektOver1G: Boolean = false
) = Avdoed(
    pid = pid,
    antallAarUtenlands = antallAarUtenlands,
    inntektFoerDoed = inntektFoerDoed,
    doedDato = doedDato,
    erMedlemAvFolketrygden = erMedlemAvFolketrygden,
    harInntektOver1G = harInntektOver1G
)
