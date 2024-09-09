package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.vedtak.FortsattMedlemskap
import no.nav.pensjon.simulator.core.domain.regler.vedtak.ForutgaendeMedlemskap
import no.nav.pensjon.simulator.core.domain.regler.vedtak.MedlemskapForUTEtterTrygdeavtaler
import no.nav.pensjon.simulator.core.domain.regler.vedtak.RettTilEksportEtterTrygdeavtaler
import java.io.Serializable

class InngangOgEksportGrunnlag(
    /**
     * Minst tre års trygdetid i Norge
     */
    var treArTrygdetidNorge: Boolean? = null,
    /**
     * Unntak fra forutgående trygdetid
     */
    var unntakFraForutgaendeTT: Unntak? = null,
    /**
     * Minst fem års trygdetid i Norge
     */
    var femArTrygdetidNorge: Boolean? = null,
    /**
     * Fortsatt medlem i folketrygden
     */
    var fortsattMedlemFT: Boolean? = null,
    /**
     * Brukeren har minst 20 års botid i Norge
     */
    var minstTyveArBotidNorge: Boolean? = null,
    /**
     * Opptjent rett til tilleggspensjon etter folketrygdloven
     */
    var opptjentRettTilTPEtterFT: Boolean? = null,
    /**
     * Eksportforbud
     */
    var eksportforbud: Eksportforbud? = null,
    /**
     * Fri eksport fordi uførhet skyldes yrkesskade
     */
    var friEksportPgaYrkesskade: Boolean? = null,
    /**
     * Eksportrett etter EØS forordning eller trygdeavtaler med EØS-land i kraft etter 01.01.1994
     */
    var eksportrettEtterEOSForordning: Eksportrett? = null,
    /**
     * Eksportrett etter trygdeavtaler med EØS-land i kraft før 01.01.1994
     */
    var eksportrettEtterTrygdeavtalerEOS: Eksportrett? = null,
    /**
     * Eksportrett etter andre trygdeavtaler
     */
    var eksportrettEtterAndreTrygdeavtaler: Eksportrett? = null,
    /**
     * IKKE I BRUK (se PENPORT-279)
     * Innvilget garantert tilleggspensjon til ung ufør/død skal eksporteres
     */
    //var innvilgetGarantertTP: Boolean = false,
    var innvilgetGarantertTP: Boolean? = null,
    /**
     * Eksportrett for garantert tilleggspensjon ung ufør.
     */
    var eksportrettGarantertTP: Unntak? = null,
    /**
     * Minst tre års forutgående medlemskap i Norge med uføretidspunkt FOM 01.01.1994
     */
    var minstTreArsFMNorge: Boolean? = null,
    /**
     * Minst fem års forutgående medlemskap i Norge med uføretidspunkt FOM 01.01.1994 og førsteKravFremsattdato FOM 01.01.2021
     */
    var minstFemArsFMNorge: Boolean? = null,
    /**
     * Minst tre års forutgående medlemskap i Norge med uføretidspunkt før 01.01.1994 og virkningstidspunkt FOM 01.01.1990
     */
    var minstTreArsFMNorgeVirkdato: Boolean? = null,
    /**
     * Unntak fra forutgående medlemskap
     */
    var unntakFraForutgaendeMedlemskap: Unntak? = null,
    /**
     * Oppfylt etter gamle no.nav.preg.domain.regler.regler og virkningsdato før 01.01.1990
     */
    //var oppfyltEtterGamleRegler: Boolean = false,
    var oppfyltEtterGamleRegler: Boolean? = null,
    /**
     * Oppfylt ved sammenlegging tre år
     */
    var oppfyltVedSammenlegging: OppfyltVedSammenlegging? = null,
    /**
     * Oppfylt ved sammenlegging fem år
     */
    var oppfyltVedSammenleggingFemAr: OppfyltVedSammenlegging? = null,
    /**
     * Oppfylt ved gjenlevendes forutgående medlemskap
     */
    //var oppfyltVedGjenlevendesMedlemskap: Boolean = false,
    var oppfyltVedGjenlevendesMedlemskap: Boolean? = null,
    /**
     * Gjenlevende fortsatt medlem av folketrygden
     */
    //var gjenlevendeMedlemFT: Boolean = false,
    var gjenlevendeMedlemFT: Boolean? = null,
    /**
     * Avdøde har minst ett års forutgående medlemskap i Norge med dødsdato før 01.01.1994 og virkningsdato FOM 01.01.1990
     */
    //var minstEttArFMNorge: Boolean = false,
    var minstEttArFMNorge: Boolean? = null,
    /**
     * En av foreldrene har minst 20 års botid i Norge.
     */
    //var foreldreMinstTyveArBotidNorge: Boolean = false,
    var foreldreMinstTyveArBotidNorge: Boolean? = null,
    /**
     * Fri eksport fordi dødsfall skyldes yrkesskade
     */
    //var friEksportDodsfall: Boolean = false,
    var friEksportDodsfall: Boolean? = null,
    /**
     * Brukeren har minst 20 års botid i Norge
     */
    //var minstTyveArTrygdetidNorgeKap20: Boolean = false,
    var minstTyveArTrygdetidNorgeKap20: Boolean? = null,
    /**
     * Minst tre års trygdetid i Norge
     */
    //var treArTrygdetidNorgeKap20: Boolean = false,
    var treArTrygdetidNorgeKap20: Boolean? = null,
    /**
     * Minst fem års trygdetid i Norge
     */
    var femArTrygdetidNorgeKap20: Boolean? = null,
    /**
     * Oppfylt ved sammenlegging fem år
     */
    var oppfyltVedSammenleggingFemArKap20: OppfyltVedSammenlegging? = null,
    /**
     * Oppfylt ved sammenlegging
     */
    var oppfyltVedSammenleggingKap20: OppfyltVedSammenlegging? = null,
    /**
     * PREG variabel for å vite om det finnes trygdeavtale når man er i BestemUngUførRS i Folketrygd flyter.
     * Trygdeavtale objetet nulles før kall til Folketrygd-flyter
     */
    @JsonIgnore var trygdeavtale: Trygdeavtale? = null
) : Serializable {

    /**
     * Copy constructor
     */
    constructor(inngangOgEksportGrunnlag: InngangOgEksportGrunnlag) : this() {
        if (inngangOgEksportGrunnlag.treArTrygdetidNorge != null) {
            this.treArTrygdetidNorge = inngangOgEksportGrunnlag.treArTrygdetidNorge
        }
        if (inngangOgEksportGrunnlag.unntakFraForutgaendeTT != null) {
            this.unntakFraForutgaendeTT = Unntak(inngangOgEksportGrunnlag.unntakFraForutgaendeTT!!)
        } else {
            //this.unntakFraForutgaendeTT = Unntak(false, null)
            this.unntakFraForutgaendeTT = null // SIMDOM
        }
        if (inngangOgEksportGrunnlag.fortsattMedlemFT != null) {
            this.fortsattMedlemFT = inngangOgEksportGrunnlag.fortsattMedlemFT
        }
        if (inngangOgEksportGrunnlag.minstTyveArBotidNorge != null) {
            this.minstTyveArBotidNorge = inngangOgEksportGrunnlag.minstTyveArBotidNorge
        }
        if (inngangOgEksportGrunnlag.opptjentRettTilTPEtterFT != null) {
            this.opptjentRettTilTPEtterFT = inngangOgEksportGrunnlag.opptjentRettTilTPEtterFT
        }
        if (inngangOgEksportGrunnlag.eksportforbud != null) {
            this.eksportforbud = Eksportforbud(inngangOgEksportGrunnlag.eksportforbud!!)
        }
        if (inngangOgEksportGrunnlag.friEksportPgaYrkesskade != null) {
            this.friEksportPgaYrkesskade = inngangOgEksportGrunnlag.friEksportPgaYrkesskade
        }
        if (inngangOgEksportGrunnlag.eksportrettEtterEOSForordning != null) {
            this.eksportrettEtterEOSForordning = Eksportrett(inngangOgEksportGrunnlag.eksportrettEtterEOSForordning!!)
        }
        if (inngangOgEksportGrunnlag.eksportrettEtterTrygdeavtalerEOS != null) {
            this.eksportrettEtterTrygdeavtalerEOS =
                Eksportrett(inngangOgEksportGrunnlag.eksportrettEtterTrygdeavtalerEOS!!)
        }
        if (inngangOgEksportGrunnlag.eksportrettEtterAndreTrygdeavtaler != null) {
            this.eksportrettEtterAndreTrygdeavtaler =
                Eksportrett(inngangOgEksportGrunnlag.eksportrettEtterAndreTrygdeavtaler!!)
        }
        if (inngangOgEksportGrunnlag.eksportrettGarantertTP != null) {
            this.eksportrettGarantertTP = Unntak(inngangOgEksportGrunnlag.eksportrettGarantertTP!!)
        }
        if (inngangOgEksportGrunnlag.minstTreArsFMNorge != null) {
            this.minstTreArsFMNorge = inngangOgEksportGrunnlag.minstTreArsFMNorge
        }
        if (inngangOgEksportGrunnlag.minstTreArsFMNorgeVirkdato != null) {
            this.minstTreArsFMNorgeVirkdato = inngangOgEksportGrunnlag.minstTreArsFMNorgeVirkdato
        }
        if (inngangOgEksportGrunnlag.unntakFraForutgaendeMedlemskap != null) {
            this.unntakFraForutgaendeMedlemskap = Unntak(inngangOgEksportGrunnlag.unntakFraForutgaendeMedlemskap!!)
        } else {
            //this.unntakFraForutgaendeMedlemskap = Unntak(false, null)
            this.unntakFraForutgaendeMedlemskap = null // SIMDOM
        }
        this.oppfyltEtterGamleRegler = inngangOgEksportGrunnlag.oppfyltEtterGamleRegler
        if (inngangOgEksportGrunnlag.oppfyltVedSammenlegging != null) {
            this.oppfyltVedSammenlegging = OppfyltVedSammenlegging(inngangOgEksportGrunnlag.oppfyltVedSammenlegging!!)
        }
        this.oppfyltVedGjenlevendesMedlemskap = inngangOgEksportGrunnlag.oppfyltVedGjenlevendesMedlemskap
        this.gjenlevendeMedlemFT = inngangOgEksportGrunnlag.gjenlevendeMedlemFT
        this.minstEttArFMNorge = inngangOgEksportGrunnlag.minstEttArFMNorge
        this.foreldreMinstTyveArBotidNorge = inngangOgEksportGrunnlag.foreldreMinstTyveArBotidNorge
        this.friEksportDodsfall = inngangOgEksportGrunnlag.friEksportDodsfall
        this.treArTrygdetidNorgeKap20 = inngangOgEksportGrunnlag.treArTrygdetidNorgeKap20
        this.minstTyveArTrygdetidNorgeKap20 = inngangOgEksportGrunnlag.minstTyveArTrygdetidNorgeKap20
        if (inngangOgEksportGrunnlag.oppfyltVedSammenleggingKap20 != null) {
            this.oppfyltVedSammenleggingKap20 =
                OppfyltVedSammenlegging(inngangOgEksportGrunnlag.oppfyltVedSammenleggingKap20!!)
        }
        if (inngangOgEksportGrunnlag.trygdeavtale != null) {
            this.trygdeavtale = Trygdeavtale(inngangOgEksportGrunnlag.trygdeavtale!!)
        }
        if (inngangOgEksportGrunnlag.femArTrygdetidNorge != null) {
            femArTrygdetidNorge = inngangOgEksportGrunnlag.femArTrygdetidNorge
        }
        if (inngangOgEksportGrunnlag.femArTrygdetidNorgeKap20 != null) {
            femArTrygdetidNorgeKap20 = inngangOgEksportGrunnlag.femArTrygdetidNorgeKap20
        }
        if (inngangOgEksportGrunnlag.oppfyltVedSammenleggingFemAr != null) {
            oppfyltVedSammenleggingFemAr =
                OppfyltVedSammenlegging(inngangOgEksportGrunnlag.oppfyltVedSammenleggingFemAr!!)
        }
        if (inngangOgEksportGrunnlag.oppfyltVedSammenleggingFemArKap20 != null) {
            oppfyltVedSammenleggingFemArKap20 =
                OppfyltVedSammenlegging(inngangOgEksportGrunnlag.oppfyltVedSammenleggingFemArKap20!!)
        }
        if (inngangOgEksportGrunnlag.minstFemArsFMNorge != null) {
            minstFemArsFMNorge = inngangOgEksportGrunnlag.minstFemArsFMNorge
        }
    }

    /**
     * For uføretrygd er innholdet i InngangOgEksportGrunnlag fordelt på to klasser, FortsattMedlemkap og ForutgaendeMedlemskap.
     * Denne constructor kan brukes for bakoverkompatibilitet med gammel regelkode som forventer et InngangOgEksportGrunnlag.
     */
    constructor(fortsattMedlemskap: FortsattMedlemskap?, forutgaendeMedlemskap: ForutgaendeMedlemskap?) : this() {
        if (fortsattMedlemskap != null) {
            if (fortsattMedlemskap.fortsattMedlemFT != null) {
                this.fortsattMedlemFT = fortsattMedlemskap.fortsattMedlemFT
            }
            this.minstTyveArBotidNorge = fortsattMedlemskap.minstTyveArBotidNorge
            this.opptjentRettTilTPEtterFT = fortsattMedlemskap.opptjentRettTilTPEtterFT
            if (fortsattMedlemskap.eksportforbud != null) {
                this.eksportforbud = Eksportforbud(fortsattMedlemskap.eksportforbud!!)
            }
            this.friEksportPgaYrkesskade = fortsattMedlemskap.friEksportPgaYrkesskade
            if (fortsattMedlemskap.eksportrettGarantertTP != null) {
                this.eksportrettGarantertTP = Unntak(fortsattMedlemskap.eksportrettGarantertTP!!)
            }
        }

        if (forutgaendeMedlemskap != null) {
            if (forutgaendeMedlemskap.minstTreArsFMNorge != null) {
                this.minstTreArsFMNorge = forutgaendeMedlemskap.minstTreArsFMNorge
                this.minstTreArsFMNorgeVirkdato = forutgaendeMedlemskap.minstTreArsFMNorge
                this.treArTrygdetidNorge = forutgaendeMedlemskap.minstTreArsFMNorge
            } else {
                this.minstTreArsFMNorge = false
                this.minstTreArsFMNorgeVirkdato = null
                this.treArTrygdetidNorge = false
            }
            if (forutgaendeMedlemskap.minstFemArsFMNorge != null) {
                minstFemArsFMNorge = forutgaendeMedlemskap.minstFemArsFMNorge
                femArTrygdetidNorge = forutgaendeMedlemskap.minstFemArsFMNorge
            } else {
                minstFemArsFMNorge = false
                femArTrygdetidNorge = false
            }
            if (forutgaendeMedlemskap.minstEttArFMNorge != null) {
                this.minstEttArFMNorge = forutgaendeMedlemskap.minstEttArFMNorge
            } else {
                this.minstEttArFMNorge = false
            }
            if (forutgaendeMedlemskap.unntakFraForutgaendeMedlemskap != null) {
                this.unntakFraForutgaendeMedlemskap = Unntak(forutgaendeMedlemskap.unntakFraForutgaendeMedlemskap!!)
            } else {
                //this.unntakFraForutgaendeMedlemskap = Unntak(false, null)
                this.unntakFraForutgaendeMedlemskap = null // SIMDOM
            }
            if (forutgaendeMedlemskap.unntakFraForutgaendeTT != null) {
                this.unntakFraForutgaendeTT = Unntak(forutgaendeMedlemskap.unntakFraForutgaendeTT!!)
            } else {
                //this.unntakFraForutgaendeTT = Unntak(false, null)
                this.unntakFraForutgaendeTT = null // SIMDOM
            }
            if (forutgaendeMedlemskap.oppfyltEtterGamleRegler != null) {
                this.oppfyltEtterGamleRegler = forutgaendeMedlemskap.oppfyltEtterGamleRegler
            } else {
                this.oppfyltEtterGamleRegler = false
            }
        }
    }

    /**
     * Mapping fra ulike Vilkar til InngangOgEksportGrunnlag (PK-9695, PKPYTON-923).
     * Gjort pr. løsningsbeskrivelse PK-6071 (PK-6951), foruten  innvilgetGarantertTP som ikke skal brukes.
     * Brukes for å opprette grunnlaget ved beregning av uføretrygd med trygdeavtaler.
     */
    constructor(
        fortsattMedlemskap: FortsattMedlemskap?,
        forutgaendeMedlemskap: ForutgaendeMedlemskap?,
        rettTilEksportEtterTrygdeavtaler: RettTilEksportEtterTrygdeavtaler?,
        medlemskapForUTEtterTrygdeavtaler: MedlemskapForUTEtterTrygdeavtaler?
    ) : this(fortsattMedlemskap, forutgaendeMedlemskap) {
        if (rettTilEksportEtterTrygdeavtaler != null) {
            if (rettTilEksportEtterTrygdeavtaler.eksportrettEtterEOSForordning != null) {
                this.eksportrettEtterEOSForordning =
                    Eksportrett(rettTilEksportEtterTrygdeavtaler.eksportrettEtterEOSForordning!!)
            }
            if (rettTilEksportEtterTrygdeavtaler.eksportrettEtterTrygdeavtalerEOS != null) {
                this.eksportrettEtterTrygdeavtalerEOS =
                    Eksportrett(rettTilEksportEtterTrygdeavtaler.eksportrettEtterTrygdeavtalerEOS!!)
            }
            if (rettTilEksportEtterTrygdeavtaler.eksportrettEtterAndreTrygdeavtaler != null) {
                this.eksportrettEtterAndreTrygdeavtaler =
                    Eksportrett(rettTilEksportEtterTrygdeavtaler.eksportrettEtterAndreTrygdeavtaler!!)
            }
        }

        if (medlemskapForUTEtterTrygdeavtaler?.oppfyltVedSammenlegging != null) {
            this.oppfyltVedSammenlegging =
                OppfyltVedSammenlegging(medlemskapForUTEtterTrygdeavtaler.oppfyltVedSammenlegging!!)
        }

        if (medlemskapForUTEtterTrygdeavtaler?.oppfyltVedSammenleggingFemAr != null) {
            oppfyltVedSammenleggingFemAr =
                OppfyltVedSammenlegging(medlemskapForUTEtterTrygdeavtaler.oppfyltVedSammenleggingFemAr!!)
        }
    }

    override fun toString(): String {
        val TAB = "    "

        val retValue = StringBuilder()

        retValue.append("InngangOgEksportGrunnlag ( ").append(super.toString()).append(TAB)
            .append("treArTrygdetidNorge = ").append(treArTrygdetidNorge).append(TAB)
            .append("unntakFraForutgaendeTT = ").append(unntakFraForutgaendeTT).append(TAB)
            .append("fortsattMedlemFT = ").append(fortsattMedlemFT).append(TAB)
            .append("minstTyveArBotidNorge = ").append(minstTyveArBotidNorge).append(TAB)
            .append("opptjentRettTilTPEtterFT = ").append(opptjentRettTilTPEtterFT).append(TAB)
            .append("eksportforbud = ").append(eksportforbud).append(TAB).append("friEksportPgaYrkesskade = ")
            .append(friEksportPgaYrkesskade).append(TAB)
            .append("eksportrettEtterEOSForordning = ").append(eksportrettEtterEOSForordning).append(TAB)
            .append("eksportrettEtterTrygdeavtalerEOS = ")
            .append(eksportrettEtterTrygdeavtalerEOS).append(TAB).append("eksportrettEtterAndreTrygdeavtaler = ")
            .append(eksportrettEtterAndreTrygdeavtaler).append(TAB)
            .append("eksportrettGarantertTP = ").append(eksportrettGarantertTP).append(TAB)
            .append("minstTreArsFMNorge = ").append(minstTreArsFMNorge).append(TAB)
            .append("minstTreArsFMNorgeVirkdato = ").append(minstTreArsFMNorgeVirkdato).append(TAB)
            .append("unntakFraForutgaendeMedlemskap = ")
            .append(unntakFraForutgaendeMedlemskap).append(TAB).append("oppfyltEtterGamleRegler = ")
            .append(oppfyltEtterGamleRegler).append(TAB)
            .append("oppfyltVedSammenlegging = ").append(oppfyltVedSammenlegging).append(TAB)
            .append("oppfyltVedGjenlevendesMedlemskap = ")
            .append(oppfyltVedGjenlevendesMedlemskap).append(TAB).append("gjenlevendeMedlemFT = ")
            .append(gjenlevendeMedlemFT).append(TAB).append("minstEttArFMNorge = ")
            .append(minstEttArFMNorge).append(TAB).append("foreldreMinstTyveArBotidNorge = ")
            .append(foreldreMinstTyveArBotidNorge).append(TAB).append("friEksportDodsfall = ")
            .append(friEksportDodsfall).append(TAB).append(" )")

        return retValue.toString()
    }
}
