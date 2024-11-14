package no.nav.pensjon.simulator.core.endring

import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagPersonSpec
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagService
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagSpec
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.OpptjeningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.Inntekt
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.person.PersongrunnlagMapper
import no.nav.pensjon.simulator.core.person.eps.EpsService
import no.nav.pensjon.simulator.core.person.eps.EpsService.Companion.EPS_GRUNNBELOEP_MULTIPLIER
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.person.Pid
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
    fun opprettSoekerGrunnlag(
        person: PenPerson,
        spec: SimuleringSpec,
        endringKravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?
    ): Kravhode {
        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat?.kravId?.let(kravService::fetchKravhode)
        val soekerGrunnlag: Persongrunnlag? = eksisterendeKravhode?.hentPersongrunnlagForSoker()?.let(::Persongrunnlag)

        soekerGrunnlag?.apply {
            bosattLandEnum = LandkodeEnum.NOR
            inngangOgEksportGrunnlag = InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
            sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
            opptjeningsgrunnlagListe = opptjeningGrunnlagListe(spec, endringKravhode, person)
            spec.flyktning?.let { flyktning = it }
            adjustPersonDetaljer(this, spec)
        }?.also {
            endringKravhode.persongrunnlagListe.add(it)
        }

        return endringKravhode
    }

    // SimulerEndringAvAPCommand.opprettPersongrunnlagForEPS
    fun opprettEpsGrunnlag(
        spec: SimuleringSpec,
        endringKravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
        grunnbeloep: Int
    ): Kravhode {
        if (spec.isTpOrigSimulering) {
            addEpsToPersongrunnlag(endringKravhode, spec, grunnbeloep)
            return endringKravhode
        }

        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat?.kravId?.let(kravService::fetchKravhode)

        eksisterendeKravhode?.persongrunnlagListe?.let {
            addEpsToPersongrunnlag(
                endringPersongrunnlagListe = endringKravhode.persongrunnlagListe,
                eksisterendePersongrunnlagListe = it,
                spec,
                beregningInfo = forrigeAlderspensjonBeregningResultat.hentBeregningsinformasjon(),
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
        val opptjeningType = OpptjeningTypeCti(OpptjeningtypeEnum.PPI.name)

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
        beregningInfo: BeregningsInformasjon?,
        grunnbeloep: Int,
    ) {
        eksisterendePersongrunnlagListe.forEach {
            addEpsToPersongrunnlag(
                endringPersongrunnlagListe,
                eksisterendePersongrunnlag = it,
                spec,
                beregningInfo,
                foersteUttakDato = spec.foersteUttakDato,
                grunnbeloep
            )
        }
    }

    // Extracted from SimulerEndringAvAPCommand.doOpprettPersongrunnlagForEPS
    private fun addEpsToPersongrunnlag(
        endringPersongrunnlagListe: MutableList<Persongrunnlag>,
        eksisterendePersongrunnlag: Persongrunnlag,
        spec: SimuleringSpec,
        beregningInfo: BeregningsInformasjon?,
        foersteUttakDato: LocalDate?,
        grunnbeloep: Int
    ) {
        if (avdoedIsValid(eksisterendePersongrunnlag, foersteUttakDato)) {
            endringPersongrunnlagListe.add(relevantPersongrunnlag(eksisterendePersongrunnlag))
        } else if (epsIsValid(eksisterendePersongrunnlag, foersteUttakDato)) {
            endringPersongrunnlagListe.add(
                epsPersongrunnlag(
                    eksisterendePersongrunnlag,
                    spec,
                    beregningInfo,
                    foersteUttakDato,
                    grunnbeloep
                )
            )
        }
    }

    private fun epsPersongrunnlag(
        eksisterendeEps: Persongrunnlag,
        spec: SimuleringSpec,
        beregningInfo: BeregningsInformasjon?,
        foersteUttakDato: LocalDate?,
        grunnbeloep: Int
    ): Persongrunnlag =
        relevantPersongrunnlag(eksisterendeEps).also {
            if (spec.type == SimuleringType.ENDR_ALDER_M_GJEN) {
                convertEpsToAvdoed(it, spec.avdoed!!) // assuming non-null avdoed
            } else if (shouldAddInntektGrunnlagForEps(beregningInfo)) {
                addInntektGrunnlagForEps(it, foersteUttakDato, grunnbeloep)
            }
        }

    // AbstraktSimulerAPFra2011Command.opprettPersongrunnlagForEPS
    // -> OpprettKravHodeHelper.opprettPersongrunnlagForEPS
    private fun addEpsToPersongrunnlag(kravhode: Kravhode, spec: SimuleringSpec, grunnbeloep: Int) {
        //kravhodeCreator.addAlderspensjonEpsGrunnlagToKrav(spec, kravhode, grunnbeloep)
        epsService.addAlderspensjonEpsGrunnlagToKrav(spec, kravhode, grunnbeloep)
    }

    // SimulerEndringAvAPCommand.updateOpptjeningsgrunnlagOnPersongrunnlag
    private fun opptjeningGrunnlagListe(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        person: PenPerson
    ): MutableList<Opptjeningsgrunnlag> {
        val pid = spec.pid
        val foedselDato = person.fodselsdato.toLocalDate()

        if (pid == null || foedselDato == null)
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
        eps.dodsdato = fromLocalDate(avdoed.doedDato)
        eps.personDetaljListe = mutableListOf(personDetalj(avdoed))
        eps.arligPGIMinst1G = avdoed.harInntektOver1G
        eps.medlemIFolketrygdenSiste3Ar = avdoed.erMedlemAvFolketrygden

        eps.opptjeningsgrunnlagListe = oppdaterOpptjeningsgrunnlagFraInntekt(
            opprinneligOpptjeningsgrunnlagListe = eps.opptjeningsgrunnlagListe,
            inntektListe = mutableListOf(inntekt(avdoed)),
            foedselDato = eps.fodselsdato.toLocalDate()
        )
    }

    private companion object {

        private fun beholdningSpec(pid: Pid, persongrunnlag: Persongrunnlag, kravhode: Kravhode) =
            BeholdningerMedGrunnlagSpec(
                pid,
                hentPensjonspoeng = true,
                hentGrunnlagForOpptjeninger = true,
                hentBeholdninger = false,
                harUfoeretrygdKravlinje = kravhode.isUforetrygd(),
                regelverkType = kravhode.regelverkTypeEnum,
                sakType = kravhode.sakType?.let { SakTypeEnum.valueOf(it.name) },
                personSpecListe = listOf(persongrunnlag.let(::personligBeholdningSpec)), //TODO redundant?
                soekerSpec = persongrunnlag.let(::personligBeholdningSpec)
            )

        private fun personligBeholdningSpec(persongrunnlag: Persongrunnlag) =
            BeholdningerMedGrunnlagPersonSpec(
                pid = persongrunnlag.penPerson?.pid!!,
                sisteGyldigeOpptjeningAar = persongrunnlag.sisteGyldigeOpptjeningsAr,
                isGrunnlagRolleSoeker = persongrunnlag.findPersonDetaljIPersongrunnlag(
                    grunnlagsrolle = GrunnlagsrolleEnum.SOKER,
                    checkBruk = true
                ) != null
            )

        // Extracted from SimulerEndringAvAPCommand.doOpprettPersongrunnlagForEPS
        private fun avdoedIsValid(persongrunnlag: Persongrunnlag, foersteUttakDato: LocalDate?): Boolean =
            foersteUttakDato?.let { isPersonDetaljValid(persongrunnlag, it, GrunnlagsrolleEnum.AVDOD) } == true

        // Extracted from SimulerEndringAvAPCommand.doOpprettPersongrunnlagForEPS
        private fun epsIsValid(persongrunnlag: Persongrunnlag, foersteUttakDato: LocalDate?): Boolean =
            foersteUttakDato?.let {
                isPersonDetaljValid(
                    persongrunnlag,
                    it,
                    GrunnlagsrolleEnum.EKTEF,
                    GrunnlagsrolleEnum.PARTNER,
                    GrunnlagsrolleEnum.SAMBO
                )
            } == true

        // SimulerEndringAvAPCommandHelper.shouldAddInntektgrunnlagForEPS
        private fun shouldAddInntektGrunnlagForEps(info: BeregningsInformasjon?) =
            info?.let { it.epsOver2G || it.epsMottarPensjon } == true

        // SimulerEndringAvAPCommandHelper.addInntektgrunnlagForEPS
        private fun addInntektGrunnlagForEps(eps: Persongrunnlag, foersteUttakDato: LocalDate?, grunnbeloep: Int) {
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
                fom = fromLocalDate(inntektFom)
                tom = null
                grunnlagKilde = GrunnlagKildeCti(GrunnlagkildeEnum.BRUKER.name)
                inntektType = InntektTypeCti(InntekttypeEnum.FPI.name) // Forventet pensjongivende inntekt
            }

        // Extracted from OpprettKravHodeHelper.oppdaterOpptjeningsgrunnlagFraInntektListe
        private fun opptjeningsgrunnlag(inntekt: Inntekt, type: OpptjeningTypeCti) =
            Opptjeningsgrunnlag().apply {
                ar = inntekt.inntektAar
                pi = inntekt.beloep.toInt()
                opptjeningType = type
            }

        // Extracted from SimulerEndringAvAPCommandHelper.convertEpsToAvdod
        private fun inntekt(avdoed: Avdoed) =
            Inntekt(
                inntektAar = LocalDate.now().year - 1,
                beloep = avdoed.inntektFoerDoed.toLong()
            )

        // Extracted from SimulerEndringAvAPCommandHelper.convertEpsToAvdod
        private fun personDetalj(avdoed: Avdoed) =
            PersonDetalj().apply {
                bruk = true
                rolleFomDato = fromLocalDate(avdoed.doedDato)
                grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            }

        // SimulerEndringAvAPCommandHelper.isPersonDetaljValid
        private fun isPersonDetaljValid(
            persongrunnlag: Persongrunnlag,
            virkningDato: LocalDate,
            vararg roller: GrunnlagsrolleEnum
        ): Boolean {
            roller.forEach {
                val personDetalj = persongrunnlag.findPersonDetaljWithRolleForPeriode(
                    rolle = it,
                    fromLocalDate(virkningDato),
                    checkBruk = true
                )

                if (isValidInFuture(personDetalj)) {
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
            val grunnlagKilde = GrunnlagKildeCti(GrunnlagkildeEnum.BRUKER.name)
            val komplettListe: MutableList<Opptjeningsgrunnlag> = opprinneligListe.toMutableList()

            inntektBasertListe.forEach {
                it.bruk = true
                it.grunnlagKilde = grunnlagKilde
                komplettListe.add(it)
            }

            return komplettListe
        }

        // SimulerEndringAvAPCommandHelper.createPersongrunnlagWithValidPersonDetaljer + filterAndUpdateInntektsgrunnlaglistOnPersongrunnlag
        private fun relevantPersongrunnlag(source: Persongrunnlag) =
            Persongrunnlag(source).apply {
                inntektsgrunnlagListe =
                    inntektsgrunnlagListe.filter { it.bruk && it.inntektType?.kode != InntekttypeEnum.FPI.name }
                        .toMutableList()
                personDetaljListe = personDetaljListe.filter { it.bruk && it.rolleTomDato == null }.toMutableList()
                // NB: In the original code (SimulerEndringAvAPCommandHelper.createPersongrunnlagWithValidPersonDetaljer)
                // the PersonDetalj objects are copied twice: new Persongrunnlag(...) and then new PersonDetalj(...)
                // which seems unnecessary
            }

        // SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun adjustPersonDetaljer(soekerGrunnlag: Persongrunnlag, spec: SimuleringSpec) {
            val medGjenlevenderett: Boolean = spec.type == SimuleringType.ENDR_ALDER_M_GJEN

            if (medGjenlevenderett) {
                val enke: PersonDetalj = enke(soekerGrunnlag) ?: enke(spec.avdoed?.doedDato)
                soekerGrunnlag.personDetaljListe =
                    mutableListOf(enke) // only a single persondetalj is used when gjenlevenderett
            } else {
                removeIrrelevantPersonDetaljer(soekerGrunnlag)
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun removeIrrelevantPersonDetaljer(soekerGrunnlag: Persongrunnlag) {
            val iterator = soekerGrunnlag.personDetaljListe.iterator()

            while (iterator.hasNext()) {
                with(iterator.next()) {
                    if (this.bruk.not() || isValidInPast(this)) {
                        iterator.remove()
                    }
                }
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun enke(persongrunnlag: Persongrunnlag): PersonDetalj? {
            persongrunnlag.personDetaljListe.forEach {
                if (it.bruk && isEnke(it) && isValidToday(it)) {
                    return it
                }
            }

            return null
        }

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun enke(fom: LocalDate?) =
            PersonDetalj().apply {
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                sivilstandTypeEnum = SivilstandEnum.ENKE
                rolleFomDato = fromLocalDate(fom)
                bruk = true
            }

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun isEnke(detalj: PersonDetalj) =
            detalj.sivilstandTypeEnum == SivilstandEnum.ENKE

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun isValidInPast(detalj: PersonDetalj): Boolean =
            detalj.rolleTomDato?.let { isBeforeByDay(it, LocalDate.now(), false) } == true

        // Extracted from SimulerEndringAvAPCommandHelper.updatePersongrunnlagForBruker
        private fun isValidToday(detalj: PersonDetalj) =
            isDateInPeriod(
                Date(),
                detalj.virkFom,
                detalj.virkTom
            ) // NB: Here virkFom|Tom is used (not rolleFom|TomDato)
        // The relationship between virk- and rolle-dato is described in https://confluence.adeo.no/pages/viewpage.action?pageId=282132550
        // ("Løsningsbeskrivelse - P17 - Periodisering av persongrunnlag - utbedring av periodebegrep i Familieforhold (PK-52707)")
        // and in https://jira.adeo.no/browse/PKDRAGE-3031

        // Extracted from SimulerEndringAvAPCommandHelper.isPersonDetaljValid
        private fun isValidInFuture(detalj: PersonDetalj?) =
            detalj?.let { it.rolleTomDato == null } == true
    }
}
