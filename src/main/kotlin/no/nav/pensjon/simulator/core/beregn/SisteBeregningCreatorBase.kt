package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.BeregningsmetodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak

// AbstraktOpprettSisteAldersberegning, OpprettSisteAldersberegningCommon, OpprettSisteBeregningCommand
abstract class SisteBeregningCreatorBase(private val context: SimulatorContext) {

    // OpprettSisteBeregningCommand.execute
    abstract fun createBeregning(
        input: SisteBeregningSpec,
        beregningsresultat: AbstraktBeregningsResultat?
    ): SisteBeregning

    // AbstraktOpprettSisteAldersberegning.populateCommonValuesOnSisteBeregning
    protected fun populate(
        sink: SisteBeregning,
        source: SisteBeregningSpec,
        beregningsresultat: AbstraktBeregningsResultat?
    ) {
        val kravhode = source.forrigeKravhode
        setAvdodGrunnlag(sink, avdodGrunnlag(kravhode))
        setEpsGrunnlag(sink, epsGrunnlag(beregningsresultat, kravhode))
        setSivilstand(sink, sokerDetalj(kravhode))
        setEktefelletilleggVedtak(sink, source.filtrertVilkarsvedtakList)
    }

    // OpprettSisteAldersberegningCommon.setAnvendtGjenlevenderettVedtakOnSisteBeregning
    protected fun setAnvendtGjenlevenderettVedtak(sink: SisteBeregning?, vedtakList: List<VilkarsVedtak>) {
        vedtakList
            .firstOrNull { KravlinjeTypeEnum.GJR == it.kravlinjeTypeEnum }
            ?.let { setAnvendtGjenlevenderettVedtak(sink, it) }
    }

    // OpprettSisteAldersberegning2011Regelverk2025.addAltKonv
    // OpprettSisteAldersberegning2016Regelverk2016.addAltKonv
    protected fun setAlternativKonvensjonData(
        sink: SisteAldersberegning2011,
        beregningKapittel20: Beregning2011,
        vinnendeMetode: BeregningsmetodeEnum?
    ) {
        val tapendeDelberegning = findTapendeDelberegning(beregningKapittel20, vinnendeMetode) ?: return

        with(sink) {
            pensjonUnderUtbetaling2025AltKonv =
                utenIrrelevanteYtelseskomponenter(tapendeDelberegning.pensjonUnderUtbetaling)
            beholdningerAltKonv = tapendeDelberegning.beholdninger
            prorataBrok_kap_20AltKonv = tapendeDelberegning.prorataBrok
            tt_anv_kap_20AltKonv = tapendeDelberegning.tt_anv
        }
    }

    // OpprettSisteAldersberegningCommon.fetchFilteredPensjonUnderUtbetaling (2 variants)
    // OpprettSisteAldersberegningCommon.getFilterPensjonUnderUtbetaling
    protected fun utenIrrelevanteYtelseskomponenter(pensjon: PensjonUnderUtbetaling?) =
        pensjon?.let {
            PensjonUnderUtbetaling(
                source = it,
                excludeBrutto = true
            ).also(::removeIrrelevanteYtelseskomponenter)
        }

    private fun removeIrrelevanteYtelseskomponenter(pensjon: PensjonUnderUtbetaling) {
        pensjon.removeYtelseskomponent { it.brukt.not() || it.opphort }
    }

    // Extracted
    private fun avdodGrunnlag(kravhode: Kravhode?): Persongrunnlag? =
        kravhode?.hentPersongrunnlagForRolle(GrunnlagsrolleEnum.AVDOD, false)

    // Extracted
    private fun sokerDetalj(kravhode: Kravhode?): PersonDetalj? = kravhode?.findPersonDetaljIBruk(GrunnlagsrolleEnum.SOKER)

    // OpprettSisteAldersberegningCommon.findDelberegning (predicate from addAltKonv)
    private fun findTapendeDelberegning(
        beregning: Beregning2011,
        vinnendeMetode: BeregningsmetodeEnum?
    ): AldersberegningKapittel20? =
        beregning.delberegning2011Liste
            .mapNotNull { it.beregning2011 as? AldersberegningKapittel20 }
            .firstOrNull { it.beregningsMetodeEnum != vinnendeMetode }

    // OpprettSisteAldersberegningCommon.fetchEpsPersongrunnlag
    /**
     * Henter eksisterende persongrunnlag for ektefelle/partner/samboer (EPS) fra databasen.
     * Det vil være et eksisterende grunnlag f.eks. dersom personen har startet uttak av pensjon, og simulerer en endring av denne.
     */
    private fun epsGrunnlag(
        eksisterendeResultat: AbstraktBeregningsResultat?,
        endringKravhode: Kravhode?
    ): Persongrunnlag? {
        val eksisterendeEpsGrunnlag: Persongrunnlag? = eksisterendeResultat?.let(this::fetchEpsGrunnlag)

        return if (eksisterendeEpsGrunnlag != null && endringKravhode?.harGjenlevenderettighet() == true) {
            Persongrunnlag(eksisterendeEpsGrunnlag) // EPS-grunnlag fra eksisterende (forrige) krav
        } else {
            endringKravhode?.let(::getEpsGrunnlag) // EPS-grunnlag fra endringskrav (nåværende)
        }
    }

    // OpprettSisteAldersberegningCommon.fetchPersongrunnlagFraForrigeKrav
    private fun fetchEpsGrunnlag(beregningsresultat: AbstraktBeregningsResultat): Persongrunnlag? {
        /*ANON
        val kravhode: Kravhode? = beregningsresultat.kravId?.let(context::fetchKravhode)

        val periodisertKravhode: Kravhode? =
            kravhode?.let {
                periodiserGrunnlag(
                    virkFom = beregningsresultat.virkFom,
                    virkTom = null,
                    originalKravhode = it,
                    periodiserFomTomDatoUtenUnntak = true,
                    sakType = null
                )
            }

        return periodisertKravhode?.let(::getEpsGrunnlag)
        */
        return null
    }

    // OpprettSisteAldersberegningCommon.getEpsPersongrunnlag
    private fun getEpsGrunnlag(kravhode: Kravhode): Persongrunnlag? =
        kravhode.persongrunnlagListe.firstOrNull(::epsIsAmongDetaljer)

    // Extracted from OpprettSisteAldersberegningCommon.getEpsPersongrunnlag
    private fun epsIsAmongDetaljer(grunnlag: Persongrunnlag): Boolean = grunnlag.personDetaljListe.any { it.isEps() }

    // OpprettSisteAldersberegningCommon.setAnvendtGjenlevenderettVedtak
    private fun setAnvendtGjenlevenderettVedtak(sink: SisteBeregning?, vedtak: VilkarsVedtak?) {
        when (sink) {
            is SisteAldersberegning2016 -> sink.anvendtGjenlevenderettVedtak = vedtak
            is SisteAldersberegning2011 -> sink.anvendtGjenlevenderettVedtak = vedtak
            else -> throw RuntimeException("Unexpected type of SisteBeregning: $sink")
        }
    }

    // OpprettSisteAldersberegningCommon.setAvdodesPersongrunnlagForSisteBeregning
    private fun setAvdodGrunnlag(sink: SisteBeregning, avdodGrunnlag: Persongrunnlag?) {
        if (avdodGrunnlag == null) return

        // Fix to avoid Hibernate trouble
        //fixUpPeriodisertPersongrunnlag(avdodesPersongrunnlag)

        when (sink) {
            is SisteAldersberegning2016 -> sink.avdodesPersongrunnlag = avdodGrunnlag
            is SisteAldersberegning2011 -> sink.avdodesPersongrunnlag = avdodGrunnlag
            else -> throw RuntimeException("Unexpected type of SisteBeregning: $sink")
        }
    }

    // OpprettSisteAldersberegningCommon.setEpsPersongrunnlagForSisteBeregning
    private fun setEpsGrunnlag(sink: SisteBeregning, epsGrunnlag: Persongrunnlag?) {
        if (epsGrunnlag == null) return

        when (sink) {
            is SisteAldersberegning2016 -> sink.eps = epsGrunnlag
            is SisteAldersberegning2011 -> sink.eps = epsGrunnlag
            else -> throw RuntimeException("Unexpected type of SisteBeregning: $sink")
        }
    }

    // OpprettSisteAldersberegningCommon.setSivilstandTypeForSisteBeregning
    private fun setSivilstand(sink: SisteBeregning, soekerDetalj: PersonDetalj?) {
        soekerDetalj?.sivilstandTypeEnum?.let { sink.sivilstandTypeEnum = it }
    }

    // OpprettSisteAldersberegningCommon.setVilkarsvedtakEktefelletilleggForSisteBeregning
    private fun setEktefelletilleggVedtak(sink: SisteBeregning, vedtakListe: List<VilkarsVedtak>) {
        vedtakListe
            .firstOrNull { KravlinjeTypeEnum.ET == it.kravlinjeTypeEnum }
            ?.let { setEktefelletilleggVedtak(sink, it) }
    }

    // Extracted from OpprettSisteAldersberegningCommon.setVilkarsvedtakEktefelletilleggForSisteBeregning
    private fun setEktefelletilleggVedtak(sink: SisteBeregning, vedtak: VilkarsVedtak) {
        when (sink) {
            is SisteAldersberegning2016 -> sink.vilkarsvedtakEktefelletillegg = vedtak
            is SisteAldersberegning2011 -> sink.vilkarsvedtakEktefelletillegg = vedtak
            else -> throw RuntimeException("Unexpected type of SisteBeregning: $sink")
        }
    }
}
