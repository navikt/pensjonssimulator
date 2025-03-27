package no.nav.pensjon.simulator.core.endring

import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagService
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.Inntekt
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.person.BeholdningUtil.beholdningSpec
import no.nav.pensjon.simulator.core.person.PersongrunnlagMapper
import no.nav.pensjon.simulator.core.person.eps.EpsService
import no.nav.pensjon.simulator.core.person.eps.EpsService.Companion.EPS_GRUNNBELOEP_MULTIPLIER
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.krav.KravService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

/**
 * Handles persongrunnlag in context of 'endring av alderspensjon'.
 * Corresponds to SimulerEndringAvAPCommand (persongrunnlag part) in PEN
 */
@Component
class EndringPersongrunnlag(
    private val context: SimulatorContext,
    private val kravService: KravService,
    private val beholdningService: BeholdningerMedGrunnlagService,
    private val epsService: EpsService,
    private val persongrunnlagMapper: PersongrunnlagMapper
) {
    // SimulerEndringAvAPCommand.opprettPersongrunnlagForBruker
    fun getPersongrunnlagForSoeker(
        person: PenPerson,
        spec: SimuleringSpec,
        endringKravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?
    ): Persongrunnlag? {
        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat?.kravId?.let(kravService::fetchKravhode)

        return eksisterendeKravhode?.hentPersongrunnlagForSoker()
            ?.let(::Persongrunnlag)
            ?.apply {
                bosattLandEnum = LandkodeEnum.NOR
                inngangOgEksportGrunnlag = InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
                sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
                opptjeningsgrunnlagListe = opptjeningGrunnlagListe(spec, endringKravhode, person)
                spec.flyktning?.let { flyktning = it }
                adjustPersondetaljListe(persongrunnlag = this, spec)
            }
    }

    // SimulerEndringAvAPCommand.opprettPersongrunnlagForEPS
    // + AbstraktSimulerAPFra2011Command.opprettPersongrunnlagForEPS
    // -> OpprettKravHodeHelper.opprettPersongrunnlagForEPS
    fun addPersongrunnlagForEpsToKravhode(
        spec: SimuleringSpec,
        endringKravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
        grunnbeloep: Int
    ): Kravhode {
        if (spec.isTpOrigSimulering || spec.epsKanOverskrives) {
            epsService.addPersongrunnlagForEpsToKravhode(spec, endringKravhode, grunnbeloep)
            return endringKravhode
        }

        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat?.kravId?.let(kravService::fetchKravhode)

        eksisterendeKravhode?.persongrunnlagListe?.let {
            addEpsToPersongrunnlag(
                endringPersongrunnlagListe = endringKravhode.persongrunnlagListe,
                eksisterendePersongrunnlagListe = it,
                spec,
                epsPaavirker = forrigeAlderspensjonBeregningResultat.epsPaavirkerBeregningen(),
                grunnbeloep
            )
        }

        return endringKravhode
    }

    // SimulerEndringAvAPCommand.oppdaterOpptjeningsgrunnlagFraInntektListe
    // -> AbstraktSimulerAPFra2011Command.oppdaterOpptjeningsgrunnlagFraInntektListe
    // -> OpprettKravHodeHelper.oppdaterOpptjeningsgrunnlagFraInntektListe
    private fun oppdaterOpptjeningsgrunnlagFraInntekt(
        opprinneligOpptjeningsgrunnlagListe: List<Opptjeningsgrunnlag>,
        inntektListe: List<Inntekt>,
        foedselDato: LocalDate?
    ): MutableList<Opptjeningsgrunnlag> {
        val opptjeningType = OpptjeningtypeEnum.PPI

        var inntektsbasertOpptjeningsgrunnlagListe: MutableList<Opptjeningsgrunnlag> =
            inntektListe
                .filter { it.beloep > 0 }
                .map { opptjeningsgrunnlag(it, opptjeningType) }
                .toMutableList()

        inntektsbasertOpptjeningsgrunnlagListe =
            context.beregnPoengtallBatch(inntektsbasertOpptjeningsgrunnlagListe, foedselDato)
        return komplettOpptjeningsgrunnlag(opprinneligOpptjeningsgrunnlagListe, inntektsbasertOpptjeningsgrunnlagListe)
    }

    // SimulerEndringAvAPCommand.doOpprettPersongrunnlagForEPS
    private fun addEpsToPersongrunnlag(
        endringPersongrunnlagListe: MutableList<Persongrunnlag>,
        eksisterendePersongrunnlagListe: List<Persongrunnlag>,
        spec: SimuleringSpec,
        epsPaavirker: Boolean,
        grunnbeloep: Int,
    ) {
        eksisterendePersongrunnlagListe.forEach {
            addEpsToEndringPersongrunnlagListe(
                endringPersongrunnlagListe,
                eksisterendePersongrunnlag = it,
                spec,
                epsPaavirker,
                foersteUttakDato = spec.foersteUttakDato,
                grunnbeloep
            )
        }
    }

    // Extracted from SimulerEndringAvAPCommand.doOpprettPersongrunnlagForEPS
    private fun addEpsToEndringPersongrunnlagListe(
        endringPersongrunnlagListe: MutableList<Persongrunnlag>,
        eksisterendePersongrunnlag: Persongrunnlag,
        spec: SimuleringSpec,
        epsPaavirker: Boolean,
        foersteUttakDato: LocalDate?,
        grunnbeloep: Int
    ) {
        if (avdoedIsValid(eksisterendePersongrunnlag, foersteUttakDato)) {
            endringPersongrunnlagListe.add(relevantPersongrunnlag(eksisterendePersongrunnlag))
        } else if (epsIsValid(eksisterendePersongrunnlag, foersteUttakDato)) {
            endringPersongrunnlagListe.add(
                endringPersongrunnlagForEps(
                    eksisterendePersongrunnlag,
                    spec,
                    epsPaavirker,
                    foersteUttakDato,
                    grunnbeloep
                )
            )
        }
    }

    private fun endringPersongrunnlagForEps(
        eksisterendeEps: Persongrunnlag,
        spec: SimuleringSpec,
        epsPaavirker: Boolean,
        foersteUttakDato: LocalDate?,
        grunnbeloep: Int
    ): Persongrunnlag =
        relevantPersongrunnlag(eksisterendeEps).also {
            if (spec.type == SimuleringType.ENDR_ALDER_M_GJEN) {
                convertEpsToAvdoed(it, spec.avdoed!!) // assuming non-null avdoed
            } else if (epsPaavirker) {
                addInntektsgrunnlagForEps(it, foersteUttakDato, grunnbeloep)
            }
        }

    // SimulerEndringAvAPCommand.updateOpptjeningsgrunnlagOnPersongrunnlag
    private fun opptjeningGrunnlagListe(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        person: PenPerson
    ): MutableList<Opptjeningsgrunnlag> {
        val pid = spec.pid

        if (pid == null || person.foedselsdato == null)
            return mutableListOf()

        val persongrunnlag: Persongrunnlag = persongrunnlagMapper.mapToPersongrunnlag(person, spec)

        // NB: The original code in SimulerEndringAvAPCommand in PEN used
        // "midlertidig persongrunnlag - skal kun benyttes til å hente opptjeningsgrunnlag fra POPP"
        // This is not necessary when using the data-minimised variant of 'fetchBeholdningerMedGrunnlag'
        // (which does not use 'kravhode')

        return beholdningService.getBeholdningerMedGrunnlag(beholdningSpec(pid, persongrunnlag, kravhode))
            .opptjeningGrunnlagListe.toMutableList()
    }

    // SimulerEndringAvAPCommandHelper.convertEpsToAvdod
    private fun convertEpsToAvdoed(eps: Persongrunnlag, avdoed: Avdoed) {
        eps.dodsdato = avdoed.doedDato.toNorwegianDateAtNoon()
        eps.personDetaljListe = mutableListOf(persondetalj(avdoed))
        eps.arligPGIMinst1G = avdoed.harInntektOver1G
        eps.medlemIFolketrygdenSiste3Ar = avdoed.erMedlemAvFolketrygden

        eps.opptjeningsgrunnlagListe = oppdaterOpptjeningsgrunnlagFraInntekt(
            opprinneligOpptjeningsgrunnlagListe = eps.opptjeningsgrunnlagListe,
            inntektListe = mutableListOf(inntekt(avdoed)),
            foedselDato = eps.fodselsdato?.toNorwegianLocalDate()
        )
    }

    private companion object {

        // Extracted from SimulerEndringAvAPCommand.doOpprettPersongrunnlagForEPS
        private fun avdoedIsValid(persongrunnlag: Persongrunnlag, foersteUttakDato: LocalDate?): Boolean =
            foersteUttakDato?.let { isPersondetaljValid(persongrunnlag, it, GrunnlagsrolleEnum.AVDOD) } == true

        // Extracted from SimulerEndringAvAPCommand.doOpprettPersongrunnlagForEPS
        private fun epsIsValid(persongrunnlag: Persongrunnlag, foersteUttakDato: LocalDate?): Boolean =
            foersteUttakDato?.let {
                isPersondetaljValid(
                    persongrunnlag,
                    virkningDato = it,
                    GrunnlagsrolleEnum.EKTEF,
                    GrunnlagsrolleEnum.PARTNER,
                    GrunnlagsrolleEnum.SAMBO
                )
            } == true

        // SimulerEndringAvAPCommandHelper.addInntektgrunnlagForEPS
        private fun addInntektsgrunnlagForEps(eps: Persongrunnlag, foersteUttakDato: LocalDate?, grunnbeloep: Int) {
            val inntektFom: LocalDate = findFomDatoInntekt(foersteUttakDato)
            eps.inntektsgrunnlagListe.add(epsInntektsgrunnlag(grunnbeloep, inntektFom))
        }

        // SimulerEndringAvAPCommandHelper.findFomDatoInntekt
        private fun findFomDatoInntekt(foersteUttakDato: LocalDate?): LocalDate {
            //val dato = if (isBeforeToday(foersteUttakDato)) foersteUttakDato else LocalDate.now()
            val now = LocalDate.now()
            val dato = foersteUttakDato?.let { if (it.isBefore(now)) it else now } ?: now
            return LocalDate.of(dato.year, 1, 1)
        }

        // SimulerEndringAvAPCommandHelper.createInntektsgrunnlagForEPS
        // Assuming kopiertFraGammeltKrav and registerKilde are not used
        private fun epsInntektsgrunnlag(grunnbeloep: Int, inntektFom: LocalDate) =
            Inntektsgrunnlag().apply {
                belop = EPS_GRUNNBELOEP_MULTIPLIER * grunnbeloep
                bruk = true
                fom = inntektFom.toNorwegianDateAtNoon()
                tom = null
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
                inntektTypeEnum = InntekttypeEnum.FPI // Forventet pensjongivende inntekt
            }

        // Extracted from OpprettKravHodeHelper.oppdaterOpptjeningsgrunnlagFraInntektListe
        private fun opptjeningsgrunnlag(inntekt: Inntekt, type: OpptjeningtypeEnum) =
            Opptjeningsgrunnlag().apply {
                ar = inntekt.inntektAar
                pi = inntekt.beloep.toInt()
                opptjeningTypeEnum = type
            }

        // Extracted from SimulerEndringAvAPCommandHelper.convertEpsToAvdod
        private fun inntekt(avdoed: Avdoed) =
            Inntekt(
                inntektAar = LocalDate.now().year - 1,
                beloep = avdoed.inntektFoerDoed.toLong()
            )

        // Extracted from SimulerEndringAvAPCommandHelper.convertEpsToAvdod
        private fun persondetalj(avdoed: Avdoed) =
            PersonDetalj().apply {
                bruk = true
                penRolleFom = avdoed.doedDato.toNorwegianDateAtNoon()
                grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            }.also {
                it.finishInit()
            }

        // SimulerEndringAvAPCommandHelper.isPersonDetaljValid
        private fun isPersondetaljValid(
            persongrunnlag: Persongrunnlag,
            virkningDato: LocalDate,
            vararg roller: GrunnlagsrolleEnum
        ): Boolean {
            roller.forEach {
                val detalj = persongrunnlag.findPersonDetaljWithRolleForPeriode(
                    rolle = it,
                    virkningDato = virkningDato.toNorwegianDateAtNoon(),
                    checkBruk = true
                )

                if (isValidInFuture(detalj)) {
                    return true
                }
            }

            return false
        }

        // Extracted from OpprettKravHodeHelper.oppdaterOpptjeningsgrunnlagFraInntektListe
        private fun komplettOpptjeningsgrunnlag(
            opprinneligListe: List<Opptjeningsgrunnlag>,
            inntektBasertListe: MutableList<Opptjeningsgrunnlag>
        ): MutableList<Opptjeningsgrunnlag> {
            val grunnlagKilde = GrunnlagkildeEnum.BRUKER
            val komplettListe: MutableList<Opptjeningsgrunnlag> = opprinneligListe.toMutableList()

            inntektBasertListe.forEach {
                it.bruk = true
                it.grunnlagKildeEnum = grunnlagKilde
                komplettListe.add(it)
            }

            return komplettListe
        }

        // SimulerEndringAvAPCommandHelper.createPersongrunnlagWithValidPersonDetaljer + filterAndUpdateInntektsgrunnlaglistOnPersongrunnlag
        private fun relevantPersongrunnlag(source: Persongrunnlag) =
            Persongrunnlag(source).apply {
                inntektsgrunnlagListe = inntektsgrunnlagListe.filter {
                    it.bruk == true && InntekttypeEnum.FPI != it.inntektTypeEnum
                }.toMutableList()

                personDetaljListe = personDetaljListe.filter {
                    it.bruk == true && it.penRolleTom == null
                }.toMutableList()
                // NB: In the original code (SimulerEndringAvAPCommandHelper.createPersongrunnlagWithValidPersonDetaljer)
                // the PersonDetalj objects are copied twice: new Persongrunnlag(...) and then new PersonDetalj(...)
                // which seems unnecessary
            }

        // SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun adjustPersondetaljListe(persongrunnlag: Persongrunnlag, spec: SimuleringSpec) {
            val medGjenlevenderett: Boolean = spec.type == SimuleringType.ENDR_ALDER_M_GJEN

            if (medGjenlevenderett) {
                val enke: PersonDetalj = enke(persongrunnlag) ?: enke(spec.avdoed?.doedDato)
                persongrunnlag.personDetaljListe =
                    mutableListOf(enke) // only a single persondetalj is used when gjenlevenderett
            } else {
                beholdRelevantePersondetaljer(persongrunnlag)
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun beholdRelevantePersondetaljer(grunnlag: Persongrunnlag) {
            val iterator = grunnlag.personDetaljListe.iterator()

            while (iterator.hasNext()) {
                with(iterator.next()) {
                    if (this.bruk != true || isValidInPast(detalj = this)) {
                        iterator.remove()
                    }
                }
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun enke(persongrunnlag: Persongrunnlag): PersonDetalj? =
            persongrunnlag.personDetaljListe.firstOrNull { it.bruk == true && isEnke(it) && isValidToday(it) }

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun enke(fom: LocalDate?) =
            PersonDetalj().apply {
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                sivilstandTypeEnum = SivilstandEnum.ENKE
                penRolleFom = fom?.toNorwegianDateAtNoon()
                bruk = true
            }.also {
                it.finishInit()
            }

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun isEnke(detalj: PersonDetalj) =
            detalj.sivilstandTypeEnum == SivilstandEnum.ENKE

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun isValidInPast(detalj: PersonDetalj): Boolean =
            detalj.penRolleTom?.let { isBeforeByDay(it, LocalDate.now(), false) } == true

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun isValidToday(detalj: PersonDetalj) =
            isDateInPeriod(
                dato = Date(),
                fom = detalj.virkFom,
                tom = detalj.virkTom
            ) // NB: Here virkFom|Tom is used (not rolleFom|TomDato)
        // The relationship between virk- and rolle-dato is described in https://confluence.adeo.no/pages/viewpage.action?pageId=282132550
        // ("Løsningsbeskrivelse - P17 - Periodisering av persongrunnlag - utbedring av periodebegrep i Familieforhold (PK-52707)")
        // and in https://jira.adeo.no/browse/PKDRAGE-3031

        // Extracted from SimulerEndringAvAPCommandHelper.isPersonDetaljValid
        private fun isValidInFuture(detalj: PersonDetalj?) =
            detalj?.let { it.penRolleTom == null } == true
    }
}
