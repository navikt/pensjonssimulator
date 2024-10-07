package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.OvergangsinfoUPtilUT
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.PeriodeUtil.findLatest
import java.util.*

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
    var afpHistorikkListe: MutableList<AfpHistorikk> = mutableListOf() // SIMDOM-EDIT: Mutable

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
     * ikke finnes vernepliktsær. må være i stigende rekkefålge, eks:<br></br>
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
    var instOpphReduksjonsperiodeListe: MutableList<InstOpphReduksjonsperiode> = mutableListOf() // SIMDOM-EDIT: Mutable

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
    var arbeidsforholdEtterUforgrunnlagListe: MutableList<ArbeidsforholdEtterUforgrunnlag> =
        mutableListOf() // SIMDOM-EDIT: Mutable

    /**
     * Informasjon om konverterting fra UP til UT.
     */
    var overgangsInfoUPtilUT: OvergangsinfoUPtilUT? = null

    /**
     * Inneholder utbetalingsgraden for uføretrygd etter inntektsavkorting.
     */
    var utbetalingsgradUTListe: MutableList<UtbetalingsgradUT> = mutableListOf() // SIMDOM-EDIT: Mutable

    /**
     * Objekt som inneholder informasjon om TP-ordningers uførepensjonsgrunnlag. Dette er manuelt registrerte data og ikke hentet fra TP-registeret eller andre eksterne kilder.
     */
    var afpTpoUpGrunnlag: AfpTpoUpGrunnlag? = null

    /**
     * Støttefelt for virk_ikke_ufor-hacket. Feltet er ikke forventet populert.
     */
    @JsonIgnore
    var forsteVirk: Date? = null

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

    // SIMDOM-ADD excludeForsteVirkningsdatoGrunnlag
    constructor(source: Persongrunnlag, excludeForsteVirkningsdatoGrunnlag: Boolean = false) : this() {
        if (source.penPerson != null) {
            this.penPerson = PenPerson(source.penPerson!!)
        }

        if (source.fodselsdato != null) {
            this.fodselsdato = source.fodselsdato!!.clone() as Date
        }

        if (source.dodsdato != null) {
            this.dodsdato = source.dodsdato!!.clone() as Date
        }

        if (source.statsborgerskapEnum != null) {
            this.statsborgerskapEnum = source.statsborgerskapEnum
        }

        if (source.flyktning != null) {
            this.flyktning = source.flyktning
        }

        for (personDetalj in source.personDetaljListe) {
            this.personDetaljListe.add(PersonDetalj(personDetalj))
        }

        if (source.sistMedlITrygden != null) {
            this.sistMedlITrygden = source.sistMedlITrygden!!.clone() as Date
        }

        if (source.hentetPopp != null) {
            this.hentetPopp = source.hentetPopp
        }

        this.hentetInnt = source.hentetInnt
        this.hentetInst = source.hentetInst
        this.hentetTT = source.hentetTT
        this.hentetArbeid = source.hentetArbeid

        if (source.overkompUtl != null) {
            this.overkompUtl = source.overkompUtl
        }

        for (opptjeningsgrunnlag in source.opptjeningsgrunnlagListe) {
            this.opptjeningsgrunnlagListe.add(Opptjeningsgrunnlag(opptjeningsgrunnlag))
        }

        for (inntektsgrunnlag in source.inntektsgrunnlagListe) {
            this.inntektsgrunnlagListe.add(Inntektsgrunnlag(inntektsgrunnlag))
        }

        for (ttPeriode in source.trygdetidPerioder) {
            this.trygdetidPerioder.add(TTPeriode(ttPeriode))
        }

        for (ttPeriode in source.trygdetidPerioderKapittel20) {
            this.trygdetidPerioderKapittel20.add(TTPeriode(ttPeriode))
        }

        if (source.trygdetid != null) {
            this.trygdetid = Trygdetid(source.trygdetid!!)
        }

        if (source.uforegrunnlag != null) {
            this.uforegrunnlag = Uforegrunnlag(source.uforegrunnlag!!)
        }

        if (source.uforeHistorikk != null) {
            this.uforeHistorikk = Uforehistorikk(source.uforeHistorikk!!)
        }

        if (source.yrkesskadegrunnlag != null) {
            this.yrkesskadegrunnlag = Yrkesskadegrunnlag(source.yrkesskadegrunnlag!!)
        }

        this.dodAvYrkesskade = source.dodAvYrkesskade

        if (source.generellHistorikk != null) {
            this.generellHistorikk = GenerellHistorikk(source.generellHistorikk!!)
        }

        for (afpHistorikk in source.afpHistorikkListe) {
            this.afpHistorikkListe.add(AfpHistorikk(afpHistorikk))
        }

        if (source.barnekull != null) {
            this.barnekull = Barnekull(source.barnekull!!)
        }

        this.antallArUtland = source.antallArUtland
        this.medlemIFolketrygdenSiste3Ar = source.medlemIFolketrygdenSiste3Ar
        this.over60ArKanIkkeForsorgesSelv = source.over60ArKanIkkeForsorgesSelv

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

        this.skiltesDelAvAvdodesTP = source.skiltesDelAvAvdodesTP

        for (instOpphReduksjonsperiode in source.instOpphReduksjonsperiodeListe) {
            this.instOpphReduksjonsperiodeListe.add(InstOpphReduksjonsperiode(instOpphReduksjonsperiode))
        }

        for (instOpphFasteUtgifterperiode in source.instOpphFasteUtgifterperiodeListe) {
            this.instOpphFasteUtgifterperiodeListe.add(InstOpphFasteUtgifterperiode(instOpphFasteUtgifterperiode))
        }

        if (source.bosattLandEnum != null) {
            this.bosattLandEnum = source.bosattLandEnum
        }

        if (!excludeForsteVirkningsdatoGrunnlag) {
            for (forsteVirkningsdatoGrunnlag in source.forsteVirkningsdatoGrunnlagListe) {
                forsteVirkningsdatoGrunnlagListe.add(ForsteVirkningsdatoGrunnlag(forsteVirkningsdatoGrunnlag))
            }
        }

        if (source.trygdetidKapittel20 != null) {
            this.trygdetidKapittel20 = Trygdetid(source.trygdetidKapittel20!!)
        }

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
            this.arbeidsforholdsgrunnlagListe.add(Arbeidsforholdsgrunnlag(afg))
        }

        for (arbeidsforholdEtterUforgrunnlag in source.arbeidsforholdEtterUforgrunnlagListe) {
            this.arbeidsforholdEtterUforgrunnlagListe.add(
                ArbeidsforholdEtterUforgrunnlag(
                    arbeidsforholdEtterUforgrunnlag
                )
            )
        }

        if (source.overgangsInfoUPtilUT != null) {
            this.overgangsInfoUPtilUT = OvergangsinfoUPtilUT(source.overgangsInfoUPtilUT!!)
        }

        for (utbetalingsgradUT in source.utbetalingsgradUTListe) {
            this.utbetalingsgradUTListe.add(UtbetalingsgradUT(utbetalingsgradUT))
        }

        if (source.trygdetidAlternativ != null) {
            this.trygdetidAlternativ = Trygdetid(source.trygdetidAlternativ!!)
        }

        this.sisteGyldigeOpptjeningsAr = source.sisteGyldigeOpptjeningsAr
        this.barnetilleggVurderingsperiode = source.barnetilleggVurderingsperiode

        if (source.afpTpoUpGrunnlag != null) {
            this.afpTpoUpGrunnlag = AfpTpoUpGrunnlag(source.afpTpoUpGrunnlag!!)
        }
        //for (vilkarsVedtak in persongrunnlag.vilkarsvedtakEPSListe) {
        //    this.vilkarsvedtakEPSListe.add(VilkarsVedtak(vilkarsVedtak))
        //}
        // SIMDOM-ADD
        this.gjelderOmsorg = source.gjelderOmsorg //: Boolean = false
        this.gjelderUforetrygd = source.gjelderUforetrygd //: Boolean = false

        source.barnetilleggVurderingsperioder.forEach {
            this.barnetilleggVurderingsperioder.add(
                BarnetilleggVurderingsperiode(it)
            )
        }

        source.beholdninger.forEach { this.beholdninger.add(Pensjonsbeholdning(it)) }
        source.trygdetider.forEach { this.trygdetider.add(Trygdetid(it)) }
        source.uforegrunnlagList.forEach { this.uforegrunnlagList.add(Uforegrunnlag(it)) }
        source.yrkesskadegrunnlagList.forEach { this.yrkesskadegrunnlagList.add(Yrkesskadegrunnlag(it)) }
        this.rawFodselsdato = source.rawFodselsdato?.clone() as? Date
        this.rawDodsdato = source.rawDodsdato?.clone() as? Date
        this.rawSistMedlITrygden = source.rawSistMedlITrygden?.clone() as? Date
        // end SIMDOM-ADD
    }

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
    fun findPersonDetaljIPersongrunnlag(grunnlagsrolle: GrunnlagsrolleEnum, checkBruk: Boolean): PersonDetalj? {
        for (detalj in personDetaljListe) {
            if (detalj.grunnlagsrolleEnum == grunnlagsrolle) {
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
        rolle: GrunnlagsrolleEnum,
        virkningDato: Date?,
        checkBruk: Boolean
    ): PersonDetalj? =
        personDetaljListe.firstOrNull {
            (!checkBruk || it.bruk) &&
                    (rolle == it.grunnlagsrolleEnum) &&
                    isDateInPeriod(virkningDato, it.virkFom, it.virkTom)
        }

    // no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.isAvdod
    fun isAvdod() = findPersonDetaljIPersongrunnlag(grunnlagsrolle = GrunnlagsrolleEnum.AVDOD, checkBruk = true) != null

    fun isBarnOrFosterbarn() =
        hasPersondetaljWithRolle(GrunnlagsrolleEnum.BARN) ||
                hasPersondetaljWithRolle(GrunnlagsrolleEnum.FBARN)

    fun isEps() =
        hasPersondetaljWithRolle(GrunnlagsrolleEnum.EKTEF) ||
                hasPersondetaljWithRolle(GrunnlagsrolleEnum.PARTNER) ||
                hasPersondetaljWithRolle(GrunnlagsrolleEnum.SAMBO)

    fun isSoker() = hasPersondetaljWithRolle(GrunnlagsrolleEnum.SOKER)

    private fun hasPersondetaljWithRolle(rolle: GrunnlagsrolleEnum) =
        personDetaljListe.any { it.bruk && it.grunnlagsrolleEnum == rolle }

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
    // end SIMDOM-ADD
}
