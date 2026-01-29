package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.exception.ImplementationUnrecoverableException
import no.nav.pensjon.simulator.core.ufoere.UfoereOpptjeningGrunnlag
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.person.Sivilstandstype
import no.nav.pensjon.simulator.person.relasjon.PersonPar
import no.nav.pensjon.simulator.person.relasjon.Soesken
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.lastDayOfMonth
import java.util.Date
import java.util.concurrent.atomic.AtomicLong
import no.nav.pensjon.simulator.person.Person as DomainPerson

@Component
class FppSimuleringSpecCreator(
    private val grunnbeloepService: GrunnbeloepService,
    private val personService: GeneralPersonService,
    private val time: Time
) {
    fun createSpec(
        simuleringType: SimuleringTypeEnum,
        uttaksdato: LocalDate,
        personopplysninger: Personopplysninger,
        opptjeningFolketrygden: OpptjeningFolketrygden?,
        barneopplysninger: Barneopplysninger?
    ) =
        Simulering().apply {
            simuleringTypeEnum = simuleringType
            this.uttaksdato = uttaksdato.toNorwegianDateAtNoon()

            if (simuleringType == AFP) {
                afpOrdningEnum = personopplysninger.valgtAfpOrdning
            }

            persongrunnlagListe = persongrunnlagListe(
                simuleringType,
                uttaksdato,
                personopplysninger,
                barneopplysninger,
                opptjeningFolketrygden,
                grunnbeloepService.naavaerendeGrunnbeloep()
            )
        }

    private fun persongrunnlagListe(
        simuleringType: SimuleringTypeEnum,
        uttaksdato: LocalDate,
        personopplysninger: Personopplysninger,
        barneopplysninger: Barneopplysninger?,
        opptjeningFolketrygden: OpptjeningFolketrygden?,
        grunnbeloep: Int
    ): List<Persongrunnlag> {
        val persongrunnlagListe = mutableListOf<Persongrunnlag>()

        persongrunnlagListe.add(
            persongrunnlagForSoeker(
                simuleringType,
                uttaksdato,
                personopplysninger,
                opptjeningFolketrygden,
                grunnbeloep
            )
        )

        if (setOf(AFP, BARN, GJENLEVENDE).contains(simuleringType).not()) {
            barneopplysninger?.let {
                val runningPersonId = AtomicLong(100L)

                it.barn.map { barn -> persongrunnlagForBarn(personopplysninger, barn, uttaksdato, runningPersonId) }
                    .forEach { grunnlag -> persongrunnlagListe.add(grunnlag) }
            }
        }

        if (hasSoekerEps(simuleringType, personopplysninger)) {
            persongrunnlagListe.add(
                persongrunnlagForEps(
                    simuleringType,
                    uttaksdato,
                    personopplysninger,
                    opptjeningFolketrygden,
                    grunnbeloep
                )
            )
        }

        // Persongrunnlag for foreldre og søsken ved simulering av barnepensjon:
        if (simuleringType == BARN) {
            val beggeForeldreDoede = personopplysninger.avdodList.size > 1

            persongrunnlagListe.add(
                persongrunnlagForForelder(
                    opptjeningFolketrygden,
                    avdoed = personopplysninger.avdodList.firstOrNull(),
                    beggeForeldreDoede
                )
            )

            if (beggeForeldreDoede) {
                persongrunnlagListe.add(
                    persongrunnlagForForelder(
                        opptjeningFolketrygden,
                        avdoed = personopplysninger.avdodList[1],
                        beggeForeldreDoede = true
                    )
                )
            }

            persongrunnlagForSoesken(barneopplysninger, persongrunnlagListe)
        }

        return persongrunnlagListe
    }

    private fun persongrunnlagForSoeker(
        simuleringType: SimuleringTypeEnum,
        uttaksdato: LocalDate,
        personopplysninger: Personopplysninger,
        opptjeningFolketrygden: OpptjeningFolketrygden?,
        grunnbeloep: Int
    ): Persongrunnlag {
        val pid = personopplysninger.ident?.let(::Pid)

        val person: DomainPerson = pid?.let(personService::person)
            ?: throw ImplementationUnrecoverableException("Person ikke funnet for PID $pid")

        val sivilstandsinformasjon: Sivilstandsinformasjon? = person.sivilstand?.let(::Sivilstandsinformasjon)

        return Persongrunnlag().apply {
            penPerson = PenPerson().apply {
                penPersonId = 1L
                this.pid = pid
            }

            val foedselsdato = personopplysninger.fodselsdato
            fodselsdato = foedselsdato?.toNorwegianDateAtNoon()
            dodsdato = null
            antallArUtland = if (simuleringType == BARN) 0 else personopplysninger.antAarIUtlandet ?: 0
            flyktning = personopplysninger.flyktning

            if (simuleringType == AFP) {
                personDetaljListe.add(
                    persondetaljForSoeker(
                        rolleFom = foedselsdato,
                        sivilstand = personopplysninger.epsData?.let(::sivilstand)
                    )
                )
            } else {
                val rolleFom: LocalDate? =
                    if (simuleringType == GJENLEVENDE)
                        personopplysninger.avdodList.firstOrNull()?.datoForDodsfall
                    else
                        foedselsdato

                personDetaljListe.add(
                    persondetaljForSoeker(
                        rolleFom,
                        sivilstand = sivilstandsinformasjon?.sivilstandstype?.let(::mapSivilstand)
                    )
                )
            }

            over60ArKanIkkeForsorgesSelv = false

            this.opptjeningsgrunnlagListe = if (simuleringType == BARN || simuleringType == GJENLEVENDE) {
                mutableListOf()
            } else {
                opptjeningFolketrygden?.egenOpptjeningFolketrygden?.let(::opptjeningsgrunnlagListe) ?: mutableListOf()
            }

            inntektsgrunnlagListe =
                inntektsgrunnlagListeForSoeker(
                    simuleringType,
                    uttaksdato,
                    personopplysninger,
                    grunnbeloep
                )

            dodAvYrkesskade = false
            medlemIFolketrygdenSiste3Ar = true
            statsborgerskapEnum = person.statsborgerskap
            ufoereOpptjeningGrunnlag =
                UfoereOpptjeningGrunnlag().apply { maksUtbetalingsgradPerArUTListe = mutableListOf() }
        }
    }

    /**
     * For all simulations except "BARN".
     */
    private fun persongrunnlagForEps(
        simuleringType: SimuleringTypeEnum,
        uttaksdato: LocalDate,
        personopplysninger: Personopplysninger,
        opptjeningFolketrygden: OpptjeningFolketrygden?,
        grunnbeloep: Int
    ): Persongrunnlag {
        val persongrunnlag = Persongrunnlag() //TODO use 'apply'
        val person = PenPerson().apply { penPersonId = 2L }
        val epsData: EpsData? = personopplysninger.epsData

        if (simuleringType == AFP) {
            val relasjon = epsData?.eps

            if (relasjon == null || sivilstatusMatch(epsData).not()) {
                person.penPersonId = -2L
                persongrunnlag.fodselsdato = epsFoedselsdato()
            } else {
                person.pid = relasjon.person?.pid?.let(::Pid)
                persongrunnlag.fodselsdato = personopplysninger.fodselsdato?.toNorwegianDateAtNoon()
            }
        } else if (simuleringType == GJENLEVENDE) {
            val pid = personopplysninger.avdodList.firstOrNull()?.relasjon?.person?.pid?.let(::Pid)
            person.pid = pid
            persongrunnlag.fodselsdato = pid?.let(personService::foedselsdato)?.toNorwegianDateAtNoon()
        }

        persongrunnlag.penPerson = person

        if (simuleringType == GJENLEVENDE) {
            personopplysninger.avdodList.firstOrNull()?.let { setPersongrunnlagFromAvdoed(persongrunnlag, avdoed = it) }
            persongrunnlag.opptjeningsgrunnlagListe =
                opptjeningFolketrygden?.avdodesOpptjeningFolketrygden?.let(::opptjeningsgrunnlagListe)
                    ?: mutableListOf()
        } else {
            persongrunnlag.antallArUtland = 0
            persongrunnlag.flyktning = false
            persongrunnlag.opptjeningsgrunnlagListe = mutableListOf()
            persongrunnlag.dodAvYrkesskade = false
            persongrunnlag.medlemIFolketrygdenSiste3Ar = true
            persongrunnlag.fastsattTrygdetid = true
        }

        persongrunnlag.personDetaljListe.add(persondetaljForEps(simuleringType, uttaksdato, personopplysninger))
        persongrunnlag.over60ArKanIkkeForsorgesSelv = false
        persongrunnlag.inntektsgrunnlagListe =
            inntektsgrunnlagListeForEps(simuleringType, uttaksdato, personopplysninger, grunnbeloep)

        persongrunnlag.statsborgerskapEnum = epsData?.let(::statsborgerskap)
        return persongrunnlag
    }

    /**
     * For the simulation of "BARN".
     */
    private fun persongrunnlagForForelder(
        opptjeningFolketrygden: OpptjeningFolketrygden?,
        avdoed: AvdoedData?,
        beggeForeldreDoede: Boolean
    ) =
        Persongrunnlag().apply {
            val relasjon: Relasjon? = avdoed?.relasjon
            val relasjonType: RelasjonTypeCode = relasjon?.relasjonsType?.internalValue ?: RelasjonTypeCode.UKJENT
            val relatertPersonDto: PersonV1? = relasjon?.person //TODO map to domain
            val relatertPid = relatertPersonDto?.pid?.let(::Pid)
            val relatertPenPerson = PenPerson().apply { pid = relatertPid }
            val relatertPerson = relatertPid?.let(personService::person)
            val foedselsdato: LocalDate? = relatertPerson?.foedselsdato
            fodselsdato = foedselsdato?.toNorwegianDateAtNoon()
            dodsdato = avdoed?.datoForDodsfall?.toNorwegianDateAtNoon()
            flyktning = avdoed?.avdodFlyktning
            arligPGIMinst1G = avdoed?.avdodInntektMinst1G
            antallArUtland = avdoed?.avdodAntAarIUtlandet ?: 0

            if (relasjonType == RelasjonTypeCode.MORA) {
                personDetaljListe.add(
                    persondetaljForForelder(rolleFom = foedselsdato, rolle = GrunnlagsrolleEnum.MOR)
                )
                relatertPenPerson.penPersonId = 3L
            } else if (relasjonType == RelasjonTypeCode.FARA) {
                personDetaljListe.add(
                    persondetaljForForelder(rolleFom = foedselsdato, rolle = GrunnlagsrolleEnum.FAR)
                )
                relatertPenPerson.penPersonId = 4L
            }

            penPerson = relatertPenPerson
            over60ArKanIkkeForsorgesSelv = false

            if (beggeForeldreDoede) {
                if (relasjonType == RelasjonTypeCode.MORA) {
                    this.opptjeningsgrunnlagListe =
                        opptjeningFolketrygden?.morsOpptjeningFolketrygden?.let(::opptjeningsgrunnlagListe)
                            ?: mutableListOf()
                } else if (relasjonType == RelasjonTypeCode.FARA) {
                    this.opptjeningsgrunnlagListe =
                        opptjeningFolketrygden?.farsOpptjeningFolketrygden?.let(::opptjeningsgrunnlagListe)
                            ?: mutableListOf()
                }
            } else {
                this.opptjeningsgrunnlagListe = mutableListOf()
            }

            if (avdoed?.dodAvYrkesskade == true) {
                yrkesskadegrunnlag = yrkesskadegrunnlag(avdoed)
            }

            dodAvYrkesskade = avdoed?.dodAvYrkesskade
            medlemIFolketrygdenSiste3Ar = true
            statsborgerskapEnum = relatertPerson?.statsborgerskap
        }

    /**
     * For the simulation of "BARN".
     */
    private fun persongrunnlagForSoesken(
        barneopplysninger: Barneopplysninger?,
        persongrunnlagListe: MutableList<Persongrunnlag>
    ) {
        if (barneopplysninger == null) {
            return
        }

        var runningPersonId = 50L

        for (soesken in barneopplysninger.sosken) {
            val persongrunnlag = Persongrunnlag().apply {
                val soeskenPid = soesken.fnr?.let(::Pid)
                val soeskenPerson: DomainPerson? = soeskenPid?.let(personService::person)
                val foedselsdato: LocalDate? = soeskenPerson?.foedselsdato

                penPerson = PenPerson().apply {
                    penPersonId = runningPersonId++
                    pid = soeskenPid
                }

                fodselsdato = foedselsdato?.toNorwegianDateAtNoon()
                antallArUtland = 0

                /* This code in PEN will always return soeskenFoedselsdato
                val rolleFom: LocalDate? =
                    if (foedselsdato?.isBefore(soeskenFoedselsdato) == true)
                        foedselsdato
                    else
                        soeskenFoedselsdato
                */

                personDetaljListe.add(persondetaljForSoesken(rolleFom = foedselsdato, soesken = soesken))
                over60ArKanIkkeForsorgesSelv = false
                opptjeningsgrunnlagListe = mutableListOf()
                dodAvYrkesskade = false
                medlemIFolketrygdenSiste3Ar = true
                statsborgerskapEnum = soeskenPerson?.statsborgerskap
            }

            persongrunnlagListe.add(persongrunnlag)
        }
    }

    /**
     * For simulating UFORE, UFOR_M_GJEN
     */
    private fun persongrunnlagForBarn(
        person: Personopplysninger,
        barn: BarneopplysningerData,
        uttaksdato: LocalDate,
        runningPersonId: AtomicLong
    ) =
        Persongrunnlag().apply {
            penPerson = PenPerson().apply {
                penPersonId = runningPersonId.getAndIncrement()
                pid = barn.fnr?.let(::Pid)
            }

            fodselsdato = person.fodselsdato?.toNorwegianDateAtNoon()
            antallArUtland = 0
            flyktning = false
            personDetaljListe.add(
                persondetaljForBarn(
                    person,
                    rolleFom = person.fodselsdato,
                    barn,
                    uttaksdato
                )
            )
            over60ArKanIkkeForsorgesSelv = false
            this.opptjeningsgrunnlagListe = mutableListOf()
            dodAvYrkesskade = false
            medlemIFolketrygdenSiste3Ar = true
            statsborgerskapEnum = barn.fnr?.let(::Pid)?.let(personService::statsborgerskap)
        }

    private fun setPersongrunnlagFromAvdoed(
        grunnlag: Persongrunnlag,
        avdoed: AvdoedData
    ) {
        grunnlag.dodsdato = avdoed.datoForDodsfall?.toNorwegianDateAtNoon()
        grunnlag.flyktning = avdoed.avdodFlyktning
        grunnlag.antallArUtland = avdoed.avdodAntAarIUtlandet ?: 0
        grunnlag.medlemIFolketrygdenSiste3Ar = avdoed.avdodMedlemFolketrygden
        grunnlag.arligPGIMinst1G = avdoed.avdodInntektMinst1G
        grunnlag.dodAvYrkesskade = avdoed.dodAvYrkesskade

        if (avdoed.dodAvYrkesskade == true) {
            grunnlag.yrkesskadegrunnlag = yrkesskadegrunnlag(avdoed)
        }
    }

    private fun persondetaljForBarn(
        personopplysninger: Personopplysninger,
        rolleFom: LocalDate?,
        barn: BarneopplysningerData,
        uttaksdato: LocalDate
    ) =
        PersonDetalj().apply {
            grunnlagsrolleEnum = GrunnlagsrolleEnum.BARN
            bruk = true
            penRolleFom = rolleFom?.toNorwegianDateAtNoon()
            borMedEnum = personopplysninger.ident?.let {
                borMedBarnStatus(
                    soekerPid = Pid(it),
                    barn,
                    dato = uttaksdato
                )
            }
            barnDetalj = barnedetalj(personopplysninger, barn)
            finishInit() // sets rolleFomDato
        }

    private fun borMedBarnStatus(
        soekerPid: Pid,
        barn: BarneopplysningerData,
        dato: LocalDate
    ): BorMedTypeEnum =
        if (barn.borMedBeggeForeldre || borSammen(pid1 = Pid(barn.fnr!!), pid2 = soekerPid, dato))
            BorMedTypeEnum.J_BARN
        else
            BorMedTypeEnum.N_BARN

    private fun borSammen(pid1: Pid, pid2: Pid, dato: LocalDate): Boolean =
        personService.borSammen(
            personer = PersonPar(pid1, pid2, dato)
        )

    private fun epsFoedselsdato(): Date =
        time.today().minusYears(UNKNOWN_EPS_DEFAULT_AGE).toNorwegianDateAtNoon()

    companion object {
        private const val UNKNOWN_EPS_DEFAULT_AGE = 59L

        private val epsSivilstatuser =
            arrayOf(
                SivilstatusType.GIFT,
                SivilstatusType.REPA,
                SivilstatusType.SAMB
            )

        private fun persondetaljForSoeker(rolleFom: LocalDate?, sivilstand: SivilstandEnum?) =
            PersonDetalj().apply {
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                penRolleFom = rolleFom?.toNorwegianDateAtNoon()
                sivilstandTypeEnum = sivilstand
                bruk = true
                finishInit()
            }

        private fun persondetaljForEps(
            simuleringType: SimuleringTypeEnum,
            uttaksdato: LocalDate,
            personopplysninger: Personopplysninger
        ) =
            PersonDetalj().apply {
                if (simuleringType == GJENLEVENDE) {
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
                    penRolleFom = personopplysninger.avdodList.firstOrNull()?.datoForDodsfall?.toNorwegianDateAtNoon()
                    borMedEnum = BorMedTypeEnum.J_AVDOD
                } else {
                    val epsData: EpsData? = personopplysninger.epsData
                    grunnlagsrolleEnum = grunnlagsrolleForEps(epsData?.valgtSivilstatus)
                    penRolleFom = epsData?.eps?.fom?.toNorwegianDateAtNoon()
                        ?: uttaksdato.minusDays(1).toNorwegianDateAtNoon()
                    borMedEnum = epsData?.let(::borMedTypeForEps)
                }

                bruk = true
                finishInit()
            }

        private fun persondetaljForSoesken(rolleFom: LocalDate?, soesken: BarneopplysningerSoeskenData) =
            PersonDetalj().apply {
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOSKEN
                penRolleFom = rolleFom?.toNorwegianDateAtNoon()
                bruk = true
                borMedEnum = soesken.oppdrattSammen?.let(::borMedTypeForSoesken)
                barnDetalj = BarnDetalj().apply { underUtdanning = soesken.underUtdanning == true }
                this.soesken = soesken(soesken)
                finishInit()
            }

        private fun persondetaljForForelder(rolleFom: LocalDate?, rolle: GrunnlagsrolleEnum) =
            PersonDetalj().apply {
                grunnlagsrolleEnum = rolle
                penRolleFom = rolleFom?.toNorwegianDateAtNoon()
                borMedEnum = BorMedTypeEnum.J_BARN
                bruk = true
                finishInit()
            }

        private fun opptjeningsgrunnlagListe(
            opptjeningFolketrygdenListe: List<OpptjeningFolketrygdenData>
        ): MutableList<Opptjeningsgrunnlag> {
            val opptjeningsgrunnlagList = mutableListOf<Opptjeningsgrunnlag>()

            opptjeningFolketrygdenListe.forEach {
                if ((it.omsorgspoeng ?: 0.0) > 0) {
                    opptjeningsgrunnlagList.add(
                        opptjeningsgrunnlag(
                            opptjeningFolketrygden = it,
                            opptjeningstype = OpptjeningtypeEnum.OSFE
                        )
                    )
                }

                if ((it.pensjonsgivendeInntekt ?: 0) > 0) {
                    opptjeningsgrunnlagList.add(
                        opptjeningsgrunnlag(
                            opptjeningFolketrygden = it,
                            opptjeningstype = OpptjeningtypeEnum.PPI
                        )
                    )
                }
            }

            return opptjeningsgrunnlagList
        }

        private fun opptjeningsgrunnlag(
            opptjeningFolketrygden: OpptjeningFolketrygdenData,
            opptjeningstype: OpptjeningtypeEnum
        ) =
            Opptjeningsgrunnlag().apply {
                opptjeningTypeEnum = opptjeningstype
                bruk = true
                ar = opptjeningFolketrygden.ar ?: 0
                pia = 0
                grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
                maksUforegrad = opptjeningFolketrygden.maksUforegrad ?: 0

                if (opptjeningstype == OpptjeningtypeEnum.PPI) {
                    pi = opptjeningFolketrygden.pensjonsgivendeInntekt ?: 0
                    pp = opptjeningFolketrygden.registrertePensjonspoeng ?: 0.0
                } else if (opptjeningstype == OpptjeningtypeEnum.OSFE) {
                    pi = 0
                    pp = opptjeningFolketrygden.omsorgspoeng ?: 0.0
                }
            }

        private fun inntektsgrunnlagListeForSoeker(
            simuleringType: SimuleringTypeEnum,
            uttaksdato: LocalDate,
            personopplysninger: Personopplysninger,
            grunnbeloep: Int
        ): MutableList<Inntektsgrunnlag> {
            val inntektsgrunnlagList = mutableListOf<Inntektsgrunnlag>()

            val forventetPensjongivendeInntekt = Inntektsgrunnlag().apply {
                bruk = true
                inntektTypeEnum = InntekttypeEnum.FPI
                grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
                fom = uttaksdato.toNorwegianDateAtNoon()
                belop = soekerBeloep(simuleringType, personopplysninger, grunnbeloep)
            }

            inntektsgrunnlagList.add(forventetPensjongivendeInntekt)

            if (simuleringType == AFP) {
                inntektsgrunnlagList.add(
                    inntektMaanedenFoerUttak(
                        uttaksdato,
                        inntektMaanedenFoerAfp = personopplysninger.inntektMndForAfp
                    )
                )
            }

            return inntektsgrunnlagList
        }

        /**
         * For the simulation of AFP and GJENLEVENDE.
         */
        private fun inntektsgrunnlagListeForEps(
            simuleringType: SimuleringTypeEnum,
            uttaksdato: LocalDate,
            personopplysninger: Personopplysninger,
            grunnbeloep: Int
        ): MutableList<Inntektsgrunnlag> {
            val inntektsgrunnlagListe: MutableList<Inntektsgrunnlag> = mutableListOf()

            val forventetPensjongivendeInntekt = Inntektsgrunnlag().apply {
                bruk = true
                inntektTypeEnum = InntekttypeEnum.FPI
                grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
                fom = uttaksdato.toNorwegianDateAtNoon()
                belop =
                    if (simuleringType == GJENLEVENDE) {
                        if (personopplysninger.avdodList.firstOrNull()?.avdodInntektMinst1G == true)
                            grunnbeloep + 1
                        else
                            0
                    } else {
                        personopplysninger.epsData?.let { epsBeloep(eps = it, grunnbeloep) } ?: 0
                    }
            }

            inntektsgrunnlagListe.add(forventetPensjongivendeInntekt)

            if (simuleringType == AFP) {
                val pensjonsinntektFraFolketrygden = Inntektsgrunnlag().apply {
                    bruk = true
                    inntektTypeEnum = InntekttypeEnum.PENF
                    fom = uttaksdato.toNorwegianDateAtNoon()
                    belop = if (personopplysninger.epsData?.epsMottarPensjon == true) 1 else 0
                    grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
                }

                inntektsgrunnlagListe.add(pensjonsinntektFraFolketrygden)
            }

            return inntektsgrunnlagListe
        }

        private fun inntektMaanedenFoerUttak(
            uttaksdato: LocalDate,
            inntektMaanedenFoerAfp: Int?
        ): Inntektsgrunnlag = Inntektsgrunnlag().apply {
            val fomDato = uttaksdato.minusMonths(1)
            bruk = true
            inntektTypeEnum = InntekttypeEnum.IMFU
            grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
            fom = fomDato.toNorwegianDateAtNoon()
            tom = fomDato.with(lastDayOfMonth()).toNorwegianDateAtNoon()
            belop = inntektMaanedenFoerAfp ?: 0
        }

        /**
         * Used when simulating BARN and GJENLEVENDE, and the deceased died from yrkesskade.
         */
        private fun yrkesskadegrunnlag(avdoed: AvdoedData) =
            Yrkesskadegrunnlag().apply {
                yrkeEnum = YrkeYrkesskadeEnum.ARB
                yug = 100
                yst = avdoed.datoForDodsfall?.toNorwegianDateAtNoon()
                antattArligInntekt = avdoed.inntektPaaDodstidspunktHvisYrkesskade ?: 0
                bruk = true
            }

        private fun soekerBeloep(
            simuleringType: SimuleringTypeEnum,
            personopplysninger: Personopplysninger,
            grunnbeloep: Int
        ): Int =
            when (simuleringType) {
                // isUnderUtdanning is null if the user is under 18 years old
                BARN -> if (personopplysninger.erUnderUtdanning == true) grunnbeloep * 2 else 0
                GJENLEVENDE -> personopplysninger.forventetArbeidsinntektGjenlevende?.toInt() ?: 0
                else -> personopplysninger.forventetArbeidsinntekt ?: 0
            }

        private fun epsBeloep(eps: EpsData, grunnbeloep: Int): Int =
            when {
                eps.epsInntektOver2G == true -> grunnbeloep * 2 + 1
                SivilstatusType.SAMB == eps.valgtSivilstatus && eps.tidligereGiftEllerBarnMedSamboer == false -> 0
                eps.erEpsInntektOver1G == true -> grunnbeloep + 1
                else -> 0
            }

        private fun sivilstatusMatch(epsData: EpsData): Boolean =
            epsData.registrertSivilstatus?.let { it.name == epsData.valgtSivilstatus?.name } == true

        private fun borMedTypeForEps(eps: EpsData): BorMedTypeEnum? =
            when (eps.valgtSivilstatus) {
                SivilstatusType.GIFT,
                SivilstatusType.SEPR -> BorMedTypeEnum.J_EKTEF

                SivilstatusType.GLAD -> BorMedTypeEnum.GLAD_EKT

                SivilstatusType.REPA,
                SivilstatusType.SEPA -> BorMedTypeEnum.J_PARTNER

                SivilstatusType.PLAD -> BorMedTypeEnum.GLAD_PART

                SivilstatusType.SAMB ->
                    if (eps.tidligereGiftEllerBarnMedSamboer == true)
                        BorMedTypeEnum.SAMBOER1_5
                    else
                        BorMedTypeEnum.SAMBOER3_2

                else -> null
            }

        private fun borMedTypeForSoesken(oppdrattSammen: Boolean): BorMedTypeEnum =
            if (oppdrattSammen)
                BorMedTypeEnum.J_SOSKEN
            else
                BorMedTypeEnum.N_SOSKEN

        private fun barnedetalj(person: Personopplysninger, barn: BarneopplysningerData) =
            BarnDetalj().apply {
                annenForelder = null
                underUtdanning = false
                borMedBeggeForeldre = barn.borMedBeggeForeldre
                inntektOver1G = barn.erInntektOver1G
                underUtdanning = person.erUnderUtdanning == true
            }

        private fun soesken(soesken: BarneopplysningerSoeskenData) =
            Soesken().apply {
                iKullMedBruker = soesken.helSosken
            }

        private fun grunnlagsrolleForEps(sivilstatus: SivilstatusType?): GrunnlagsrolleEnum =
            when (sivilstatus) {
                SivilstatusType.GIFT,
                SivilstatusType.GLAD,
                SivilstatusType.SEPR -> GrunnlagsrolleEnum.EKTEF

                SivilstatusType.SAMB -> GrunnlagsrolleEnum.SAMBO

                SivilstatusType.REPA,
                SivilstatusType.PLAD,
                SivilstatusType.SEPA -> GrunnlagsrolleEnum.PARTNER

                else -> throw InvalidArgumentException("Kunne ikke mappe valgtSivilstatus $sivilstatus til GrunnlagsrolleCode")
            }

        private fun sivilstand(eps: EpsData): SivilstandEnum =
            sivilstand(
                angittSivilstatus = eps.valgtSivilstatus,
                registrertSivilstand = eps.registrertSivilstatus
            )

        private fun sivilstand(
            angittSivilstatus: SivilstatusType?,
            registrertSivilstand: SivilstandEnum?
        ): SivilstandEnum =
            when (angittSivilstatus) {
                SivilstatusType.ENKE -> SivilstandEnum.ENKE
                SivilstatusType.GIFT -> SivilstandEnum.GIFT
                SivilstatusType.GJES -> illegal(angittSivilstatus)
                SivilstatusType.GJPA -> SivilstandEnum.GJPA
                SivilstatusType.GJSA -> illegal(angittSivilstatus)
                SivilstatusType.GLAD -> SivilstandEnum.GIFT
                SivilstatusType.NULL -> SivilstandEnum.NULL
                SivilstatusType.PLAD -> illegal(angittSivilstatus)
                SivilstatusType.REPA -> SivilstandEnum.REPA

                SivilstatusType.SAMB ->
                    when (registrertSivilstand) {
                        SivilstandEnum.SKIL -> SivilstandEnum.SKIL
                        else -> SivilstandEnum.UGIF
                    }

                SivilstatusType.SEPA -> SivilstandEnum.SEPA
                SivilstatusType.SEPR -> SivilstandEnum.SEPR
                SivilstatusType.SKIL -> SivilstandEnum.SKIL
                SivilstatusType.SKPA -> SivilstandEnum.SKPA
                SivilstatusType.UGIF -> SivilstandEnum.UGIF

                else -> throw IllegalArgumentException("Unknown SivilstatusType $angittSivilstatus")
            }

        private fun hasSoekerEps(
            simuleringType: SimuleringTypeEnum,
            personopplysninger: Personopplysninger
        ): Boolean =
            when (simuleringType) {
                BARN -> false
                ALDER_M_GJEN, GJENLEVENDE -> true
                else -> epsSivilstatuser.any { it == personopplysninger.epsData?.valgtSivilstatus }
            }

        private fun mapSivilstand(sivilstand: Sivilstandstype): SivilstandEnum? =
            when (sivilstand) {
                Sivilstandstype.UOPPGITT -> SivilstandEnum.NULL
                Sivilstandstype.UGIFT -> SivilstandEnum.UGIF
                Sivilstandstype.ENKE_ELLER_ENKEMANN -> SivilstandEnum.ENKE
                Sivilstandstype.SKILT -> SivilstandEnum.SKIL
                Sivilstandstype.SEPARERT -> SivilstandEnum.SEPR
                Sivilstandstype.REGISTRERT_PARTNER -> SivilstandEnum.REPA
                Sivilstandstype.SEPARERT_PARTNER -> SivilstandEnum.SEPA
                Sivilstandstype.SKILT_PARTNER -> SivilstandEnum.SKPA
                Sivilstandstype.GJENLEVENDE_PARTNER -> SivilstandEnum.GJPA
                else -> null
            }

        private fun statsborgerskap(epsData: EpsData): LandkodeEnum =
            epsData.eps?.person?.personUtland?.statsborgerskap ?: LandkodeEnum.NOR

        private fun illegal(sivilstatus: SivilstatusType): Nothing {
            throw InvalidArgumentException("Sivilstatus $sivilstatus har ingen tilsvarende sivilstand-verdi")
        }
    }
}
