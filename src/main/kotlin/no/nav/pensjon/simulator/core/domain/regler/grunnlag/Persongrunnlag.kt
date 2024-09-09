package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.OvergangsinfoUPtilUT
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagsrolleCti
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import java.io.Serializable
import java.util.*

/**
 * Persongrunnlag inneholder nødvendige data knyttet til en bestemt person.
 * Persongrunnlag brukes som inndata til kall på en regeltjeneste og må defineres før kallet.
 */
class Persongrunnlag(

    var penPerson: PenPerson? = null,
    var fodselsdato: Date? = null,

    /**
     * Brukes ved beregning av ytelser til gjenlevende ektefelle og barn.
     */
    var dodsdato: Date? = null,

    var statsborgerskap: LandCti? = null,
    var flyktning: Boolean? = null,

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
    var personDetaljListe: MutableList<PersonDetalj> = mutableListOf(),

    /**
     * Dato for sist innmeldt i Folketrygden- for fremtidig trygdetid.
     */
    var sistMedlITrygden: Date? = null,

    /**
     * Siste gyldige år for opptjening som benyttes for alderspensjon2011/2016/2025 og AFP Privat.
     * For eksempel skal saksbehandling som utføres i 2010 ta utgangspunkt i opptjeninger til
     * og med 2008 (sisteGyldigeOpptjeningsAr vil her være 2008).
     */
    var sisteGyldigeOpptjeningsAr: Int = 0,

    /**
     * Angir om opptjeningsinformasjonen er hentet fra Opptjeningsregisteret og
     * registrert som grunnlag på kravet. Det vil være kun ett element i listen
     * ved beregnings/vilkårsprøvingstjenester. Ved tjenesten
     * KontrollerInformasjonsgrunnlag vil det være flere elementer.
     */
    var hentetPopp: Boolean? = null,

    /**
     * Angir om informasjonen om inntektsinformasjon er hentet fra
     * Inntektsregisteret og registrert som grunnlag på kravet.
     */
    //var hentetInnt: Boolean = false,
    var hentetInnt: Boolean? = null,

    /**
     * Angir om informasjonen om institusjonsopphold er hentet fra
     * Institusjonsoppholdsregisteret og registrert som grunnlag på kravet.
     */
    //var hentetInst: Boolean = false,
    var hentetInst: Boolean? = null,

    /**
     * Angir om trygdetidsinformasjon er hentet inn og registrert som grunnlag
     * på kravet.
     */
    //var hentetTT: Boolean = false,
    var hentetTT: Boolean? = null,

    /**
     * Angir om informasjon om arbeidsforhold er hentet fra
     * Arbeidsforholdregisteret og registrert som grunnlag på kravet.
     */
    //var hentetArbeid: Boolean = false,
    var hentetArbeid: Boolean? = null,

    /**
     * Angir om personens ytelse skal beregnes med overkompensasjonsregler.
     * Gjelde utenlandstilfeller.
     */
    var overkompUtl: Boolean? = null,

    /**
     * Liste av opptjeningsgrunnlag for personen.
     */
    var opptjeningsgrunnlagListe: MutableList<Opptjeningsgrunnlag> = mutableListOf(),

    /**
     * Liste av inntektsgrunnlag for personen.
     */
    var inntektsgrunnlagListe: MutableList<Inntektsgrunnlag> = mutableListOf(),

    /**
     * Liste av trygdetidsperioder for personen.
     */
    var trygdetidPerioder: MutableList<TTPeriode> = mutableListOf(),

    /**
     * Liste av trygdetidsperioder for personen.
     * For beregning av trygdetid for AP2016 iht. kapittel 20 og AP2025.
     */
    var trygdetidPerioderKapittel20: MutableList<TTPeriode> = mutableListOf(),

    /**
     * Framtidig og faktisk trygdetid. Denne ligger også i VilkarsVedtak, men er
     * nødvendig når vi skal ytelsesberegninger der det ikke finnes et vedtak.
     * F.eks Barnepensjon gjenlevende.
     */
    var trygdetid: Trygdetid? = null,

    /**
     * Samme som over, men for ny opptjeningsmodell.
     */
    var trygdetidKapittel20: Trygdetid? = null,

    /**
     * Trygdetid for alternative Uføretidspunkt.
     */
    var trygdetidAlternativ: Trygdetid? = null,

    /**
     * Spesifikke grunnlagsdata for uførepensjon.
     */
    var uforegrunnlag: Uforegrunnlag? = null,

    /**
     * Historikk for uføreytelser.Inneholder en blanding av data fra
     * Uforegrunnlag og UforeEkstra.
     */
    var uforeHistorikk: Uforehistorikk? = null,

    /**
     * Spesifikke grunnlagsdata for yrkesskadepensjon. Dersom yrkesskadegrunnlag =
     * null betyr det at personen ikke har yrkesskade.
     */
    var yrkesskadegrunnlag: Yrkesskadegrunnlag? = null,

    /**
     * Angir om en avdød døde av yrkesskade. Hvis satt vil det finnes et yrkesskadegrunnlag og en dodsdato.
     * Det skal da ikke finnes et uføregrunnlag. Brukes ved BP/GJP/GJR.
     */
    //var dodAvYrkesskade: Boolean = false,
    var dodAvYrkesskade: Boolean? = null,

    /**
     * Generell historisk informasjon om en person. Ventetilleggsgrunnlag, fravik_19_3.
     */
    var generellHistorikk: GenerellHistorikk? = null,

    /**
     * Historikk for AFP ytelser. Inneholder informasjon relevant for
     * perioden(e) bruker hadde AFP.
     */
    var afpHistorikkListe: MutableList<AfpHistorikk> = mutableListOf(),

    /**
     * Beskriver hvor mange barn det er i kullet.
     */
    var barnekull: Barnekull? = null,

    /**
     * Hentet som eneste element fra PEN Persongrunnlag.barnetilleggVurderingsperiodeListe
     */
    var barnetilleggVurderingsperiode: BarnetilleggVurderingsperiode? = null,

    /**
     * Antall år personen har bodd/arbeidet i utlandet etter fylte 16 år.
     */
    var antallArUtland: Int = 0,

    /**
     * Angir om personen har vært medlem i Folketrygden de siste 3 år. Brukes
     * ved simulering.
     */
    var medlemIFolketrygdenSiste3Ar: Boolean? = null, //SIMDOM-EDIT nullable

    /**
     * Angir om personen er over 60 år eller ikke kan forsørge seg selv. Brukes
     * i simulering for å angi vilkår for ektefelletillegg.
     */
    var over60ArKanIkkeForsorgesSelv: Boolean? = null, //SIMDOM-EDIT nullable

    /**
     * Liste av utenlandsopphold.
     */
    var utenlandsoppholdListe: MutableList<Utenlandsopphold> = mutableListOf(),

    /**
     * Trygdeavtale, representerer en saksbehandlers vurdering av hvilken
     * avtale/konvensjon som skal anvendes i en utenlandssak.
     */
    var trygdeavtale: Trygdeavtale? = null,

    /**
     * Detaljer knyttet til trygdeavtale. Brukes i beregningen av
     * tilleggspensjon i utenlandssaker.
     */
    var trygdeavtaledetaljer: Trygdeavtaledetaljer? = null,

    /**
     * Grunnlag påkrevd for å kunne behandle inngang og eksport av pensjonssaker mellom Norge og utland.
     */
    var inngangOgEksportGrunnlag: InngangOgEksportGrunnlag? = null,

    /**
     * Grunnlag påkrevd for å kunne behandle inngang og eksport av pensjonssaker mellom Norge og utland.
     */
    // SIMDOM-MOVE
    var forsteVirkningsdatoGrunnlagListe: MutableList<ForsteVirkningsdatoGrunnlag> = mutableListOf(),

    /**
     * Årlig pensjonsgivende inntekt var minst 1G på dødstidspunktet.
     */
    //var arligPGIMinst1G: Boolean = false,
    var arligPGIMinst1G: Boolean? = null,

    /**
     * Angir omdet skal beregnes etter artikkel 10 - nordisk konvensjon.
     * Trygdetiden kan bli redusert.
     */
    //var artikkel10: Boolean = false,
    var artikkel10: Boolean? = null,

    /**
     * Årstall for avtjent verneplikt. Maks 4 år godkjennes. Er null dersom det
     * ikke finnes vernepliktsår. Må være i stigende rekkefølge, eks:<br>
     * <code>[0] = 2001</code><br>
     * <code>[1] = 2002</code><br>
     * <code>[2] = 2004</code>
     */
    //var vernepliktAr: IntArray? = null,
    var vernepliktAr: IntArray = IntArray(0),

    /**
     * Den skiltes del av avdødes tilleggspensjon. Angis i prosent. Default
     * verdi settes til -99 fordi 0 er en lovlig verdi.
     */
    var skiltesDelAvAvdodesTP: Int = -99,

    /**
     * Liste av institusjonsoppholdsreduksjonsperioder relatert til
     * persongrunnlaget
     */
    var instOpphReduksjonsperiodeListe: MutableList<InstOpphReduksjonsperiode> = mutableListOf(),

    /**
     * Liste av institusjonsoppholdsfasteutgifterperioder relatert til
     * persongrunnlaget
     */
    var instOpphFasteUtgifterperiodeListe: MutableList<InstOpphFasteUtgifterperiode> = mutableListOf(),

    /**
     * Landskode - det land personen er bosatt i på VIRK. Intern PREG attributt.
     * Default settes denne til NOR og erstattes med Trygdeavtale.bostedsland
     * dersom trygdeavtale ikke er null.
     */
    var bosattLand: LandCti? = null,

    var pensjonsbeholdning: Pensjonsbeholdning? = null,

    /**
     * Informasjon om førstegangstjenesteperioder.
     */
    var forstegangstjenestegrunnlag: Forstegangstjeneste? = null,

    var dagpengegrunnlagListe: MutableList<Dagpengegrunnlag> = mutableListOf(),

    var omsorgsgrunnlagListe: MutableList<Omsorgsgrunnlag> = mutableListOf(),

    /**
     * Informasjon om arbeidsforhold med perioder og stillingsprosent.
     */
    var arbeidsforholdsgrunnlagListe: MutableList<Arbeidsforholdsgrunnlag> = mutableListOf(),

    /**
     * Contains information about post injury arbeidsforhold perioder and stillingsprosent.
     */
    var arbeidsforholdEtterUforgrunnlagListe: MutableList<ArbeidsforholdEtterUforgrunnlag> = mutableListOf(),

    /**
     *  overgangsinfoUPtilUT the overgangsinfoUPtilUT to set
     */
    var overgangsInfoUPtilUT: OvergangsinfoUPtilUT? = null,

    /**
     * Inneholder utbetalingsgraden for uføretrygd etter inntektsavkorting.
     */
    var utbetalingsgradUTListe: MutableList<UtbetalingsgradUT> = mutableListOf(),

    /**
     * Intern PREG attributt. Relasjon til personen's tilhørende vedtak.
     */
    @JsonIgnore
    var vilkarsVedtak: VilkarsVedtak? = null,

    /**
     * Intern PREG attributt. Sivilstand som gjelder på beregningstidspunkt.
     */
    @JsonIgnore
    var sivilstandType: SivilstandTypeCti? = null,

    /**
     * Intern PREG attributt. Grunnlagsrolle som gjelder på beregningstidspunkt.
     */
    @JsonIgnore
    var grunnlagsrolle: GrunnlagsrolleCti? = null,

    /**
     * Intern PREG attributt. BarnDetalj som gjelder på beregningstidspunkt.
     */
    @JsonIgnore
    var barnDetalj: BarnDetalj? = null,

    /**
     * Intern PREG attributt. Angir om brukeren skal behandles som gift. Satt på
     * grunnlag av tilknyttet persons borMed relasjon til søker.
     */
    @JsonIgnore
    var behandlesSomGift: Boolean = false,

    /**
     * Intern PREG attributt. Angir om poengrekkeberegningen skal bruke
     * opptjening kun til og med 67 år.
     */
    @JsonIgnore
    var P67: Boolean = false,

    @JsonIgnore
    var borMed: BorMedTypeCti? = null,

    @JsonIgnore
    var instOpphReduksjonsperiode: InstOpphReduksjonsperiode? = null,

    @JsonIgnore
    var instOpphFasteUtgifterperiode: InstOpphFasteUtgifterperiode? = null,

    /**
     * Denne PREG variabel brukes ikke slik den er navngitt!
     * Er omdøpt til "mottarPensjon" i regelmotor og brukes til
     * å angi om personen selv mottar pensjon.
     */
    @JsonIgnore
    var ektefellenMottarPensjon: Boolean = false,

    @JsonIgnore
    var personDetalj: PersonDetalj? = null,

    @JsonIgnore
    var poengtillegg: Double = 0.0,

    @JsonIgnore
    var boddEllerArbeidetIUtlandet: Boolean = false,

    /**
     * Midlertidig felt. Brukes til å sette første virk på grunnlaget til MOR/FAR
     * ved barnepensjon. Angir første virk på vedtaket (som ikke er med).
     * Ref CR 81364 og 85157 - fiktiv uføreperiode med type VIRK_IKKE_UFOR.
     * Kun getter/setter for feltet, ikke med i constructor'ene.
     */
    @JsonIgnore
    var forsteVirk: Date? = null,

    /**
     * Objekt som inneholder informasjon om TP-ordningers uførepensjonsgrunnlag. Dette er manuelt registrerte data og ikke hentet fra TP-registeret eller andre eksterne kilder.
     */
    var AfpTpoUpGrunnlag: AfpTpoUpGrunnlag? = null,

    /**
     * Liste over hovedytelser som kan påvirke ytelsen. Kun fastsatt for EPS.
     */
    @JsonIgnore
    var vilkarsvedtakEPSListe: MutableList<VilkarsVedtak> = mutableListOf()
) : Serializable {
    // SIMDOM-ADD
    @JsonIgnore
    var gjelderOmsorg: Boolean = false
    @JsonIgnore
    var gjelderUforetrygd: Boolean = false
    @JsonIgnore
    val barnetilleggVurderingsperioder: MutableList<BarnetilleggVurderingsperiode> =
        mutableListOf() // discriminator: BT_VURDERINGSPERIODE
    @JsonIgnore
    val beholdninger: MutableList<Pensjonsbeholdning> = mutableListOf() // BEHOLDNING
    @JsonIgnore
    val trygdetider: MutableList<Trygdetid> = mutableListOf() // TRYGDETID
    @JsonIgnore
    val uforegrunnlagList: MutableList<Uforegrunnlag> = mutableListOf() // UFORE
    @JsonIgnore
    val yrkesskadegrunnlagList: MutableList<Yrkesskadegrunnlag> = mutableListOf() // YRKESKADE
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
    // end SIMDOM-ADD

    val sortedTrygdetidPerioderKapittel20: MutableList<TTPeriode>
        get() {
            val sortedList = trygdetidPerioderKapittel20
            sortedList.sort()
            return sortedList
        }

    /**
     * Copy Constructor
     */
    // SIMDOM-ADD excludeForsteVirkningsdatoGrunnlag
    constructor(persongrunnlag: Persongrunnlag, excludeForsteVirkningsdatoGrunnlag: Boolean = false) : this() {
        if (persongrunnlag.penPerson != null) {
            this.penPerson = PenPerson(persongrunnlag.penPerson!!)
        }
        if (persongrunnlag.fodselsdato != null) {
            this.fodselsdato = persongrunnlag.fodselsdato!!.clone() as Date
        }
        if (persongrunnlag.dodsdato != null) {
            this.dodsdato = persongrunnlag.dodsdato!!.clone() as Date
        }
        if (persongrunnlag.statsborgerskap != null) {
            this.statsborgerskap = LandCti(persongrunnlag.statsborgerskap)
        }
        if (persongrunnlag.flyktning != null) {
            this.flyktning = persongrunnlag.flyktning
        }
        for (personDetalj in persongrunnlag.personDetaljListe) {
            this.personDetaljListe.add(PersonDetalj(personDetalj))
        }
        if (persongrunnlag.sistMedlITrygden != null) {
            this.sistMedlITrygden = persongrunnlag.sistMedlITrygden!!.clone() as Date
        }
        if (persongrunnlag.hentetPopp != null) {
            this.hentetPopp = persongrunnlag.hentetPopp
        }
        this.hentetInnt = persongrunnlag.hentetInnt
        this.hentetInst = persongrunnlag.hentetInst
        this.hentetTT = persongrunnlag.hentetTT
        this.hentetArbeid = persongrunnlag.hentetArbeid
        if (persongrunnlag.overkompUtl != null) {
            this.overkompUtl = persongrunnlag.overkompUtl
        }
        for (opptjeningsgrunnlag in persongrunnlag.opptjeningsgrunnlagListe) {
            this.opptjeningsgrunnlagListe.add(Opptjeningsgrunnlag(opptjeningsgrunnlag))
        }
        for (inntektsgrunnlag in persongrunnlag.inntektsgrunnlagListe) {
            this.inntektsgrunnlagListe.add(Inntektsgrunnlag(inntektsgrunnlag))
        }
        for (ttPeriode in persongrunnlag.trygdetidPerioder) {
            this.trygdetidPerioder.add(TTPeriode(ttPeriode))
        }
        for (ttPeriode in persongrunnlag.trygdetidPerioderKapittel20) {
            this.trygdetidPerioderKapittel20.add(TTPeriode(ttPeriode))
        }
        if (persongrunnlag.trygdetid != null) {
            this.trygdetid = Trygdetid(persongrunnlag.trygdetid!!)
        }
        if (persongrunnlag.uforegrunnlag != null) {
            this.uforegrunnlag = Uforegrunnlag(persongrunnlag.uforegrunnlag!!)
        }
        if (persongrunnlag.uforeHistorikk != null) {
            this.uforeHistorikk = Uforehistorikk(persongrunnlag.uforeHistorikk!!)
        }
        if (persongrunnlag.yrkesskadegrunnlag != null) {
            this.yrkesskadegrunnlag = Yrkesskadegrunnlag(persongrunnlag.yrkesskadegrunnlag!!)
        }
        this.dodAvYrkesskade = persongrunnlag.dodAvYrkesskade
        if (persongrunnlag.generellHistorikk != null) {
            this.generellHistorikk = GenerellHistorikk(persongrunnlag.generellHistorikk!!)
        }
        for (afpHistorikk in persongrunnlag.afpHistorikkListe) {
            this.afpHistorikkListe.add(AfpHistorikk(afpHistorikk))
        }
        if (persongrunnlag.barnekull != null) {
            this.barnekull = Barnekull(persongrunnlag.barnekull!!)
        }
        this.antallArUtland = persongrunnlag.antallArUtland
        this.medlemIFolketrygdenSiste3Ar = persongrunnlag.medlemIFolketrygdenSiste3Ar
        this.over60ArKanIkkeForsorgesSelv = persongrunnlag.over60ArKanIkkeForsorgesSelv
        for (utenlandsopphold in persongrunnlag.utenlandsoppholdListe) {
            this.utenlandsoppholdListe.add(Utenlandsopphold(utenlandsopphold))
        }
        if (persongrunnlag.trygdeavtale != null) {
            this.trygdeavtale = Trygdeavtale(persongrunnlag.trygdeavtale!!)
        }
        if (persongrunnlag.trygdeavtaledetaljer != null) {
            this.trygdeavtaledetaljer = Trygdeavtaledetaljer(persongrunnlag.trygdeavtaledetaljer!!)
        }
        if (persongrunnlag.inngangOgEksportGrunnlag != null) {
            this.inngangOgEksportGrunnlag = InngangOgEksportGrunnlag(persongrunnlag.inngangOgEksportGrunnlag!!)
        }
        this.arligPGIMinst1G = persongrunnlag.arligPGIMinst1G
        this.artikkel10 = persongrunnlag.artikkel10
        if (persongrunnlag.vernepliktAr != null) {
            this.vernepliktAr = persongrunnlag.vernepliktAr!!.clone()
        }
        this.skiltesDelAvAvdodesTP = persongrunnlag.skiltesDelAvAvdodesTP
        for (instOpphReduksjonsperiode in persongrunnlag.instOpphReduksjonsperiodeListe) {
            this.instOpphReduksjonsperiodeListe.add(InstOpphReduksjonsperiode(instOpphReduksjonsperiode))
        }
        for (instOpphFasteUtgifterperiode in persongrunnlag.instOpphFasteUtgifterperiodeListe) {
            this.instOpphFasteUtgifterperiodeListe.add(InstOpphFasteUtgifterperiode(instOpphFasteUtgifterperiode))
        }
        if (persongrunnlag.bosattLand != null) {
            this.bosattLand = LandCti(persongrunnlag.bosattLand)
        }

        if (!excludeForsteVirkningsdatoGrunnlag) {
            for (forsteVirkningsdatoGrunnlag in persongrunnlag.forsteVirkningsdatoGrunnlagListe) {
                forsteVirkningsdatoGrunnlagListe.add(ForsteVirkningsdatoGrunnlag(forsteVirkningsdatoGrunnlag))
            }
        }

        if (persongrunnlag.trygdetidKapittel20 != null) {
            this.trygdetidKapittel20 = Trygdetid(persongrunnlag.trygdetidKapittel20!!)
        }
        if (persongrunnlag.pensjonsbeholdning != null) {
            this.pensjonsbeholdning = Pensjonsbeholdning(persongrunnlag.pensjonsbeholdning!!)
        }
        for (dagpengegrunnlag in persongrunnlag.dagpengegrunnlagListe) {
            this.dagpengegrunnlagListe.add(Dagpengegrunnlag(dagpengegrunnlag))
        }
        if (persongrunnlag.forstegangstjenestegrunnlag != null) {
            this.forstegangstjenestegrunnlag = Forstegangstjeneste(persongrunnlag.forstegangstjenestegrunnlag!!)
        }
        for (omsorgsgrunnlag in persongrunnlag.omsorgsgrunnlagListe) {
            this.omsorgsgrunnlagListe.add(Omsorgsgrunnlag(omsorgsgrunnlag))
        }
        for (afg in persongrunnlag.arbeidsforholdsgrunnlagListe) {
            this.arbeidsforholdsgrunnlagListe.add(Arbeidsforholdsgrunnlag(afg))
        }
        for (arbeidsforholdEtterUforgrunnlag in persongrunnlag.arbeidsforholdEtterUforgrunnlagListe) {
            this.arbeidsforholdEtterUforgrunnlagListe.add(
                ArbeidsforholdEtterUforgrunnlag(
                    arbeidsforholdEtterUforgrunnlag
                )
            )
        }

        if (persongrunnlag.overgangsInfoUPtilUT != null) {
            this.overgangsInfoUPtilUT = OvergangsinfoUPtilUT(persongrunnlag.overgangsInfoUPtilUT!!)
        }
        for (utbetalingsgradUT in persongrunnlag.utbetalingsgradUTListe) {
            this.utbetalingsgradUTListe.add(UtbetalingsgradUT(utbetalingsgradUT))
        }
        if (persongrunnlag.trygdetidAlternativ != null) {
            this.trygdetidAlternativ = Trygdetid(persongrunnlag.trygdetidAlternativ!!)
        }
        //PREG_
        if (persongrunnlag.vilkarsVedtak != null) {
            this.vilkarsVedtak = VilkarsVedtak(persongrunnlag.vilkarsVedtak!!)
        }
        if (persongrunnlag.sivilstandType != null) {
            this.sivilstandType = SivilstandTypeCti(persongrunnlag.sivilstandType)
        }
        if (persongrunnlag.grunnlagsrolle != null) {
            this.grunnlagsrolle = GrunnlagsrolleCti(persongrunnlag.grunnlagsrolle)
        }
        if (persongrunnlag.barnDetalj != null) {
            this.barnDetalj = BarnDetalj(persongrunnlag.barnDetalj!!)
        }
        this.behandlesSomGift = persongrunnlag.behandlesSomGift
        this.P67 = persongrunnlag.P67
        if (persongrunnlag.borMed != null) {
            this.borMed = BorMedTypeCti(persongrunnlag.borMed)
        }
        if (persongrunnlag.instOpphReduksjonsperiode != null) {
            this.instOpphReduksjonsperiode = InstOpphReduksjonsperiode(persongrunnlag.instOpphReduksjonsperiode!!)
        }
        if (persongrunnlag.instOpphFasteUtgifterperiode != null) {
            this.instOpphFasteUtgifterperiode =
                InstOpphFasteUtgifterperiode(persongrunnlag.instOpphFasteUtgifterperiode!!)
        }
        this.ektefellenMottarPensjon = persongrunnlag.ektefellenMottarPensjon
        if (persongrunnlag.personDetalj != null) {
            this.personDetalj = PersonDetalj(persongrunnlag.personDetalj!!)
        }
        this.poengtillegg = persongrunnlag.poengtillegg
        this.boddEllerArbeidetIUtlandet = persongrunnlag.boddEllerArbeidetIUtlandet
        this.sisteGyldigeOpptjeningsAr = persongrunnlag.sisteGyldigeOpptjeningsAr
        this.barnetilleggVurderingsperiode = persongrunnlag.barnetilleggVurderingsperiode

        if (persongrunnlag.AfpTpoUpGrunnlag != null) {
            this.AfpTpoUpGrunnlag = AfpTpoUpGrunnlag(persongrunnlag.AfpTpoUpGrunnlag!!)
        }
        for (vilkarsVedtak in persongrunnlag.vilkarsvedtakEPSListe) {
            this.vilkarsvedtakEPSListe.add(VilkarsVedtak(vilkarsVedtak))
        }
        // SIMDOM-ADD
        this.gjelderOmsorg = persongrunnlag.gjelderOmsorg //: Boolean = false
        this.gjelderUforetrygd = persongrunnlag.gjelderUforetrygd //: Boolean = false
        persongrunnlag.barnetilleggVurderingsperioder.forEach {
            this.barnetilleggVurderingsperioder.add(
                BarnetilleggVurderingsperiode(it)
            )
        }
        persongrunnlag.beholdninger.forEach { this.beholdninger.add(Pensjonsbeholdning(it)) }
        persongrunnlag.trygdetider.forEach { this.trygdetider.add(Trygdetid(it)) }
        persongrunnlag.uforegrunnlagList.forEach { this.uforegrunnlagList.add(Uforegrunnlag(it)) }
        persongrunnlag.yrkesskadegrunnlagList.forEach { this.yrkesskadegrunnlagList.add(Yrkesskadegrunnlag(it)) }
        this.rawFodselsdato = persongrunnlag.rawFodselsdato?.clone() as? Date
        this.rawDodsdato = persongrunnlag.rawDodsdato?.clone() as? Date
        this.rawSistMedlITrygden = persongrunnlag.rawSistMedlITrygden?.clone() as? Date
        // end SIMDOM-ADD
    }

    /**
     * Laget ifm. migrering - noen regelsett/flyter som ser ut til å trenge denne variabelen som liste.
     */
    fun getVernepliktArAsList(): List<Int> {
        val vernepliktArList = mutableListOf<Int>()
        vernepliktAr?.forEach { vernepliktArList.add(it) }
        return vernepliktArList
    }

    /**
     * Returnerer førsteKravFremsattDato utledet fra forsteVirkningsdatoGrunnlagListe. Hvis listen har innhold er førsteKravFremsattDato den tidligste dato blant disse.
     * @return Date
     */
    fun finnForsteKravFremsattDato(): Date? {
        if (forsteVirkningsdatoGrunnlagListe.size >= 1) {
            forsteVirkningsdatoGrunnlagListe.sortWith { obj: ForsteVirkningsdatoGrunnlag, other: ForsteVirkningsdatoGrunnlag? ->
                obj.compareTo(
                    other!!
                )
            }
            return forsteVirkningsdatoGrunnlagListe[0].kravFremsattDato
        }
        return null
    }

    //SIMDOM-ADD:

    fun addBeholdning(beholdning: Pensjonsbeholdning) {
        beholdninger.add(beholdning)
        pensjonsbeholdning = findLatest(beholdninger)
    }

    fun replaceBeholdninger(list: List<Pensjonsbeholdning>) {
        beholdninger.clear()
        beholdninger.addAll(list)
        // pensjon-regler only uses latest beholdning
        pensjonsbeholdning = findLatest(beholdninger) // cf. kjerne.Persongrunnlag.getPensjonsbeholdning
    }

    /**
     * NB: Comment on source of this method (no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.findPersonDetaljIPersongrunnlag):
     * "deprecated - this method picks PersonDetalj without considering periode; this is a strategy that could lead to errors"
     */
    fun findPersonDetaljIPersongrunnlag(grunnlagsrolle: GrunnlagRolle, checkBruk: Boolean): PersonDetalj? {
        for (detalj in personDetaljListe) {
            if (detalj.grunnlagsrolle!!.kode == grunnlagsrolle.name) {
                if (checkBruk) {
                    if (detalj.bruk) {
                        return detalj
                    }
                } else {
                    return detalj
                }
            }
        }

        return null
    }

    // no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.findPersonDetaljWithRolleForPeriode
    fun findPersonDetaljWithRolleForPeriode(
        rolle: GrunnlagRolle,
        virkningDato: Date?,
        checkBruk: Boolean
    ): PersonDetalj? =
        personDetaljListe.firstOrNull {
            (!checkBruk || it.bruk) &&
                    (rolle.name == it.grunnlagsrolle?.kode) &&
                    isDateInPeriod(virkningDato, it.virkFom, it.virkTom)
        }

    // no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.isAvdod
    fun isAvdod() = findPersonDetaljIPersongrunnlag(grunnlagsrolle = GrunnlagRolle.AVDOD, checkBruk = true) != null

    fun isBarnOrFosterbarn() =
        hasPersondetaljWithRolle(GrunnlagRolle.BARN) ||
                hasPersondetaljWithRolle(GrunnlagRolle.FBARN)

    fun isEps() =
        hasPersondetaljWithRolle(GrunnlagRolle.EKTEF) ||
                hasPersondetaljWithRolle(GrunnlagRolle.PARTNER) ||
                hasPersondetaljWithRolle(GrunnlagRolle.SAMBO)

    fun isSoker() = hasPersondetaljWithRolle(GrunnlagRolle.SOKER)

    private fun hasPersondetaljWithRolle(rolle: GrunnlagRolle) =
        personDetaljListe.any { it.bruk && it.grunnlagsrolle!!.kode == rolle.name }

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
}
