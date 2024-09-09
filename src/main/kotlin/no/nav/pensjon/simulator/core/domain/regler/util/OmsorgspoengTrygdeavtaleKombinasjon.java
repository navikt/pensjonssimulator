package no.nav.pensjon.simulator.core.domain.regler.util;

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.OpptjeningUT;
import no.nav.pensjon.simulator.core.domain.regler.util.OmsorgspoengCompareUtil.StorrelseOgLeksikografiskArstallComparator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OmsorgspoengTrygdeavtaleKombinasjon implements Comparable<OmsorgspoengTrygdeavtaleKombinasjon> {
    private final boolean erUtvidetOmfang;
    private double nasjonaltSnitt;
    private double beregningsgrunnlagUavrundet;
    private double sumAvJusterteBelopIBG;
    private final LinkedList<OpptjeningUT> kombinasjon;
    private OmsorgspoengTrygdeavtaleKombinasjon utvidetOmfangKombinasjon;
    private int antallValgteIal;
    private int antallPlasser;
    private boolean harNorskOpptjening;
    private List<OpptjeningUT> relevanteArForUtvidetOmfang;

    /**
     * Merk at denne kopikonstruktøren ikke er fullverdig da den ikke kopierer OpptjeningUT.
     */
    public OmsorgspoengTrygdeavtaleKombinasjon(OmsorgspoengTrygdeavtaleKombinasjon kombinasjon) {
        this.nasjonaltSnitt = kombinasjon.nasjonaltSnitt;
        this.beregningsgrunnlagUavrundet = kombinasjon.beregningsgrunnlagUavrundet;
        this.sumAvJusterteBelopIBG = kombinasjon.sumAvJusterteBelopIBG;
        this.kombinasjon = new LinkedList<>();
        this.kombinasjon.addAll(kombinasjon.kombinasjon);
        if (kombinasjon.utvidetOmfangKombinasjon != null) {
            this.utvidetOmfangKombinasjon = new OmsorgspoengTrygdeavtaleKombinasjon(kombinasjon.utvidetOmfangKombinasjon);
        }
        this.antallValgteIal = kombinasjon.antallValgteIal;
        this.antallPlasser = kombinasjon.antallPlasser;
        this.harNorskOpptjening = kombinasjon.harNorskOpptjening;
        this.relevanteArForUtvidetOmfang = kombinasjon.relevanteArForUtvidetOmfang;
        this.erUtvidetOmfang = kombinasjon.erUtvidetOmfang;
    }

    /**
     * Brukes for å initialisere kombinasjon med et gitt antall plasser.
     */
    public OmsorgspoengTrygdeavtaleKombinasjon(int antallPlasser, boolean erUtvidetOmfang) {
        nasjonaltSnitt = 0.0;
        beregningsgrunnlagUavrundet = 0.0;
        sumAvJusterteBelopIBG = 0.0;
        kombinasjon = new LinkedList<>();
        antallValgteIal = 0;
        this.antallPlasser = antallPlasser;
        harNorskOpptjening = false;
        this.erUtvidetOmfang = erUtvidetOmfang;
    }

    /**
     * Legg til opptjeningUT til kombinasjon.
     * Setter flagg om norsk opptjening hvis avkortetBeløp er over 0.0.
     * Minsker antall plasser.
     **/
    public void leggTilOpptjeningUT(OpptjeningUT o) {
        if (o.getVerdi() > 0.0) {
            harNorskOpptjening = true;
        }
        if (o.getInntektIAvtaleland()) {
            antallValgteIal++;
        }
        antallPlasser--;
        kombinasjon.add(o);
    }

    /**
     * Legg til flere OpptjeningUT ved iterativt kall til leggTilOpptjeningUT(OpptjeningUT)
     */
    public void leggTilOpptjeningUTer(List<OpptjeningUT> os) {
        for (OpptjeningUT o : os) {
            leggTilOpptjeningUT(o);
        }
    }

    /**
     * Regn ut det nasjonale snittet for kombinasjonen.
     * Antar at utvidet omfang ikke finnes.
     */
    public void regnNasjonaltSnitt() {
        int antallIkkeNull = 0;
        int antallIAL = 0;
        double nasjonalt = 0.0;
        for (OpptjeningUT o : kombinasjon) {
            if (o.getVerdi() > 0.0) {
                antallIkkeNull++;
                nasjonalt += o.getVerdi();
            }
            if (o.getInntektIAvtaleland()) {
                antallIAL++;
            }
        }
        double nasjonaltSnitt = 0.0;

//        IKKE (originalt omfang OG (antall PGI = 5 ELLER antall IAL = 0))
        if (antallIkkeNull > 0 && !(!erUtvidetOmfang && (antallIkkeNull == 5 || antallIAL == 0))) {
//        if ( antallIkkeNull > 0 ) {
            nasjonaltSnitt = nasjonalt / antallIkkeNull;
        }

        this.nasjonaltSnitt = nasjonaltSnitt;
    }

    /**
     * Regner uavrundet beregningsgrunnlag.
     * Funksjonalitet for å arve fra kombinasjon fra utvidet omfang hvis denne finnes.
     */
    public void regnBeregningsgrunnlagUavrundet() {
        if (utvidetOmfangKombinasjon != null) {
            beregningsgrunnlagUavrundet = utvidetOmfangKombinasjon.beregningsgrunnlagUavrundet;
        } else {
            double sumAvTreBeste = 0.0;
            //Sorterer OpptjeningUTer i synkende rekkefølge. Måten det sorteres på er avhengig av om vi behandler utvidet omfang eller ikke.
            if (this.erUtvidetOmfang) {
                kombinasjon.sort(new OmsorgspoengCompareUtil.UtvidetOmfangComparator(nasjonaltSnitt));
                for (int i = 0; i < 3 && i < kombinasjon.size(); i++) {
                    if (kombinasjon.get(i).getVerdi() == 0) {
                        sumAvTreBeste += nasjonaltSnitt;
                    } else {
                        sumAvTreBeste += kombinasjon.get(i).getVerdi();
                    }
                    sumAvJusterteBelopIBG += kombinasjon.get(i).getJustertBelop();
                }
            } else {
                kombinasjon.sort(new OmsorgspoengCompareUtil.OriginaltOmfangComparator(nasjonaltSnitt));
                for (int i = 0; i < 3 && i < kombinasjon.size(); i++) {
                    if (kombinasjon.get(i).getInntektIAvtaleland() && kombinasjon.get(i).getVerdi() < nasjonaltSnitt) {
                        sumAvTreBeste += nasjonaltSnitt;
                    } else {
                        sumAvTreBeste += kombinasjon.get(i).getVerdi();
                    }
                    sumAvJusterteBelopIBG += kombinasjon.get(i).getJustertBelop();
                }
            }

            this.beregningsgrunnlagUavrundet = sumAvTreBeste / 3;
        }
    }

    //    @Override
    public int compareTo(OmsorgspoengTrygdeavtaleKombinasjon k) {
        if (k.getBeregningsgrunnlagUavrundet() > beregningsgrunnlagUavrundet) {
            return 1;
        } else if (k.getBeregningsgrunnlagUavrundet() < beregningsgrunnlagUavrundet) {
            return -1;
        }
        //Kombinasjonene kunne ikke distingveres på beregningsgrunnlagUavrundet.
        //Forsøker å sortere på beste sum av justertBelop for årene som inngår i snittet beregningsgrunnlaget i stedet.
        else if (k.getSumAvJusterteBelopIBG() > sumAvJusterteBelopIBG) {
            return 1;
        } else if (k.getSumAvJusterteBelopIBG() < sumAvJusterteBelopIBG) {
            return -1;
        }
        //OpptjeningUT kunne ikke distingveres på justertBelop heller. Sorter disse leksikografisk på årene til OpptjeningUT.
        List<OpptjeningUT> valgteArK = new ArrayList<>(k.getKombinasjon());
        if (k.getUtvidetOmfang() != null) {
            valgteArK.addAll(k.getUtvidetOmfang().getKombinasjon());
        }
        List<OpptjeningUT> valgteArDenne = new ArrayList<>(kombinasjon);
        if (utvidetOmfangKombinasjon != null) {
            valgteArDenne.addAll(utvidetOmfangKombinasjon.getKombinasjon());
        }
        valgteArK.sort(new OmsorgspoengCompareUtil.SynkendeArstallComparator());
        valgteArDenne.sort(new OmsorgspoengCompareUtil.SynkendeArstallComparator());
        return new StorrelseOgLeksikografiskArstallComparator().compare(valgteArDenne, valgteArK);
    }

    public double getNasjonaltSnitt() {
        return nasjonaltSnitt;
    }

    public double getBeregningsgrunnlagUavrundet() {
        return beregningsgrunnlagUavrundet;
    }

    public double getSumAvJusterteBelopIBG() {
        return sumAvJusterteBelopIBG;
    }

    public LinkedList<OpptjeningUT> getKombinasjon() {
        return kombinasjon;
    }

    public LinkedList<OpptjeningUT> getKombinasjonSortert() {
        if (!this.erUtvidetOmfang) {
            kombinasjon.sort(new OmsorgspoengCompareUtil.OriginaltOmfangComparator(nasjonaltSnitt));
        } else {
            kombinasjon.sort(new OmsorgspoengCompareUtil.UtvidetOmfangComparator(nasjonaltSnitt));
        }
        return kombinasjon;
    }

    public OmsorgspoengTrygdeavtaleKombinasjon getUtvidetOmfang() {
        return utvidetOmfangKombinasjon;
    }

    /**
     * Setter kombinasjon fra utvidet omfang. Merk at sum av justerte beløp, nasjonalt snitt arves.
     */
    public void setUtvidetOmfang(OmsorgspoengTrygdeavtaleKombinasjon kombinasjon) {
        this.nasjonaltSnitt = kombinasjon.nasjonaltSnitt;
        this.sumAvJusterteBelopIBG = kombinasjon.sumAvJusterteBelopIBG;
        this.utvidetOmfangKombinasjon = kombinasjon;
    }

    public int getAntallValgteIal() {
        return antallValgteIal;
    }

    public int getAntallPlasser() {
        return antallPlasser;
    }

    public boolean isHarNorskOpptjening() {
        return harNorskOpptjening;
    }

    public List<OpptjeningUT> getRelevanteArForUtvidetOmfang() {
        return relevanteArForUtvidetOmfang;
    }

    public void setRelevanteArForUtvidetOmfang(List<OpptjeningUT> relevanteArForUtvidetOmfang) {
        this.relevanteArForUtvidetOmfang = relevanteArForUtvidetOmfang;
    }
}
