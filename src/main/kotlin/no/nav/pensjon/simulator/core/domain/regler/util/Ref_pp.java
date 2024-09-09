package no.nav.pensjon.simulator.core.domain.regler.util;

import no.nav.pensjon.simulator.core.domain.regler.Omsorgsopptjening;

public class Ref_pp implements Comparable<Ref_pp> {
    Omsorgsopptjening pt;
    double poengtall;
    int antallÅr;
    boolean omsorg;
    boolean erDummy;

    public Ref_pp(Omsorgsopptjening pt, double verdi, int i, boolean omsorg, boolean dummy) {
        this.pt = pt;
        poengtall = verdi;
        antallÅr = i;
        this.omsorg = omsorg;
        this.erDummy = dummy;
    }

    @Override
    public int compareTo(Ref_pp o) {
        if (poengtall > o.poengtall) {
            return -1;
        } else if (poengtall < o.poengtall) {
            return 1;
        } else if (o.pt.getJustertBelop() > pt.getJustertBelop()) {
            return 1;
        } else if (o.pt.getJustertBelop() < pt.getJustertBelop()) {
            return -1;
        } else {
            return o.pt.getOpptjeningsar() - pt.getOpptjeningsar();
        }
    }
}
