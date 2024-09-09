package no.nav.pensjon.simulator.core.domain.regler.util;

import no.nav.pensjon.simulator.core.domain.regler.*;
import no.nav.pensjon.simulator.core.domain.regler.beregning.*;
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.KrigOgGammelYrkesskade;
import no.nav.pensjon.simulator.core.domain.regler.kode.TypeCti;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Helper class used to determine if two Object trees may be considered
 * equivalent. Note that 'equivalent' does not imply 'equal' for some classes.
 * Currently support for comparison of Beregning pairs.
 * Checker classes for lower-level tree nodes may easily be reused if we need to
 * add support for comparison of sub-trees.
 *
 * @author Jan Haugland, Accenture
 */

public class BeregningSammenlikner {
    // All local 'checker classes' used here extend a common abstract
    // NullSafeChecker that handle these cases:
    // - The Objects are actually same instance. (equivalent)
    // - Both are null. (equivalent)
    // - One is null, the other is not. (not equivalent)
    //
    // The extensions do field level comparison if we have two distinct non-null
    // instances.

    // General checker for comparison of primitive fields,
    // and for classes where equal-method can be used to determine eqivalence:

    private static final EqualChecker equalChecker = new EqualChecker();

    // Tailored checkers for classes where equals-method cannot be used to determine equivalence:

    private static final BeregningChecker beregningChecker = new BeregningChecker();
    private static final TypeCtiChecker typeCtiChecker = new TypeCtiChecker();
    private static final KrigOgGammelYrkesskadeChecker krigOgGammelYrkesskadeChecker = new KrigOgGammelYrkesskadeChecker();
    private static final PenPersonChecker penPersonChecker = new PenPersonChecker();
    private static final UforeEkstraChecker uforeEkstraChecker = new UforeEkstraChecker();
    private static final BeregningUforeperiodeChecker beregningUforeperiodeChecker = new BeregningUforeperiodeChecker();
    private static final TrygdetidChecker trygdetidChecker = new TrygdetidChecker();
    private static final TTUtlandEOSChecker tTUtlandEOSChecker = new TTUtlandEOSChecker();
    private static final TTUtlandKonvensjonChecker tTUtlandKonvensjonChecker = new TTUtlandKonvensjonChecker();
    private static final SluttpoengtallChecker sluttpoengtallChecker = new SluttpoengtallChecker();
    private static final PoengrekkeChecker poengrekkeChecker = new PoengrekkeChecker();
    private static final FramtidigPensjonspoengtallChecker framtidigPensjonspoengtallChecker = new FramtidigPensjonspoengtallChecker();
    private static final BeregningRelasjonChecker beregningRelasjonChecker = new BeregningRelasjonChecker();
    private static final PoengtallChecker poengtallChecker = new PoengtallChecker();
    private static final MerknadChecker merknadChecker = new MerknadChecker();

    // Some Ytelseskomponent extensions have no additional fields, so we just compare their superclass:

    private static final YtelseskomponentChecker<Ytelseskomponent> plainYtelseskomponentChecker = new YtelseskomponentChecker<>();

    // Other Ytelseskomponent extensions have additional fields that also need to be compared:

    private static final GrunnpensjonChecker grunnpensjonChecker = new GrunnpensjonChecker();
    private static final TilleggspensjonChecker tilleggspensjonChecker = new TilleggspensjonChecker();
    private static final SertilleggChecker sertilleggChecker = new SertilleggChecker();
    private static final VentetilleggChecker ventetilleggChecker = new VentetilleggChecker();
    private static final EktefelletilleggChecker ektefelletilleggChecker = new EktefelletilleggChecker();
    private static final BarnetilleggFellesbarnChecker barnetilleggFellesbarnChecker = new BarnetilleggFellesbarnChecker();
    private static final BarnetilleggSerkullsbarnChecker barnetilleggSerkullsbarnChecker = new BarnetilleggSerkullsbarnChecker();

    // Wrappers for List item checking.
    private static final ListChecker<Object> listEqualChecker = new ListChecker<>(equalChecker);
    private static final ListChecker<BeregningRelasjon> beregningRelasjonListChecker = new ListChecker<>(beregningRelasjonChecker);
    private static final ListChecker<Poengtall> poengtallListChecker = new ListChecker<>(poengtallChecker);
    private static final ListChecker<Merknad> merknadListChecker = new ListChecker<>(merknadChecker);
    private static final Logger log = LoggerFactory.getLogger(BeregningSammenlikner.class);

    // Never instansiate. Only static access:
    private BeregningSammenlikner() {
    }

    /**
     * Entry from outside world. Check if two instances of Beregning may be
     * considered equivalent.
     *
     * @return true if equivalent
     */
    public static boolean areEquivalent(Beregning beregning1, Beregning beregning2) {
        return beregningChecker.areEquivalent(beregning1, beregning2, new HashSet<>(), "Beregning");
    }

    // Safety precaution if we ever hit same identity combination on several- or
    // circular reference paths:
    // Note that combinations (o1, o2) and (o2, o1) are considered equal
    // regardless of sequence, and thus also give same hashCode.
    private static class CheckedPair {
        private final Object o1;
        private final Object o2;

        protected CheckedPair(Object o1, Object o2) {
            this.o1 = o1;
            this.o2 = o2;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(o1) + System.identityHashCode(o2);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CheckedPair)) {
                return false;
            }

            CheckedPair that = (CheckedPair) o;

            return o1 == that.o1 && o2 == that.o2 || o1 == that.o2 && o2 == that.o1;
        }
    }

    private static class BeregningChecker extends NullSafeChecker<Beregning> {
        // Note that two instances are considered equivalent even if following
        // fields are different:
        // - virkFom
        // - virkTom
        // - totalVinner
        // - beregningArsak

        @Override
        protected boolean haveEquivalentValues(Beregning o1, Beregning o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getBrutto(), o2.getBrutto(), property + ".brutto")
                    && equalChecker.areEquivalent(o1.getNetto(), o2.getNetto(), property + ".netto")
                    && equalChecker.areEquivalent(o1.getG(), o2.getG(), property + ".g")
                    && equalChecker.areEquivalent(o1.getTt_anv(), o2.getTt_anv(), property + ".tt_anv")
                    && equalChecker.areEquivalent(o1.getEktefelleInntektOver2g(), o2.getEktefelleInntektOver2g(), property + ".ektefelleInntektOver2g")
                    && equalChecker.areEquivalent(o1.getP67beregning(), o2.getP67beregning(), property + ".P67beregning")
                    && equalChecker.areEquivalent(o1.getAfpPensjonsgrad(), o2.getAfpPensjonsgrad(), property + ".afpPensjonsgrad")
                    && equalChecker.areEquivalent(o1.getFribelop(), o2.getFribelop(), property + ".fribelop")
                    && equalChecker.areEquivalent(o1.getFriinntekt(), o2.getFriinntekt(), property + ".friinntekt")
                    && equalChecker.areEquivalent(o1.getBeregnetFremtidigInntekt(), o2.getBeregnetFremtidigInntekt(), property + ".beregnetFremtidigInntekt")
                    && equalChecker.areEquivalent(o1.getEktefelleMottarPensjon(), o2.getEktefelleMottarPensjon(), property + ".ektefelleMottarPensjon")
                    && equalChecker.areEquivalent(o1.getGradert(), o2.getGradert(), property + ".gradert")
                    && equalChecker.areEquivalent(o1.getIkkeTraverser(), o2.getIkkeTraverser(), property + ".ikkeTraverser")
                    && equalChecker.areEquivalent(o1.getInntektBruktIAvkorting(), o2.getInntektBruktIAvkorting(), property + ".inntektBruktIAvkorting")
                    && equalChecker.areEquivalent(o1.getRedusertPgaInstOpphold(), o2.getRedusertPgaInstOpphold(), property + ".redusertPgaInstOpphold")
                    && equalChecker.areEquivalent(o1.getUfg(), o2.getUfg(), property + ".ufg")
                    && equalChecker.areEquivalent(o1.getYug(), o2.getYug(), property + ".yug")
                    && equalChecker.areEquivalent(o1.getBrukOpptjeningFra65I66Aret(), o2.getBrukOpptjeningFra65I66Aret(), property + ".brukOpptjeningFra65I66Aret")
                    && typeCtiChecker.areEquivalent(o1.getBeregningsMetode(), o2.getBeregningsMetode(), checkedPairs, property + ".beregningsMetode")
                    && typeCtiChecker.areEquivalent(o1.getBeregningType(), o2.getBeregningType(), checkedPairs, property + ".beregningType")
                    && typeCtiChecker.areEquivalent(o1.getResultatType(), o2.getResultatType(), checkedPairs, property + ".resultatType")
                    && typeCtiChecker.areEquivalent(o1.getMinstepensjontype(), o2.getMinstepensjontype(), checkedPairs, property + ".minstepensjontype")
                    && typeCtiChecker.areEquivalent(o1.getBenyttetSivilstand(), o2.getBenyttetSivilstand(), checkedPairs, property + ".benyttetSivilstand")
                    && typeCtiChecker.areEquivalent(o1.getBrukersSivilstand(), o2.getBrukersSivilstand(), checkedPairs, property + ".brukersSivilstand")
                    && penPersonChecker.areEquivalent(o1.getPenPerson(), o2.getPenPerson(), checkedPairs, property + ".penPerson")
                    && grunnpensjonChecker.areEquivalent(o1.getGp(), o2.getGp(), checkedPairs, property + ".gp")
                    && tilleggspensjonChecker.areEquivalent(o1.getTp(), o2.getTp(), checkedPairs, property + ".tp")
                    && sertilleggChecker.areEquivalent(o1.getSt(), o2.getSt(), checkedPairs, property + ".st")
                    && plainYtelseskomponentChecker.areEquivalent(o1.getAfpTillegg(), o2.getAfpTillegg(), checkedPairs, property + ".afpTillegg")
                    && ventetilleggChecker.areEquivalent(o1.getVt(), o2.getVt(), checkedPairs, property + ".vt")
                    && plainYtelseskomponentChecker.areEquivalent(o1.getP851_tillegg(), o2.getP851_tillegg(), checkedPairs, property + ".p851_tillegg")
                    && ektefelletilleggChecker.areEquivalent(o1.getEt(), o2.getEt(), checkedPairs, property + ".et")
                    && barnetilleggFellesbarnChecker.areEquivalent(o1.getTfb(), o2.getTfb(), checkedPairs, property + ".tfb")
                    && barnetilleggSerkullsbarnChecker.areEquivalent(o1.getTsb(), o2.getTsb(), checkedPairs, property + ".tsb")
                    && plainYtelseskomponentChecker.areEquivalent(o1.getFamilietillegg(), o2.getFamilietillegg(), checkedPairs, property + ".familietillegg")
                    && plainYtelseskomponentChecker.areEquivalent(o1.getTilleggFasteUtgifter(), o2.getTilleggFasteUtgifter(), checkedPairs, property + ".tilleggFasteUtgifter")
                    && trygdetidChecker.areEquivalent(o1.getTrygdetid(), o2.getTrygdetid(), checkedPairs, property + ".trygdetid")
                    && uforeEkstraChecker.areEquivalent(o1.getUforeEkstra(), o2.getUforeEkstra(), checkedPairs, property + ".uforeEkstra")
                    && krigOgGammelYrkesskadeChecker.areEquivalent(o1.getKrigOgGammelYrkesskade(), o2.getKrigOgGammelYrkesskade(), checkedPairs, property
                    + ".krigOgGammelYrkesskade")
                    && beregningRelasjonListChecker.areEquivalent(o1.getDelberegningsListe(), o2.getDelberegningsListe(), checkedPairs, property + ".delberegningsListe")
                    && merknadListChecker.areEquivalent(o1.getMerknadListe(), o2.getMerknadListe(), checkedPairs, property + ".merknadListe");
        }
    }

    private static class TypeCtiChecker extends NullSafeChecker<TypeCti> {
        @Override
        protected boolean haveEquivalentValues(TypeCti o1, TypeCti o2, Set<CheckedPair> checkedPairs, String property) {
            // Cti instanses considered equivalent if same type (ie. same Class)
            // and equal code values.
            return equalChecker.areEquivalent(o1.getClass(), o2.getClass(), checkedPairs, property)
                    && equalChecker.areEquivalent(o1.getKode(), o2.getKode(), checkedPairs, property + ".kode");
        }
    }

    private static class BeregningRelasjonChecker extends NullSafeChecker<BeregningRelasjon> {
        @Override
        protected boolean haveEquivalentValues(BeregningRelasjon o1, BeregningRelasjon o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getBruk(), o2.getBruk(), property + ".bruk")
                    && beregningChecker.areEquivalent(o1.getBeregning(), o2.getBeregning(), checkedPairs, property + ".beregning");
        }
    }

    private static class KrigOgGammelYrkesskadeChecker extends NullSafeChecker<KrigOgGammelYrkesskade> {
        @Override
        protected boolean haveEquivalentValues(KrigOgGammelYrkesskade o1, KrigOgGammelYrkesskade o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getGrunnlagForUtbetaling(), o2.getGrunnlagForUtbetaling(), property + ".grunnlagForUtbetaling")
                    && equalChecker.areEquivalent(o1.getKapitalutlosning(), o2.getKapitalutlosning(), property + ".kapitalutlosning")
                    && equalChecker.areEquivalent(o1.getMendel(), o2.getMendel(), property + ".mendel")
                    && equalChecker.areEquivalent(o1.getPensjonsgrad(), o2.getPensjonsgrad(), property + ".pensjonsgrad")
                    && equalChecker.areEquivalent(o1.getPs(), o2.getPs(), property + ".ps")
                    && equalChecker.areEquivalent(o1.getYg(), o2.getYg(), property + ".yg");
        }
    }

    private static class PenPersonChecker extends NullSafeChecker<PenPerson> {
        @Override
        protected boolean haveEquivalentValues(PenPerson o1, PenPerson o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getPenPersonId(), o2.getPenPersonId(), property + ".penPersonId");
        }
    }

    private static class MerknadChecker extends NullSafeChecker<Merknad> {
        @Override
        protected boolean haveEquivalentValues(Merknad o1, Merknad o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getKode(), o2.getKode(), checkedPairs, property)
                    && listEqualChecker.areEquivalent(o1.getArgumentListe(), o2.getArgumentListe(), checkedPairs, property + ".argumentListe");
        }
    }

    private static class GrunnpensjonChecker extends YtelseskomponentChecker<Grunnpensjon> {
        @Override
        protected boolean haveEquivalentValues(Grunnpensjon o1, Grunnpensjon o2, Set<CheckedPair> checkedPairs, String property) {
            return super.haveEquivalentValues(o1, o2, checkedPairs, property)
                    && equalChecker.areEquivalent(o1.getPSats_gp(), o2.getPSats_gp(), property + ".pSats_gp");
        }
    }

    private static class TilleggspensjonChecker extends YtelseskomponentChecker<Tilleggspensjon> {
        @Override
        protected boolean haveEquivalentValues(Tilleggspensjon o1, Tilleggspensjon o2, Set<CheckedPair> checkedPairs, String property) {
            return super.haveEquivalentValues(o1, o2, checkedPairs, property)
                    && equalChecker.areEquivalent(o1.getSkiltesDelAvAdodesTP(), o2.getSkiltesDelAvAdodesTP(), property + ".skiltesDelAvAdodesTP")
                    && sluttpoengtallChecker.areEquivalent(o1.getOpt(), o2.getOpt(), checkedPairs, property + ".opt")
                    && sluttpoengtallChecker.areEquivalent(o1.getSpt(), o2.getSpt(), checkedPairs, property + ".spt")
                    && sluttpoengtallChecker.areEquivalent(o1.getYpt(), o2.getYpt(), checkedPairs, property + ".ypt");
        }
    }

    private static class SertilleggChecker extends YtelseskomponentChecker<Sertillegg> {
        @Override
        protected boolean haveEquivalentValues(Sertillegg o1, Sertillegg o2, Set<CheckedPair> checkedPairs, String property) {
            return super.haveEquivalentValues(o1, o2, checkedPairs, property)
                    && equalChecker.areEquivalent(o1.getPSats_st(), o2.getPSats_st(), property + ".pSats_st");
        }
    }

    private static class VentetilleggChecker extends YtelseskomponentChecker<Ventetillegg> {
        @Override
        protected boolean haveEquivalentValues(Ventetillegg o1, Ventetillegg o2, Set<CheckedPair> checkedPairs, String property) {
            return super.haveEquivalentValues(o1, o2, checkedPairs, property)
                    && equalChecker.areEquivalent(o1.getVenteTillegg_GP(), o2.getVenteTillegg_GP(), property + ".venteTillegg_GP")
                    && equalChecker.areEquivalent(o1.getVenteTillegg_TP(), o2.getVenteTillegg_TP(), property + ".venteTillegg_TP")
                    && equalChecker.areEquivalent(o1.getVenteTilleggProsent(), o2.getVenteTilleggProsent(), property + ".venteTilleggProsent");
        }
    }

    private static class EktefelletilleggChecker extends YtelseskomponentChecker<Ektefelletillegg> {
        @Override
        protected boolean haveEquivalentValues(Ektefelletillegg o1, Ektefelletillegg o2, Set<CheckedPair> checkedPairs, String property) {
            return super.haveEquivalentValues(o1, o2, checkedPairs, property)
                    && equalChecker.areEquivalent(o1.getFribelop(), o2.getFribelop(), property + ".fribelop")
                    && equalChecker.areEquivalent(o1.getSamletInntektAvkort(), o2.getSamletInntektAvkort(), property + ".samletInntektAvkort");
        }
    }

    private static class BarnetilleggFellesbarnChecker extends YtelseskomponentChecker<BarnetilleggFellesbarn> {
        @Override
        protected boolean haveEquivalentValues(BarnetilleggFellesbarn o1, BarnetilleggFellesbarn o2, Set<CheckedPair> checkedPairs, String property) {
            return super.haveEquivalentValues(o1, o2, checkedPairs, property)
                    && equalChecker.areEquivalent(o1.getAntallBarn(), o2.getAntallBarn(), property + ".antallBarn")
                    && equalChecker.areEquivalent(o1.getFribelop(), o2.getFribelop(), property + ".fribelop")
                    && equalChecker.areEquivalent(o1.getBtDiff_eos(), o2.getBtDiff_eos(), property + ".btDiff_eos")
                    && equalChecker.areEquivalent(o1.getSamletInntektAvkort(), o2.getSamletInntektAvkort(), property + ".samletInntektAvkort");
        }
    }

    private static class BarnetilleggSerkullsbarnChecker extends YtelseskomponentChecker<BarnetilleggSerkullsbarn> {
        @Override
        protected boolean haveEquivalentValues(BarnetilleggSerkullsbarn o1, BarnetilleggSerkullsbarn o2, Set<CheckedPair> checkedPairs, String property) {
            return super.haveEquivalentValues(o1, o2, checkedPairs, property)
                    && equalChecker.areEquivalent(o1.getAntallBarn(), o2.getAntallBarn(), property + ".antallBarn")
                    && equalChecker.areEquivalent(o1.getFribelop(), o2.getFribelop(), property + ".fribelop")
                    && equalChecker.areEquivalent(o1.getBtDiff_eos(), o2.getBtDiff_eos(), property + ".btDiff_eos")
                    && equalChecker.areEquivalent(o1.getSamletInntektAvkort(), o2.getSamletInntektAvkort(), property + ".samletInntektAvkort");
        }
    }

    private static class UforeEkstraChecker extends NullSafeChecker<UforeEkstra> {
        @Override
        protected boolean haveEquivalentValues(UforeEkstra o1, UforeEkstra o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getTak(), o2.getTak(), property + ".tak")
                    && equalChecker.areEquivalent(o1.getFpp(), o2.getFpp(), property + ".fpp")
                    && equalChecker.areEquivalent(o1.getFppGaranti(), o2.getFppGaranti(), property + ".fppGaranti")
                    && equalChecker.areEquivalent(o1.getRedusertAntFppAr(), o2.getRedusertAntFppAr(), property + ".redusertAntFppAr")
                    && typeCtiChecker.areEquivalent(o1.getInntektkode1(), o2.getInntektkode1(), checkedPairs, property + ".inntektkode1")
                    && typeCtiChecker.areEquivalent(o1.getInntektkode2(), o2.getInntektkode2(), checkedPairs, property + ".inntektkode2")
                    && typeCtiChecker.areEquivalent(o1.getFppGarantiKode(), o2.getFppGarantiKode(), checkedPairs, property + ".fppGarantiKode")
                    && beregningUforeperiodeChecker.areEquivalent(o1.getUforeperiode(), o2.getUforeperiode(), checkedPairs, property + ".uforeperiode")
                    && beregningUforeperiodeChecker.areEquivalent(o1.getUforeperiodeYSK(), o2.getUforeperiodeYSK(), checkedPairs, property + ".uforeperiodeYSK");
        }
    }

    private static class BeregningUforeperiodeChecker extends NullSafeChecker<BeregningUforeperiode> {
        @Override
        protected boolean haveEquivalentValues(BeregningUforeperiode o1, BeregningUforeperiode o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getFodselsArYngsteBarn(), o2.getFodselsArYngsteBarn(), checkedPairs, property + ".fodselsArYngsteBarn")
                    && equalChecker.areEquivalent(o1.getFpp(), o2.getFpp(), checkedPairs, property + ".fpp")
                    && equalChecker.areEquivalent(o1.getFpp_omregnet(), o2.getFpp_omregnet(), checkedPairs, property + ".fpp_omregnet")
                    && equalChecker.areEquivalent(o1.getFppGaranti(), o2.getFppGaranti(), checkedPairs, property + ".fppGaranti")
                    && typeCtiChecker.areEquivalent(o1.getFppGarantiKode(), o2.getFppGarantiKode(), checkedPairs, property + ".fppGarantiKode")
                    && equalChecker.areEquivalent(o1.getOpt(), o2.getOpt(), checkedPairs, property + ".opt")
                    && equalChecker.areEquivalent(o1.getOpt_pa_e91(), o2.getOpt_pa_e91(), checkedPairs, property + ".opt_pa_e91")
                    && equalChecker.areEquivalent(o1.getOpt_pa_f92(), o2.getOpt_pa_f92(), checkedPairs, property + ".opt_pa_f92")
                    && equalChecker.areEquivalent(o1.getRedusertAntFppAr(), o2.getRedusertAntFppAr(), property + ".redusertAntFppAr")
                    && equalChecker.areEquivalent(o1.getSpt(), o2.getSpt(), checkedPairs, property + ".spt")
                    && equalChecker.areEquivalent(o1.getSpt_pa_e91(), o2.getSpt_pa_e91(), checkedPairs, property + ".spt_pa_e91")
                    && equalChecker.areEquivalent(o1.getSpt_pa_f92(), o2.getSpt_pa_f92(), checkedPairs, property + ".spt_pa_f92")
                    && typeCtiChecker.areEquivalent(o1.getUforeType(), o2.getUforeType(), checkedPairs, property + ".uforeType")
                    && equalChecker.areEquivalent(o1.getYpt_pa_e91(), o2.getYpt_pa_e91(), checkedPairs, property + ".ypt_pa_e91")
                    && equalChecker.areEquivalent(o1.getYpt_pa_f92(), o2.getYpt_pa_f92(), checkedPairs, property + ".ypt_pa_f92")
                    && equalChecker.areEquivalent(o1.getUfg(), o2.getUfg(), property + ".ufg")
                    && equalChecker.areEquivalent(o1.getUfgFom(), o2.getUfgFom(), checkedPairs, property + ".ufgFom")
                    && equalChecker.areEquivalent(o1.getUfgTom(), o2.getUfgTom(), checkedPairs, property + ".ufgTom")
                    && equalChecker.areEquivalent(o1.getUft(), o2.getUft(), checkedPairs, property + ".uft")
                    && equalChecker.areEquivalent(o1.getUftTom(), o2.getUftTom(), checkedPairs, property + ".uftTom")
                    && equalChecker.areEquivalent(o1.getVirk(), o2.getVirk(), checkedPairs, property + ".virk")
                    && equalChecker.areEquivalent(o1.getSpt_pa_e91_eos(), o2.getSpt_pa_e91_eos(), property + ".spt_pa_e91_eos")
                    && equalChecker.areEquivalent(o1.getSpt_pa_f92_eos(), o2.getSpt_pa_f92_eos(), property + ".spt_pa_f92_eos")
                    && equalChecker.areEquivalent(o1.getYpt(), o2.getYpt(), checkedPairs, property + ".ypt")
                    && equalChecker.areEquivalent(o1.getPaa(), o2.getPaa(), checkedPairs, property + ".paa");
        }
    }

    private static class TrygdetidChecker extends NullSafeChecker<Trygdetid> {
        @Override
        protected boolean haveEquivalentValues(Trygdetid o1, Trygdetid o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getFtt(), o2.getFtt(), property + ".ftt")
                    && equalChecker.areEquivalent(o1.getTt(), o2.getTt(), property + ".tt")
                    && equalChecker.areEquivalent(o1.getFtt_redusert(), o2.getFtt_redusert(), property + ".ftt_redusert")
                    && equalChecker.areEquivalent(o1.getOpptjeningsperiode(), o2.getOpptjeningsperiode(), property + ".opptjeningsperiode")
                    && equalChecker.areEquivalent(o1.getTt_67_70(), o2.getTt_67_70(), property + ".tt_67_70")
                    && equalChecker.areEquivalent(o1.getTt_E66(), o2.getTt_E66(), property + ".tt_E66")
                    && equalChecker.areEquivalent(o1.getTt_F67(), o2.getTt_F67(), property + ".tt_F67")
                    && equalChecker.areEquivalent(o1.getTt_fa_mnd(), o2.getTt_fa_mnd(), property + ".tt_fa_mnd")
                    && equalChecker.areEquivalent(o1.getTrygdetidId(), o2.getTrygdetidId(), property + ".trygdetidId")
                    && tTUtlandEOSChecker.areEquivalent(o1.getTtUtlandEos(), o2.getTtUtlandEos(), checkedPairs, property + ".ttUtlandEos")
                    && tTUtlandKonvensjonChecker.areEquivalent(o1.getTtUtlandKonvensjon(), o2.getTtUtlandKonvensjon(), checkedPairs, property + ".ttUtlandKonvensjon")
                    && merknadListChecker.areEquivalent(o1.getMerknadListe(), o2.getMerknadListe(), checkedPairs, property + ".merknadListe");
        }
    }

    private static class TTUtlandEOSChecker extends NullSafeChecker<TTUtlandEOS> {
        @Override
        protected boolean haveEquivalentValues(TTUtlandEOS o1, TTUtlandEOS o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getFtt_eos(), o2.getFtt_eos(), property + ".ftt_eos")
                    && equalChecker.areEquivalent(o1.getFtt_eos_redusert(), o2.getFtt_eos_redusert(), property + ".ftt_eos_redusert")
                    && equalChecker.areEquivalent(o1.getTt_eos_anv_ar(), o2.getTt_eos_anv_ar(), property + ".tt_eos_anv_ar")
                    && equalChecker.areEquivalent(o1.getTt_eos_anv_mnd(), o2.getTt_eos_anv_mnd(), property + ".tt_eos_anv_mnd")
                    && equalChecker.areEquivalent(o1.getTt_eos_nevner(), o2.getTt_eos_nevner(), property + ".tt_eos_nevner")
                    && equalChecker.areEquivalent(o1.getTt_eos_pro_rata_mnd(), o2.getTt_eos_pro_rata_mnd(), property + ".tt_eos_pro_rata_mnd")
                    && equalChecker.areEquivalent(o1.getTt_eos_teller(), o2.getTt_eos_teller(), property + ".tt_eos_teller")
                    && equalChecker.areEquivalent(o1.getTt_eos_teoretisk_mnd(), o2.getTt_eos_teoretisk_mnd(), property + ".tt_eos_teoretisk_mnd")
                    && equalChecker.areEquivalent(o1.getTt_konvensjon_ar(), o2.getTt_konvensjon_ar(), property + ".tt_konvensjon_ar")
                    && equalChecker.areEquivalent(o1.getTt_lik_pa(), o2.getTt_lik_pa(), property + ".tt_lik_pa")
                    && merknadListChecker.areEquivalent(o1.getMerknadListe(), o2.getMerknadListe(), checkedPairs, property + ".merknadListe");
        }
    }

    private static class TTUtlandKonvensjonChecker extends NullSafeChecker<TTUtlandKonvensjon> {
        @Override
        protected boolean haveEquivalentValues(TTUtlandKonvensjon o1, TTUtlandKonvensjon o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getFt_ar(), o2.getFt_ar(), property + ".ft_ar")
                    && equalChecker.areEquivalent(o1.getFtt_A10_brutto(), o2.getFtt_A10_brutto(), property + ".ftt_A10_brutto")
                    && equalChecker.areEquivalent(o1.getFtt_A10_netto(), o2.getFtt_A10_netto(), property + ".ftt_A10_netto")
                    && equalChecker.areEquivalent(o1.getFtt_A10_redusert(), o2.getFtt_A10_redusert(), property + ".ftt_A10_redusert")
                    && equalChecker.areEquivalent(o1.getTt_A10_anv_aar(), o2.getTt_A10_anv_aar(), property + ".tt_A10_anv_aar")
                    && equalChecker.areEquivalent(o1.getTt_A10_fa_mnd(), o2.getTt_A10_fa_mnd(), property + ".tt_A10_fa_mnd")
                    && equalChecker.areEquivalent(o1.getTt_A10_nevner(), o2.getTt_A10_nevner(), property + ".tt_A10_nevner")
                    && equalChecker.areEquivalent(o1.getTt_A10_teller(), o2.getTt_A10_teller(), property + ".tt_A10_teller")
                    && equalChecker.areEquivalent(o1.getTt_konvensjon_ar(), o2.getTt_konvensjon_ar(), property + ".tt_konvensjon_ar")
                    && equalChecker.areEquivalent(o1.getTt_lik_pa(), o2.getTt_lik_pa(), property + ".tt_lik_pa")
                    && merknadListChecker.areEquivalent(o1.getMerknadListe(), o2.getMerknadListe(), checkedPairs, property + ".merknadListe");
        }
    }

    private static class SluttpoengtallChecker extends NullSafeChecker<Sluttpoengtall> {
        // We get an unnecessary warning below.
        // Dit not want to camouflage it with @SuppressWarnings("unchecked")
        // Why does getMerknadListe() return just List and not List<Merknad>?

        @Override
        protected boolean haveEquivalentValues(Sluttpoengtall o1, Sluttpoengtall o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getFpp_grad_eos(), o2.getFpp_grad_eos(), property + ".fpp_grad_eos")
                    && equalChecker.areEquivalent(o1.getPt(), o2.getPt(), property + ".pt")
                    && equalChecker.areEquivalent(o1.getPoengTillegg(), o2.getPoengTillegg(), property + ".poengTillegg")
                    && poengrekkeChecker.areEquivalent(o1.getPoengrekke(), o2.getPoengrekke(), checkedPairs, property + "poengrekke")
                    && merknadListChecker.areEquivalent(o1.getMerknadListe(), o2.getMerknadListe(), checkedPairs, property + ".merknadListe");
        }
    }

    private static class PoengrekkeChecker extends NullSafeChecker<Poengrekke> {
        @Override
        protected boolean haveEquivalentValues(Poengrekke o1, Poengrekke o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getPa(), o2.getPa(), property + ".pa")
                    && equalChecker.areEquivalent(o1.getPa_e91(), o2.getPa_e91(), property + ".pa_e91")
                    && equalChecker.areEquivalent(o1.getPa_f92(), o2.getPa_f92(), property + ".pa_f92")
                    && equalChecker.areEquivalent(o1.getPa_fa_norge(), o2.getPa_fa_norge(), property + ".pa_fa_norge")
                    && equalChecker.areEquivalent(o1.getTpi(), o2.getTpi(), property + ".tpi")
                    && equalChecker.areEquivalent(o1.getPa_no(), o2.getPa_no(), property + ".pa_no")
                    && equalChecker.areEquivalent(o1.getSiste_fpp_aar(), o2.getSiste_fpp_aar(), property + ".siste_fpp_aar")
                    && equalChecker.areEquivalent(o1.getPa_nordisk_framt_brutto(), o2.getPa_nordisk_framt_brutto(), property + ".pa_nordisk_framt_brutto")
                    && equalChecker.areEquivalent(o1.getPa_nordisk_framt_netto(), o2.getPa_nordisk_framt_netto(), property + ".pa_nordisk_framt_netto")
                    && equalChecker.areEquivalent(o1.getTpi_faktor(), o2.getTpi_faktor(), property + "tpi_faktor")
                    && equalChecker.areEquivalent(o1.getPaa(), o2.getPaa(), property + "paa")
                    && equalChecker.areEquivalent(o1.getPa_eos_pro_rata(), o2.getPa_eos_pro_rata(), property + "pa_eos_pro_rata")
                    && equalChecker.areEquivalent(o1.getPa_eos_teoretisk(), o2.getPa_eos_teoretisk(), property + "pa_eos_teoretisk")
                    && equalChecker.areEquivalent(o1.getPa_fa_norden(), o2.getPa_fa_norden(), property + ".pa_fa_norden")
                    && equalChecker.areEquivalent(o1.getPa_pro_rata_teller(), o2.getPa_pro_rata_teller(), property + ".pa_pro_rata_teller")
                    && equalChecker.areEquivalent(o1.getPa_pro_rata_nevner(), o2.getPa_pro_rata_nevner(), property + ".pa_pro_rata_nevner")
                    && equalChecker.areEquivalent(o1.getFpa(), o2.getFpa(), property + ".fpa")
                    && poengtallListChecker.areEquivalent(o1.getPoengtallListe(), o2.getPoengtallListe(), checkedPairs, property + ".poengtallListe")
                    && merknadListChecker.areEquivalent(o1.getMerknadListe(), o2.getMerknadListe(), checkedPairs, property + ".merknadListe")
                    && framtidigPensjonspoengtallChecker.areEquivalent(o1.getFpp(), o2.getFpp(), checkedPairs, property + ".fpp")
                    && framtidigPensjonspoengtallChecker.areEquivalent(o1.getFpp_omregnet(), o2.getFpp_omregnet(), checkedPairs, property + ".fpp_omregnet");
        }
    }

    private static class FramtidigPensjonspoengtallChecker extends NullSafeChecker<FramtidigPensjonspoengtall> {
        @Override
        protected boolean haveEquivalentValues(FramtidigPensjonspoengtall o1, FramtidigPensjonspoengtall o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getPt(), o2.getPt(), property + ".pt")
                    && poengtallListChecker.areEquivalent(o1.getPoengtallListe(), o2.getPoengtallListe(), checkedPairs, property + ".poengtallListe")
                    && merknadListChecker.areEquivalent(o1.getMerknadListe(), o2.getMerknadListe(), checkedPairs, property + ".merknadListe");
        }
    }

    private static class PoengtallChecker extends NullSafeChecker<Poengtall> {
        @Override
        protected boolean haveEquivalentValues(Poengtall o1, Poengtall o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getAr(), o2.getAr(), property + ".ar")
                    && equalChecker.areEquivalent(o1.getBruktIBeregning(), o2.getBruktIBeregning(), property + ".bruktIBeregning")
                    && equalChecker.areEquivalent(o1.getPi(), o2.getPi(), property + ".pi")
                    && equalChecker.areEquivalent(o1.getPia(), o2.getPia(), property + ".pia")
                    && equalChecker.areEquivalent(o1.getPp(), o2.getPp(), property + ".pp")
                    && equalChecker.areEquivalent(o1.getGv(), o2.getGv(), property + ".gv")
                    && equalChecker.areEquivalent(o1.getMaksUforegrad(), o2.getMaksUforegrad(), property + ".maksUforegrad")
                    && typeCtiChecker.areEquivalent(o1.getPoengtallType(), o2.getPoengtallType(), checkedPairs, property + ".poengtallType")
                    && merknadListChecker.areEquivalent(o1.getMerknadListe(), o2.getMerknadListe(), checkedPairs, property + ".merknadListe");
        }
    }

    private static class YtelseskomponentChecker<T extends Ytelseskomponent> extends NullSafeChecker<T> {
        @Override
        protected boolean haveEquivalentValues(T o1, T o2, Set<CheckedPair> checkedPairs, String property) {
            return equalChecker.areEquivalent(o1.getClass(), o2.getClass(), checkedPairs, property)
                    && equalChecker.areEquivalent(o1.getBrutto(), o2.getBrutto(), property + ".brutto")
                    && equalChecker.areEquivalent(o1.getNetto(), o2.getNetto(), property + ".netto");
//                    && equalChecker.areEquivalent(o1.getFradrag(), o2.getFradrag(), checkedPairs, property + ".fradrag")
//                    && merknadListChecker.areEquivalent(o1.getMerknadListe(), o2.getMerknadListe(), checkedPairs, property + ".merknadListe");
        }
    }

    private static class EqualChecker extends NullSafeChecker<Object> {
        @Override
        protected boolean haveEquivalentValues(Object o1, Object o2, Set<CheckedPair> checkedPairs, String property) {
            if (!o1.equals(o2)) {
                if (log.isDebugEnabled()) {
                    log.debug(property + " " + o1 + " != " + o2);
                }
                return false;
            } else {
                return true;
            }
        }

        protected boolean areEquivalent(boolean o1, boolean o2, String property) {
            if (o1 != o2) {
                if (log.isDebugEnabled()) {
                    log.debug(property + " " + o1 + " != " + o2);
                }
                return false;
            } else {
                return true;
            }
        }
        protected boolean areEquivalent(int o1, int o2, String property) {
            if (o1 != o2) {
                if (log.isDebugEnabled()) {
                    log.debug(property + " " + o1 + " != " + o2);
                }
                return false;
            } else {
                return true;
            }
        }

        protected boolean areEquivalent(long o1, long o2, String property) {
            if (o1 != o2) {
                if (log.isDebugEnabled()) {
                    log.debug(property + " " + o1 + " != " + o2);
                }
                return false;
            } else {
                return true;
            }
        }

        protected boolean areEquivalent(double o1, double o2, String property) {
            if (o1 != o2) {
                if (log.isDebugEnabled()) {
                    log.debug(property + " " + o1 + " != " + o2);
                }
                return false;
            } else {
                return true;
            }
        }
    }

    private static class ListChecker<T> extends NullSafeChecker<List<? extends T>> {
        private final NullSafeChecker<T> itemChecker;

        protected ListChecker(NullSafeChecker<T> itemChecker) {
            this.itemChecker = itemChecker;
        }

        // NOTE!!!
        // Lists are only considered equivalent here if equivalent pairs have same position in both lists.
        // (Ie. first item in list 1 must be equivalent to first item in list 2 etc)

        @Override
        protected boolean haveEquivalentValues(List<? extends T> o1, List<? extends T> o2, Set<CheckedPair> checkedPairs, String property) {
            if (o1.size() != o2.size()) {
                return false;
            }

            Iterator<? extends T> i1 = o1.iterator();
            Iterator<? extends T> i2 = o2.iterator();

            // Only need to check hasNext() on one Iterator because we just verified that sizes are equal.
            while (i1.hasNext()) {
                if (!itemChecker.areEquivalent(i1.next(), i2.next(), checkedPairs, property)) {
                    return false;
                }
            }

            return true;
        }
    }

    // Common superclass for all checkers:
    private static abstract class NullSafeChecker<T> {
        protected abstract boolean haveEquivalentValues(T o1, T o2, Set<CheckedPair> checkedPairs, String property);

        protected boolean areEquivalent(T o1, T o2, Set<CheckedPair> checkedPairs, String property) {
            // Equivalent if same instance, or if both are null:

            if (o1 == o2) {
                return true;
            }

            // Not equivalent if just one is null:

            if (o1 == null || o2 == null) {
                return false;
            }

            // Different instances - neither is null.

            if (checkedPairs.add(new CheckedPair(o1, o2))) {
                // Do required comparisons of individual instance members
                // if this is the first time we hit this combination.

                return haveEquivalentValues(o1, o2, checkedPairs, property);
            } else {
                // Comparison already started elsewhere.
                //
                // 1) It may completed.
                // Instances are equivalent, or we would not be here now.
                //
                // 2) It may be in progress if we arrived here on a circular reference path.
                // Break the loop and assume equivalence for now.
                // Rely on the initial comparison to return correct result when completed.

                return true;
            }
        }
    }
}
