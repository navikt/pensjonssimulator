package no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktAvkortingsinformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AldersberegningKapittel19
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AvkortingsinformasjonBT
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AvkortingsinformasjonUT
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.FremtidigBarnetilleggperiode
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.PensjonUnderUtbetaling
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.TapendeBeregningsmetode
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.TidligereBarnetilleggperiode
import no.nav.pensjon.simulator.core.domain.regler.enum.Beregningsarsak
import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import java.util.Date

fun AvkortingsinformasjonBT.copy() =
    AvkortingsinformasjonBT().also {
        it.fribelopVedVirk = this.fribelopVedVirk
        it.restTilUtbetalingForJustering = this.restTilUtbetalingForJustering
        it.avviksbelop = this.avviksbelop
        it.justeringsbelopUbegrensetPerAr = this.justeringsbelopUbegrensetPerAr
        it.justeringsbelopPerAr = this.justeringsbelopPerAr
        it.forventetEtteroppgjor = this.forventetEtteroppgjor
        it.inntektPeriodisert = this.inntektPeriodisert
        it.fribelopPeriodisert = this.fribelopPeriodisert
        it.avviksjusteringTypeEnum = this.avviksjusteringTypeEnum
        it.barnetilleggPeriodeListe =
            this.barnetilleggPeriodeListe.filterIsInstance<TidligereBarnetilleggperiode>()
                .map(::TidligereBarnetilleggperiode).toMutableList()
        it.barnetilleggPeriodeListe.addAll(
            this.barnetilleggPeriodeListe.filterIsInstance<FremtidigBarnetilleggperiode>()
                .map(::FremtidigBarnetilleggperiode)
        )
        copyAvkortingsinformasjon(source = this, target = it)
    }

fun AvkortingsinformasjonUT.copy() =
    AvkortingsinformasjonUT().also {
        it.oifu = this.oifu
        it.oieu = this.oieu
        it.belopsgrense = this.belopsgrense
        it.inntektsgrense = this.inntektsgrense
        it.ugradertBruttoPerAr = this.ugradertBruttoPerAr
        it.kompensasjonsgrad = this.kompensasjonsgrad
        it.utbetalingsgrad = this.utbetalingsgrad
        it.forventetInntekt = this.forventetInntekt
        it.inntektsgrenseNesteAr = this.inntektsgrenseNesteAr
        it.inntektstakNesteAr = this.inntektstakNesteAr
        it.differansebelop = this.differansebelop
        it.oifuForBarnetillegg = this.oifuForBarnetillegg
        copyAvkortingsinformasjon(source = this, target = it)
    }

fun BeregningsInformasjon.copy() =
    BeregningsInformasjon().also {
        it.forholdstallUttak = this.forholdstallUttak
        it.forholdstall67 = this.forholdstall67
        it.delingstallUttak = this.delingstallUttak
        it.delingstall67 = this.delingstall67
        it.spt = this.spt?.let(::Sluttpoengtall)
        it.opt = this.opt?.let(::Sluttpoengtall)
        it.ypt = this.ypt?.let(::Sluttpoengtall)
        it.grunnpensjonAvkortet = this.grunnpensjonAvkortet
        it.merknadListe = this.merknadListe.map { it.copy() }
        it.mottarMinstePensjonsniva = this.mottarMinstePensjonsniva
        it.minstepensjonArsak = this.minstepensjonArsak
        it.rettPaGjenlevenderett = this.rettPaGjenlevenderett
        it.gjenlevenderettAnvendt = this.gjenlevenderettAnvendt
        it.avdodesTilleggspensjonBrukt = this.avdodesTilleggspensjonBrukt
        it.avdodesTrygdetidBrukt = this.avdodesTrygdetidBrukt
        it.ungUfor = this.ungUfor
        it.ungUforAnvendt = this.ungUforAnvendt
        it.yrkesskadeRegistrert = this.yrkesskadeRegistrert
        it.yrkesskadeAnvendt = this.yrkesskadeAnvendt
        it.yrkesskadegrad = this.yrkesskadegrad
        it.penPerson = this.penPerson?.let(::PenPerson)
        it.beregningsMetodeEnum = this.beregningsMetodeEnum
        it.eksport = this.eksport
        it.resultatTypeEnum = this.resultatTypeEnum
        it.tapendeBeregningsmetodeListe = this.tapendeBeregningsmetodeListe.map { it.copy() }
        it.trygdetid = this.trygdetid
        it.tt_anv = this.tt_anv
        it.vurdertBosattlandEnum = this.vurdertBosattlandEnum
        it.ensligPensjonInstOpph = this.ensligPensjonInstOpph
        it.instOppholdTypeEnum = this.instOppholdTypeEnum
        it.instOpphAnvendt = this.instOpphAnvendt
        it.tp = this.tp
        it.ttBeregnetForGrunnlagsrolle = this.ttBeregnetForGrunnlagsrolle
        it.ungUforGarantiFrafalt = this.ungUforGarantiFrafalt
        //--- Extra:
        it.epsMottarPensjon = this.epsMottarPensjon
        it.epsOver2G = this.epsOver2G
        it.unclearedDelingstallUttak = this.unclearedDelingstallUttak
        it.unclearedDelingstall67 = this.unclearedDelingstall67
    }

fun BeregningsResultatAlderspensjon2011.copy() =
    BeregningsResultatAlderspensjon2011().also {
        it.pensjonUnderUtbetalingUtenGJR = this.pensjonUnderUtbetalingUtenGJR?.let(::PensjonUnderUtbetaling)
        it.beregningsInformasjonKapittel19 = this.beregningsInformasjonKapittel19?.copy()
        it.beregningsInformasjonAvdod = this.beregningsInformasjonAvdod?.copy()
        it.beregningKapittel19 = this.beregningKapittel19?.let(::AldersberegningKapittel19)
        copyBeregningsResultat(source = this, target = it)
    }

fun TapendeBeregningsmetode.copy() =
    TapendeBeregningsmetode().also {
        it.beregningMetodeTypeEnum = this.beregningMetodeTypeEnum
    }

private fun copyAvkortingsinformasjon(source: AbstraktAvkortingsinformasjon, target: AbstraktAvkortingsinformasjon) {
    target.antallMndFor = source.antallMndFor
    target.antallMndEtter = source.antallMndEtter
    target.inntektstak = source.inntektstak
    target.avkortingsbelopPerAr = source.avkortingsbelopPerAr
    target.restTilUtbetaling = source.restTilUtbetaling
    target.inntektsavkortingTypeEnum = source.inntektsavkortingTypeEnum
}

private fun copyBeregningsResultat(source: AbstraktBeregningsResultat, target: AbstraktBeregningsResultat) {
    target.virkFom = source.virkFom?.clone() as? Date
    target.virkTom = source.virkTom?.clone() as? Date
    target.merknadListe = source.merknadListe.map(::Merknad).toMutableList()
    target.pensjonUnderUtbetaling = source.pensjonUnderUtbetaling?.let(::PensjonUnderUtbetaling)
    target.brukersSivilstandEnum = source.brukersSivilstandEnum
    target.benyttetSivilstandEnum = source.benyttetSivilstandEnum
    target.beregningArsakEnum = source.beregningArsakEnum
    target.lonnsvekstInformasjon = source.lonnsvekstInformasjon?.let(::LonnsvekstInformasjon)
    target.uttaksgrad = source.uttaksgrad
    target.gjennomsnittligUttaksgradSisteAr = source.gjennomsnittligUttaksgradSisteAr
    target.kravId = source.kravId
    target.epsMottarPensjon = source.epsMottarPensjon
    target.epsPaavirkerBeregning = source.epsPaavirkerBeregning
    target.harGjenlevenderett = source.harGjenlevenderett
}
