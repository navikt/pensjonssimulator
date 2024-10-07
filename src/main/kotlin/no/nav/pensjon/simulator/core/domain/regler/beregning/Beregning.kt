package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.*
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.MinstenivatilleggIndividuelt
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.MinstenivatilleggPensjonistpar
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.EosEkstra
import java.io.Serializable
import java.util.*

/**
 * Beregning er resultatet fra en beregning i pensjon-regler.
 */
class Beregning : Serializable {

    /**
     * Representerer personen beregning gjelder for.
     */
    var penPerson: PenPerson? = null

    /**
     * Virkningstidspunktet ytelsen kan utbetales fra.
     */
    var virkFom: Date? = null

    /**
     * Virkningstidspunktet ytelsen kan utbetales til.
     */
    var virkTom: Date? = null

    /**
     * beløp før avkorting.
     */
    var brutto = 0

    /**
     * beløp etter avkorting.
     */
    var netto = 0

    /**
     * Grunnpensjonen, regulert med lønnsvekst - 0.0075. Fra og med Horisonten.
     */
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

    /**
     * Tilleggspensjonen.
     */
    var tp: Tilleggspensjon? = null
    var tpKapittel3: Tilleggspensjon? = null

    /**
     * særtillegget.
     */
    var st: Sertillegg? = null
    var stKapittel3: Sertillegg? = null

    /**
     * SamletektefelleGarantiTillegg
     */
    var minstenivatilleggPensjonistpar: MinstenivatilleggPensjonistpar? = null

    /**
     * MinstenivatilleggIndividuelt
     */
    var minstenivatilleggIndividuelt: MinstenivatilleggIndividuelt? = null

    /**
     * AFP tillegget.
     */
    var afpTillegg: AfpTillegg? = null

    /**
     * Ventetillegget
     */
    var vt: Ventetillegg? = null
    var vtKapittel3: Ventetillegg? = null

    /**
     * §8.5.1 tillegget.
     */
    var p851_tillegg: Paragraf_8_5_1_tillegg? = null

    /**
     * Ektefelletillegget.
     */
    var et: Ektefelletillegg? = null

    /**
     * Barnetillegget fellesbarn.
     */
    var tfb: BarnetilleggFellesbarn? = null

    /**
     * Barnetillegget særkullsbarn.
     */
    var tsb: BarnetilleggSerkullsbarn? = null

    /**
     * Familietillegg - aktuelt ved institusjonsopphold.
     */
    var familietillegg: Familietillegg? = null

    /**
     * Tillegg for faste utgifter - aktuelt ved institusjonsopphold.
     */
    var tilleggFasteUtgifter: FasteUtgifterTillegg? = null

    /**
     * Brukes bare ved g-omregningstjenesten BEF270.
     */
    var garantitillegg_Art_27: Garantitillegg_Art_27? = null

    /**
     * Brukes bare ved g-omregningstjenesten BEF270.
     */
    var garantitillegg_Art_50: Garantitillegg_Art_50? = null

    /**
     * Brukes bare ved g-omregningstjenesten BEF270.
     */
    var hjelpeloshetsbidrag: Hjelpeloshetsbidrag? = null

    /**
     * Brukes bare ved g-omregningstjenesten BEF270.
     */
    var krigOgGammelYrkesskade: KrigOgGammelYrkesskade? = null

    /**
     * Ved konvertering av uførepensjon til uføretrygd, og gradert yrkesskade
     * brukes denne for å angi andel tilleggspensjon som stammer fra restgrad
     * og som stammer fra yrkesskadegrad.
     */
    var konverteringsdataUT: KonverteringsdataUT? = null

    /**
     * Brukes bare ved g-omregningstjenesten BEF270.
     */
    var mendel: Mendel? = null

    /**
     * Brukes bare ved g-omregningstjenesten BEF270.
     */
    var tilleggTilHjelpIHuset: TilleggTilHjelpIHuset? = null

    /**
     * Grunnbeløpet.
     */
    var g = 0

    /**
     * Anvendt tt i beregningen, i hele år. Blir satt lik antall poengår dersom antall
     * poengår (fra poengrekke-beregningen) er større enn Trygdetid.tt.
     */
    var tt_anv = 0

    /**
     * Angir hvilken metode som ble benyttet ved beregningen.EØS/nordisk/et annet land
     */
    var beregningsMetodeEnum: BeregningsmetodeEnum? = null

    /**
     * Framtidig trygdetid.
     */
    var trygdetid: Trygdetid? = null

    /**
     * Liste av beregninger. Kan inneholde alternative beregninger gjort ved lønnsomhetsberegninger.Vinneren ligger da i hovedobjektet mens taperen(e) ligger i denne listen.
     */
    var delberegningsListe: List<BeregningRelasjon> = mutableListOf()

    /**
     * Angir type beregning: BER, HJELPEBER, SAM_BER, VUR_BER.
     */
    var beregningTypeEnum: BeregningtypeEnum? = null

    /**
     * Hvilken resultattype en beregning er : AP,AP_GJP osv.
     */
    var resultatTypeEnum: ResultattypeEnum? = null

    /**
     * Flegg som brukes på beregninger med beregningType HJELPEBER.
     * Anvendes i lønnsomhetsberegninger (VUR_BER). Settes på den av delberegningene som ikke er HJELPEBER
     * dersom TP'en fra HJELPEBER er brukt. Da er også flagget bruk i beregningsrelasjonen som hjelpeberegningen
     * inngår i satt.
     */
    var ikkeTraverser = false

    /**
     * Er ektefelles totalinntekt, inkludert alle relevante inntekter > 2G.
     */
    var ektefelleInntektOver2g = false

    /**
     * Hvorvidt det er en P67 beregning.Gjelder alderspensjon .
     */
    var p67beregning = false

    /**
     * årsak til periodisering av beregningen, G-justering, regelendring osv.
     * Settes dersom beregningen blir periodiserer av pensjon-regler (i.e. det returneres en liste av beregninger ).
     * Skal brukes i saksbehandling for å vise årsaken til periodiseringen av beregningen.
     * Hvis det er flere årsaker til at det er en periode i beregningen på samme periode (i.e. endring i sats og regel på samme dag så må det være en prioritering av endringene.
     * Da skal den årsak som mest sjelden skjer settes.
     */
    var beregningArsakEnum: Beregningsarsak? = null

    /**
     * Angir type av minstepensjon.
     * ER_MINST_PEN eller IKKE_MINST_PEN
     */
    var minstepensjontypeEnum: MinstepensjonstypeEnum? = null

    /**
     * Samler opp årsakene til at en brukers minstepensjonstatus er satt og tilhørende verdier
     * dersom disse er relevante til beslutningen.
     */
    var minstepensjonArsak: String? = null

    /**
     * Angir om denne beregningen er top-noden av beregningene.
     */
    var totalVinner = false

    /**
     * Pensjonsgraden ved AFP,heltall mellom 0-100.
     */
    var afpPensjonsgrad = 0

    /**
     * Fribeløpet en pensjonist kan ha før vilkårene for å få ytelsen blir avkortet (AP = 2G, GJP = 0,5G etc).
     */
    var fribelop = 0

    /**
     * Friinntekt = (IEU) Inntekt den Uføre (UP) kan ha etter uføretidspunktet.
     * Beregnet etter inntekt før Uføre (IFU).
     * IEU er grensen for hva inntekten kan være før en må revurdere Uføregraden
     */
    var friinntekt = 0

    /**
     * Beregnet fremtidig inntekt.Det er en G-omregnet oppgitt fremtidig inntekt som skal brukes av inntektskontrollen i Myggen.
     * beregnetFremtidigInntekt er G-omregnet oppgitt fremtidig inntekt som skal brukes av inntektskontrollen i Myggen.
     * pensjon-regler bruker Inntekt av type (FPI eller HYPF eller HYPF2G) og opptjusterer denne ihht til G:
     * beregnetFremtidigInntekt = (FPI eller HYPF eller HYPF2G) * nyG/gammelG
     * Dette attributtet er ytelsesuavhengig.
     * 30-jan-2008: Denne må antagelig suppleres med en tilsvarende beregnetFremtidigInntekt på Ektefelletillegget.
     * Verdien skal oppfattes som en grenseverdi,dvs den verdien som ble lagt til grunn ved eventuell avkorting.
     * For UP vil denne verdien settes lik uforeEkstra.tak.
     */
    var beregnetFremtidigInntekt = 0

    /**
     * Angir om ektefellen mottar ytelser fra Folketrygden, dvs har inntekt av type PENF.
     */
    var ektefelleMottarPensjon = false

    /**
     * Andre beregningsdata ved beregning av uførepensjon.
     */
    var uforeEkstra: UforeEkstra? = null

    /**
     * Benyttet sivilstand (tilknyttetPerson.borMedType
     */
    var benyttetSivilstandEnum: BorMedTypeEnum? = null

    /**
     * Brukerens sivilstand (som definert i TPS).
     */
    var brukersSivilstandEnum: SivilstandEnum? = null

    /**
     * Angir om beregningen er gjort med mindre enn full grad for ufg,afpPensjonsgrad eller tt_anv..
     * Er satt dersom ufg < 100%, tt_anv < 40 eller afpPensjongrad < 100%.
     */
    var gradert = false

    /**
     * Inntekten som har ligget til grunn ved inntektsavkorting - Det er denne inntekten vi i Myggen vil kjøre inntektskontroll mot.
     */
    var inntektBruktIAvkorting = 0

    /**
     * Angir om ytelsene er redusert på grunn av institusjonsopphold.
     */
    var redusertPgaInstOpphold = false

    /**
     * Angir hvilken type institusjon beregningen angår.
     */
    var instOppholdTypeEnum: JustertPeriodeEnum? = null

    /**
     * Angir den siste Uføregraden fra Uføregrunnlaget som ble lagt til grunn for beregningen.
     * Kun relevant for uførepensjon.
     */
    var ufg = 0

    /**
     * Angir den siste yrkesskadegraden fra yrkesskadegrunnlaget som ble lagt til grunn for beregningen.
     * Kun relevant for ytelser som involverer yrkesskade.
     */
    var yug = 0

    /**
     * Angir om opptjeningen fra det 65 året er brukt som opptjening i de 66 året fram til brukeren
     * fyller 70 år.
     */
    var brukOpptjeningFra65I66Aret = false

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
    var pubReguleringFratrekk = 0.0

    /**
     * Beregnet trygdetid som kun gjelder for grunnlagsrollen (eks. søker). Det vil si at det ikke ligger
     * noen gjenlevendedel i denne trygdetiden. Det er heller nødvendigvis ikke denne trygdetiden som er
     * benyttet i beregningen.
     */
    var ttBeregnetForGrunnlagsrolle = 0

    /**
     * Flagg som angir om ung ufør garanti er tatt bort ved eksport.
     */
    var ungUforGarantiFrafalt = false

    /**
     * Intern pensjon-regler variabel.
     * Anvendt uføretidspunkt. Hentes normalt fra Uføregrunnlag men kan være hentet fra Uførehistorikk ved
     * lønnsomhetsberegning.
     */
    var uft: Date? = null

    /**
     * Intern pensjon-regler variabel.
     * Anvendt yrkesskadetidspunkt. Hentes normalt fra yrkesskadegrunnlag men kan være hentet fra yrkesskadehistorikk
     * ved lønnsomhetsberegning.
     */
    var yst: Date? = null

    /**
     * Liste av merknader - forklaringer,unntak og avvisningsgrunner fra regelmotoren.
     */
    var merknadListe: MutableList<Merknad> = mutableListOf()

    /**
     * Copy constructor for Beregning
     * NB! Kopierer ikke delberegninger
     */
    constructor(source: Beregning?) {
        if (source == null) {
            return
        }

        if (source.penPerson != null) {
            penPerson = PenPerson(source.penPerson!!)
        }

        if (source.virkFom != null) {
            virkFom = source.virkFom!!.clone() as Date
        }

        if (source.virkTom != null) {
            virkTom = source.virkTom!!.clone() as Date
        }

        brutto = source.brutto
        netto = source.netto

        if (source.gp != null) {
            gp = Grunnpensjon(source.gp!!)
        }

        if (source.gpAfpPensjonsregulert != null) {
            gpAfpPensjonsregulert = Grunnpensjon(source.gpAfpPensjonsregulert!!)
        }

        if (source.tp != null) {
            tp = Tilleggspensjon(source.tp!!)
        }

        if (source.st != null) {
            st = Sertillegg(source.st!!)
        }
        /* TODO include if copying is needed in SimuleringResultPreparer.modifiedCopyOfBeregningsresultat
        if (source.minstenivatilleggPensjonistpar != null) {
            minstenivatilleggPensjonistpar = MinstenivatilleggPensjonistpar(source.minstenivatilleggPensjonistpar!!)
        }

        if (source.afpTillegg != null) {
            afpTillegg = AfpTillegg(source.afpTillegg!!)
        }

        if (source.vt != null) {
            vt = Ventetillegg(source.vt!!)
        }

        if (source.p851_tillegg != null) {
            p851_tillegg = Paragraf_8_5_1_tillegg(source.p851_tillegg!!)
        }

        if (source.et != null) {
            et = Ektefelletillegg(source.et!!)
        }

        if (source.tfb != null) {
            tfb = BarnetilleggFellesbarn(source.tfb!!)
        }

        if (source.tsb != null) {
            tsb = BarnetilleggSerkullsbarn(source.tsb!!)
        }

        if (source.familietillegg != null) {
            familietillegg = Familietillegg(source.familietillegg!!)
        }

        if (source.tilleggFasteUtgifter != null) {
            tilleggFasteUtgifter = FasteUtgifterTillegg(source.tilleggFasteUtgifter!!)
        }

        g = source.g
        tt_anv = source.tt_anv

        if (source.beregningsMetodeEnum != null) {
            beregningsMetodeEnum = source.beregningsMetodeEnum
        }

        if (source.trygdetid != null) {
            trygdetid = Trygdetid(source.trygdetid!!)
        }

        if (source.beregningTypeEnum != null) {
            beregningTypeEnum = source.beregningTypeEnum
        }

        if (source.resultatTypeEnum != null) {
            resultatTypeEnum = source.resultatTypeEnum
        }

        ikkeTraverser = source.ikkeTraverser
        ektefelleInntektOver2g = source.ektefelleInntektOver2g
        p67beregning = source.p67beregning

        if (source.beregningArsakEnum != null) {
            beregningArsakEnum = source.beregningArsakEnum
        }

        if (source.minstepensjontypeEnum != null) {
            minstepensjontypeEnum = source.minstepensjontypeEnum
        }

        minstepensjonArsak = source.minstepensjonArsak
        totalVinner = source.totalVinner
        afpPensjonsgrad = source.afpPensjonsgrad
        fribelop = source.fribelop
        friinntekt = source.friinntekt
        beregnetFremtidigInntekt = source.beregnetFremtidigInntekt
        ektefelleMottarPensjon = source.ektefelleMottarPensjon

        if (source.uforeEkstra != null) {
            uforeEkstra = UforeEkstra(source.uforeEkstra!!)
        }

        if (source.benyttetSivilstandEnum != null) {
            benyttetSivilstandEnum = source.benyttetSivilstandEnum
        }

        if (source.brukersSivilstandEnum != null) {
            brukersSivilstandEnum = source.brukersSivilstandEnum
        }

        gradert = source.gradert
        inntektBruktIAvkorting = source.inntektBruktIAvkorting
        redusertPgaInstOpphold = source.redusertPgaInstOpphold

        if (source.instOppholdTypeEnum != null) {
            instOppholdTypeEnum = source.instOppholdTypeEnum
        }

        redusertPgaInstOpphold = source.redusertPgaInstOpphold
        ufg = source.ufg
        yug = source.yug
        brukOpptjeningFra65I66Aret = source.brukOpptjeningFra65I66Aret

        if (source.eosEkstra != null) {
            eosEkstra = EosEkstra(source.eosEkstra!!)
        }

        for (merknad in source.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }

        if (source.lonnsvekstInformasjon != null) {
            lonnsvekstInformasjon = LonnsvekstInformasjon(source.lonnsvekstInformasjon!!)
        }

        if (source.gpKapittel3 != null) {
            gpKapittel3 = Grunnpensjon(source.gpKapittel3!!)
        }

        if (source.tpKapittel3 != null) {
            tpKapittel3 = Tilleggspensjon(source.tpKapittel3!!)
        }

        if (source.stKapittel3 != null) {
            stKapittel3 = Sertillegg(source.stKapittel3!!)
        }

        if (source.vtKapittel3 != null) {
            vtKapittel3 = Ventetillegg(source.vtKapittel3!!)
        }

        if (source.minstenivatilleggIndividuelt != null) {
            minstenivatilleggIndividuelt = MinstenivatilleggIndividuelt(source.minstenivatilleggIndividuelt!!)
        }

        ttBeregnetForGrunnlagsrolle = source.ttBeregnetForGrunnlagsrolle
        if (source.konverteringsdataUT != null) {
            konverteringsdataUT = KonverteringsdataUT(source.konverteringsdataUT!!)
        }

        if (source.uft != null) {
            uft = source.uft!!.clone() as Date
        }

        if (source.yst != null) {
            yst = source.yst!!.clone() as Date
        }
        //ftType = beregning.ftType
        //avtaleBeregningsmetode = beregning.avtaleBeregningsmetode
        //tt_anv_andre = beregning.tt_anv_andre
        //tt_anv_avdodBrukt = beregning.tt_anv_avdodBrukt
        //if (beregning.uforeEkstraAvdod != null) {
        //    uforeEkstraAvdod = UforeEkstra(beregning.uforeEkstraAvdod!!)
        //}
        //beregningsnavn = beregning.beregningsnavn
        pubReguleringFratrekk = source.pubReguleringFratrekk
        //prorata_anv_gp = beregning.prorata_anv_gp

        //PEN-objekter
        if (source.garantitillegg_Art_27 != null) {
            garantitillegg_Art_27 = Garantitillegg_Art_27(source.garantitillegg_Art_27!!)
        }
        if (source.garantitillegg_Art_50 != null) {
            garantitillegg_Art_50 = Garantitillegg_Art_50(source.garantitillegg_Art_50!!)
        }

        if (source.hjelpeloshetsbidrag != null) {
            hjelpeloshetsbidrag = Hjelpeloshetsbidrag(source.hjelpeloshetsbidrag!!)
        }

        if (source.krigOgGammelYrkesskade != null) {
            krigOgGammelYrkesskade = KrigOgGammelYrkesskade(source.krigOgGammelYrkesskade!!)
        }

        if (source.mendel != null) {
            mendel = Mendel(source.mendel!!)
        }

        if (source.tilleggTilHjelpIHuset != null) {
            tilleggTilHjelpIHuset = TilleggTilHjelpIHuset(source.tilleggTilHjelpIHuset!!)
        }
        */
        ungUforGarantiFrafalt = source.ungUforGarantiFrafalt
        //gammelUngUfor = beregning.gammelUngUfor
    }

    constructor()
}
