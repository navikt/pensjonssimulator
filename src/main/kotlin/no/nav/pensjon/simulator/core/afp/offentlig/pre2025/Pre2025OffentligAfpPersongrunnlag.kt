package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InngangOgEksportGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.KravhodeCreator
import no.nav.pensjon.simulator.core.legacy.util.DateUtil
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toDate
import no.nav.pensjon.simulator.krav.KravService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// Corresponds to SimulerAFPogAPCommand (persongrunnlag part)
@Component
class Pre2025OffentligAfpPersongrunnlag(
    val kravhodeCreator: KravhodeCreator,
    val kravService: KravService
) {
    // SimulerAFPogAPCommand.opprettPersongrunnlagForBruker
    fun opprettSoekerGrunnlag(
        person: PenPerson,
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?
    ): Kravhode {
        if (forrigeAlderspensjonBeregningResultat == null) {
            return opprettSoekerPersongrunnlag(spec, kravhode, person)
        }

        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat.kravId?.let(kravService::fetchKravhode)

        if (eksisterendeKravhode?.findPersonDetaljIBruk(GrunnlagsrolleEnum.SOKER) == null) {
            return kravhode
        }

        val eksisterendeSoekerGrunnlag: Persongrunnlag = eksisterendeKravhode.hentPersongrunnlagForSoker()
        kravhode.persongrunnlagListe.add(persongrunnlag(eksisterendeSoekerGrunnlag, spec))
        return kravhode
    }

    // SimulerAFPogAPCommand.opprettPersongrunnlagForEPS
    fun opprettEpsGrunnlag(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
        grunnbeloep: Int
    ): Kravhode {
        if (forrigeAlderspensjonBeregningResultat == null) {
            opprettEpsPersongrunnlag(spec, kravhode, grunnbeloep)
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
            if (shouldAddEpsInntektGrunnlag(forrigeAlderspensjonBeregningResultat.hentBeregningsinformasjon())) {
                retainPersondetaljerHavingVirksomRolle(it)
                it.sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
                addEpsInntektGrunnlag(foersteUttakDato = spec.foersteUttakDato, grunnbeloep, persongrunnlag = it)
                kravhode.persongrunnlagListe.add(it)
            }
        }

        return kravhode
    }

    // Extracted from SimulerAFPogAPCommand.opprettPersongrunnlagForBruker
    private fun persongrunnlag(soekerGrunnlagBase: Persongrunnlag, spec: SimuleringSpec) =
        Persongrunnlag(source = soekerGrunnlagBase, excludeForsteVirkningsdatoGrunnlag = true).apply {
            beholdKunEnkeHvisEnSlikPersondetaljFinnes(persongrunnlag = this)
            retainVirksommePersondetaljer(persongrunnlag = this)
            spec.flyktning?.let { this.flyktning = it }
            this.antallArUtland = spec.utlandAntallAar
            this.sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
            this.bosattLandEnum = LandkodeEnum.NOR
            this.inngangOgEksportGrunnlag = InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
        }

    // AbstraktSimulerAPFra2011Command.opprettPersongrunnlagForBruker
    // -> OpprettKravHodeHelper.opprettPersongrunnlagForBruker
    private fun opprettSoekerPersongrunnlag(spec: SimuleringSpec, kravhode: Kravhode, person: PenPerson): Kravhode =
        kravhodeCreator.addSokerPersongrunnlagToKravForNormalSimulering(spec, kravhode, person)

    // AbstraktSimulerAPFra2011Command.opprettPersongrunnlagForEPS
    // -> OpprettKravHodeHelper.opprettPersongrunnlagForEPS
    private fun opprettEpsPersongrunnlag(spec: SimuleringSpec, kravhode: Kravhode, grunnbeloep: Int) {
        kravhodeCreator.addAlderspensjonEpsGrunnlagToKrav(spec, kravhode, grunnbeloep)
    }

    companion object {
        private val today: LocalDate = LocalDate.now()

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

        // SimulerAFPogAPCommandHelper.filterPersondetaljIfSivilstandsTypeEnkeExists
        private fun beholdKunEnkeHvisEnSlikPersondetaljFinnes(persongrunnlag: Persongrunnlag) {
            var brukEnkeListe = false
            val enkeListe: MutableList<PersonDetalj> = mutableListOf()

            persongrunnlag.personDetaljListe.forEach {
                if (it.bruk && gjelderEnke(it) && strengtVirksom(it)) {
                    enkeListe.add(it)
                    brukEnkeListe = true
                }
            }

            if (brukEnkeListe) {
                persongrunnlag.personDetaljListe = enkeListe
            }
        }

        // Extracted from SimulerAFPogAPCommandHelper.filterPersondetaljIfSivilstandsTypeEnkeExists
        private fun gjelderEnke(detalj: PersonDetalj) = SivilstandEnum.ENKE == detalj.sivilstandTypeEnum

        // Extracted from SimulerAFPogAPCommandHelper.filterPersondetaljIfSivilstandsTypeEnkeExists
        private fun strengtVirksom(detalj: PersonDetalj) = virksom(detalj, allowSameDay = false)

        // Extracted from SimulerAFPogAPCommandHelper.removeAllPersondetaljWithTomDateBeforeToday
        private fun virksom(detalj: PersonDetalj) = virksom(detalj, allowSameDay = true)

        private fun virksom(detalj: PersonDetalj, allowSameDay: Boolean) =
            detalj.virkTom == null || DateUtil.isAfterByDay(detalj.virkTom, today.toDate(), allowSameDay)

        // SimulerAFPogAPCommand.addInntektgrunnlagForEPS
        private fun addEpsInntektGrunnlag(
            foersteUttakDato: LocalDate?,
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

        // SimulerAFPogAPCommand.findFomDatoInntekt
        private fun inntektFom(foersteUttakDato: LocalDate?): LocalDate {
            val date1 = foersteUttakDato?.toDate() //TODO use LocalDate throughout

            val date: Date? =
                if (DateUtil.isBeforeToday(date1))
                    date1
                else
                    today.toDate()

            return LocalDate.of(DateUtil.getYear(date), 1, 1)
        }

        // SimulerAFPogAPCommand.createInntektsgrunnlagForEPS
        private fun epsInntektGrunnlag(grunnbeloep: Int, fomDatoInntekt: LocalDate) =
            Inntektsgrunnlag().apply {
                belop = grunnbeloep * 3
                bruk = true
                fom = fomDatoInntekt.toDate() // is set to noon in property
                grunnlagKilde = GrunnlagKildeCti(GrunnlagkildeEnum.BRUKER.name)
                inntektType = InntektTypeCti(InntekttypeEnum.FPI.name)
                //kopiertFraGammeltKrav = false <--- not used by pensjon-regler
                //registerKilde = null <--- not used by pensjon-regler
                tom = null // is set to noon in property
            }

        // SimulerAFPogAPCommand.shouldAddInntektgrunnlagForEPS
        private fun shouldAddEpsInntektGrunnlag(beregningInformasjon: BeregningsInformasjon?): Boolean {
            val epsHarInntektOver2G: Boolean = beregningInformasjon?.epsOver2G ?: false
            val epsMottarPensjon: Boolean = beregningInformasjon?.epsMottarPensjon ?: false
            return epsHarInntektOver2G || epsMottarPensjon
        }

        // SimulerAFPogAPCommandHelper.removeInvalidPersondetaljFromPersongrunnlag
        private fun retainPersondetaljerHavingVirksomRolle(persongrunnlag: Persongrunnlag) {
            persongrunnlag.personDetaljListe =
                persongrunnlag.personDetaljListe.filter { it.bruk && it.rolleTomDato == null }.toMutableList()
        }

        // SimulerAFPogAPCommandHelper.removeAllPersondetaljWithTomDateBeforeToday
        private fun retainVirksommePersondetaljer(persongrunnlag: Persongrunnlag) {
            persongrunnlag.personDetaljListe =
                persongrunnlag.personDetaljListe.filter { it.bruk && virksom(it) }.toMutableList()
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
                    val copy = Persongrunnlag(it, excludeForsteVirkningsdatoGrunnlag = true)
                    retainForventetPensjongivendeInntekt(copy)
                    addPersongrunnlagToEpsListForEachVirksomPersondetalj(it.personDetaljListe, epsGrunnlagListe, copy)
                }
            }
        }

        // Extracted from SimulerAFPogAPCommandHelper.copyAndAddPersongrunnlagIfNotNull
        private fun addPersongrunnlagToEpsListForEachVirksomPersondetalj(
            personDetaljListe: MutableList<PersonDetalj>,
            epsGrunnlagListe: MutableList<Persongrunnlag>,
            persongrunnlag: Persongrunnlag
        ) {
            personDetaljListe.forEach {
                if (it.bruk && it.virkTom == null) {
                    epsGrunnlagListe.add(persongrunnlag)
                }
            }
        }

        // SimulerAFPogAPCommandHelper.filterAndUpdateInntektsgrunnlaglistOnPersongrunnlag
        private fun retainForventetPensjongivendeInntekt(persongrunnlag: Persongrunnlag) {
            persongrunnlag.inntektsgrunnlagListe =
                persongrunnlag.inntektsgrunnlagListe
                    .filter { it.bruk && isForventetPensjongivendeInntekt(it) }
                    .toMutableList()
        }

        private fun isForventetPensjongivendeInntekt(grunnlag: Inntektsgrunnlag): Boolean =
            grunnlag.inntektType?.kode != InntekttypeEnum.FPI.name
    }
}
