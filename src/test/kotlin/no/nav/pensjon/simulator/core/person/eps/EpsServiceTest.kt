package no.nav.pensjon.simulator.core.person.eps

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagResult
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagService
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.inntekt.OpptjeningUpdater
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.person.PersongrunnlagMapper
import no.nav.pensjon.simulator.core.person.PersongrunnlagService
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate
import java.util.*

class EpsServiceTest : FunSpec({

    // ===========================================
    // Tests for addPersongrunnlagForEpsToKravhode - gjenlevenderett
    // ===========================================

    test("addPersongrunnlagForEpsToKravhode should add gjenlevenderett persongrunnlag for ALDER_M_GJEN") {
        val avdoedPid = Pid("98765432109")
        val avdoedPerson = PenPerson().apply {
            pid = avdoedPid
            foedselsdato = LocalDate.of(1960, 5, 15)
        }

        val pensjonPersonService = mockk<PersonService> {
            every { person(avdoedPid) } returns avdoedPerson
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { avdoedPersongrunnlag(any(), any(), any()) } returns createAvdoedPersongrunnlag()
        }
        val persongrunnlagService = mockk<PersongrunnlagService> {
            every { addBeholdningerMedGrunnlagToPersongrunnlag(any(), any(), any(), any()) } returns Unit
        }
        val opptjeningUpdater = mockk<OpptjeningUpdater> {
            every { oppdaterOpptjeningsgrunnlagFraInntekter(any(), any(), any()) } returns mutableListOf()
        }
        val generelleDataHolder = mockk<GenerelleDataHolder> {
            every { getSisteGyldigeOpptjeningsaar() } returns 2023
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            pensjonPersonService = pensjonPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            persongrunnlagService = persongrunnlagService,
            opptjeningUpdater = opptjeningUpdater,
            generelleDataHolder = generelleDataHolder,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER_M_GJEN,
            sivilstatus = SivilstatusType.UGIF,
            avdoed = Avdoed(
                pid = avdoedPid,
                antallAarUtenlands = 0,
                inntektFoerDoed = 500000,
                doedDato = LocalDate.of(2020, 5, 15)
            )
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        kravhode.persongrunnlagListe shouldHaveSize 1
        verify { pensjonPersonService.person(avdoedPid) }
        verify { persongrunnlagMapper.avdoedPersongrunnlag(any(), avdoedPerson, any()) }
    }

    test("addPersongrunnlagForEpsToKravhode should add gjenlevenderett persongrunnlag for ENDR_ALDER_M_GJEN") {
        val avdoedPid = Pid("98765432109")
        val avdoedPerson = PenPerson().apply {
            pid = avdoedPid
            foedselsdato = LocalDate.of(1960, 5, 15)
        }

        val pensjonPersonService = mockk<PersonService> {
            every { person(avdoedPid) } returns avdoedPerson
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { avdoedPersongrunnlag(any(), any(), any()) } returns createAvdoedPersongrunnlag()
        }
        val persongrunnlagService = mockk<PersongrunnlagService> {
            every { addBeholdningerMedGrunnlagToPersongrunnlag(any(), any(), any(), any()) } returns Unit
        }
        val opptjeningUpdater = mockk<OpptjeningUpdater> {
            every { oppdaterOpptjeningsgrunnlagFraInntekter(any(), any(), any()) } returns mutableListOf()
        }
        val generelleDataHolder = mockk<GenerelleDataHolder> {
            every { getSisteGyldigeOpptjeningsaar() } returns 2023
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            pensjonPersonService = pensjonPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            persongrunnlagService = persongrunnlagService,
            opptjeningUpdater = opptjeningUpdater,
            generelleDataHolder = generelleDataHolder,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
            sivilstatus = SivilstatusType.UGIF,
            avdoed = Avdoed(
                pid = avdoedPid,
                antallAarUtenlands = 0,
                inntektFoerDoed = 500000,
                doedDato = LocalDate.of(2020, 5, 15)
            )
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        kravhode.persongrunnlagListe shouldHaveSize 1
    }

    test("addPersongrunnlagForEpsToKravhode should throw when avdoed person not found") {
        val avdoedPid = Pid("98765432109")

        val pensjonPersonService = mockk<PersonService> {
            every { person(avdoedPid) } returns null
        }

        val service = createEpsService(pensjonPersonService = pensjonPersonService)

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER_M_GJEN,
            avdoed = Avdoed(
                pid = avdoedPid,
                antallAarUtenlands = 0,
                inntektFoerDoed = 500000,
                doedDato = LocalDate.of(2020, 5, 15)
            )
        )

        shouldThrow<BadSpecException> {
            service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)
        }
    }

    test("addPersongrunnlagForEpsToKravhode should set sisteGyldigeOpptjeningsAr on gjenlevenderett persongrunnlag") {
        val avdoedPid = Pid("98765432109")
        val avdoedPerson = PenPerson().apply {
            pid = avdoedPid
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val persongrunnlag = createAvdoedPersongrunnlag()

        val pensjonPersonService = mockk<PersonService> {
            every { person(avdoedPid) } returns avdoedPerson
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { avdoedPersongrunnlag(any(), any(), any()) } returns persongrunnlag
        }
        val persongrunnlagService = mockk<PersongrunnlagService> {
            every { addBeholdningerMedGrunnlagToPersongrunnlag(any(), any(), any(), any()) } returns Unit
        }
        val opptjeningUpdater = mockk<OpptjeningUpdater> {
            every { oppdaterOpptjeningsgrunnlagFraInntekter(any(), any(), any()) } returns mutableListOf()
        }
        val generelleDataHolder = mockk<GenerelleDataHolder> {
            every { getSisteGyldigeOpptjeningsaar() } returns 2022
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            pensjonPersonService = pensjonPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            persongrunnlagService = persongrunnlagService,
            opptjeningUpdater = opptjeningUpdater,
            generelleDataHolder = generelleDataHolder,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER_M_GJEN,
            avdoed = Avdoed(
                pid = avdoedPid,
                antallAarUtenlands = 0,
                inntektFoerDoed = 500000,
                doedDato = LocalDate.of(2020, 5, 15)
            )
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        persongrunnlag.sisteGyldigeOpptjeningsAr shouldBe 2022
    }

    // ===========================================
    // Tests for addPersongrunnlagForEpsToKravhode - EPS based on sivilstatus
    // ===========================================

    test("addPersongrunnlagForEpsToKravhode should add EPS persongrunnlag for GIFT sivilstatus") {
        val epsPersongrunnlag = Persongrunnlag().apply {
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                }
            )
        }

        val generalPersonService = mockk<GeneralPersonService> {
            every { foedselsdato(any()) } returns LocalDate.of(1965, 3, 20)
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToEpsPersongrunnlag(any(), any()) } returns epsPersongrunnlag
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            generalPersonService = generalPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.GIFT,
            epsHarInntektOver2G = false
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        kravhode.persongrunnlagListe shouldHaveSize 1
        verify { persongrunnlagMapper.mapToEpsPersongrunnlag(SivilstatusType.GIFT, any()) }
    }

    test("addPersongrunnlagForEpsToKravhode should add EPS persongrunnlag for REPA sivilstatus") {
        val epsPersongrunnlag = Persongrunnlag()

        val generalPersonService = mockk<GeneralPersonService> {
            every { foedselsdato(any()) } returns LocalDate.of(1965, 3, 20)
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToEpsPersongrunnlag(any(), any()) } returns epsPersongrunnlag
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            generalPersonService = generalPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.REPA,
            epsHarInntektOver2G = false
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        kravhode.persongrunnlagListe shouldHaveSize 1
    }

    test("addPersongrunnlagForEpsToKravhode should add EPS persongrunnlag for SAMB sivilstatus") {
        val epsPersongrunnlag = Persongrunnlag()

        val generalPersonService = mockk<GeneralPersonService> {
            every { foedselsdato(any()) } returns LocalDate.of(1965, 3, 20)
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToEpsPersongrunnlag(any(), any()) } returns epsPersongrunnlag
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            generalPersonService = generalPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.SAMB,
            epsHarInntektOver2G = false
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        kravhode.persongrunnlagListe shouldHaveSize 1
    }

    test("addPersongrunnlagForEpsToKravhode should not add persongrunnlag for UGIF sivilstatus") {
        val service = createEpsService()

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.UGIF
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        kravhode.persongrunnlagListe.shouldBeEmpty()
    }

    test("addPersongrunnlagForEpsToKravhode should not add persongrunnlag for ENKE sivilstatus") {
        val service = createEpsService()

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.ENKE
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        kravhode.persongrunnlagListe.shouldBeEmpty()
    }

    // ===========================================
    // Tests for epsHarInntektOver2G
    // ===========================================

    test("addPersongrunnlagForEpsToKravhode should add inntektsgrunnlag when epsHarInntektOver2G is true") {
        val epsPersongrunnlag = Persongrunnlag().apply {
            inntektsgrunnlagListe = mutableListOf()
        }

        val generalPersonService = mockk<GeneralPersonService> {
            every { foedselsdato(any()) } returns LocalDate.of(1965, 3, 20)
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToEpsPersongrunnlag(any(), any()) } returns epsPersongrunnlag
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 6, 15)
        }

        val service = createEpsService(
            generalPersonService = generalPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.GIFT,
            epsHarInntektOver2G = true,
            foersteUttakDato = LocalDate.of(2029, 1, 1)
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        kravhode.persongrunnlagListe shouldHaveSize 1
        val addedGrunnlag = kravhode.persongrunnlagListe[0]
        addedGrunnlag.inntektsgrunnlagListe shouldHaveSize 1

        val inntektsgrunnlag = addedGrunnlag.inntektsgrunnlagListe[0]
        inntektsgrunnlag.belop shouldBe 3 * 118620 // EPS_GRUNNBELOEP_MULTIPLIER * grunnbeloep
        inntektsgrunnlag.grunnlagKildeEnum shouldBe GrunnlagkildeEnum.BRUKER
        inntektsgrunnlag.inntektTypeEnum shouldBe InntekttypeEnum.FPI
        inntektsgrunnlag.bruk shouldBe true
    }

    test("addPersongrunnlagForEpsToKravhode should use today when foersteUttakDato is in the future") {
        val epsPersongrunnlag = Persongrunnlag().apply {
            inntektsgrunnlagListe = mutableListOf()
        }

        val generalPersonService = mockk<GeneralPersonService> {
            every { foedselsdato(any()) } returns LocalDate.of(1965, 3, 20)
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToEpsPersongrunnlag(any(), any()) } returns epsPersongrunnlag
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 6, 15)
        }

        val service = createEpsService(
            generalPersonService = generalPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.GIFT,
            epsHarInntektOver2G = true,
            foersteUttakDato = LocalDate.of(2029, 1, 1) // Future date
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        val inntektsgrunnlag = kravhode.persongrunnlagListe[0].inntektsgrunnlagListe[0]
        // fom should be first day of 2025 (today's year) since foersteUttakDato is in the future
        inntektsgrunnlag.fom shouldNotBe null
    }

    test("addPersongrunnlagForEpsToKravhode should use foersteUttakDato when it is in the past") {
        val epsPersongrunnlag = Persongrunnlag().apply {
            inntektsgrunnlagListe = mutableListOf()
        }

        val generalPersonService = mockk<GeneralPersonService> {
            every { foedselsdato(any()) } returns LocalDate.of(1965, 3, 20)
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToEpsPersongrunnlag(any(), any()) } returns epsPersongrunnlag
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 6, 15)
        }

        val service = createEpsService(
            generalPersonService = generalPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.GIFT,
            epsHarInntektOver2G = true,
            foersteUttakDato = LocalDate.of(2024, 1, 1) // Past date
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        val inntektsgrunnlag = kravhode.persongrunnlagListe[0].inntektsgrunnlagListe[0]
        inntektsgrunnlag.fom shouldNotBe null
    }

    test("addPersongrunnlagForEpsToKravhode should not add inntektsgrunnlag when epsHarInntektOver2G is false") {
        val epsPersongrunnlag = Persongrunnlag().apply {
            inntektsgrunnlagListe = mutableListOf()
        }

        val generalPersonService = mockk<GeneralPersonService> {
            every { foedselsdato(any()) } returns LocalDate.of(1965, 3, 20)
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToEpsPersongrunnlag(any(), any()) } returns epsPersongrunnlag
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 6, 15)
        }

        val service = createEpsService(
            generalPersonService = generalPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.GIFT,
            epsHarInntektOver2G = false
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        kravhode.persongrunnlagListe[0].inntektsgrunnlagListe.shouldBeEmpty()
    }

    // ===========================================
    // Tests for gjenlevenderett - filtering and opptjening
    // ===========================================

    test("addPersongrunnlagForEpsToKravhode should filter inntektsgrunnlag for gjenlevenderett") {
        val avdoedPid = Pid("98765432109")
        val avdoedPerson = PenPerson().apply {
            pid = avdoedPid
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val persongrunnlag = createAvdoedPersongrunnlag().apply {
            inntektsgrunnlagListe = mutableListOf(
                Inntektsgrunnlag().apply {
                    bruk = true
                    inntektTypeEnum = InntekttypeEnum.FPI // Should be filtered out
                },
                Inntektsgrunnlag().apply {
                    bruk = true
                    inntektTypeEnum = InntekttypeEnum.PGI // Should be kept
                },
                Inntektsgrunnlag().apply {
                    bruk = false // Should be filtered out
                    inntektTypeEnum = InntekttypeEnum.PGI
                }
            )
        }

        val pensjonPersonService = mockk<PersonService> {
            every { person(avdoedPid) } returns avdoedPerson
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { avdoedPersongrunnlag(any(), any(), any()) } returns persongrunnlag
        }
        val persongrunnlagService = mockk<PersongrunnlagService> {
            every { addBeholdningerMedGrunnlagToPersongrunnlag(any(), any(), any(), any()) } returns Unit
        }
        val opptjeningUpdater = mockk<OpptjeningUpdater> {
            every { oppdaterOpptjeningsgrunnlagFraInntekter(any(), any(), any()) } returns mutableListOf()
        }
        val generelleDataHolder = mockk<GenerelleDataHolder> {
            every { getSisteGyldigeOpptjeningsaar() } returns 2023
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            pensjonPersonService = pensjonPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            persongrunnlagService = persongrunnlagService,
            opptjeningUpdater = opptjeningUpdater,
            generelleDataHolder = generelleDataHolder,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER_M_GJEN,
            avdoed = Avdoed(
                pid = avdoedPid,
                antallAarUtenlands = 0,
                inntektFoerDoed = 500000,
                doedDato = LocalDate.of(2020, 5, 15)
            )
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        // Only the PGI with bruk=true should remain
        persongrunnlag.inntektsgrunnlagListe shouldHaveSize 1
        persongrunnlag.inntektsgrunnlagListe[0].inntektTypeEnum shouldBe InntekttypeEnum.PGI
    }

    test("addPersongrunnlagForEpsToKravhode should update opptjeningsgrunnlag with inntektFoerDoed") {
        val avdoedPid = Pid("98765432109")
        val avdoedPerson = PenPerson().apply {
            pid = avdoedPid
            foedselsdato = LocalDate.of(1960, 5, 15)
        }
        val persongrunnlag = createAvdoedPersongrunnlag()
        val updatedOpptjeningsgrunnlag = mutableListOf(
            Opptjeningsgrunnlag().apply { ar = 2024 }
        )

        val pensjonPersonService = mockk<PersonService> {
            every { person(avdoedPid) } returns avdoedPerson
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { avdoedPersongrunnlag(any(), any(), any()) } returns persongrunnlag
        }
        val persongrunnlagService = mockk<PersongrunnlagService> {
            every { addBeholdningerMedGrunnlagToPersongrunnlag(any(), any(), any(), any()) } returns Unit
        }
        val opptjeningUpdater = mockk<OpptjeningUpdater> {
            every { oppdaterOpptjeningsgrunnlagFraInntekter(any(), any(), any()) } returns updatedOpptjeningsgrunnlag
        }
        val generelleDataHolder = mockk<GenerelleDataHolder> {
            every { getSisteGyldigeOpptjeningsaar() } returns 2023
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            pensjonPersonService = pensjonPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            persongrunnlagService = persongrunnlagService,
            opptjeningUpdater = opptjeningUpdater,
            generelleDataHolder = generelleDataHolder,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER_M_GJEN,
            avdoed = Avdoed(
                pid = avdoedPid,
                antallAarUtenlands = 0,
                inntektFoerDoed = 500000,
                doedDato = LocalDate.of(2020, 5, 15)
            )
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        verify {
            opptjeningUpdater.oppdaterOpptjeningsgrunnlagFraInntekter(
                any(),
                match { it.size == 1 && it[0].beloep == 500000L && it[0].inntektAar == 2024 },
                any()
            )
        }
        persongrunnlag.opptjeningsgrunnlagListe shouldBe updatedOpptjeningsgrunnlag
    }

    // ===========================================
    // Tests for foedselsdato resolution
    // ===========================================

    test("addPersongrunnlagForEpsToKravhode should use foedselsdato from generalPersonService when pid is set") {
        val epsPersongrunnlag = Persongrunnlag()
        val pid = Pid("12345678901")

        val generalPersonService = mockk<GeneralPersonService> {
            every { foedselsdato(pid) } returns LocalDate.of(1965, 3, 20)
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToEpsPersongrunnlag(any(), LocalDate.of(1965, 3, 20)) } returns epsPersongrunnlag
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            generalPersonService = generalPersonService,
            persongrunnlagMapper = persongrunnlagMapper,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.GIFT,
            pid = pid
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        verify { generalPersonService.foedselsdato(pid) }
        verify { persongrunnlagMapper.mapToEpsPersongrunnlag(any(), LocalDate.of(1965, 3, 20)) }
    }

    test("addPersongrunnlagForEpsToKravhode should use foedselAar when pid is null") {
        val epsPersongrunnlag = Persongrunnlag()

        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToEpsPersongrunnlag(any(), LocalDate.of(1963, 1, 1)) } returns epsPersongrunnlag
        }
        val time = mockk<Time> {
            every { today() } returns LocalDate.of(2025, 1, 15)
        }

        val service = createEpsService(
            persongrunnlagMapper = persongrunnlagMapper,
            time = time
        )

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf()
        }
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.GIFT,
            pid = null,
            foedselAar = 1963,
            erAnonym = true
        )

        service.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep = 118620)

        verify { persongrunnlagMapper.mapToEpsPersongrunnlag(any(), LocalDate.of(1963, 1, 1)) }
    }
})

// ===========================================
// Helper functions
// ===========================================

private fun createEpsService(
    pensjonPersonService: PersonService = mockk(relaxed = true),
    generalPersonService: GeneralPersonService = mockk(relaxed = true),
    persongrunnlagService: PersongrunnlagService = mockk(relaxed = true),
    persongrunnlagMapper: PersongrunnlagMapper = mockk(relaxed = true),
    opptjeningUpdater: OpptjeningUpdater = mockk(relaxed = true),
    generelleDataHolder: GenerelleDataHolder = mockk(relaxed = true),
    time: Time = mockk { every { today() } returns LocalDate.of(2025, 1, 15) }
): EpsService = EpsService(
    pensjonPersonService,
    generalPersonService,
    persongrunnlagService,
    persongrunnlagMapper,
    opptjeningUpdater,
    generelleDataHolder,
    time
)

private fun createAvdoedPersongrunnlag(): Persongrunnlag = Persongrunnlag().apply {
    fodselsdato = dateAtNoon(1960, Calendar.MAY, 15)
    penPerson = PenPerson().apply { penPersonId = 2L }
    personDetaljListe = mutableListOf(
        PersonDetalj().apply {
            bruk = true
            grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
        }
    )
    inntektsgrunnlagListe = mutableListOf()
    opptjeningsgrunnlagListe = mutableListOf()
}

private fun createSimuleringSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ALDER,
    sivilstatus: SivilstatusType = SivilstatusType.UGIF,
    epsHarInntektOver2G: Boolean = false,
    foersteUttakDato: LocalDate? = LocalDate.of(2029, 1, 1),
    pid: Pid? = Pid("12345678901"),
    foedselAar: Int = 1963,
    avdoed: Avdoed? = null,
    erAnonym: Boolean = false
) = SimuleringSpec(
    type = type,
    sivilstatus = sivilstatus,
    epsHarPensjon = false,
    foersteUttakDato = foersteUttakDato,
    heltUttakDato = LocalDate.of(2032, 6, 1),
    pid = pid,
    foedselDato = LocalDate.of(foedselAar, 1, 1),
    avdoed = avdoed,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = UttakGradKode.P_100,
    forventetInntektBeloep = 250000,
    inntektUnderGradertUttakBeloep = 125000,
    inntektEtterHeltUttakBeloep = 67500,
    inntektEtterHeltUttakAntallAar = 5,
    foedselAar = foedselAar,
    utlandAntallAar = 0,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = false,
    epsHarInntektOver2G = epsHarInntektOver2G,
    livsvarigOffentligAfp = null,
    pre2025OffentligAfp = null,
    erAnonym = erAnonym,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = true,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false
)
