package no.nav.pensjon.simulator.core.domain.regler.util;

import no.nav.pensjon.simulator.core.domain.regler.IBeregning;
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementerer Iterator pattern for beregningstre. Bruker prefix traversering.
 */
public class BeregningstreIterator implements Iterator<IBeregning> {

    private final ArrayList<IBeregning> beregningsliste = new ArrayList<>();
    private Iterator<IBeregning> iterator;

    public BeregningstreIterator(IBeregning beregning) {
        if (beregning != null) {
            beregningsliste.add(beregning);
            prefixTraverser(beregning);
            iterator = beregningsliste.iterator();
        }
    }

    /**
     * Rekursiv metode som prefix traverserer beregningstre og legger hver delberegning
     * funnet i beregningsliste. Toppnoden blir lagt på som første element.
     */
    private void prefixTraverser(IBeregning beregning) {
        List<BeregningRelasjon> delberegningsliste = beregning.getDelberegningsListe();

        if (!delberegningsliste.isEmpty()) {
            for (BeregningRelasjon beregningrelasjon : delberegningsliste) {
                IBeregning delberegning = beregningrelasjon.getIBeregning();
                if (delberegning != null) {
                    beregningsliste.add(delberegning);
                    prefixTraverser(delberegning);
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public IBeregning next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
