package no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok
import no.nav.pensjon.simulator.core.domain.regler.util.formula.Formel
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning.copyYtelseskomponent
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.util.copy
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktAfpLivsvarig as LivsvarigAfp
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarig as LivsvarigOffentligAfp
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag as LivsvarigOffentligAfpGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatBeregning as PrivatAfpBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig as PrivatAfp

fun AfpKompensasjonstillegg.copy() =
    AfpKompensasjonstillegg().also {
        it.referansebelop = this.referansebelop
        it.reduksjonsbelop = this.reduksjonsbelop
        it.forholdstallKompensasjonstillegg = this.forholdstallKompensasjonstillegg
        copyYtelseskomponent(source = this, target = it)
    }

fun AfpKronetillegg.copy() =
    AfpKronetillegg().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun AldersberegningKapittel19.copy() =
    AldersberegningKapittel19().also {
        it.restpensjon = this.restpensjon?.copy()
        it.basispensjon = this.basispensjon?.copy()
        it.basispensjonUtenGJR = this.basispensjonUtenGJR?.copy()
        it.restpensjonUtenGJR = this.restpensjonUtenGJR?.copy()
        it.forholdstall = this.forholdstall
        it.ftBenyttetArsakListe = this.ftBenyttetArsakListe.map { o -> o.copy() }.toMutableList()
        copyBeregning2011(source = this, target = it)
    }

fun AldersberegningKapittel20.copy() =
    AldersberegningKapittel20().also {
        it.delingstall = this.delingstall
        it.beholdninger = this.beholdninger?.copy()
        it.pensjonUnderUtbetaling = this.pensjonUnderUtbetaling?.let(::PensjonUnderUtbetaling)
        it.dtBenyttetArsakListe = this.dtBenyttetArsakListe.map { o -> o.copy() }
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
            this.barnetilleggPeriodeListe.filterIsInstance<TidligereBarnetilleggperiode>().map { o -> o.copy() }
                .toMutableList()
        it.barnetilleggPeriodeListe.addAll(
            this.barnetilleggPeriodeListe.filterIsInstance<FremtidigBarnetilleggperiode>().map { o -> o.copy() })
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

fun BarnetilleggFellesbarnUT.copy() =
    BarnetilleggFellesbarnUT().also {
        it.belopFratrukketAnnenForeldersInntekt = this.belopFratrukketAnnenForeldersInntekt
        it.brukersInntektTilAvkortning = this.brukersInntektTilAvkortning
        it.inntektAnnenForelder = this.inntektAnnenForelder
        it.annenForelderUforetrygdForJustering = this.annenForelderUforetrygdForJustering
        copyBarnetilleggUT(source = this, target = it)
    }

fun BarnetilleggSerkullsbarnUT.copy() =
    BarnetilleggSerkullsbarnUT().also {
        it.brukersGjenlevendetilleggForJustering = this.brukersGjenlevendetilleggForJustering
        copyBarnetilleggUT(source = this, target = it)
    }

fun Basispensjon.copy() =
    Basispensjon().also {
        it.totalbelop = this.totalbelop
        it.gp = this.gp?.let(::BasisGrunnpensjon)
        it.tp = this.tp?.let(::BasisTilleggspensjon)
        it.pt = this.pt?.let(::BasisPensjonstillegg)
        it.formelKodeEnum = this.formelKodeEnum
    }

fun BeregningsgrunnlagKonvertert.copy() =
    BeregningsgrunnlagKonvertert().also {
        it.skattekompensertbelop = this.skattekompensertbelop?.copy()
        it.inntektVedSkadetidspunkt = this.inntektVedSkadetidspunkt
        copyBeregningsgrunnlag(source = this, target = it)
    }

fun BeregningsgrunnlagOrdiner.copy() =
    BeregningsgrunnlagOrdiner().also {
        it.opptjeningUTListe = this.opptjeningUTListe.map(::OpptjeningUT)
        it.nasjonaltSnitt = this.nasjonaltSnitt
        it.sluttpoengtall = this.sluttpoengtall
        copyBeregningsgrunnlag(source = this, target = it)
    }

fun BeregningsgrunnlagYrkesskade.copy() =
    BeregningsgrunnlagYrkesskade().also {
        it.inntektVedSkadetidspunkt = this.inntektVedSkadetidspunkt
        it.sluttpoengtall = this.sluttpoengtall
        copyBeregningsgrunnlag(source = this, target = it)
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
        it.merknadListe = this.merknadListe.map { o -> o.copy() }
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
        it.tapendeBeregningsmetodeListe = this.tapendeBeregningsmetodeListe.map { o -> o.copy() }
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

fun BeregningsResultatAfpPrivat.copy() =
    BeregningsResultatAfpPrivat().also {
        it.afpPrivatBeregning = this.afpPrivatBeregning?.copy()
        copyBeregningsResultat(source = this, target = it)
    }

// PEN: BeregningsresultatAfpPrivat.hentLivsvarigDelIBruk
fun BeregningsResultatAfpPrivat.privatAfp(): PrivatAfp? =
    this.pensjonUnderUtbetaling?.ytelseskomponenter.orEmpty().let(::privatAfp)

fun BeregningsResultatAlderspensjon2011.copy() =
    BeregningsResultatAlderspensjon2011().also {
        it.pensjonUnderUtbetalingUtenGJR = this.pensjonUnderUtbetalingUtenGJR?.let(::PensjonUnderUtbetaling)
        it.beregningsInformasjonKapittel19 = this.beregningsInformasjonKapittel19?.copy()
        it.beregningsInformasjonAvdod = this.beregningsInformasjonAvdod?.copy()
        it.beregningKapittel19 = this.beregningKapittel19?.copy()
        copyBeregningsResultat(source = this, target = it)
    }

fun BeregningsResultatAlderspensjon2016.copy() =
    BeregningsResultatAlderspensjon2016().also {
        it.andelKapittel19 = this.andelKapittel19
        it.beregningsResultat2011 = this.beregningsResultat2011?.copy()
        it.beregningsResultat2025 = this.beregningsResultat2025?.copy()
        copyBeregningsResultat(source = this, target = it)
    }

fun BeregningsResultatAlderspensjon2025.copy() =
    BeregningsResultatAlderspensjon2025().also {
        it.beregningKapittel20 = this.beregningKapittel20?.copy()
        it.beregningsInformasjonKapittel20 = this.beregningsInformasjonKapittel20?.copy()
        copyBeregningsResultat(source = this, target = it)
    }

fun BeregningsresultatUforetrygd.copy() =
    BeregningsresultatUforetrygd().also {
        it.uforetrygdberegning = this.uforetrygdberegning?.copy()
        copyBeregningsResultat(source = this, target = it)
    }

fun EktefelletilleggUT.copy() =
    EktefelletilleggUT().also {
        it.tidligereBelopAr = this.tidligereBelopAr
        it.nettoAkk = this.nettoAkk
        it.nettoRestAr = this.nettoRestAr
        it.avkortningsbelopPerAr = this.avkortningsbelopPerAr
        it.etForSkattekomp = this.etForSkattekomp
        it.upForSkattekomp = this.upForSkattekomp
        copyYtelseskomponent(source = this, target = it)
    }

fun FremskrevetAfpLivsvarig.copy() =
    FremskrevetAfpLivsvarig().also {
        it.reguleringsfaktor = this.reguleringsfaktor
        it.gap = this.gap
        it.gjennomsnittligUttaksgradSisteAr = this.gjennomsnittligUttaksgradSisteAr
        copyLivsvarigAfp(source = this, target = it)
    }

fun FremskrivingsDetaljer.copy() =
    FremskrivingsDetaljer().also {
        it.justeringTomDato = this.justeringTomDato?.copy()
        it.justeringsfaktor = this.justeringsfaktor
        it.teller = this.teller
        it.nevner = this.nevner
        it.arskull = this.arskull
    }

fun FremtidigBarnetilleggperiode.copy() =
    FremtidigBarnetilleggperiode().also {
        it.fradragPerAr = this.fradragPerAr
        it.nettoPerArForJustering = this.nettoPerArForJustering
        it.nettoPerAr = this.nettoPerAr
        it.nettoPerMnd = this.nettoPerMnd
        it.restTilUtbetaling = this.restTilUtbetaling
        it.restTilUtbetalingForJustering = this.restTilUtbetalingForJustering
        it.justeringsbelopUbegrensetPerAr = this.justeringsbelopUbegrensetPerAr
        it.justeringsbelopPerAr = this.justeringsbelopPerAr
        it.justeringsbelopPerMnd = this.justeringsbelopPerMnd
        copyBarnetilleggperiode(source = this, target = it)
    }

fun FtDtArsak.copy() =
    FtDtArsak().also {
        it.ftDtArsakEnum = this.ftDtArsakEnum
    }

fun Garantipensjon.copy() =
    Garantipensjon().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun Garantitillegg.copy() =
    Garantitillegg().also {
        copyYtelseskomponent(source = this, target = it)
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

fun Gjenlevendetillegg.copy() =
    Gjenlevendetillegg().also {
        it.tidligereBelopAr = this.tidligereBelopAr
        it.bgKonvertert = this.bgKonvertert
        it.bgGjenlevendetillegg = this.bgGjenlevendetillegg
        it.nettoAkk = this.nettoAkk
        it.nettoRestAr = this.nettoRestAr
        it.avkortningsbelopPerAr = this.avkortningsbelopPerAr
        it.nyttGjenlevendetillegg = this.nyttGjenlevendetillegg
        it.avkortingsfaktorGJT = this.avkortingsfaktorGJT
        it.gjenlevendetilleggInformasjon = this.gjenlevendetilleggInformasjon?.let(::GjenlevendetilleggInformasjon)
        it.periodisertAvvikEtteroppgjor = this.periodisertAvvikEtteroppgjor
        it.eksportFaktor = this.eksportFaktor
        it.grunnlagGjenlevendetillegg = this.grunnlagGjenlevendetillegg
        this.formelMap.forEach { (key, value) -> it.formelMap[key] = Formel(value) }
        copyYtelseskomponent(source = this, target = it)
    }

fun GjenlevendetilleggAP.copy() =
    GjenlevendetilleggAP().also {
        it.apKap19MedGJR = this.apKap19MedGJR
        it.apKap19UtenGJR = this.apKap19UtenGJR
        it.referansebelop = this.referansebelop
        it.sumReguleringsfradrag = this.sumReguleringsfradrag
        it.anvendtUttaksgrad = this.anvendtUttaksgrad
        it.metode = this.metode
        this.formelMap.forEach { (key, value) -> it.formelMap[key] = Formel(value) }
        copyYtelseskomponent(source = this, target = it)
    }

fun GjenlevendetilleggAPKap19.copy() =
    GjenlevendetilleggAPKap19().also {
        it.apKap19MedGJR = this.apKap19MedGJR
        it.apKap19UtenGJR = this.apKap19UtenGJR
        it.referansebelop = this.referansebelop
        it.metode = this.metode
        this.formelMap.forEach { (key, value) -> it.formelMap[key] = Formel(value) }
        copyYtelseskomponent(source = this, target = it)
    }

fun Inntektspensjon.copy() =
    Inntektspensjon().also {
        it.eksportBrok = this.eksportBrok?.let(::Brok)
        copyYtelseskomponent(source = this, target = it)
    }

fun JusteringsInformasjon.copy() =
    JusteringsInformasjon().also {
        it.totalJusteringsfaktor = this.totalJusteringsfaktor
        it.justeringsTypeEnum = this.justeringsTypeEnum
        it.elementer = this.elementer.map(::copyJustering).toMutableList()
    }

fun Justertbelop.copy() =
    Justertbelop().also {
        it.g01052014 = this.g01052014
        it.gjennomsnittligG2014 = this.gjennomsnittligG2014
        it.justertbelop = this.justertbelop
        it.overgangsbelop = this.overgangsbelop?.copy()
    }

fun JustertGarantipensjonsniva.copy() =
    JustertGarantipensjonsniva().also {
        it.garantipensjonsniva = this.garantipensjonsniva
        it.justeringsInformasjon = this.justeringsInformasjon
        it.belop = this.belop
    }

fun LivsvarigOffentligAfp.copy() =
    LivsvarigOffentligAfp().also {
        it.sistRegulertG = this.sistRegulertG
        it.uttaksdato = this.uttaksdato
        copyLivsvarigAfp(source = this, target = it)
    }

fun LivsvarigOffentligAfpGrunnlag.copy() =
    LivsvarigOffentligAfpGrunnlag(
        sistRegulertG = this.sistRegulertG,
        bruttoPerAr = this.bruttoPerAr,
        uttaksdato = this.uttaksdato,
        virkTom = this.virkTom
    )

fun LonnsvekstDetaljer.copy() =
    LonnsvekstDetaljer().also {
        it.justeringTomDato = this.justeringTomDato?.copy()
        it.justeringsfaktor = this.justeringsfaktor
        it.lonnsvekst = this.lonnsvekst
    }

fun LonnsvekstInformasjon.copy() =
    LonnsvekstInformasjon().also {
        it.lonnsvekst = this.lonnsvekst
        it.reguleringsDato = this.reguleringsDato?.copy()
        it.uttaksgradVedRegulering = this.uttaksgradVedRegulering
    }

fun MinstenivatilleggIndividuelt.copy() =
    MinstenivatilleggIndividuelt().also {
        it.samletPensjonForMNT = this.samletPensjonForMNT
        it.mpn = this.mpn?.let(::MinstePensjonsniva)
        it.garPN = this.garPN?.let(::Garantipensjonsniva)
        copyYtelseskomponent(source = this, target = it)
    }

fun MinstenivatilleggPensjonistpar.copy() =
    MinstenivatilleggPensjonistpar().also {
        it.bruker = this.bruker?.let(::BeregningsInformasjonMinstenivatilleggPensjonistpar)
        it.ektefelle = this.ektefelle?.let(::BeregningsInformasjonMinstenivatilleggPensjonistpar)
        copyYtelseskomponent(source = this, target = it)
    }

fun Overgangsbelop.copy() =
    Overgangsbelop().also {
        it.formelKodeEnum = this.formelKodeEnum
        it.gpBrutto = this.gpBrutto
        it.gpSats = this.gpSats
        it.overgangsbelop = this.overgangsbelop
        it.stBrutto = this.stBrutto
        it.tpBrutto = this.tpBrutto
    }

fun OvergangsinfoUPtilUT.copy() =
    OvergangsinfoUPtilUT().also {
        it.ektefelletilleggUT = this.ektefelletilleggUT?.copy()
        it.inntektsgrenseorFriinntektsdato = this.inntektsgrenseorFriinntektsdato
        it.konvertertBeregningsgrunnlagOrdiner = this.konvertertBeregningsgrunnlagOrdiner?.copy()
        it.konvertertBeregningsgrunnlagYrkesskade = this.konvertertBeregningsgrunnlagYrkesskade?.copy()
        it.konvertertBeregningsgrunnlagGJT = this.konvertertBeregningsgrunnlagGJT?.copy()
        it.anvendtTrygdetidUP = this.anvendtTrygdetidUP?.let(::AnvendtTrygdetid)
        it.anvendtTrygdetidUPHjemme = this.anvendtTrygdetidUPHjemme?.let(::AnvendtTrygdetid)
        it.anvendtTrygdetidUP_egen = this.anvendtTrygdetidUP_egen?.let(::AnvendtTrygdetid)
        it.minstepensjontypeEnum = this.minstepensjontypeEnum
        it.resultatKildeEnum = this.resultatKildeEnum
        it.sertilleggNetto = this.sertilleggNetto
    }

fun PrivatAfp.copy() =
    PrivatAfp().also {
        it.justeringsbelop = this.justeringsbelop
        it.afpProsentgrad = this.afpProsentgrad
        it.afpForholdstall = this.afpForholdstall
        copyLivsvarigAfp(source = this, target = it)
    }

fun PrivatAfpBeregning.copy() =
    PrivatAfpBeregning().also {
        it.afpPrivatLivsvarig = this.afpPrivatLivsvarig?.copy()
        it.afpKompensasjonstillegg = this.afpKompensasjonstillegg?.copy()
        it.afpKronetillegg = this.afpKronetillegg?.copy()
        it.afpOpptjening = this.afpOpptjening?.copy()
        copyBeregning2011(source = this, target = it)
    }

fun Skattekompensertbelop.copy() =
    Skattekompensertbelop().also {
        it.faktor = this.faktor
        it.formelKodeEnum = this.formelKodeEnum
        it.arsbelop = this.arsbelop
        it.justertbelop = this.justertbelop?.copy()
        it.tillegg = this.tillegg
    }

fun Skjermingstillegg.copy() =
    Skjermingstillegg().also {
        it.ft67Soker = this.ft67Soker
        it.skjermingsgrad = this.skjermingsgrad
        it.ufg = this.ufg
        it.basGp_bruttoPerAr = this.basGp_bruttoPerAr
        it.basTp_bruttoPerAr = this.basTp_bruttoPerAr
        it.basPenT_bruttoPerAr = this.basPenT_bruttoPerAr
        copyYtelseskomponent(source = this, target = it)
    }

fun TemporarYtelseskomponent.copy() =
    TemporarYtelseskomponent().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun TapendeBeregningsmetode.copy() =
    TapendeBeregningsmetode().also {
        it.beregningMetodeTypeEnum = this.beregningMetodeTypeEnum
    }

fun TidligereBarnetilleggperiode.copy() =
    TidligereBarnetilleggperiode().also {
        it.faktiskFradragPerAr = this.faktiskFradragPerAr
        it.avviksbelop = this.avviksbelop
        copyBarnetilleggperiode(source = this, target = it)
    }

fun Uforetrygdberegning.copy() =
    Uforetrygdberegning().also {
        it.bruttoPerAr = this.bruttoPerAr
        it.formelKodeEnum = this.formelKodeEnum
        it.grunnbelop = this.grunnbelop
        it.minsteytelse = this.minsteytelse?.let(::Minsteytelse)
        it.prorataBrok = this.prorataBrok?.let(::Brok)
        it.uforegrad = this.uforegrad
        it.uforetidspunkt = this.uforetidspunkt?.copy()
        it.egenopptjentUforetrygd = this.egenopptjentUforetrygd?.let(::EgenopptjentUforetrygd)
        it.egenopptjentUforetrygdBest = this.egenopptjentUforetrygdBest
        it.yrkesskadegrad = this.yrkesskadegrad
        it.yrkesskadetidspunkt = this.yrkesskadetidspunkt?.copy()
        it.mottarMinsteytelse = this.mottarMinsteytelse
        it.minsteytelseArsak = this.minsteytelseArsak
        it.instOppholdTypeEnum = this.instOppholdTypeEnum
        it.instOpphAnvendt = this.instOpphAnvendt
        it.uforeEkstra = this.uforeEkstra?.let(::UforeEkstraUT)
        it.ytelseVedDodEnum = this.ytelseVedDodEnum
        copyBeregning2011(source = this, target = it)
    }

fun UforetrygdOrdiner.copy() =
    UforetrygdOrdiner().also {
        it.minsteytelse = this.minsteytelse?.let(::Minsteytelse)
        it.egenopptjentUforetrygd = this.egenopptjentUforetrygd?.let(::EgenopptjentUforetrygd)
        it.egenopptjentUforetrygdBest = this.egenopptjentUforetrygdBest
        it.avkortingsinformasjon = this.avkortingsinformasjon?.copy()
        it.nettoAkk = this.nettoAkk
        it.nettoRestAr = this.nettoRestAr
        it.avkortningsbelopPerAr = this.avkortningsbelopPerAr
        it.periodisertAvvikEtteroppgjor = this.periodisertAvvikEtteroppgjor
        it.fradragPerArUtenArbeidsforsok = this.fradragPerArUtenArbeidsforsok
        it.tidligereBelopAr = this.tidligereBelopAr
        copyYtelseskomponent(source = this, target = it)
    }

fun UtbetalingsgradUT.copy() =
    UtbetalingsgradUT().also {
        it.ar = this.ar
        it.utbetalingsgrad = this.utbetalingsgrad
    }

private fun copyAvkortingsinformasjon(source: AbstraktAvkortingsinformasjon, target: AbstraktAvkortingsinformasjon) {
    target.antallMndFor = source.antallMndFor
    target.antallMndEtter = source.antallMndEtter
    target.inntektstak = source.inntektstak
    target.avkortingsbelopPerAr = source.avkortingsbelopPerAr
    target.restTilUtbetaling = source.restTilUtbetaling
    target.inntektsavkortingTypeEnum = source.inntektsavkortingTypeEnum
}

fun copyBarnetillegg(
    source: AbstraktBarnetillegg,
    target: AbstraktBarnetillegg
) {
    target.antallBarn = source.antallBarn
    target.avkortet = source.avkortet
    target.btDiff_eos = source.btDiff_eos
    target.fribelop = source.fribelop
    target.mpnSatsFT = source.mpnSatsFT
    target.proratanevner = source.proratanevner
    target.proratateller = source.proratateller
    target.samletInntektAvkort = source.samletInntektAvkort
    target.tt_anv = source.tt_anv
    target.forsorgingstilleggNiva = source.forsorgingstilleggNiva
    target.avkortingsArsakListEnum = source.avkortingsArsakListEnum.map { it }.toMutableList()
    copyYtelseskomponent(source, target)
}

fun copyBarnetilleggperiode(
    source: AbstraktBarnetilleggperiode,
    target: AbstraktBarnetilleggperiode
) {
    target.fomDato = source.fomDato?.copy()
    target.tomDato = source.tomDato?.copy()
    target.lengde = source.lengde
    target.antallBarn = source.antallBarn
    target.fribelop = source.fribelop
    target.bruttoPerAr = source.bruttoPerAr
    target.reguleringsfaktor = source.reguleringsfaktor?.let(::Brok)
    target.avkortingsbelopPerAr = source.avkortingsbelopPerAr
}

private fun copyBarnetilleggUT(
    source: AbstraktBarnetilleggUT,
    target: AbstraktBarnetilleggUT
) {
    target.avkortingsinformasjon = source.avkortingsinformasjon?.copy()
    target.avkortningsbelopPerAr = source.avkortningsbelopPerAr
    target.inntektstak = source.inntektstak
    target.nettoAkk = source.nettoAkk
    target.nettoRestAr = source.nettoRestAr
    target.periodisertAvvikEtteroppgjor = source.periodisertAvvikEtteroppgjor
    target.reduksjonsinformasjon = source.reduksjonsinformasjon?.let(::Reduksjonsinformasjon)
    target.tidligereBelopAr = source.tidligereBelopAr
    target.brukersUforetrygdForJustering = source.brukersUforetrygdForJustering
    copyBarnetillegg(source, target)
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
    target.merknadListe = source.merknadListe.map { o -> o.copy() }
    target.beregningGjelderTypeEnum = source.beregningGjelderTypeEnum

    if (copyDelberegninger) {
        target.delberegning2011Liste = source.delberegning2011Liste.map(::BeregningRelasjon)
    }
}

private fun copyBeregningsgrunnlag(source: AbstraktBeregningsgrunnlag, target: AbstraktBeregningsgrunnlag) {
    target.formelKodeEnum = source.formelKodeEnum
    target.arsbelop = source.arsbelop
    target.antattInntektFaktorKap19 = source.antattInntektFaktorKap19
    target.antattInntektFaktorKap20 = source.antattInntektFaktorKap20
}

private fun copyBeregningsResultat(source: AbstraktBeregningsResultat, target: AbstraktBeregningsResultat) {
    target.virkFom = source.virkFom?.copy()
    target.virkTom = source.virkTom?.copy()
    target.merknadListe = source.merknadListe.map { o -> o.copy() }.toMutableList()
    target.pensjonUnderUtbetaling = source.pensjonUnderUtbetaling?.let(::PensjonUnderUtbetaling)
    target.brukersSivilstandEnum = source.brukersSivilstandEnum
    target.benyttetSivilstandEnum = source.benyttetSivilstandEnum
    target.beregningArsakEnum = source.beregningArsakEnum
    target.lonnsvekstInformasjon = source.lonnsvekstInformasjon?.copy()
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
        else -> throw RuntimeException("Unsupported IJustering type: ${source.javaClass.name}")
    }

private fun copyLivsvarigAfp(
    source: LivsvarigAfp,
    target: LivsvarigAfp
) {
    copyYtelseskomponent(source, target)
}

private fun privatAfp(ytelseKomponentListe: List<Ytelseskomponent>): PrivatAfp? =
    ytelseKomponentListe.firstOrNull {
        it.ytelsekomponentTypeEnum == YtelseskomponentTypeEnum.AFP_PRIVAT_LIVSVARIG
    } as? PrivatAfp
