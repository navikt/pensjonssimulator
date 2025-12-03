package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato

/**
 * Maps kravhode from DTO (data transfer object) to pensjonssimulator domain.
 * The DTO is a hybrid of PEN and pensjon-regler properties.
 * This basically performs the inverse mapping of SimulatorKravMapper in PEN.
 */
object PenKravhodeMapper {

    fun kravhode(source: PenKravhode) =
        Kravhode().apply {
            kravId = source.kravId
            kravFremsattDato = source.kravFremsattDato
            onsketVirkningsdato = source.onsketVirkningsdato?.toNorwegianLocalDate()
            gjelder = source.gjelder
            sakId = source.sakId
            sakType = source.sakType
            sakPenPersonFnr = source.sakPenPersonFnr
            sakForsteVirkningsdatoListe = source.sakForsteVirkningsdatoListe.map(::virkningDato)
            boddEllerArbeidetIUtlandet = source.boddEllerArbeidetIUtlandet
            kravlinjeListe = source.kravlinjeListe.map(::kravlinje).toMutableList()
            persongrunnlagListe = source.persongrunnlagListe.map(::persongrunnlag).toMutableList()
            uttaksgradListe = source.uttaksgradListe
            regelverkTypeEnum = source.regelverkTypeEnum
            sisteSakstypeForAPEnum = source.sisteSakstypeForAPEnum
            afpOrdningEnum = source.afpOrdningEnum
            afptillegg = source.afptillegg
            brukOpptjeningFra65I66Aret = source.brukOpptjeningFra65I66Aret
            kravVelgTypeEnum = source.kravVelgTypeEnum
            boddArbeidUtlandFar = source.boddArbeidUtlandFar
            boddArbeidUtlandMor = source.boddArbeidUtlandMor
            boddArbeidUtlandAvdod = source.boddArbeidUtlandAvdod
            btVurderingsperiodeBenyttet = source.btVurderingsperiodeBenyttet
        }

    private fun barnDetalj(source: PenBarnDetalj) =
        BarnDetalj().apply {
            annenForelder = source.annenForelder?.let(::penPerson)
            borMedBeggeForeldre = source.borMedBeggeForeldre
            underUtdanning = source.underUtdanning
            inntektOver1G = source.inntektOver1G
            // borFomDato/borTomDato settes ikke, da PEN BarnDetalj ikke har disse verdiene
        }

    private fun virkningDato(source: PenFoersteVirkningDato) =
        FoersteVirkningDato(
            sakType = source.sakType?.let(SakTypeEnum::valueOf),
            kravlinjeType = source.kravlinjeTypeEnum?.let(KravlinjeTypeEnum::valueOf),
            virkningDato = source.virkningsdato?.toNorwegianLocalDate(),
            annenPerson = source.annenPerson?.let(::penPerson)
        )

    private fun virkningDatoGrunnlag(source: PenFoersteVirkningDatoGrunnlag) =
        ForsteVirkningsdatoGrunnlag().apply {
            bruker = source.bruker?.let(::penPerson)
            annenPerson = source.annenPerson?.let(::penPerson)
            kravlinjeTypeEnum = source.kravlinjeTypeEnum
            virkningsdato = source.virkningsdato
            kravFremsattDato = source.kravFremsattDato
        }

    private fun kravlinje(source: PenKravlinje) =
        Kravlinje().apply {
            kravlinjeStatus = source.kravlinjeStatus
            kravlinjeTypeEnum = source.kravlinjeTypeEnum
            hovedKravlinje = source.kravlinjeTypeEnum?.erHovedkravlinje == true
            land = source.land?.let { LandkodeEnum.valueOf(it.name) }
            relatertPerson = source.relatertPerson?.let(::penPerson)
        }

    private fun omsorgsgrunnlag(source: PenOmsorgGrunnlag) =
        Omsorgsgrunnlag().apply {
            ar = source.ar
            omsorgTypeEnum = source.omsorgTypeEnum
            personOmsorgFor = source.personOmsorgFor?.let(::penPerson)
            bruk = source.bruk
        }

    private fun penPerson(source: PenPenPerson) =
        PenPerson().apply {
            penPersonId = source.penPersonId ?: 0L
        }

    private fun pensjonsbeholdning(source: PenPensjonsbeholdning) =
        Pensjonsbeholdning().apply {
            fom = source.fom
            tom = source.tom
            ar = source.ar
            totalbelop = source.totalbelop
            opptjening = source.opptjening
            lonnsvekstInformasjon = source.lonnsvekstInformasjon
            reguleringsInformasjon = source.reguleringsInformasjon
            formelKodeEnum = source.formelKodeEnum ?: source.formelkodeEnum
            merknadListe = source.merknadListe
            // beholdningsTypeEnum set in constructor
        }

    /**
     * Corresponds to GrunnlagToReglerMapper.mapPersonDetaljToRegler in PEN.
     * Note in particular that virkFom/virkTom are mapped to rolleFomDato/rolleTomDato.
     */
    private fun personDetalj(source: PenPersonDetalj) =
        PersonDetalj().apply {
            grunnlagsrolleEnum = source.grunnlagsrolleEnum
            rolleFomDato = source.virkFom // yes, virkFom is actually mapped to rolleFomDato
            rolleTomDato = source.virkTom // ... and virkTom is mapped to rolleTomDato
            sivilstandTypeEnum = source.sivilstandTypeEnum
            sivilstandRelatertPerson = source.sivilstandRelatertPerson?.let(::penPerson)
            borMedEnum = source.borMedEnum
            barnDetalj = source.barnDetalj?.let(::barnDetalj)
            tillegg = source.tillegg
            bruk = source.bruk
            grunnlagKildeEnum = source.grunnlagKildeEnum
            serskiltSatsUtenET = source.serskiltSatsUtenET
            epsAvkallEgenPensjon = source.epsAvkallEgenPensjon
            //--- Extra:
            penRolleFom = source.rolleFomDato // the original 'rolle f.o.m.-dato' in PEN
            penRolleTom = source.rolleTomDato // the original 'rolle t.o.m.-dato' in PEN
            virkFom = source.virkFom
            virkTom = source.virkTom
        } // do not call finishInit() here; the values received from PEN are already 'finished'

    private fun persongrunnlag(source: PenPersongrunnlag) =
        Persongrunnlag().apply {
            penPerson = source.penPerson?.let(::penPerson)
            fodselsdato = source.fodselsdato
            dodsdato = source.dodsdato
            flyktning = source.flyktning
            sistMedlITrygden = source.sistMedlITrygden
            sisteGyldigeOpptjeningsAr = source.sisteGyldigeOpptjeningsAr
            hentetPopp = source.hentetPopp
            hentetInnt = source.hentetInnt
            hentetInst = source.hentetInst
            hentetTT = source.hentetTT
            hentetArbeid = source.hentetArbeid
            overkompUtl = source.overkompUtl
            dodAvYrkesskade = source.dodAvYrkesskade
            antallArUtland = source.antallArUtland
            medlemIFolketrygdenSiste3Ar = source.medlemIFolketrygdenSiste3Ar
            over60ArKanIkkeForsorgesSelv = source.over60ArKanIkkeForsorgesSelv
            arligPGIMinst1G = source.arligPGIMinst1G
            artikkel10 = source.artikkel10
            skiltesDelAvAvdodesTP = source.skiltesDelAvAvdodesTP
            vernepliktAr = source.vernepliktAr
            barnetilleggVurderingsperiode = source.barnetilleggVurderingsperiode
            forsteVirkningsdatoGrunnlagListe =
                source.forsteVirkningsdatoGrunnlagListe.map(::virkningDatoGrunnlag).toMutableList()
            trygdeavtaledetaljer = source.trygdeavtaledetaljer
            inngangOgEksportGrunnlag = source.inngangOgEksportGrunnlag
            inntektsgrunnlagListe = source.inntektsgrunnlagListe
            personDetaljListe = source.personDetaljListe.map(::personDetalj).toMutableList()
            utbetalingsgradUTListe = source.utbetalingsgradUTListe
            opptjeningsgrunnlagListe = source.opptjeningsgrunnlagListe
            trygdetidPerioder = source.trygdetidPerioder
            trygdetidPerioderKapittel20 = source.trygdetidPerioderKapittel20
            afpHistorikkListe = source.afpHistorikkListe
            bosattLandEnum = source.bosattLandEnum
            statsborgerskapEnum = source.statsborgerskapEnum
            trygdetid = source.trygdetid
            trygdetidKapittel20 = source.trygdetidKapittel20
            trygdetidAlternativ = source.trygdetidAlternativ
            trygdeavtale = source.trygdeavtale
            barnekull = source.barnekull
            uforeHistorikk = source.uforeHistorikk
            generellHistorikk = source.generellHistorikk
            forstegangstjenestegrunnlag = source.forstegangstjenestegrunnlag
            pensjonsbeholdning = source.pensjonsbeholdning?.let(::pensjonsbeholdning)
            uforegrunnlag = source.uforegrunnlag
            uforegrunnlagList = source.uforegrunnlagList ?: mutableListOf()
            instOpphReduksjonsperiodeListe = source.instOpphReduksjonsperiodeListe
            instOpphFasteUtgifterperiodeListe = source.instOpphFasteUtgifterperiodeListe
            dagpengegrunnlagListe = source.dagpengegrunnlagListe
            omsorgsgrunnlagListe = source.omsorgsgrunnlagListe.map(::omsorgsgrunnlag).toMutableList()
            arbeidsforholdEtterUforgrunnlagListe = source.arbeidsforholdEtterUforgrunnlagListe
            utenlandsoppholdListe = source.utenlandsoppholdListe
            overgangsInfoUPtilUT = source.overgangsInfoUPtilUT
            afpTpoUpGrunnlag = source.afpTpoUpGrunnlag
            normertPensjonsalderGrunnlag = source.normertPensjonsalderGrunnlag
            yrkesskadegrunnlag = source.yrkesskadegrunnlag
            yrkesskadegrunnlagList = source.yrkesskadegrunnlagList ?: mutableListOf()
            barnetilleggVurderingsperioder = source.barnetilleggVurderingsperioder ?: mutableListOf()
            beholdninger = source.flatBeholdninger.orEmpty().map(::pensjonsbeholdning).toMutableList()
            livsvarigOffentligAfpGrunnlagListe = source.livsvarigOffentligAfpGrunnlagListe ?: mutableListOf()
            trygdetider = source.trygdetider ?: mutableListOf()
            gjelderOmsorg = source.gjelderOmsorg
            gjelderUforetrygd = source.gjelderUforetrygd
        }.also {
            it.finishInit()
            //TODO should noon() be called in finishInit here or in mapper in PEN?
        }
}
