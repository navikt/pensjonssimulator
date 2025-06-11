package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.OvergangsinfoUPtilUT
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import java.util.*

// 2025-04-05
/**
 * Persongrunnlag inneholder nødvendige data knyttet til en bestemt person.
 * Persongrunnlag brukes som inndata til kall på en regeltjeneste og må defineres før kallet.
 */
class Persongrunnlag() {
    /**
     * Representerer personen.
     */
    var penPerson: PenPerson? = null

    /**
     * Søkers fødselsdato, brukes kun ved simuleringer.
     * Da benyttes ikke reelle personer.
     */
    var fodselsdato: Date? = null

    /**
     * Personens eventuelle dødsdato, brukes ved beregning av ytelser til
     * gjenlevende ektefelle og barn.
     */
    var dodsdato: Date? = null

    /**
     * Personens statsborgerskap.
     */
    var statsborgerskapEnum: LandkodeEnum? = null

    /**
     * Angir om personen er flyktning.
     */
    var flyktning: Boolean? = null

    /**
     * Liste av PersonDetalj-objekter som representerer periodisert
     * detaljinformasjon om personen. Dersom listen har mer enn ett element
     * betyr dette at personens detaljinformasjon har forandret seg over tid,
     * eksempelvis skifte av sivilstand. Det er kun tjenesten
     * KontrollerInformasjonsgrunnlag som vil motta en personDetaljListe med mer
     * enn ett element. Andre tjenester vil kun ha ett element i
     * personDetaljListe. Eks: ved skifte av sivilstand vil en tjeneste bli kalt
     * to ganger.
     */
    var personDetaljListe: MutableList<PersonDetalj> = mutableListOf()

    /**
     * Dato for sist innmeldt i Folketrygden- for fremtidig trygdetid.
     */
    var sistMedlITrygden: Date? = null

    /**
     * Siste gyldige år for opptjening som benyttes for alderspensjon2011/2016/2025 og AFP Privat.
     * For eksempel skal saksbehandling som utfåres i 2010 ta utgangspunkt i opptjeninger til
     * og med 2008 (sisteGyldigeOpptjeningsAr vil her være 2008).
     */
    var sisteGyldigeOpptjeningsAr = 0

    /**
     * Angir om opptjeningsinformasjonen er hentet fra Opptjeningsregisteret og
     * registrert som grunnlag på kravet. Det vil være kun ett element i listen
     * ved beregnings/vilkårsprøvingstjenester. Ved tjenesten
     * KontrollerInformasjonsgrunnlag vil det være flere elementer.
     */
    var hentetPopp: Boolean? = null

    /**
     * Angir om informasjonen om inntektsinformasjon er hentet fra
     * Inntektsregisteret og registrert som grunnlag på kravet.
     */
    var hentetInnt: Boolean? = null

    /**
     * Angir om informasjonen om institusjonsopphold er hentet fra
     * Institusjonsoppholdsregisteret og registrert som grunnlag på kravet.
     */
    var hentetInst: Boolean? = null

    /**
     * Angir om trygdetidsinformasjon er hentet inn og registrert som grunnlag
     * på kravet.
     */
    var hentetTT: Boolean? = null

    /**
     * Angir om informasjon om arbeidsforhold er hentet fra
     * Arbeidsforholdregisteret og registrert som grunnlag på kravet.
     */
    var hentetArbeid: Boolean? = null

    /**
     * Angir om personens ytelse skal beregnes med overkompensasjonsregler.
     * Gjelde utenlandstilfeller.
     */
    var overkompUtl: Boolean? = null

    /**
     * Liste av opptjeningsgrunnlag for personen.
     */
    var opptjeningsgrunnlagListe: MutableList<Opptjeningsgrunnlag> = mutableListOf()

    /**
     * Liste av inntektsgrunnlag for personen.
     */
    var inntektsgrunnlagListe: MutableList<Inntektsgrunnlag> = mutableListOf()

    /**
     * Liste av trygdetidsperioder for personen.
     */
    var trygdetidPerioder: MutableList<TTPeriode> = mutableListOf()

    /**
     * Liste av trygdetidsperioder for personen.
     * For beregning av trygdetid for AP2016 iht. kapittel 20 og AP2025.
     */
    var trygdetidPerioderKapittel20: MutableList<TTPeriode> = mutableListOf()

    /**
     * Framtidig og faktisk trygdetid. Denne ligger også i VilkarsVedtak, men er
     * nødvendig når vi skal ytelsesberegninger der det ikke finnes et vedtak.
     * F.eks Barnepensjon gjenlevende.
     */
    var trygdetid: Trygdetid? = null

    /**
     * Samme som over, men for ny opptjeningsmodell.
     */
    var trygdetidKapittel20: Trygdetid? = null

    /**
     * Trygdetid for alternative uføretidspunkt.
     */
    var trygdetidAlternativ: Trygdetid? = null

    /**
     * Spesifikke grunnlagsdata for uførepensjon.
     */
    var uforegrunnlag: Uforegrunnlag? = null

    /**
     * Historikk for Uføreytelser.Inneholder en blanding av data fra
     * Uforegrunnlag og UforeEkstra.
     */
    var uforeHistorikk: Uforehistorikk? = null

    /**
     * Spesifikke grunnlagsdata for yrkesskadepensjon. Dersom yrkesskadegrunnlag =
     * null betyr det at personen ikke har yrkesskade.
     */
    var yrkesskadegrunnlag: Yrkesskadegrunnlag? = null

    /**
     * Angir om en avdød døde av yrkesskade. Hvis satt vil det finnes et yrkesskadegrunnlag og en dodsdato.
     * Det skal da ikke finnes et Uføregrunnlag. Brukes ved BP/GJP/GJR.
     */
    var dodAvYrkesskade: Boolean? = null

    /**
     * Generell historisk informasjon om en person. Ventetilleggsgrunnlag, fravik_19_3.
     */
    var generellHistorikk: GenerellHistorikk? = null

    /**
     * Historikk for AFP ytelser. Inneholder informasjon relevant for
     * perioden(e) bruker hadde AFP.
     */
    var afpHistorikkListe: List<AfpHistorikk> = mutableListOf()

    /**
     * Beskriver hvor mange barn det er i kullet.
     */
    var barnekull: Barnekull? = null

    /**
     * Hentet som eneste element fra PEN Persongrunnlag.barnetilleggVurderingsperiodeListe
     */
    var barnetilleggVurderingsperiode: BarnetilleggVurderingsperiode? = null

    /**
     * Antall år personen har bodd/arbeidet i utlandet etter fylte 16 år.
     */
    var antallArUtland = 0

    /**
     * Angir om personen har vært medlem i Folketrygden de siste 3 år. Brukes
     * ved simulering.
     */
    var medlemIFolketrygdenSiste3Ar: Boolean? = null

    /**
     * Angir om personen er over 60 år eller ikke kan forsørge seg selv. Brukes
     * i simulering for å angi vilkår for ektefelletillegg.
     */
    var over60ArKanIkkeForsorgesSelv: Boolean? = null

    /**
     * Liste av utenlandsopphold.
     */
    var utenlandsoppholdListe: MutableList<Utenlandsopphold> = mutableListOf()

    /**
     * Trygdeavtale, representerer en saksbehandlers vurdering av hvilken
     * avtale/konvensjon som skal anvendes i en utenlandssak.
     */
    var trygdeavtale: Trygdeavtale? = null

    /**
     * Detaljer knyttet til trygdeavtale. Brukes i beregningen av
     * tilleggspensjon i utenlandssaker.
     */
    var trygdeavtaledetaljer: Trygdeavtaledetaljer? = null

    /**
     * Grunnlag påkrevd for å kunne behandle inngang og eksport av pensjonssaker mellom Norge og utland.
     */
    var inngangOgEksportGrunnlag: InngangOgEksportGrunnlag? = null

    /**
     * Grunnlag påkrevd for å kunne behandle inngang og eksport av pensjonssaker mellom Norge og utland.
     */
    var forsteVirkningsdatoGrunnlagListe: MutableList<ForsteVirkningsdatoGrunnlag> = mutableListOf()

    /**
     * årlig pensjonsgivende inntekt var minst 1G på dødstidspunktet.
     */
    var arligPGIMinst1G: Boolean? = null

    /**
     * Angir omdet skal beregnes etter artikkel 10 - nordisk konvensjon.
     * Trygdetiden kan bli redusert.
     */
    var artikkel10: Boolean? = null

    /**
     * Årstall for avtjent verneplikt. Maks 4 år godkjennes. Er null dersom det
     * ikke finnes vernepliktsår. må være i stigende rekkefølge, eks:<br></br>
     * `[0] = 2001`<br></br>
     * `[1] = 2002`<br></br>
     * `[2] = 2004`
     */
    var vernepliktAr: IntArray? = null

    /**
     * Den skiltes del av avdødes tilleggspensjon. Angis i prosent. Default
     * verdi settes til -99 fordi 0 er en lovlig verdi.
     */
    var skiltesDelAvAvdodesTP = -99

    /**
     * Liste av institusjonsoppholdsreduksjonsperioder relatert til
     * persongrunnlaget
     */
    var instOpphReduksjonsperiodeListe: List<InstOpphReduksjonsperiode> = mutableListOf()

    /**
     * Liste av institusjonsoppholdsfasteutgifterperioder relatert til
     * persongrunnlaget
     */
    var instOpphFasteUtgifterperiodeListe: MutableList<InstOpphFasteUtgifterperiode> = mutableListOf()

    /**
     * Landskode - det land personen er bosatt i på VIRK. Intern pensjon-regler attributt.
     * Default settes denne til NOR og erstattes med Trygdeavtale.bostedsland
     * dersom trygdeavtale ikke er null.
     */
    var bosattLandEnum: LandkodeEnum? = null
    var pensjonsbeholdning: Pensjonsbeholdning? = null

    /**
     * Informasjon om Førstegangstjenesteperioder.
     */
    var forstegangstjenestegrunnlag: Forstegangstjeneste? = null
    var dagpengegrunnlagListe: MutableList<Dagpengegrunnlag> = mutableListOf()
    var omsorgsgrunnlagListe: MutableList<Omsorgsgrunnlag> = mutableListOf()

    /**
     * Informasjon om arbeidsforhold med perioder og stillingsprosent.
     */
    var arbeidsforholdsgrunnlagListe: MutableList<Arbeidsforholdsgrunnlag> = mutableListOf()

    /**
     * Contains information about post injury arbeidsforhold perioder and stillingsprosent.
     */
    var arbeidsforholdEtterUforgrunnlagListe: List<ArbeidsforholdEtterUforgrunnlag> = mutableListOf()

    /**
     * Informasjon om konverterting fra UP til UT.
     */
    var overgangsInfoUPtilUT: OvergangsinfoUPtilUT? = null

    /**
     * Inneholder utbetalingsgraden for uføretrygd etter inntektsavkorting.
     */
    var utbetalingsgradUTListe: List<UtbetalingsgradUT> = mutableListOf()

    /**
     * Objekt som inneholder informasjon om TP-ordningers uførepensjonsgrunnlag. Dette er manuelt registrerte data og ikke hentet fra TP-registeret eller andre eksterne kilder.
     */
    var afpTpoUpGrunnlag: AfpTpoUpGrunnlag? = null

    /**
     * Støttefelt for virk_ikke_ufor-hacket. Feltet er ikke forventet populert.
     */
    @JsonIgnore
    var forsteVirk: Date? = null

    /**
     * Representerer grunnlaget for normert pensjonsalder
     *
     * Dette feltet inneholder normert, øvre og nedre pensjonsalder (i år og måneder)
     * som benyttes i saksbehandlingen.
     */
    var normertPensjonsalderGrunnlag: NormertPensjonsalderGrunnlag? = null

    // SIMDOM-ADD
    @JsonIgnore
    var gjelderOmsorg: Boolean = false

    @JsonIgnore
    var gjelderUforetrygd: Boolean = false

    @JsonIgnore
    var barnetilleggVurderingsperioder: MutableList<BarnetilleggVurderingsperiode> =
        mutableListOf() // discriminator: BT_VURDERINGSPERIODE

    @JsonIgnore
    var beholdninger: MutableList<Pensjonsbeholdning> = mutableListOf() // BEHOLDNING

    @JsonIgnore
    var livsvarigOffentligAfpGrunnlagListe: List<AfpOffentligLivsvarigGrunnlag> = emptyList()

    @JsonIgnore
    var trygdetider: MutableList<Trygdetid> = mutableListOf() // TRYGDETID

    @JsonIgnore
    var uforegrunnlagList: MutableList<Uforegrunnlag> = mutableListOf() // UFORE

    @JsonIgnore
    var yrkesskadegrunnlagList: MutableList<Yrkesskadegrunnlag> = mutableListOf() // YRKESKADE

    @JsonIgnore
    var rawFodselsdato: Date? = null

    @JsonIgnore
    var rawDodsdato: Date? = null

    @JsonIgnore
    var rawSistMedlITrygden: Date? = null
    // NB: Note legacy comment for forsteVirkningsdatoGrunnlagTransferList:
    // Midlertidig variant av ForsteVirkningsdatoGrunnlag, brukt for populering av PREG-requester

    fun finishInit() {
        rawFodselsdato = fodselsdato
        rawDodsdato = dodsdato
        rawSistMedlITrygden = sistMedlITrygden
        fodselsdato = rawFodselsdato?.noon()
        dodsdato = rawDodsdato?.noon()
        sistMedlITrygden = rawSistMedlITrygden?.noon()
    }

    // SIMDOM-ADD excludeTrygdetidPerioder, excludeForsteVirkningsdatoGrunnlag
    constructor(
        source: Persongrunnlag,
        excludeForsteVirkningsdatoGrunnlag: Boolean = false,
        excludeTrygdetidPerioder: Boolean = false
    ) : this() {
        penPerson = source.penPerson?.let(::PenPerson)
        fodselsdato = source.fodselsdato?.clone() as? Date
        dodsdato = source.dodsdato?.clone() as? Date
        statsborgerskapEnum = source.statsborgerskapEnum
        flyktning = source.flyktning
        source.personDetaljListe.forEach { personDetaljListe.add(PersonDetalj(it)) }
        sistMedlITrygden = source.sistMedlITrygden?.clone() as? Date
        hentetPopp = source.hentetPopp
        hentetInnt = source.hentetInnt
        hentetInst = source.hentetInst
        hentetTT = source.hentetTT
        hentetArbeid = source.hentetArbeid
        source.overkompUtl?.let { overkompUtl = it }
        source.opptjeningsgrunnlagListe.forEach { opptjeningsgrunnlagListe.add(Opptjeningsgrunnlag(it)) }
        source.inntektsgrunnlagListe.forEach { inntektsgrunnlagListe.add(Inntektsgrunnlag(it)) }

        if (excludeTrygdetidPerioder.not()) {
            source.trygdetidPerioder.forEach { trygdetidPerioder.add(TTPeriode(it)) }
            source.trygdetidPerioderKapittel20.forEach { trygdetidPerioderKapittel20.add(TTPeriode(it)) }
        }

        trygdetid = source.trygdetid?.copy()
        source.uforegrunnlag?.let { uforegrunnlag = Uforegrunnlag(it) }
        source.uforeHistorikk?.let { uforeHistorikk = Uforehistorikk(it) }
        source.yrkesskadegrunnlag?.let { yrkesskadegrunnlag = Yrkesskadegrunnlag(it) }
        dodAvYrkesskade = source.dodAvYrkesskade
        source.generellHistorikk?.let { generellHistorikk = GenerellHistorikk(it) }
        afpHistorikkListe = source.afpHistorikkListe.map { it.copy() }.toMutableList()
        barnekull = source.barnekull?.copy()
        antallArUtland = source.antallArUtland
        medlemIFolketrygdenSiste3Ar = source.medlemIFolketrygdenSiste3Ar
        over60ArKanIkkeForsorgesSelv = source.over60ArKanIkkeForsorgesSelv

        for (utenlandsopphold in source.utenlandsoppholdListe) {
            this.utenlandsoppholdListe.add(Utenlandsopphold(utenlandsopphold))
        }

        if (source.trygdeavtale != null) {
            this.trygdeavtale = Trygdeavtale(source.trygdeavtale!!)
        }

        if (source.trygdeavtaledetaljer != null) {
            this.trygdeavtaledetaljer = Trygdeavtaledetaljer(source.trygdeavtaledetaljer!!)
        }

        if (source.inngangOgEksportGrunnlag != null) {
            this.inngangOgEksportGrunnlag = InngangOgEksportGrunnlag(source.inngangOgEksportGrunnlag!!)
        }

        this.arligPGIMinst1G = source.arligPGIMinst1G
        this.artikkel10 = source.artikkel10

        if (source.vernepliktAr != null) {
            this.vernepliktAr = source.vernepliktAr!!.clone()
        }

        skiltesDelAvAvdodesTP = source.skiltesDelAvAvdodesTP
        instOpphReduksjonsperiodeListe = source.instOpphReduksjonsperiodeListe.map(::InstOpphReduksjonsperiode)

        for (instOpphFasteUtgifterperiode in source.instOpphFasteUtgifterperiodeListe) {
            this.instOpphFasteUtgifterperiodeListe.add(instOpphFasteUtgifterperiode.copy())
        }

        if (source.bosattLandEnum != null) {
            this.bosattLandEnum = source.bosattLandEnum
        }

        if (!excludeForsteVirkningsdatoGrunnlag) {
            for (forsteVirkningsdatoGrunnlag in source.forsteVirkningsdatoGrunnlagListe) {
                forsteVirkningsdatoGrunnlagListe.add(ForsteVirkningsdatoGrunnlag(forsteVirkningsdatoGrunnlag))
            }
        }

        trygdetidKapittel20 = source.trygdetidKapittel20?.copy()

        if (source.pensjonsbeholdning != null) {
            this.pensjonsbeholdning = Pensjonsbeholdning(source.pensjonsbeholdning!!)
        }

        for (dagpengegrunnlag in source.dagpengegrunnlagListe) {
            this.dagpengegrunnlagListe.add(Dagpengegrunnlag(dagpengegrunnlag))
        }

        if (source.forstegangstjenestegrunnlag != null) {
            this.forstegangstjenestegrunnlag = Forstegangstjeneste(source.forstegangstjenestegrunnlag!!)
        }

        for (omsorgsgrunnlag in source.omsorgsgrunnlagListe) {
            this.omsorgsgrunnlagListe.add(Omsorgsgrunnlag(omsorgsgrunnlag))
        }

        for (afg in source.arbeidsforholdsgrunnlagListe) {
            this.arbeidsforholdsgrunnlagListe.add(afg.copy())
        }

        arbeidsforholdEtterUforgrunnlagListe = source.arbeidsforholdEtterUforgrunnlagListe.map { it.copy() }
        overgangsInfoUPtilUT = source.overgangsInfoUPtilUT?.copy()
        utbetalingsgradUTListe = source.utbetalingsgradUTListe.map { it.copy() }
        trygdetidAlternativ = source.trygdetidAlternativ?.copy()
        sisteGyldigeOpptjeningsAr = source.sisteGyldigeOpptjeningsAr
        barnetilleggVurderingsperiode = source.barnetilleggVurderingsperiode

        if (source.afpTpoUpGrunnlag != null) {
            this.afpTpoUpGrunnlag = AfpTpoUpGrunnlag(source.afpTpoUpGrunnlag!!)
        }
        //for (vilkarsVedtak in persongrunnlag.vilkarsvedtakEPSListe) {
        //    this.vilkarsvedtakEPSListe.add(VilkarsVedtak(vilkarsVedtak))
        //}
        //--- Extra:
        gjelderOmsorg = source.gjelderOmsorg //: Boolean = false
        gjelderUforetrygd = source.gjelderUforetrygd //: Boolean = false
        barnetilleggVurderingsperioder = source.barnetilleggVurderingsperioder.map { it.copy() }.toMutableList()
        beholdninger = source.beholdninger.map { it.copy() }.toMutableList()
        livsvarigOffentligAfpGrunnlagListe = source.livsvarigOffentligAfpGrunnlagListe.map { it.copy() }
        trygdetider = source.trygdetider.map { it.copy() }.toMutableList()
        uforegrunnlagList = source.uforegrunnlagList.map(::Uforegrunnlag).toMutableList()
        yrkesskadegrunnlagList = source.yrkesskadegrunnlagList.map(::Yrkesskadegrunnlag).toMutableList()
        rawFodselsdato = source.rawFodselsdato?.clone() as? Date
        rawDodsdato = source.rawDodsdato?.clone() as? Date
        rawSistMedlITrygden = source.rawSistMedlITrygden?.clone() as? Date
        normertPensjonsalderGrunnlag = source.normertPensjonsalderGrunnlag?.let {
            NormertPensjonsalderGrunnlag(
                ovreAr = it.ovreAr,
                ovreMnd = it.ovreMnd,
                normertAr = it.normertAr,
                normertMnd = it.normertMnd,
                nedreAr = it.nedreAr,
                nedreMnd = it.nedreMnd,
                erPrognose = it.erPrognose
            )
        }
        // end extra ---
    }

    fun addBeholdning(beholdning: Pensjonsbeholdning) {
        beholdninger.add(beholdning)
        pensjonsbeholdning = findLatest(beholdninger)
    }

    fun replaceBeholdninger(list: List<Pensjonsbeholdning>) {
        beholdninger.clear()
        beholdninger.addAll(list)
        // pensjon-regler only uses latest beholdning
        pensjonsbeholdning = findLatest(beholdninger) // cf. PEN kjerne.Persongrunnlag.getPensjonsbeholdning
    }

    /**
     * NB: Comment on source of this method (no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.findPersonDetaljIPersongrunnlag):
     * "deprecated - this method picks PersonDetalj without considering periode; this is a strategy that could lead to errors"
     */
    fun findPersonDetaljIPersongrunnlag(rolle: GrunnlagsrolleEnum, checkBruk: Boolean): PersonDetalj? =
        personDetaljListe.firstOrNull {
            (!checkBruk || it.bruk == true) && it.grunnlagsrolleEnum == rolle
        }

    // PEN: no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.findPersonDetaljWithRolleForPeriode
    fun findPersonDetaljWithRolleForPeriode(
        rolle: GrunnlagsrolleEnum,
        virkningDato: Date?,
        checkBruk: Boolean
    ): PersonDetalj? =
        personDetaljListe.firstOrNull {
            (!checkBruk || it.bruk == true) &&
                    rolle == it.grunnlagsrolleEnum &&
                    isDateInPeriod(virkningDato, it.virkFom, it.virkTom)
        }

    // PEN: no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.isAvdod
    fun isAvdod() = findPersonDetaljIPersongrunnlag(rolle = GrunnlagsrolleEnum.AVDOD, checkBruk = true) != null

    fun isBarnOrFosterbarn() =
        hasPersondetaljWithRolle(GrunnlagsrolleEnum.BARN) ||
                hasPersondetaljWithRolle(GrunnlagsrolleEnum.FBARN)

    fun isEps() =
        hasPersondetaljWithRolle(GrunnlagsrolleEnum.EKTEF) ||
                hasPersondetaljWithRolle(GrunnlagsrolleEnum.PARTNER) ||
                hasPersondetaljWithRolle(GrunnlagsrolleEnum.SAMBO)

    fun isSoker() = hasPersondetaljWithRolle(GrunnlagsrolleEnum.SOKER)

    private fun hasPersondetaljWithRolle(rolle: GrunnlagsrolleEnum) =
        personDetaljListe.any { it.bruk == true && it.grunnlagsrolleEnum == rolle }

    fun removePersonDetalj(detalj: PersonDetalj) {
        personDetaljListe.remove(detalj)
    }

    fun addBeholdninger(list: List<Pensjonsbeholdning>) {
        list.forEach { beholdninger.add(it) }
        pensjonsbeholdning = findLatest(beholdninger)
    }

    fun clearBeholdningListe() {
        beholdninger.clear()
        pensjonsbeholdning = null
    }

    fun deleteUforegrunnlag() {
        uforegrunnlagList.clear()
    }

    fun deleteYrkesskadegrunnlag() {
        yrkesskadegrunnlagList.clear()
    }

    fun is3_2Samboer(): Boolean =
        personDetaljListe.any {
            it.bruk == true && it.isGrunnlagsrolleSamboer() && it.is3_2Samboer()
        }

    /**
     * Finner nyeste trygdetid, hvis ingen finnes returneres null
     */
    // PEN: Persongrunnlag.findLatestTrygdetid + Trygdetid.compareTo
    fun latestTrygdetid(): Trygdetid? =
        trygdetider.filter { it.virkFom != null }.maxByOrNull { it.virkFom!! }
            ?: trygdetider.firstOrNull()

    // end SIMDOM-ADD
}
