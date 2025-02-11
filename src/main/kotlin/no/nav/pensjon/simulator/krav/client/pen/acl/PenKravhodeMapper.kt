package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.SakType
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
 * This basically performs the inverse mapping of KravhodeMapperForSimulator in PEN.
 */
object PenKravhodeMapper {

    fun kravhode(source: PenKravhode) =
        Kravhode().apply {
            kravId = source.kravId
            kravFremsattDato = source.kravFremsattDato
            onsketVirkningsdato = source.onsketVirkningsdato
            gjelder = source.gjelder
            sakId = source.sakId
            sakType = source.sakType
            sakPenPersonFnr = source.sakPenPersonFnr
            sakForsteVirkningsdatoListe = source.sakForsteVirkningsdatoListe.map(::virkningDato)
            boddEllerArbeidetIUtlandet = source.boddEllerArbeidetIUtlandet
            kravlinjeListe = source.kravlinjeListe.map(::kravlinje).toMutableList()
            persongrunnlagListe = source.persongrunnlagListe.map(::persongrunnlag).toMutableList()
            uttaksgradListe = source.uttaksgradListe
            regelverkTypeEnum = source.regelverkTypeCti?.let { RegelverkTypeEnum.valueOf(it.kode) }
            sisteSakstypeForAPEnum = source.sisteSakstypeForAP?.let { SakTypeEnum.valueOf(it.kode) }
            afpOrdningEnum = source.afpOrdning?.let { AFPtypeEnum.valueOf(it.kode) }
            afptillegg = source.afptillegg
            brukOpptjeningFra65I66Aret = source.brukOpptjeningFra65I66Aret
            kravVelgTypeEnum = source.kravVelgType?.let { KravVelgtypeEnum.valueOf(it.kode) }
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
            sakType = source.sakType?.let(SakType::valueOf),
            kravlinjeType = source.kravlinjeType?.let(KravlinjeTypeEnum::valueOf),
            virkningDato = source.virkningsdato?.toNorwegianLocalDate(),
            annenPerson = source.annenPerson?.let(::penPerson)
        )

    private fun virkningDatoGrunnlag(source: PenFoersteVirkningDatoGrunnlag) =
        ForsteVirkningsdatoGrunnlag().apply {
            bruker = source.bruker?.let(::penPerson)
            annenPerson = source.annenPerson?.let(::penPerson)
            kravlinjeType = source.kravlinjeType
            virkningsdato = source.virkningsdato
            kravFremsattDato = source.kravFremsattDato
        }

    private fun kravlinje(source: PenKravlinje) =
        Kravlinje().apply {
            kravlinjeStatus = source.kravlinjeStatus
            kravlinjeType = source.kravlinjeType
            land = source.land?.let { LandkodeEnum.valueOf(it.name) }
            relatertPerson = source.relatertPerson?.let(::penPerson)
        }

    private fun omsorgsgrunnlag(source: PenOmsorgGrunnlag) =
        Omsorgsgrunnlag().apply {
            ar = source.ar
            omsorgType = source.omsorgType
            personOmsorgFor = source.personOmsorgFor?.let(::penPerson)
            bruk = source.bruk
        }

    private fun penPerson(source: PenPenPerson) =
        PenPerson(source.penPersonId).apply {
            pid = source.pid
            fodselsdato = source.fodselsdato
            afpHistorikkListe = source.afpHistorikkListe
            uforehistorikk = source.uforehistorikk
            generellHistorikk = source.generellHistorikk
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
            formelkode = source.formelkode
            beholdningsType = source.beholdningsType
            merknadListe = source.merknadListe
        }

    private fun personDetalj(source: PenPersonDetalj) =
        PersonDetalj().apply {
            barnDetalj = source.barnDetalj?.let(::barnDetalj)
            borMedEnum = source.borMed?.let { BorMedTypeEnum.valueOf(it.kode) }
            bruk = source.bruk
            grunnlagKildeEnum = source.grunnlagKilde?.let { GrunnlagkildeEnum.valueOf(it.kode) }
            epsAvkallEgenPensjon = source.epsAvkallEgenPensjon
            grunnlagsrolleEnum = source.grunnlagsrolle?.let { GrunnlagsrolleEnum.valueOf(it.kode) }
            tillegg = source.tillegg

            rolleFomDato = source.rolleFomDato
            rolleTomDato = source.rolleTomDato
            virkFom = source.virkFom
            virkTom = source.virkTom
            /* TODO: PEN to regler mapping:
            rolleFomDato = source.virkFom
            rolleTomDato = source.virkTom
            */

            serskiltSatsUtenET = source.serskiltSatsUtenET
            sivilstandRelatertPerson = source.sivilstandRelatertPerson?.let(::penPerson)
            sivilstandTypeEnum = source.sivilstandType?.let { SivilstandEnum.valueOf(it.kode) }
        }.also {
            it.finishInit()
            //TODO
            // due to rolleFomDato manipulation in finishInit, have to use legacyRolleFomDato in logic
            // (rolleFomDato shall only be used by regler)
        }

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
            bosattLandEnum = source.bosattLand?.let { LandkodeEnum.valueOf(it.kode) }
            statsborgerskapEnum = source.statsborgerskap?.let { LandkodeEnum.valueOf(it.kode) }
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
            uforegrunnlagList = source.uforegrunnlagList
            instOpphReduksjonsperiodeListe = source.instOpphReduksjonsperiodeListe
            instOpphFasteUtgifterperiodeListe = source.instOpphFasteUtgifterperiodeListe
            dagpengegrunnlagListe = source.dagpengegrunnlagListe
            omsorgsgrunnlagListe = source.omsorgsgrunnlagListe.map(::omsorgsgrunnlag).toMutableList()
            arbeidsforholdEtterUforgrunnlagListe = source.arbeidsforholdEtterUforgrunnlagListe
            utenlandsoppholdListe = source.utenlandsoppholdListe
            overgangsInfoUPtilUT = source.overgangsInfoUPtilUT
            afpTpoUpGrunnlag = source.AfpTpoUpGrunnlag
            yrkesskadegrunnlag = source.yrkesskadegrunnlag
            yrkesskadegrunnlagList = source.yrkesskadegrunnlagList
            barnetilleggVurderingsperioder = source.barnetilleggVurderingsperioder
            beholdninger = source.beholdninger.map(::pensjonsbeholdning).toMutableList()
            trygdetider = source.trygdetider
            gjelderOmsorg = source.gjelderOmsorg
            gjelderUforetrygd = source.gjelderUforetrygd
        }.also {
            it.finishInit()
            //TODO should noon() be called in finishInit here or in mapper in PEN?
        }
}
