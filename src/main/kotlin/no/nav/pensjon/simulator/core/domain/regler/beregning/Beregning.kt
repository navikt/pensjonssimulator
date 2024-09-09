package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.IBeregning
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.*
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.Beregning2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.MinstenivatilleggIndividuelt
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.MinstenivatilleggPensjonistpar
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.EosEkstra
import no.nav.pensjon.simulator.core.domain.regler.kode.*
import java.io.Serializable
import java.util.*

/**
 * Beregning er resultatet fra en beregning i PREG.
 */
class Beregning : IBeregning, Serializable {
    /**
     * Referanse tilbake til beregningsrelasjon dersom denne beregning inngår i en beregningsrelasjon.
     */
    @JsonIgnore
    var beregningsrelasjon: BeregningRelasjon? = null

    var penPerson: PenPerson? = null
    var virkFom: Date? = null
    var virkTom: Date? = null
    var brutto: Int = 0
    var netto: Int = 0
    var gp: Grunnpensjon? = null

    /**
     * Grunnpensjonen regulert med lønnsvekst.Fra og med Horisonten.
     */
    var gpKapittel3: Grunnpensjon? = null

    /**
     * Uavkortet grunnpensjon regulert med lønnsvekst - 0.0075.
     * Trengs kun for Afp hvor gp er avkortet kan være avkortet til 70%.
     */
    var gpAfpPensjonsregulert: Grunnpensjon? = null
    var tp: Tilleggspensjon? = null
    var tpKapittel3: Tilleggspensjon? = null
    var st: Sertillegg? = null
    var stKapittel3: Sertillegg? = null
    var minstenivatilleggPensjonistpar: MinstenivatilleggPensjonistpar? = null
    var minstenivatilleggIndividuelt: MinstenivatilleggIndividuelt? = null
    var afpTillegg: AfpTillegg? = null
    var vt: Ventetillegg? = null
    var vtKapittel3: Ventetillegg? = null
    var p851_tillegg: Paragraf_8_5_1_tillegg? = null
    var et: Ektefelletillegg? = null
    var tfb: BarnetilleggFellesbarn? = null
    var tsb: BarnetilleggSerkullsbarn? = null
    var familietillegg: Familietillegg? = null
    var tilleggFasteUtgifter: FasteUtgifterTillegg? = null
    var garantitillegg_Art_27: Garantitillegg_Art_27? = null
    var garantitillegg_Art_50: Garantitillegg_Art_50? = null
    var hjelpeloshetsbidrag: Hjelpeloshetsbidrag? = null
    var krigOgGammelYrkesskade: KrigOgGammelYrkesskade? = null

    /**
     * Ved konvertering av uførepensjon til uføretrygd, og gradert yrkesskade
     * brukes denne for å angi andel tilleggspensjon som stammer fra restgrad
     * og som stammer fra yrkesskadegrad.
     */
    var konverteringsdataUT: KonverteringsdataUT? = null
    var mendel: Mendel? = null
    var tilleggTilHjelpIHuset: TilleggTilHjelpIHuset? = null
    var g: Int = 0

    /**
     * Anvendt tt i beregningen, i hele år. Blir satt lik antall poengår dersom antall
     * poengår (fra poengrekke-beregningen) er større enn Trygdetid.tt.
     */
    var tt_anv: Int = 0

    /**
     * Angir hvilken metode som ble benyttet ved beregningen.EØS/nordisk/et annet land
     */
    var beregningsMetode: BeregningMetodeTypeCti? = null
    var trygdetid: Trygdetid? = null

    /**
     * Liste av beregninger.Kan inneholde alternative beregninger gjort ved lønnsomhetsberegninger.Vinneren ligger da i hovedobjektet mens taperen(e) ligger i denne listen.
     */
    override var delberegningsListe: MutableList<BeregningRelasjon> = mutableListOf()

    /**
     * Angir type beregning: BER, HJELPEBER, SAM_BER, VUR_BER.
     */
    var beregningType: BeregningTypeCti? = null

    /**
     * Hvilken resultattype en beregning er : AP,AP_GJP osv.
     */
    var resultatType: ResultatTypeCti? = null

    /**
     * Felt som gjøres for å holde orden på de forskjellige beregningene,
     * f.eks hjelpeberegninger.
     */
    @JsonIgnore
    override var beregningsnavn: String = resultatType?.kode ?: "Ukjentnavn"

    /**
     * Flegg som brukes på beregninger med beregningType HJELPEBER.
     * Anvendes i lønnsomhetsberegninger (VUR_BER). Settes på den av delberegningene som ikke er HJELPEBER
     * dersom TP'en fra HJELPEBER er brukt. Da er også flagget bruk i beregningsrelasjonen som hjelpeberegningen
     * inngår i satt.
     */
    var ikkeTraverser: Boolean = false

    /**
     * Er ektefelles totalinntekt, inkludert alle relevante inntekter > 2G.
     */
    var ektefelleInntektOver2g: Boolean = false

    /**
     * Hvorvidt det er en P67 beregning.Gjelder alderspensjon .
     */
    var p67beregning: Boolean = false

    /**
     * Årsak til periodisering av beregningen, G-justering, regelendring osv.
     * Settes dersom beregningen blir periodiserer av PREG (i.e. det returneres en liste av beregninger ).
     * Skal brukes i saksbehandling for å vise årsaken til periodiseringen av beregningen.
     * Hvis det er flere årsaker til at det er en periode i beregningen på samme periode (i.e. endring i sats og regel på samme dag så må det være en prioritering av endringene.
     * Da skal den årsak som mest sjelden skjer settes.
     */
    var beregningArsak: BeregningArsakCti? = null

    /**
     * Angir type av minstepensjon.
     * ER_MINST_PEN eller IKKE_MINST_PEN
     */
    var minstepensjontype: MinstepensjonTypeCti? = null

    /**
     * Samler opp årsakene til at en brukers minstepensjonstatus er satt og tilhørende verdier
     * dersom disse er relevante til beslutningen.
     */
    var minstepensjonArsak: String? = null

    /**
     * Angir om denne beregningen er top-noden av beregningene.
     */
    var totalVinner: Boolean = false

    /**
     * Pensjonsgraden ved AFP,heltall mellom 0-100.
     */
    var afpPensjonsgrad: Int = 0

    /**
     * Fribeløpet en pensjonist kan ha før vilkårene for å få ytelsen blir avkortet (AP = 2G, GJP = 0,5G etc).
     */
    var fribelop: Int = 0

    /**
     * Friinntekt = (IEU) Inntekt den uføre (UP) kan ha etter uføretidspunktet.
     * Beregnet etter inntekt før uføre (IFU).
     * IEU er grensen for hva inntekten kan være før en må revurdere uføregraden
     */
    var friinntekt: Int = 0

    /**
     * Beregnet fremtidig inntekt.Det er en G-omregnet oppgitt fremtidig inntekt som skal brukes av inntektskontrollen i Myggen.
     * beregnetFremtidigInntekt er G-omregnet oppgitt fremtidig inntekt som skal brukes av inntektskontrollen i Myggen.
     * PREG bruker Inntekt av type (FPI eller HYPF eller HYPF2G) og opptjusterer denne ihht til G:
     * beregnetFremtidigInntekt = (FPI eller HYPF eller HYPF2G) * nyG/gammelG
     * Dette attributtet er ytelsesuavhengig.
     * 30-jan-2008: Denne må antagelig suppleres med en tilsvarende beregnetFremtidigInntekt på Ektefelletillegget.
     * Verdien skal oppfattes som en grenseverdi,dvs den verdien som ble lagt til grunn ved eventuell avkorting.
     * For UP vil denne verdien settes lik uforeEkstra.tak.
     */
    var beregnetFremtidigInntekt: Int = 0

    /**
     * Angir om ektefellen mottar ytelser fra Folketrygden, dvs har inntekt av type PENF.
     */
    var ektefelleMottarPensjon: Boolean = false

    /**
     * Andre beregningsdata ved beregning av uførepensjon.
     */
    var uforeEkstra: UforeEkstra? = null

    /**
     * Benyttet sivilstand (tilknyttetPerson.borMedType
     */
    var benyttetSivilstand: BorMedTypeCti? = null

    /**
     * Brukerens sivilstand (som definert i TPS).
     */
    var brukersSivilstand: SivilstandTypeCti? = null

    /**
     * Angir om beregningen er gjort med mindre enn full grad for ufg,afpPensjonsgrad eller tt_anv..
     * Er satt dersom ufg < 100%, tt_anv < 40 eller afpPensjongrad < 100%.
     */
    var gradert: Boolean = false

    /**
     * Inntekten som har ligget til grunn ved inntektsavkorting - Det er denne inntekten vi i Myggen vil kjøre inntektskontroll mot.
     */
    var inntektBruktIAvkorting: Int = 0

    /**
     * Angir om ytelsene er redusert på grunn av institusjonsopphold.
     */
    var redusertPgaInstOpphold: Boolean = false

    /**
     * Angir hvilken type institusjon beregningen angår.
     */
    var instOppholdType: JustertPeriodeCti? = null

    /**
     * Angir den siste uføregraden fra uføregrunnlaget som ble lagt til grunn for beregningen.
     * Kun relevant for uførepensjon.
     */
    var ufg: Int = 0

    /**
     * Angir den siste yrkesskadegraden fra yrkesskadegrunnlaget som ble lagt til grunn for beregningen.
     * Kun relevant for ytelser som involverer yrkesskade.
     */
    var yug: Int = 0

    /**
     * Angir om opptjeningen fra det 65 året er brukt som opptjening i de 66 året fram til brukeren
     * fyller 70 år.
     */
    var brukOpptjeningFra65I66Aret: Boolean = false

    /**
     * Informasjon ang EØS beregning. Objektet sparer på data for bruk ved konvertering til AP.
     * EosEkstra finnes da i grunnlaget på GenerellHistorikk.
     */
    var eosEkstra: EosEkstra? = null

    /**
     * Innført i Horisonten
     */
    var lonnsvekstInformasjon: LonnsvekstInformasjon? = null

    /**
     * 0.0075
     */
    var pubReguleringFratrekk: Double = 0.0

    /**
     * Beregnet trygdetid som kun gjelder for grunnlagsrollen (eks. søker). Det vil si at det ikke ligger
     * noen gjenlevendedel i denne trygdetiden. Det er heller nødvendigvis ikke denne trygdetiden som er
     * benyttet i beregningen.
     */
    var ttBeregnetForGrunnlagsrolle: Int = 0

    /*
     * Flagg som angir om ung ufør garanti er tatt bort ved eksport.
     */
    var ungUforGarantiFrafalt: Boolean = false

    /**
     * Intern PREG variabel.
     * Anvendt uføretidspunkt. Hentes normalt fra uføregrunnlag men kan være hentet fra uførehistorikk ved
     * lønnsomhetsberegning.
     */
    var uft: Date? = null

    /**
     * Intern PREG variabel.
     * Anvendt yrkesskadetidspunkt. Hentes normalt fra yrkesskadegrunnlag men kan være hentet fra yrkesskadehistorikk
     * ved lønnsomhetsberegning.
     */
    var yst: Date? = null

    /**
     * Intern PREG variabel.
     * Benyttes for å avgjøre hvilken beregningsmetode som skal kjøres for forsørgertillegg.
     */
    @JsonIgnore
    var ftType: String? = null

    /**
     * Liste av merknader - forklaringer,unntak og avvisningsgrunner fra regelmotoren.
     */
    var merknadListe: MutableList<Merknad> = mutableListOf()

    /**
     * Indikerer om beregningen er gjort på grunnlag av en GammelUngUfør-garanti.
     */
    @JsonIgnore
    var gammelUngUfor: Boolean = false

    /**
     * Angir om den avdødes tt_anv ble valgt. Settes i regelsettet GJPbestemTrygdetidRS.
     */
    @JsonIgnore
    var tt_anv_avdodBrukt: Boolean = false

    /**
     * Holder på brukers originale tt_anv dersom den har blitt overskrevet.
     */
    @JsonIgnore
    var tt_anv_andre: Int = 0

    /**
     * Sparer unna uforeEkstra i denne før det blir slettet i FjernUforeEkstraGJPRS;
     */
    @JsonIgnore
    var uforeEkstraAvdod: UforeEkstra? = null

    @JsonIgnore
    var avtaleBeregningsmetode: String? = null

    /**
     * Prorata faktor anvendt for grunnpensjon.
     */
    @JsonIgnore
    var prorata_anv_gp: Double = 0.0

    val beregningListe: MutableList<BeregningRelasjon>
        get() = delberegningsListe

    // skip
    val ytelseskomponenter: MutableList<Ytelseskomponent>
        get() {
            val retval = ArrayList<Ytelseskomponent>()
            for (f in javaClass.declaredFields) {
                try {
                    val o = f.get(this)
                    if (o != null && o is Ytelseskomponent) {
                        retval.add(o)
                    }
                } catch (_: IllegalAccessException) {
                }

            }
            return retval
        }

    val ytelseskomponenterIPub: List<Ytelseskomponent>
        get() {
            val retval: MutableList<Ytelseskomponent> = mutableListOf()
            for (f in javaClass.declaredFields) {
                if (f.name.contains("Kapittel3")) {
                    continue
                }
                try {
                    val o = f.get(this)
                    if (o != null && o is Ytelseskomponent) {
                        retval.add(o)
                    }
                } catch (_: IllegalAccessException) {
                }
            }
            return retval
        }

    constructor(beregning: Beregning?, inkluderDelberegninger: Boolean) : this(beregning) {
        if (beregning == null) {
            return
        }
        if (inkluderDelberegninger) {
            kopierberegningstre(beregning, this)
        }
    }

    /**
     * Copy constructor for Beregning
     * NB! Kopierer ikke delberegninger
     */
    constructor(beregning: Beregning?) {
        if (beregning == null) {
            return
        }
        if (beregning.penPerson != null) {
            penPerson = PenPerson(beregning.penPerson!!)
        }
        if (beregning.virkFom != null) {
            virkFom = beregning.virkFom!!.clone() as Date
        }
        if (beregning.virkTom != null) {
            virkTom = beregning.virkTom!!.clone() as Date
        }
        brutto = beregning.brutto
        netto = beregning.netto
        if (beregning.gp != null) {
            gp = Grunnpensjon(beregning.gp!!)
        }
        if (beregning.gpAfpPensjonsregulert != null) {
            gpAfpPensjonsregulert = Grunnpensjon(beregning.gpAfpPensjonsregulert!!)
        }
        if (beregning.tp != null) {
            tp = Tilleggspensjon(beregning.tp!!)
        }
        if (beregning.st != null) {
            st = Sertillegg(beregning.st!!)
        }
        if (beregning.minstenivatilleggPensjonistpar != null) {
            minstenivatilleggPensjonistpar = MinstenivatilleggPensjonistpar(beregning.minstenivatilleggPensjonistpar!!)
        }
        if (beregning.afpTillegg != null) {
            afpTillegg = AfpTillegg(beregning.afpTillegg!!)
        }
        if (beregning.vt != null) {
            vt = Ventetillegg(beregning.vt!!)
        }
        if (beregning.p851_tillegg != null) {
            p851_tillegg = Paragraf_8_5_1_tillegg(beregning.p851_tillegg!!)
        }
        if (beregning.et != null) {
            et = Ektefelletillegg(beregning.et!!)
        }
        if (beregning.tfb != null) {
            tfb = BarnetilleggFellesbarn(beregning.tfb!!)
        }
        if (beregning.tsb != null) {
            tsb = BarnetilleggSerkullsbarn(beregning.tsb!!)
        }
        if (beregning.familietillegg != null) {
            familietillegg = Familietillegg(beregning.familietillegg!!)
        }
        if (beregning.tilleggFasteUtgifter != null) {
            tilleggFasteUtgifter = FasteUtgifterTillegg(beregning.tilleggFasteUtgifter!!)
        }
        g = beregning.g
        tt_anv = beregning.tt_anv
        if (beregning.beregningsMetode != null) {
            beregningsMetode = BeregningMetodeTypeCti(beregning.beregningsMetode)
        }
        if (beregning.trygdetid != null) {
            trygdetid = Trygdetid(beregning.trygdetid!!)
        }
        if (beregning.beregningType != null) {
            beregningType = BeregningTypeCti(beregning.beregningType)
        }
        if (beregning.resultatType != null) {
            resultatType = ResultatTypeCti(beregning.resultatType)
        }
        ikkeTraverser = beregning.ikkeTraverser
        ektefelleInntektOver2g = beregning.ektefelleInntektOver2g
        p67beregning = beregning.p67beregning
        if (beregning.beregningArsak != null) {
            beregningArsak = BeregningArsakCti(beregning.beregningArsak)
        }
        if (beregning.minstepensjontype != null) {
            minstepensjontype = MinstepensjonTypeCti(beregning.minstepensjontype)
        }
        minstepensjonArsak = beregning.minstepensjonArsak
        totalVinner = beregning.totalVinner
        afpPensjonsgrad = beregning.afpPensjonsgrad
        fribelop = beregning.fribelop
        friinntekt = beregning.friinntekt
        beregnetFremtidigInntekt = beregning.beregnetFremtidigInntekt
        ektefelleMottarPensjon = beregning.ektefelleMottarPensjon
        if (beregning.uforeEkstra != null) {
            uforeEkstra = UforeEkstra(beregning.uforeEkstra!!)
        }
        if (beregning.benyttetSivilstand != null) {
            benyttetSivilstand = BorMedTypeCti(beregning.benyttetSivilstand)
        }
        if (beregning.brukersSivilstand != null) {
            brukersSivilstand = SivilstandTypeCti(beregning.brukersSivilstand)
        }
        gradert = beregning.gradert
        inntektBruktIAvkorting = beregning.inntektBruktIAvkorting
        redusertPgaInstOpphold = beregning.redusertPgaInstOpphold
        if (beregning.instOppholdType != null) {
            instOppholdType = JustertPeriodeCti(beregning.instOppholdType)
        }
        redusertPgaInstOpphold = beregning.redusertPgaInstOpphold
        ufg = beregning.ufg
        yug = beregning.yug
        brukOpptjeningFra65I66Aret = beregning.brukOpptjeningFra65I66Aret
        if (beregning.eosEkstra != null) {
            eosEkstra = EosEkstra(beregning.eosEkstra!!)
        }
        for (merknad in beregning.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }
        if (beregning.lonnsvekstInformasjon != null) {
            lonnsvekstInformasjon = LonnsvekstInformasjon(beregning.lonnsvekstInformasjon!!)
        }
        if (beregning.gpKapittel3 != null) {
            gpKapittel3 = Grunnpensjon(beregning.gpKapittel3!!)
        }
        if (beregning.tpKapittel3 != null) {
            tpKapittel3 = Tilleggspensjon(beregning.tpKapittel3!!)
        }
        if (beregning.stKapittel3 != null) {
            stKapittel3 = Sertillegg(beregning.stKapittel3!!)
        }
        if (beregning.vtKapittel3 != null) {
            vtKapittel3 = Ventetillegg(beregning.vtKapittel3!!)
        }

        if (beregning.minstenivatilleggIndividuelt != null) {
            minstenivatilleggIndividuelt = MinstenivatilleggIndividuelt(beregning.minstenivatilleggIndividuelt!!)
        }

        ttBeregnetForGrunnlagsrolle = beregning.ttBeregnetForGrunnlagsrolle
        if (beregning.konverteringsdataUT != null) {
            konverteringsdataUT = KonverteringsdataUT(beregning.konverteringsdataUT!!)
        }

        if (beregning.uft != null) {
            uft = beregning.uft!!.clone() as Date
        }
        if (beregning.yst != null) {
            yst = beregning.yst!!.clone() as Date
        }
        ftType = beregning.ftType
        avtaleBeregningsmetode = beregning.avtaleBeregningsmetode
        tt_anv_andre = beregning.tt_anv_andre
        tt_anv_avdodBrukt = beregning.tt_anv_avdodBrukt
        if (beregning.uforeEkstraAvdod != null) {
            uforeEkstraAvdod = UforeEkstra(beregning.uforeEkstraAvdod!!)
        }
        beregningsnavn = beregning.beregningsnavn
        pubReguleringFratrekk = beregning.pubReguleringFratrekk
        avtaleBeregningsmetode = beregning.avtaleBeregningsmetode
        prorata_anv_gp = beregning.prorata_anv_gp

        //PEN-objekter
        if (beregning.garantitillegg_Art_27 != null) {
            garantitillegg_Art_27 = Garantitillegg_Art_27(beregning.garantitillegg_Art_27!!)
        }
        if (beregning.garantitillegg_Art_50 != null) {
            garantitillegg_Art_50 = Garantitillegg_Art_50(beregning.garantitillegg_Art_50!!)
        }
        if (beregning.hjelpeloshetsbidrag != null) {
            hjelpeloshetsbidrag = Hjelpeloshetsbidrag(beregning.hjelpeloshetsbidrag!!)
        }
        if (beregning.krigOgGammelYrkesskade != null) {
            krigOgGammelYrkesskade = KrigOgGammelYrkesskade(beregning.krigOgGammelYrkesskade!!)
        }
        if (beregning.mendel != null) {
            mendel = Mendel(beregning.mendel!!)
        }
        if (beregning.tilleggTilHjelpIHuset != null) {
            tilleggTilHjelpIHuset = TilleggTilHjelpIHuset(beregning.tilleggTilHjelpIHuset!!)
        }
        beregningsnavn = beregning.beregningsnavn
        ungUforGarantiFrafalt = beregning.ungUforGarantiFrafalt
        gammelUngUfor = beregning.gammelUngUfor
    }

    constructor()

    constructor(
        beregningsnavn: String = "Ukjentnavn",
        beregningsrelasjon: BeregningRelasjon? = null,
        penPerson: PenPerson? = null,
        virkFom: Date? = null,
        virkTom: Date? = null,
        brutto: Int = 0,
        netto: Int = 0,
        gp: Grunnpensjon? = null,
        gpKapittel3: Grunnpensjon? = null,
        gpAfpPensjonsregulert: Grunnpensjon? = null,
        tp: Tilleggspensjon? = null,
        tpKapittel3: Tilleggspensjon? = null,
        st: Sertillegg? = null,
        stKapittel3: Sertillegg? = null,
        minstenivatilleggPensjonistpar: MinstenivatilleggPensjonistpar? = null,
        minstenivatilleggIndividuelt: MinstenivatilleggIndividuelt? = null,
        afpTillegg: AfpTillegg? = null,
        vt: Ventetillegg? = null,
        vtKapittel3: Ventetillegg? = null,
        p851_tillegg: Paragraf_8_5_1_tillegg? = null,
        et: Ektefelletillegg? = null,
        tfb: BarnetilleggFellesbarn? = null,
        tsb: BarnetilleggSerkullsbarn? = null,
        familietillegg: Familietillegg? = null,
        tilleggFasteUtgifter: FasteUtgifterTillegg? = null,
        garantitillegg_Art_27: Garantitillegg_Art_27? = null,
        garantitillegg_Art_50: Garantitillegg_Art_50? = null,
        hjelpeloshetsbidrag: Hjelpeloshetsbidrag? = null,
        krigOgGammelYrkesskade: KrigOgGammelYrkesskade? = null,
        konverteringsdataUT: KonverteringsdataUT? = null,
        mendel: Mendel? = null,
        tilleggTilHjelpIHuset: TilleggTilHjelpIHuset? = null,
        g: Int = 0,
        tt_anv: Int = 0,
        beregningsMetode: BeregningMetodeTypeCti? = null,
        trygdetid: Trygdetid? = null,
        delberegningsListe: MutableList<BeregningRelasjon> = mutableListOf(),
        beregningType: BeregningTypeCti? = null,
        resultatType: ResultatTypeCti? = null,
        ikkeTraverser: Boolean = false,
        ektefelleInntektOver2g: Boolean = false,
        p67beregning: Boolean = false,
        beregningArsak: BeregningArsakCti? = null,
        minstepensjontype: MinstepensjonTypeCti? = null,
        minstepensjonArsak: String? = null,
        totalVinner: Boolean = false,
        afpPensjonsgrad: Int = 0,
        fribelop: Int = 0,
        friinntekt: Int = 0,
        beregnetFremtidigInntekt: Int = 0,
        ektefelleMottarPensjon: Boolean = false,
        uforeEkstra: UforeEkstra? = null,
        benyttetSivilstand: BorMedTypeCti? = null,
        brukersSivilstand: SivilstandTypeCti? = null,
        gradert: Boolean = false,
        inntektBruktIAvkorting: Int = 0,
        redusertPgaInstOpphold: Boolean = false,
        instOppholdType: JustertPeriodeCti? = null,
        ufg: Int = 0,
        yug: Int = 0,
        brukOpptjeningFra65I66Aret: Boolean = false,
        eosEkstra: EosEkstra? = null,
        lonnsvekstInformasjon: LonnsvekstInformasjon? = null,
        pubReguleringFratrekk: Double = 0.0,
        ttBeregnetForGrunnlagsrolle: Int = 0,
        ungUforGarantiFrafalt: Boolean = false,
        uft: Date? = null,
        yst: Date? = null,
        ftType: String? = null,
        merknadListe: MutableList<Merknad> = mutableListOf(),
        gammelUngUfor: Boolean = false,
        tt_anv_avdodBrukt: Boolean = false,
        tt_anv_andre: Int = 0,
        uforeEkstraAvdod: UforeEkstra? = null,
        avtaleBeregningsmetode: String? = null,
        prorata_anv_gp: Double = 0.0
    ) {
        this.beregningsnavn = beregningsnavn
        this.beregningsrelasjon = beregningsrelasjon
        this.penPerson = penPerson
        this.virkFom = virkFom
        this.virkTom = virkTom
        this.brutto = brutto
        this.netto = netto
        this.gp = gp
        this.gpKapittel3 = gpKapittel3
        this.gpAfpPensjonsregulert = gpAfpPensjonsregulert
        this.tp = tp
        this.tpKapittel3 = tpKapittel3
        this.st = st
        this.stKapittel3 = stKapittel3
        this.minstenivatilleggPensjonistpar = minstenivatilleggPensjonistpar
        this.minstenivatilleggIndividuelt = minstenivatilleggIndividuelt
        this.afpTillegg = afpTillegg
        this.vt = vt
        this.vtKapittel3 = vtKapittel3
        this.p851_tillegg = p851_tillegg
        this.et = et
        this.tfb = tfb
        this.tsb = tsb
        this.familietillegg = familietillegg
        this.tilleggFasteUtgifter = tilleggFasteUtgifter
        this.garantitillegg_Art_27 = garantitillegg_Art_27
        this.garantitillegg_Art_50 = garantitillegg_Art_50
        this.hjelpeloshetsbidrag = hjelpeloshetsbidrag
        this.krigOgGammelYrkesskade = krigOgGammelYrkesskade
        this.konverteringsdataUT = konverteringsdataUT
        this.mendel = mendel
        this.tilleggTilHjelpIHuset = tilleggTilHjelpIHuset
        this.g = g
        this.tt_anv = tt_anv
        this.beregningsMetode = beregningsMetode
        this.trygdetid = trygdetid
        this.beregningType = beregningType
        this.resultatType = resultatType
        this.ikkeTraverser = ikkeTraverser
        this.ektefelleInntektOver2g = ektefelleInntektOver2g
        this.p67beregning = p67beregning
        this.beregningArsak = beregningArsak
        this.minstepensjontype = minstepensjontype
        this.minstepensjonArsak = minstepensjonArsak
        this.totalVinner = totalVinner
        this.afpPensjonsgrad = afpPensjonsgrad
        this.fribelop = fribelop
        this.friinntekt = friinntekt
        this.beregnetFremtidigInntekt = beregnetFremtidigInntekt
        this.ektefelleMottarPensjon = ektefelleMottarPensjon
        this.uforeEkstra = uforeEkstra
        this.benyttetSivilstand = benyttetSivilstand
        this.brukersSivilstand = brukersSivilstand
        this.gradert = gradert
        this.inntektBruktIAvkorting = inntektBruktIAvkorting
        this.redusertPgaInstOpphold = redusertPgaInstOpphold
        this.instOppholdType = instOppholdType
        this.ufg = ufg
        this.yug = yug
        this.brukOpptjeningFra65I66Aret = brukOpptjeningFra65I66Aret
        this.eosEkstra = eosEkstra
        this.lonnsvekstInformasjon = lonnsvekstInformasjon
        this.pubReguleringFratrekk = pubReguleringFratrekk
        this.ttBeregnetForGrunnlagsrolle = ttBeregnetForGrunnlagsrolle
        this.ungUforGarantiFrafalt = ungUforGarantiFrafalt
        this.uft = uft
        this.yst = yst
        this.ftType = ftType
        this.gammelUngUfor = gammelUngUfor
        this.tt_anv_avdodBrukt = tt_anv_avdodBrukt
        this.tt_anv_andre = tt_anv_andre
        this.uforeEkstraAvdod = uforeEkstraAvdod
        this.avtaleBeregningsmetode = avtaleBeregningsmetode
        this.prorata_anv_gp = prorata_anv_gp
        this.delberegningsListe = delberegningsListe
        this.merknadListe = merknadListe
    }

    /**
     * Kopierer et beregningstre rekursivt
     *
     * @param b - originalt beregningstre
     * @param t - en beregning som potensielt skal utfylles med delberegninger
     * @return t - kopi av beregningstreet
     * Forutsetning: alle beregninger i b er unike.
     * Advarsel : hvis to beregninger peker på hverandre => stack overflow
     */
    private fun kopierberegningstre(b: Beregning, t: Beregning) {
        // Sjekker om delberegninger har delberegninger
        for (relasjon in b.delberegningsListe) {
            val kopi = Beregning(relasjon.beregning)
            t.delberegningsListe.add(BeregningRelasjon(kopi, relasjon.bruk))
            if (relasjon.beregning?.delberegningsListe?.size!! > 0) {
                kopierberegningstre(relasjon.beregning!!, kopi)
            }
        }
    }

    fun putBeregningListe(beregningListe: MutableList<BeregningRelasjon>) {
        delberegningsListe.clear()
        for (br in beregningListe) {
            delberegningsListe.add(br)
        }
    }

    fun erLovNode(): Boolean {
        return false
    }

    fun erToppnode(): Boolean {
        return false
    }

    /**
     * Legger til beregningRelasjon til lista. BeregningsId blir satt på den respektive beregningen.
     *
     * @param beregningRelasjon
     * @param beregningsnavn
     */
    fun addBeregningRelasjon(beregningRelasjon: BeregningRelasjon?, beregningsnavn: String?) {
        if (beregningsnavn != null && beregningRelasjon != null) {
            val eksisterende = getBeregningRelasjon(beregningsnavn)
            if (eksisterende != null) {
                // Fjern eksisterende
                delberegningsListe.remove(eksisterende)
            }
            if (beregningRelasjon.beregning != null) {
                beregningRelasjon.beregning!!.beregningsnavn = beregningsnavn
            }
            if (beregningRelasjon.beregning2011 != null) {
                beregningRelasjon.beregning2011!!.beregningsnavn = beregningsnavn
            }
            beregningRelasjon.parentBeregning = this
            delberegningsListe.add(beregningRelasjon)
        }
    }

    fun addBeregning(beregning2011: Beregning2011, beregningsnavn: String) {
        beregning2011.beregningsnavn = beregningsnavn
        val br = BeregningRelasjon()
        br.beregning2011 = beregning2011
        addBeregningRelasjon(br, beregningsnavn)
    }

    fun addBeregning(beregning: Beregning, beregningsnavn: String) {
        beregning.beregningsnavn = beregningsnavn
        val br = BeregningRelasjon()
        br.beregning = beregning
        addBeregningRelasjon(br, beregningsnavn)
    }

    /**
     * Hent BeregningsRelasjon som tilhører en bestemt beregning
     *
     * @param beregningId
     */
    fun getBeregningRelasjon(beregningId: String): BeregningRelasjon? {
        for (br in delberegningsListe) {
            if (br.beregning != null && beregningId == br.beregning!!.beregningsnavn) {
                return br
            }
            if (br.beregning2011 != null && beregningId == br.beregning2011!!.beregningsnavn) {
                return br
            }
        }
        return null
    }
}
