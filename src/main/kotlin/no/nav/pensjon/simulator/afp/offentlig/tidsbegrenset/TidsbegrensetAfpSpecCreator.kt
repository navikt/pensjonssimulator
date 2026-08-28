package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.spec.UtlandPeriodeConverter
import no.nav.pensjon.simulator.core.ufoere.UfoereOpptjeningGrunnlag
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Person
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

@Component
class TidsbegrensetAfpSpecCreator(
    private val grunnbeloepService: GrunnbeloepService,
    private val personService: GeneralPersonService,
    private val time: Time
) {
    fun createSpec(
        uttakFom: LocalDate,
        personinfo: PersonSpec,
        opptjeningListe: List<FolketrygdOpptjeningSpec>,
        utenlandsoppholdListe: List<UtlandPeriode>
    ) =
        Simulering().apply {
            simuleringTypeEnum = SimuleringTypeEnum.AFP
            uttaksdatoLd = uttakFom
            afpOrdningEnum = personinfo.angittAfpOrdning

            persongrunnlagListe = persongrunnlagListe(
                uttakFom,
                personinfo,
                opptjeningListe,
                utenlandsoppholdListe,
                grunnbeloepService.naavaerendeGrunnbeloep()
            )
        }

    private fun persongrunnlagListe(
        uttakFom: LocalDate,
        personinfo: PersonSpec,
        opptjeningListe: List<FolketrygdOpptjeningSpec>,
        utenlandsoppholdListe: List<UtlandPeriode>,
        grunnbeloep: Int
    ): List<Persongrunnlag> {
        val grunnlagListe = mutableListOf(
            persongrunnlagForSoeker(uttakFom, personinfo, opptjeningListe, utenlandsoppholdListe)
        )

        if (epsSivilstatuser.any { it == personinfo.eps?.angittSivilstatus }) {
            grunnlagListe.add(persongrunnlagForEps(uttakFom, personinfo, grunnbeloep))
        }

        return grunnlagListe
    }

    private fun persongrunnlagForSoeker(
        uttakFom: LocalDate,
        personinfo: PersonSpec,
        opptjeningListe: List<FolketrygdOpptjeningSpec>,
        utenlandsoppholdListe: List<UtlandPeriode>
    ): Persongrunnlag {
        val soekerPid = personinfo.pid
        val person: Person = personService.person(soekerPid)
        val foedselsdato = personinfo.foedselsdato

        return Persongrunnlag().apply {
            penPerson = penPerson(soekerPid, penPersonId = 1L)

            antallArUtland = foedselsdato?.let {
                UtlandPeriodeConverter.limitedAntallAar(periodeListe = utenlandsoppholdListe, foedselsdato = it)
            } ?: 0

            personDetaljListe.add(
                persondetaljForSoeker(
                    rolleFom = foedselsdato,
                    sivilstand = personinfo.eps?.let(::sivilstand)
                )
            )

            opptjeningsgrunnlagListe = opptjeningsgrunnlagListe(opptjeningListe)
            inntektsgrunnlagListe = inntektsgrunnlagListeForSoeker(uttakFom, personinfo)
            ufoereOpptjeningGrunnlag = ufoereOpptjeningsgrunnlag()
            fodselsdatoLd = foedselsdato
            statsborgerskapEnum = person.statsborgerskap
            flyktning = personinfo.flyktning
            medlemIFolketrygdenSiste3Ar = true
            over60ArKanIkkeForsorgesSelv = false
            dodsdatoLd = null
            dodAvYrkesskade = false
        }
    }

    private fun persongrunnlagForEps(
        uttakFom: LocalDate,
        personinfo: PersonSpec,
        grunnbeloep: Int
    ): Persongrunnlag {
        val eps: EpsSpec? = personinfo.eps
        val relasjon = eps?.relasjon
        val harRelasjon = relasjon != null && sivilstatusMatch(eps)

        return Persongrunnlag().apply {
            penPerson = if (harRelasjon)
                penPerson(pid = relasjon.person?.pid, penPersonId = 2L)
            else
                penPerson(penPersonId = -2L)

            fodselsdatoLd = if (harRelasjon) personinfo.foedselsdato else epsFoedselsdato()
            antallArUtland = 0
            flyktning = false
            opptjeningsgrunnlagListe = mutableListOf()
            dodAvYrkesskade = false
            medlemIFolketrygdenSiste3Ar = true
            fastsattTrygdetid = true
            eps?.let { personDetaljListe.add(persondetaljForEps(uttakFom, eps = it)) }
            over60ArKanIkkeForsorgesSelv = false
            statsborgerskapEnum = eps?.let(::statsborgerskap)
            inntektsgrunnlagListe =
                eps?.let { inntektsgrunnlagListeForEps(uttakFom, eps = it, grunnbeloep) } ?: mutableListOf()
        }
    }

    private fun epsFoedselsdato(): LocalDate =
        time.today().minusYears(DEFAULT_EPS_ALDER_AAR.toLong())

    companion object {
        private const val DEFAULT_EPS_ALDER_AAR = 59

        private val epsSivilstatuser =
            arrayOf(
                SivilstatusType.GIFT,
                SivilstatusType.REPA,
                SivilstatusType.SAMB
            )

        private fun penPerson(pid: Pid? = null, penPersonId: Long) =
            PenPerson().apply {
                this.pid = pid
                this.penPersonId = penPersonId
            }

        private fun persondetaljForSoeker(rolleFom: LocalDate?, sivilstand: SivilstandEnum?) =
            PersonDetalj().apply {
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                penRolleFom = rolleFom
                sivilstandTypeEnum = sivilstand
                bruk = true
                finishInit()
            }

        private fun persondetaljForEps(uttakFom: LocalDate, eps: EpsSpec) =
            PersonDetalj().apply {
                grunnlagsrolleEnum = grunnlagsrolleForEps(eps.angittSivilstatus)
                penRolleFom = eps.relasjon?.fom ?: uttakFom.minusDays(1)
                borMedEnum = eps.let(::borMedTypeForEps)
                bruk = true
                finishInit()
            }

        private fun opptjeningsgrunnlagListe(
            opptjeningListe: List<FolketrygdOpptjeningSpec>
        ): MutableList<Opptjeningsgrunnlag> {
            val opptjeningsgrunnlagList = mutableListOf<Opptjeningsgrunnlag>()

            opptjeningListe.forEach {
                if ((it.omsorgspoeng ?: 0.0) > 0) {
                    opptjeningsgrunnlagList.add(
                        opptjeningsgrunnlag(
                            opptjening = it,
                            opptjeningstype = OpptjeningtypeEnum.OSFE
                        )
                    )
                }

                if ((it.pensjonsgivendeInntekt ?: 0) > 0) {
                    opptjeningsgrunnlagList.add(
                        opptjeningsgrunnlag(
                            opptjening = it,
                            opptjeningstype = OpptjeningtypeEnum.PPI
                        )
                    )
                }
            }

            return opptjeningsgrunnlagList
        }

        private fun opptjeningsgrunnlag(
            opptjening: FolketrygdOpptjeningSpec,
            opptjeningstype: OpptjeningtypeEnum
        ) =
            Opptjeningsgrunnlag().apply {
                opptjeningTypeEnum = opptjeningstype
                bruk = true
                ar = opptjening.aar ?: 0
                pia = 0
                grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
                maksUforegrad = opptjening.maxUfoeregrad ?: 0

                if (opptjeningstype == OpptjeningtypeEnum.PPI) {
                    pi = opptjening.pensjonsgivendeInntekt ?: 0
                    pp = opptjening.registrertePensjonspoeng ?: 0.0
                } else if (opptjeningstype == OpptjeningtypeEnum.OSFE) {
                    pi = 0
                    pp = opptjening.omsorgspoeng ?: 0.0
                }
            }

        private fun ufoereOpptjeningsgrunnlag() =
            UfoereOpptjeningGrunnlag().apply {
                maksUtbetalingsgradPerArUTListe = mutableListOf()
            }

        private fun inntektsgrunnlagListeForSoeker(
            uttakFom: LocalDate,
            personinfo: PersonSpec
        ): MutableList<Inntektsgrunnlag> =
            mutableListOf(
                forventetPensjonsgivendeInntekt(
                    inntektFom = uttakFom,
                    beloep = personinfo.forventetArbeidsinntekt ?: 0
                ),
                inntektMaanedenFoerUttak(
                    inntektFom = uttakFom.minusMonths(1),
                    beloep = personinfo.inntektMaanedenFoerAfp ?: 0
                )
            )

        private fun inntektsgrunnlagListeForEps(
            uttakFom: LocalDate,
            eps: EpsSpec,
            grunnbeloep: Int
        ): MutableList<Inntektsgrunnlag> =
            mutableListOf(
                forventetPensjonsgivendeInntekt(
                    inntektFom = uttakFom,
                    beloep = epsInntektBeloep(eps, grunnbeloep)
                ),
                pensjonsinntektFraFolketrygden(
                    inntektFom = uttakFom,
                    beloep = if (eps.mottarPensjon == true) 1 else 0
                )
            )

        private fun pensjonsinntektFraFolketrygden(inntektFom: LocalDate, beloep: Int) =
            simulertInntektsgrunnlag(type = InntekttypeEnum.PENF, beloep, inntektFom)

        private fun forventetPensjonsgivendeInntekt(inntektFom: LocalDate, beloep: Int) =
            simulertInntektsgrunnlag(type = InntekttypeEnum.FPI, beloep, inntektFom)

        private fun simulertInntektsgrunnlag(
            type: InntekttypeEnum,
            beloep: Int,
            fom: LocalDate,
            tom: LocalDate? = null
        ) =
            Inntektsgrunnlag().apply {
                inntektTypeEnum = type
                belop = beloep
                fomLd = fom
                tomLd = tom
                bruk = true
                grunnlagKildeEnum = GrunnlagkildeEnum.SIMULERING
            }

        private fun inntektMaanedenFoerUttak(inntektFom: LocalDate, beloep: Int) =
            simulertInntektsgrunnlag(
                type = InntekttypeEnum.IMFU,
                beloep,
                fom = inntektFom,
                tom = inntektFom.with(lastDayOfMonth())
            )

        private fun epsInntektBeloep(eps: EpsSpec, grunnbeloep: Int): Int =
            when {
                eps.harInntektOver2G == true -> grunnbeloep * 2 + 1
                SivilstatusType.SAMB == eps.angittSivilstatus && eps.tidligereGiftEllerBarnMedSamboer == false -> 0
                eps.harInntektOver1G == true -> grunnbeloep + 1
                else -> 0
            }

        private fun sivilstatusMatch(eps: EpsSpec): Boolean =
            //TODO make more robust
            eps.registrertSivilstand?.let { it.name == eps.angittSivilstatus?.name } == true

        private fun borMedTypeForEps(eps: EpsSpec): BorMedTypeEnum? =
            when (eps.angittSivilstatus) {
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

        private fun grunnlagsrolleForEps(sivilstatus: SivilstatusType?): GrunnlagsrolleEnum =
            when (sivilstatus) {
                SivilstatusType.GIFT,
                SivilstatusType.GLAD,
                SivilstatusType.SEPR -> GrunnlagsrolleEnum.EKTEF

                SivilstatusType.SAMB -> GrunnlagsrolleEnum.SAMBO

                SivilstatusType.REPA,
                SivilstatusType.PLAD,
                SivilstatusType.SEPA -> GrunnlagsrolleEnum.PARTNER

                else -> throw InvalidArgumentException("kunne ikke mappe sivilstatus $sivilstatus til grunnlagsrolle")
            }

        private fun sivilstand(eps: EpsSpec): SivilstandEnum =
            sivilstand(
                angittSivilstatus = eps.angittSivilstatus,
                registrertSivilstand = eps.registrertSivilstand
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

                else -> throw IllegalArgumentException("ukjent angitt sivilstatus $angittSivilstatus")
            }

        private fun statsborgerskap(eps: EpsSpec): LandkodeEnum =
            eps.relasjon?.person?.statsborgerskap ?: LandkodeEnum.NOR

        private fun illegal(sivilstatus: SivilstatusType): Nothing {
            throw InvalidArgumentException("Sivilstatus $sivilstatus har ingen tilsvarende sivilstand-verdi")
        }
    }
}