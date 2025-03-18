package no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning.copyYtelseskomponent
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import java.util.*

fun AfpPrivatBeregning.copy() =
    AfpPrivatBeregning().also {
        it.afpPrivatLivsvarig = this.afpPrivatLivsvarig
        it.afpKompensasjonstillegg = this.afpKompensasjonstillegg
        it.afpKronetillegg = this.afpKronetillegg
        it.afpOpptjening = this.afpOpptjening
        copyBeregning2011(source = this, target = it)
    }

fun AfpPrivatLivsvarig.copy() =
    AfpPrivatLivsvarig().also {
        it.justeringsbelop = this.justeringsbelop
        it.afpProsentgrad = this.afpProsentgrad
        it.afpForholdstall = this.afpForholdstall
        copyAfpLivsvarig(source = this, target = it)
    }

fun AldersberegningKapittel19.copy() =
    AldersberegningKapittel19().also {
        it.restpensjon = this.restpensjon?.let(::Basispensjon)
        it.basispensjon = this.basispensjon?.let(::Basispensjon)
        it.basispensjonUtenGJR = this.basispensjonUtenGJR?.let(::Basispensjon)
        it.restpensjonUtenGJR = this.restpensjonUtenGJR?.let(::Basispensjon)
        it.forholdstall = this.forholdstall
        it.ftBenyttetArsakListe = this.ftBenyttetArsakListe.map { it.copy() }.toMutableList()
        copyBeregning2011(source = this, target = it)
    }

fun AldersberegningKapittel20.copy() =
    AldersberegningKapittel20().also {
        it.delingstall = this.delingstall
        it.beholdninger = this.beholdninger?.copy()
        it.pensjonUnderUtbetaling = this.pensjonUnderUtbetaling?.let(::PensjonUnderUtbetaling)
        it.dtBenyttetArsakListe = this.dtBenyttetArsakListe.map { it.copy() }
        it.beholdningerForForsteuttak = this.beholdningerForForsteuttak
        it.prorataBrok = this.prorataBrok?.let(::Brok)
        copyBeregning2011(source = this, target = it)
    }

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
        it.beregningKapittel19 = this.beregningKapittel19?.copy()
        copyBeregningsResultat(source = this, target = it)
    }

fun FremskrivingsDetaljer.copy() =
    FremskrivingsDetaljer().also {
        it.justeringTomDato = this.justeringTomDato?.clone() as? Date
        it.justeringsfaktor = this.justeringsfaktor
        it.teller = this.teller
        it.nevner = this.nevner
        it.arskull = this.arskull
    }

fun FtDtArsak.copy() =
    FtDtArsak().also {
        it.ftDtArsakEnum = this.ftDtArsakEnum
    }

fun GarantitilleggInformasjon.copy() =
    GarantitilleggInformasjon().also {
        it.anvendtSivilstandEnum = this.anvendtSivilstandEnum
        it.pensjonsbeholdningBelopVed67 = this.pensjonsbeholdningBelopVed67
        it.garantipensjonsbeholdningBelopVed67 = this.garantipensjonsbeholdningBelopVed67
        it.tt_kapittel20Ved67 = this.tt_kapittel20Ved67
        it.tt_kapittel19Ved67 = this.tt_kapittel19Ved67
        it.pa_f92Ved67 = this.pa_f92Ved67
        it.pa_e91Ved67 = this.pa_e91Ved67
        it.ftVed67 = this.ftVed67
        it.dtVed67 = this.dtVed67
        it.sptVed67 = this.sptVed67
        it.tt_2009 = this.tt_2009
        it.pa_f92_2009 = this.pa_f92_2009
        it.pa_e91_2009 = this.pa_e91_2009
        it.spt_2009 = this.spt_2009
        it.ft67_1962 = this.ft67_1962
        it.dt67_1962 = this.dt67_1962
        it.estimertTTkap19Benyttet = this.estimertTTkap19Benyttet
    }

fun JusteringsInformasjon.copy() =
    JusteringsInformasjon().also {
        it.totalJusteringsfaktor = this.totalJusteringsfaktor
        it.justeringsTypeEnum = this.justeringsTypeEnum
        it.elementer = this.elementer.map(::copyJustering).toMutableList()
    }

fun JustertGarantipensjonsniva.copy() =
    JustertGarantipensjonsniva().also {
        it.garantipensjonsniva = this.garantipensjonsniva
        it.justeringsInformasjon = this.justeringsInformasjon
        it.belop = this.belop
    }

fun LonnsvekstDetaljer.copy() =
    LonnsvekstDetaljer().also {
        it.justeringTomDato = this.justeringTomDato?.clone() as? Date
        it.justeringsfaktor = this.justeringsfaktor
        it.lonnsvekst = this.lonnsvekst
    }

fun TapendeBeregningsmetode.copy() =
    TapendeBeregningsmetode().also {
        it.beregningMetodeTypeEnum = this.beregningMetodeTypeEnum
    }

fun Uforetrygdberegning.copy() =
    Uforetrygdberegning().also {
        it.bruttoPerAr = this.bruttoPerAr
        it.formelKodeEnum = this.formelKodeEnum
        it.grunnbelop = this.grunnbelop
        it.minsteytelse = this.minsteytelse?.let(::Minsteytelse)
        it.prorataBrok = this.prorataBrok?.let(::Brok)
        it.uforegrad = this.uforegrad
        it.uforetidspunkt = this.uforetidspunkt?.clone() as? Date
        it.egenopptjentUforetrygd = this.egenopptjentUforetrygd?.let(::EgenopptjentUforetrygd)
        it.egenopptjentUforetrygdBest = this.egenopptjentUforetrygdBest
        it.yrkesskadegrad = this.yrkesskadegrad
        it.yrkesskadetidspunkt = this.yrkesskadetidspunkt?.clone() as? Date
        it.mottarMinsteytelse = this.mottarMinsteytelse
        it.minsteytelseArsak = this.minsteytelseArsak
        it.instOppholdTypeEnum = this.instOppholdTypeEnum
        it.instOpphAnvendt = this.instOpphAnvendt
        it.uforeEkstra = this.uforeEkstra?.let(::UforeEkstraUT)
        it.ytelseVedDodEnum = this.ytelseVedDodEnum
        copyBeregning2011(source = this, target = it)
    }

private fun copyAvkortingsinformasjon(source: AbstraktAvkortingsinformasjon, target: AbstraktAvkortingsinformasjon) {
    target.antallMndFor = source.antallMndFor
    target.antallMndEtter = source.antallMndEtter
    target.inntektstak = source.inntektstak
    target.avkortingsbelopPerAr = source.avkortingsbelopPerAr
    target.restTilUtbetaling = source.restTilUtbetaling
    target.inntektsavkortingTypeEnum = source.inntektsavkortingTypeEnum
}

private fun copyAfpLivsvarig(
    source: AbstraktAfpLivsvarig,
    target: AbstraktAfpLivsvarig
) {
    copyYtelseskomponent(source, target)
}

private fun copyBeregning2011(
    source: Beregning2011,
    target: Beregning2011,
    copyDelberegninger: Boolean = true
) {
    target.gjelderPerson = source.gjelderPerson?.penPersonId?.let(::PenPerson)
    target.grunnbelop = source.grunnbelop
    target.tt_anv = source.tt_anv
    target.resultatTypeEnum = source.resultatTypeEnum
    target.beregningsMetodeEnum = source.beregningsMetodeEnum
    target.beregningTypeEnum = source.beregningTypeEnum
    target.merknadListe = source.merknadListe.map(::Merknad)
    target.beregningGjelderTypeEnum = source.beregningGjelderTypeEnum

    if (copyDelberegninger) {
        target.delberegning2011Liste = source.delberegning2011Liste.map(::BeregningRelasjon)
    }
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
    target.beregningsinformasjon = source.beregningsinformasjon?.copy()
}

private fun copyJustering(source: IJustering): IJustering =
    when (source) {
        is LonnsvekstDetaljer -> source.copy()
        is FremskrivingsDetaljer -> source.copy()
        is GReguleringDetaljer -> throw RuntimeException("Deprecated IJustering type: GReguleringDetaljer")
        else -> throw RuntimeException("Unsupported IJustering type: ${source.javaClass.name}")
    }
