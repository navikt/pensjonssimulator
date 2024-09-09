package no.nav.pensjon.simulator.core.domain.regler.util;

import no.nav.pensjon.simulator.core.domain.regler.Omsorgsopptjening;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * "Brute Force" alternativ til klassen Omsorgspoeng.
 * Beregn snitt av 3 beste år av alle lovlige utvalg på 3 eller 5 siste år.
 * År med omsorg kan enten tas med i utvalget ved at faktisk opptjening
 * brukes i snittberegningen, eller året kan brukes som "bindeår" ved
 * at man hopper over året.
 * Det er lagt inn en optimalisering som går ut på at år med omsorg og
 * faktisk opptjening lik 0 fjernes fra opptjeningslisten før beregning starter.
 * Slike år vil uansett ikke kunne bidra til et snitt over 0 men kan bare brukes
 * som "bindeår".
 */
public class OmsorgspoengBF {
    //Størrelse på utvalg. Settes til 3 eller 5.
    private int sett_storrelse;

    //Største snitt som er funnet.
    private double storste_snitt = 0;

    // Utvalg som gir det største snitt
    private Omsorgsopptjening[] snitt_utvalg;

    // Stack som holder på utvalg som skal beregnes
    private LinkedList<Omsorgsopptjening> utvalg_stack;

    /**
     * Snitt av 3 største verdier i utvalg.
     * Runder av til 2 desimaler.
     */
    public double beregnSnitt(Omsorgsopptjening[] utvalg) {
        if (utvalg == null || utvalg.length == 0) {
            return 0.0;
        }

        Arrays.sort(utvalg, new OmsorgspoengCompareUtil.PoengtallComparator());
        double sum = 0.0;
        for (int i = 0; i < 3 && i < utvalg.length; i++) {
            sum += utvalg[i].getVerdi();
        }
        return Avrunding.avrund2Desimaler(sum / 3);
    }

    /**
     * Finn alle lovlige utvalg.
     * Bruker rekursjon:
     * Hvis utvalg har ønsket størrelse så beregn snitt og returner
     * Ellers
     * 1. Legger aktuellOpptjening til utvalg.
     * 2. Rekursivt kall for å finne alle lovlige utvalg videre fra neste posisjon i liste.
     * 3. Har nå forsøkt alle lovlige utvalg hvor aktuellOpptjening inngår. Fjerner derfor aktuellOpptjening fra utvalget.
     * 4. Hvis aktuellOpptjening er omsorg så hopp over denne og finn alle lovlige utvalg videre fra neste posisjon i liste
     */
    private void finnLovligUtvalg(Omsorgsopptjening[] liste, int pos) {
        if (utvalg_stack.size() == sett_storrelse) {
            Omsorgsopptjening[] nyttUtvalg = utvalg_stack.toArray(new Omsorgsopptjening[0]);
            double snitt = beregnSnitt(nyttUtvalg);
            if (snitt > storste_snitt || snitt_utvalg == null) {
                storste_snitt = snitt;
                snitt_utvalg = nyttUtvalg;
            }
        } else if (utvalg_stack.size() < sett_storrelse && pos < liste.length) {
            Omsorgsopptjening aktuellOpptjening = liste[pos];
            utvalg_stack.push(aktuellOpptjening);
            finnLovligUtvalg(liste, pos + 1);
            utvalg_stack.pop();
            if (aktuellOpptjening.getOmsorg()) {
                finnLovligUtvalg(liste, pos + 1);
            }
        }
    }

    /**
     * Optimalisering: Hopper over omsorgsår med verdi lik 0.
     */
    private Omsorgsopptjening[] fjernOmsorgsarUtenVerdi(Omsorgsopptjening[] opptjeningsliste) {
        ArrayList<Omsorgsopptjening> nyListe = new ArrayList<>();
        for (Omsorgsopptjening o : opptjeningsliste) {
            if (o.getOmsorg() && o.getVerdi() == 0.0) {
            } else {
                nyListe.add(o);
            }
        }
        return nyListe.toArray(new Omsorgsopptjening[0]);
    }

    /**
     * Beregn beste snitt av 3 fra et utvalg lik sett_storrelse (3 eller 5).
     */
    private Omsorgsopptjening[] beregnBesteSnitt(Omsorgsopptjening[] opptjeningsliste) {
        if (opptjeningsliste == null || opptjeningsliste.length <= sett_storrelse) {
            return opptjeningsliste;
        }

        storste_snitt = 0;
        utvalg_stack = new LinkedList<>();
        snitt_utvalg = null;

        // Sikre at opptjeningsliste er sortert med seneste år først.
        Arrays.sort(opptjeningsliste, new OmsorgspoengCompareUtil.SynkendeArstallComparator());

        Omsorgsopptjening[] listeOptimalisert = fjernOmsorgsarUtenVerdi(opptjeningsliste);
        if (listeOptimalisert.length == 0) {
            // Grensetilfelle hvor alle år er hoppet over pga. omsorg og verdi lik 0.
            // Returnerer utvalg lik første 5 fra opptjeningsliste.
            return Arrays.copyOf(opptjeningsliste, 5);
        } else if (listeOptimalisert.length <= sett_storrelse) {
            return listeOptimalisert;
        }

        // Finn lovlige utvalg og beregn beste snitt.
        finnLovligUtvalg(listeOptimalisert, 0);

        return snitt_utvalg;
    }

    /**
     * Finn lovlig utvalg på 5 omsorgsoopptjening som gir det beste snitt av 3.
     */
    public Omsorgsopptjening[] beregnBesteSnitt3av5(Omsorgsopptjening[] opptjeningsliste) {
        sett_storrelse = 5;
        return beregnBesteSnitt(opptjeningsliste);
    }

    /**
     * Finn lovlig utvalg på 3 omsorgsopptjening som gir det beste snitt av 3.
     */
    public Omsorgsopptjening[] beregnBesteSnitt3av3(Omsorgsopptjening[] opptjeningsliste) {
        sett_storrelse = 3;
        return beregnBesteSnitt(opptjeningsliste);
    }

    /**
     * For bakoverkompatibilitet.
     */
    public double beregnFPP3Beste(Omsorgsopptjening[] poengtallliste) {
        Omsorgsopptjening[] vinner = beregnBesteSnitt3av3(poengtallliste);
        return beregnSnitt(vinner);
    }
}
