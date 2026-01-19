package no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import no.nav.pensjon.simulator.core.util.copy

fun AfpHistorikk.copy() =
    AfpHistorikk().also {
        it.afpFpp = this.afpFpp
        it.virkFom = this.virkFom?.copy()
        it.virkTom = this.virkTom?.copy()
        it.afpPensjonsgrad = this.afpPensjonsgrad
        it.afpOrdningEnum = this.afpOrdningEnum
    }

fun AfpOpptjening.copy() =
    AfpOpptjening().also {
        copyBeholdning(source = this, target = it)
    }

fun AfpTpoUpGrunnlag.copy() =
    AfpTpoUpGrunnlag().also {
        it.belop = this.belop
        it.virkFom = this.virkFom?.copy()
    }

fun AntallArMndDag.copy() =
    AntallArMndDag().also {
        it.antallAr = this.antallAr
        it.antallMnd = this.antallMnd
        it.antallDager = this.antallDager
    }

fun AntallArOgMnd.copy() =
    AntallArOgMnd().also {
        it.antallAr = this.antallAr
        it.antallMnd = this.antallMnd
    }

fun ArbeidsforholdEtterUforgrunnlag.copy() =
    ArbeidsforholdEtterUforgrunnlag().also {
        it.fomDato = this.fomDato?.copy()
        it.stillingsprosent = this.stillingsprosent
        it.varigTilrettelagtArbeid = this.varigTilrettelagtArbeid
    }

fun Arbeidsforholdsgrunnlag.copy() =
    Arbeidsforholdsgrunnlag().also {
        it.fomDato = this.fomDato?.copy()
        it.tomDato = this.tomDato?.copy()
        it.stillingsprosent = this.stillingsprosent
        it.arbeidsgiver = this.arbeidsgiver
        it.orgNummer = this.orgNummer
    }

fun BarnDetalj.copy() =
    BarnDetalj().also {
        it.annenForelder = this.annenForelder?.copy()
        it.borMedBeggeForeldre = this.borMedBeggeForeldre
        it.borFomDato = this.borFomDato?.copy()
        it.borTomDato = this.borTomDato?.copy()
        it.inntektOver1G = this.inntektOver1G
        it.underUtdanning = this.underUtdanning
    }

fun Barnekull.copy() =
    Barnekull().also {
        it.antallBarn = this.antallBarn
        it.bruk = this.bruk
    }

fun BarnetilleggVilkar.copy() =
    BarnetilleggVilkar().also {
        it.btVilkarTypeEnum = this.btVilkarTypeEnum
        it.vurdertTil = this.vurdertTil
    }

fun BarnetilleggVurderingsperiode.copy() =
    BarnetilleggVurderingsperiode().also {
        it.fomDato = this.fomDato?.copy()
        it.tomDato = this.tomDato?.copy()
        it.btVilkarListe = this.btVilkarListe.map { o -> o.copy() }
    }

fun Beholdninger.copy() =
    Beholdninger().also {
        it.beholdninger = this.beholdninger.map(::copyBeholdning)
    }

// PEN: Beholdninger.findBeholdningAvType
fun Beholdninger.beholdning(type: BeholdningtypeEnum) =
    beholdninger.firstOrNull { type == it.beholdningsTypeEnum }

fun EosEkstra.copy() =
    EosEkstra().also {
        it.proRataBeregningTypeEnum = this.proRataBeregningTypeEnum
        it.redusertAntFppAr = this.redusertAntFppAr
        it.spt_eos = this.spt_eos
        it.spt_pa_f92_eos = this.spt_pa_f92_eos
        it.spt_pa_e91_eos = this.spt_pa_e91_eos
        it.vilkar3_17Aok = this.vilkar3_17Aok
    }

fun Garantipensjonsbeholdning.copy() =
    Garantipensjonsbeholdning().also {
        it.justertGarantipensjonsniva = this.justertGarantipensjonsniva?.copy()
        it.pensjonsbeholdning = this.pensjonsbeholdning
        it.delingstallVedNormertPensjonsalder = this.delingstallVedNormertPensjonsalder
        it.satsTypeEnum = this.satsTypeEnum
        it.sats = this.sats
        it.garPN_tt_anv = this.garPN_tt_anv
        it.garPN_justert = this.garPN_justert
        copyBeholdning(source = this, target = it)
    }

fun Garantitilleggsbeholdning.copy() =
    Garantitilleggsbeholdning().also {
        it.garantitilleggInformasjon = this.garantitilleggInformasjon?.copy()
        copyBeholdning(source = this, target = it)
    }

fun GarantiTrygdetid.copy() =
    GarantiTrygdetid().also {
        it.trygdetid_garanti = this.trygdetid_garanti
        it.fomDato = this.fomDato?.copy()
        it.tomDato = this.tomDato?.copy()
    }

fun InngangOgEksportGrunnlag.copy() =
    InngangOgEksportGrunnlag().also {
        it.treArTrygdetidNorge = this.treArTrygdetidNorge
        it.femArTrygdetidNorge = this.femArTrygdetidNorge
        it.unntakFraForutgaendeTT = this.unntakFraForutgaendeTT?.let(::Unntak)
        it.fortsattMedlemFT = this.fortsattMedlemFT
        it.minstTyveArBotidNorge = this.minstTyveArBotidNorge
        it.opptjentRettTilTPEtterFT = this.opptjentRettTilTPEtterFT
        it.eksportforbud = this.eksportforbud?.let(::Eksportforbud)
        it.friEksportPgaYrkesskade = this.friEksportPgaYrkesskade
        it.eksportrettEtterEOSForordning = this.eksportrettEtterEOSForordning?.let(::Eksportrett)
        it.eksportrettEtterTrygdeavtalerEOS = this.eksportrettEtterTrygdeavtalerEOS?.let(::Eksportrett)
        it.eksportrettEtterAndreTrygdeavtaler = this.eksportrettEtterAndreTrygdeavtaler?.let(::Eksportrett)
        it.eksportrettGarantertTP = this.eksportrettGarantertTP?.let(::Unntak)
        it.minstTreArsFMNorge = this.minstTreArsFMNorge
        it.minstFemArsFMNorge = this.minstFemArsFMNorge
        it.minstTreArsFMNorgeVirkdato = this.minstTreArsFMNorgeVirkdato
        it.unntakFraForutgaendeMedlemskap = this.unntakFraForutgaendeMedlemskap?.let(::Unntak)
        it.oppfyltEtterGamleRegler = this.oppfyltEtterGamleRegler
        it.oppfyltVedSammenlegging = this.oppfyltVedSammenlegging?.let(::OppfyltVedSammenlegging)
        it.oppfyltVedSammenleggingFemAr = this.oppfyltVedSammenleggingFemAr?.let(::OppfyltVedSammenlegging)
        it.oppfyltVedGjenlevendesMedlemskap = this.oppfyltVedGjenlevendesMedlemskap
        it.gjenlevendeMedlemFT = this.gjenlevendeMedlemFT
        it.minstEttArFMNorge = this.minstEttArFMNorge
        it.foreldreMinstTyveArBotidNorge = this.foreldreMinstTyveArBotidNorge
        it.friEksportDodsfall = this.friEksportDodsfall
        it.minstTyveArTrygdetidNorgeKap20 = this.minstTyveArTrygdetidNorgeKap20
        it.treArTrygdetidNorgeKap20 = this.treArTrygdetidNorgeKap20
        it.femArTrygdetidNorgeKap20 = this.femArTrygdetidNorgeKap20
        it.oppfyltVedSammenleggingKap20 = this.oppfyltVedSammenleggingKap20?.let(::OppfyltVedSammenlegging)
        it.oppfyltVedSammenleggingFemArKap20 = this.oppfyltVedSammenleggingFemArKap20?.let(::OppfyltVedSammenlegging)
    }

fun InstOpphFasteUtgifterperiode.copy() =
    InstOpphFasteUtgifterperiode().also {
        it.instOpphFasteUtgifterperiodeId = this.instOpphFasteUtgifterperiodeId
        it.fom = this.fom?.copy()
        it.tom = this.tom?.copy()
        it.fasteUtgifter = this.fasteUtgifter
    }

fun NormertPensjonsalderGrunnlag.copy() =
    NormertPensjonsalderGrunnlag(
        ovreAr = this.ovreAr,
        ovreMnd = this.ovreMnd,
        normertAr = this.normertAr,
        normertMnd = this.normertMnd,
        nedreAr = this.nedreAr,
        nedreMnd = this.nedreMnd,
        erPrognose = this.erPrognose
    )

fun Omsorgsgrunnlag.copy() =
    Omsorgsgrunnlag().also {
        it.ar = this.ar
        it.omsorgTypeEnum = this.omsorgTypeEnum
        it.personOmsorgFor = this.personOmsorgFor?.copy()
        it.bruk = this.bruk
    }

fun Pensjonsbeholdning.copy() =
    Pensjonsbeholdning().also {
        it.fom = this.fom?.copy()
        it.tom = this.tom?.copy()
        copyBeholdning(source = this, target = it)
    }

fun Unntak.copy() =
    Unntak().also {
        it.unntak = this.unntak
        it.unntakTypeEnum = this.unntakTypeEnum
        it.eksportUnntakEnum = this.eksportUnntakEnum
    }

fun Uttaksgrad.copy() =
    Uttaksgrad().also {
        it.fomDato = this.fomDato?.copy()
        it.tomDato = this.tomDato?.copy()
        it.uttaksgrad = this.uttaksgrad
    }

fun Ventetilleggsgrunnlag.copy() =
    Ventetilleggsgrunnlag().also {
        it.ventetilleggprosent = this.ventetilleggprosent
        it.vt_spt = this.vt_spt
        it.vt_opt = this.vt_opt
        it.vt_pa = this.vt_pa
        it.tt_vent = this.tt_vent
    }

private fun copyBeholdning(source: Beholdning): Beholdning =
    when (source) {
        is AfpOpptjening -> source.copy()
        is Garantitilleggsbeholdning -> source.copy()
        is Garantipensjonsbeholdning -> source.copy()
        is Pensjonsbeholdning -> source.copy()
        else -> throw RuntimeException("Unsupported Beholdning type: ${source.javaClass.name}")
    }

private fun copyBeholdning(source: Beholdning, target: Beholdning) {
    target.ar = source.ar
    target.totalbelop = source.totalbelop
    target.reguleringsInformasjon = source.reguleringsInformasjon?.let(::ReguleringsInformasjon)
    target.opptjening = source.opptjening?.let(::Opptjening)
    target.beholdningsTypeEnum = source.beholdningsTypeEnum
    target.formelKodeEnum = source.formelKodeEnum
    target.merknadListe = source.merknadListe.map { o -> o.copy() }.toMutableList()
    target.lonnsvekstInformasjon = source.lonnsvekstInformasjon?.copy()
}
