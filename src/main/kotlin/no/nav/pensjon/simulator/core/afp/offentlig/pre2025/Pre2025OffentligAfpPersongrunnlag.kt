package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InngangOgEksportGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.person.PersongrunnlagService
import no.nav.pensjon.simulator.core.person.eps.EpsService
import no.nav.pensjon.simulator.core.person.eps.EpsService.Companion.EPS_GRUNNBELOEP_MULTIPLIER
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianNoon
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDag
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate

// Corresponds to SimulerAFPogAPCommand (persongrunnlag part) in PEN
@Component
class Pre2025OffentligAfpPersongrunnlag(
    private val kravService: KravService,
    private val persongrunnlagService: PersongrunnlagService,
    private val epsService: EpsService,
    private val time: Time
) {
    // SimulerAFPogAPCommand.opprettPersongrunnlagForBruker
    // + AbstraktSimulerAPFra2011Command.opprettPersongrunnlagForBruker
    // -> OpprettKravHodeHelper.opprettPersongrunnlagForBruker
    fun getPersongrunnlagForSoeker(
        person: PenPerson,
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?
    ): Persongrunnlag? {
        if (forrigeAlderspensjonBeregningResultat == null) {
            return persongrunnlagService.getPersongrunnlagForSoeker(spec, kravhode, person)
        }

        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat.kravId?.let(kravService::fetchKravhode)

        if (eksisterendeKravhode?.findPersonDetaljIBruk(GrunnlagsrolleEnum.SOKER) == null) {
            //TODO check eksisterendeKravhode.hentPersongrunnlagForSoker() == null instead (see below)
            return null
        }

        return persongrunnlag(
            source = eksisterendeKravhode.hentPersongrunnlagForSoker(),
            spec
        )
    }

    // SimulerAFPogAPCommand.opprettPersongrunnlagForEPS
    // + AbstraktSimulerAPFra2011Command.opprettPersongrunnlagForEPS
    // -> OpprettKravHodeHelper.opprettPersongrunnlagForEPS
    fun addPersongrunnlagForEpsToKravhode(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
        grunnbeloep: Int
    ): Kravhode {
        if (forrigeAlderspensjonBeregningResultat == null) {
            epsService.addPersongrunnlagForEpsToKravhode(spec, kravhode, grunnbeloep)
            return kravhode
        }

        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat.kravId?.let(kravService::fetchKravhode)

        val epsAndAvdoedGrunnlagListe: List<Persongrunnlag> =
            copyVirksomPersongrunnlagForEpsAndAvdod(eksisterendeKravhode)

        val avdoedGrunnlag: Persongrunnlag? =
            persongrunnlagHavingRolle(epsAndAvdoedGrunnlagListe, GrunnlagsrolleEnum.AVDOD)

        val epsGrunnlag: Persongrunnlag? = persongrunnlagHavingRolle(
            epsAndAvdoedGrunnlagListe,
            GrunnlagsrolleEnum.EKTEF,
            GrunnlagsrolleEnum.SAMBO,
            GrunnlagsrolleEnum.PARTNER
        )

        avdoedGrunnlag?.let {
            retainPersondetaljerHavingVirksomRolle(it)
            it.sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
            kravhode.persongrunnlagListe.add(it)
            return kravhode
        }

        epsGrunnlag?.let {
            // SimulerAFPogAPCommand.shouldAddInntektgrunnlagForEPS
            if (forrigeAlderspensjonBeregningResultat.epsPaavirkerBeregningen()) {
                retainPersondetaljerHavingVirksomRolle(it)
                it.sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
                addEpsInntektGrunnlag(foersteUttakDato = spec.foersteUttakDato!!, grunnbeloep, persongrunnlag = it)
                kravhode.persongrunnlagListe.add(it)
            }
        }

        return kravhode
    }

    // Extracted from SimulerAFPogAPCommand.opprettPersongrunnlagForBruker
    private fun persongrunnlag(source: Persongrunnlag, spec: SimuleringSpec) =
        Persongrunnlag(
            source,
            excludeForsteVirkningsdatoGrunnlag = true
        ).apply {
            beholdKunEnkeHvisEnSlikPersondetaljFinnes(persongrunnlag = this)
            beholdVirksommePersondetaljer(persongrunnlag = this)
            spec.flyktning?.let { this.flyktning = it }
            this.antallArUtland = spec.utlandAntallAar
            this.sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
            this.bosattLandEnum = LandkodeEnum.NOR
            this.inngangOgEksportGrunnlag = InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
        }

    // PEN: SimulerAFPogAPCommand.addInntektgrunnlagForEPS
    private fun addEpsInntektGrunnlag(
        foersteUttakDato: LocalDate,
        grunnbeloep: Int,
        persongrunnlag: Persongrunnlag
    ) {
        persongrunnlag.inntektsgrunnlagListe.add(
            epsInntektGrunnlag(
                grunnbeloep,
                fomDatoInntekt = inntektFom(foersteUttakDato)
            )
        )
    }

    // PEN: SimulerAFPogAPCommand.findFomDatoInntekt
    private fun inntektFom(foersteUttakDato: LocalDate): LocalDate {
        val today = time.today()

        val date: LocalDate =
            if (foersteUttakDato.isBefore(today)) foersteUttakDato
            else today

        return foersteDag(date.year)
    }

    // PEN: SimulerAFPogAPCommandHelper.filterPersondetaljIfSivilstandsTypeEnkeExists
    private fun beholdKunEnkeHvisEnSlikPersondetaljFinnes(persongrunnlag: Persongrunnlag) {
        var brukEnkeListe = false
        val enkeListe: MutableList<PersonDetalj> = mutableListOf()

        persongrunnlag.personDetaljListe.forEach {
            if (it.bruk == true && gjelderEnke(it) && strengtVirksom(it)) {
                enkeListe.add(it)
                brukEnkeListe = true
            }
        }

        if (brukEnkeListe) {
            persongrunnlag.personDetaljListe = enkeListe
        }
    }

    // PEN: SimulerAFPogAPCommandHelper.removeAllPersondetaljWithTomDateBeforeToday
    private fun beholdVirksommePersondetaljer(persongrunnlag: Persongrunnlag) {
        persongrunnlag.personDetaljListe =
            persongrunnlag.personDetaljListe.filter {
                it.bruk == true && virksom(it)
            }.toMutableList()
    }

    // Extracted from SimulerAFPogAPCommandHelper.filterPersondetaljIfSivilstandsTypeEnkeExists in PEN
    private fun strengtVirksom(detalj: PersonDetalj) = virksom(detalj, allowSameDay = false)

    // Extracted from SimulerAFPogAPCommandHelper.removeAllPersondetaljWithTomDateBeforeToday in PEN
    private fun virksom(detalj: PersonDetalj) = virksom(detalj, allowSameDay = true)

    private fun virksom(detalj: PersonDetalj, allowSameDay: Boolean) =
        detalj.virkTom == null ||
                isAfterByDay(detalj.virkTom?.toNorwegianNoon(), time.today().toNorwegianDateAtNoon(), allowSameDay)

    companion object {

        // SimulerAFPogAPCommandHelper.getPgFromListIfContainsPdForGrunnlagsrolle
        fun persongrunnlagHavingRolle(
            persongrunnlagListe: List<Persongrunnlag>,
            vararg grunnlagRoller: GrunnlagsrolleEnum
        ): Persongrunnlag? {
            persongrunnlagListe.forEach {
                for (rolle in grunnlagRoller) {
                    if (it.findPersonDetaljIPersongrunnlag(rolle, checkBruk = true) != null) {
                        return it
                    }
                }
            }

            return null
        }

        // Extracted from SimulerAFPogAPCommandHelper.filterPersondetaljIfSivilstandsTypeEnkeExists
        private fun gjelderEnke(detalj: PersonDetalj) = SivilstandEnum.ENKE == detalj.sivilstandTypeEnum

        // SimulerAFPogAPCommand.createInntektsgrunnlagForEPS
        private fun epsInntektGrunnlag(grunnbeloep: Int, fomDatoInntekt: LocalDate) =
            Inntektsgrunnlag().apply {
                belop = EPS_GRUNNBELOEP_MULTIPLIER * grunnbeloep
                bruk = true
                fom = fomDatoInntekt.toNorwegianDateAtNoon() // is set to noon in property
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
                inntektTypeEnum = InntekttypeEnum.FPI
                //kopiertFraGammeltKrav = false <--- not used by pensjon-regler
                //registerKilde = null <--- not used by pensjon-regler
                tom = null
            }

        // SimulerAFPogAPCommandHelper.removeInvalidPersondetaljFromPersongrunnlag
        private fun retainPersondetaljerHavingVirksomRolle(persongrunnlag: Persongrunnlag) {
            persongrunnlag.personDetaljListe =
                persongrunnlag.personDetaljListe.filter {
                    it.bruk == true && it.penRolleTom == null
                }.toMutableList()
        }

        // SimulerAFPogAPCommandHelper.getCopyOfPersongrunnlagForEPSAndAvdodWhereTomDateIsNull
        private fun copyVirksomPersongrunnlagForEpsAndAvdod(kravhode: Kravhode?): List<Persongrunnlag> {
            val epsGrunnlagListe: MutableList<Persongrunnlag> = mutableListOf()

            copyAndAddPersongrunnlag(
                epsGrunnlagListe,
                kravhode?.findPersonGrunnlagIGrunnlagsRolle(GrunnlagsrolleEnum.EKTEF),
                kravhode?.findPersonGrunnlagIGrunnlagsRolle(GrunnlagsrolleEnum.PARTNER),
                kravhode?.findPersonGrunnlagIGrunnlagsRolle(GrunnlagsrolleEnum.SAMBO),
                kravhode?.findPersonGrunnlagIGrunnlagsRolle(GrunnlagsrolleEnum.AVDOD)
            )

            return epsGrunnlagListe
        }

        // SimulerAFPogAPCommandHelper.copyAndAddPersongrunnlagIfNotNull
        private fun copyAndAddPersongrunnlag(
            epsGrunnlagListe: MutableList<Persongrunnlag>,
            vararg persongrunnlagListe: Persongrunnlag?
        ) {
            persongrunnlagListe.forEach {
                if (it != null) {
                    val copy = Persongrunnlag(
                        source = it,
                        excludeForsteVirkningsdatoGrunnlag = true
                    )
                    beholdForventetPensjongivendeInntekt(copy)
                    addPersongrunnlagToEpsListForEachVirksomPersondetalj(it.personDetaljListe, epsGrunnlagListe, copy)
                }
            }
        }

        // Extracted from SimulerAFPogAPCommandHelper.copyAndAddPersongrunnlagIfNotNull
        private fun addPersongrunnlagToEpsListForEachVirksomPersondetalj(
            persondetaljListe: MutableList<PersonDetalj>,
            epsGrunnlagListe: MutableList<Persongrunnlag>,
            persongrunnlag: Persongrunnlag
        ) {
            persondetaljListe.forEach {
                if (it.bruk == true && it.virkTom == null) {
                    epsGrunnlagListe.add(persongrunnlag)
                }
            }
        }

        // SimulerAFPogAPCommandHelper.filterAndUpdateInntektsgrunnlaglistOnPersongrunnlag
        private fun beholdForventetPensjongivendeInntekt(persongrunnlag: Persongrunnlag) {
            persongrunnlag.inntektsgrunnlagListe =
                persongrunnlag.inntektsgrunnlagListe
                    .filter { it.bruk == true && isForventetPensjongivendeInntekt(it) }
                    .toMutableList()
        }

        private fun isForventetPensjongivendeInntekt(grunnlag: Inntektsgrunnlag): Boolean =
            InntekttypeEnum.FPI != grunnlag.inntektTypeEnum
    }
}
