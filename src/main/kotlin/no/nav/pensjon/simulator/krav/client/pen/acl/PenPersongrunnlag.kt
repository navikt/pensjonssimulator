package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.OvergangsinfoUPtilUT
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import java.util.*

/**
 * Persongrunnlag DTO (data transfer object) received from PEN.
 * Corresponds to no.nav.pensjon.pen.domain.api.simulator.grunnlag.Persongrunnlag in PEN.
 */
class PenPersongrunnlag(
    var penPerson: PenPenPerson? = null,
    var fodselsdato: Date? = null,
    var dodsdato: Date? = null,
    var statsborgerskapEnum: LandkodeEnum? = null,
    var flyktning: Boolean? = null,
    var personDetaljListe: MutableList<PenPersonDetalj> = mutableListOf(),
    var sistMedlITrygden: Date? = null,
    var sisteGyldigeOpptjeningsAr: Int = 0,
    var hentetPopp: Boolean? = null,
    var hentetInnt: Boolean? = null,
    var hentetInst: Boolean? = null,
    var hentetTT: Boolean? = null,
    var hentetArbeid: Boolean? = null,
    var overkompUtl: Boolean? = null,
    var opptjeningsgrunnlagListe: MutableList<Opptjeningsgrunnlag> = mutableListOf(),
    var inntektsgrunnlagListe: MutableList<Inntektsgrunnlag> = mutableListOf(),
    var trygdetidPerioder: MutableList<TTPeriode> = mutableListOf(),
    var trygdetidPerioderKapittel20: MutableList<TTPeriode> = mutableListOf(),
    var trygdetid: Trygdetid? = null,
    var trygdetidKapittel20: Trygdetid? = null,
    var trygdetidAlternativ: Trygdetid? = null,
    var uforegrunnlag: Uforegrunnlag? = null,
    var uforeHistorikk: Uforehistorikk? = null,
    var yrkesskadegrunnlag: Yrkesskadegrunnlag? = null,
    var dodAvYrkesskade: Boolean? = null,
    var generellHistorikk: GenerellHistorikk? = null,
    var afpHistorikkListe: MutableList<AfpHistorikk> = mutableListOf(),
    var barnekull: Barnekull? = null,
    var barnetilleggVurderingsperiode: BarnetilleggVurderingsperiode? = null,
    var antallArUtland: Int = 0,
    var medlemIFolketrygdenSiste3Ar: Boolean? = null,
    var over60ArKanIkkeForsorgesSelv: Boolean? = null,
    var utenlandsoppholdListe: MutableList<Utenlandsopphold> = mutableListOf(),
    var trygdeavtale: Trygdeavtale? = null,
    var trygdeavtaledetaljer: Trygdeavtaledetaljer? = null,
    var inngangOgEksportGrunnlag: InngangOgEksportGrunnlag? = null,
    var forsteVirkningsdatoGrunnlagListe: MutableList<PenFoersteVirkningDatoGrunnlag> = mutableListOf(),
    var arligPGIMinst1G: Boolean? = null,
    var artikkel10: Boolean? = null,
    var vernepliktAr: IntArray = IntArray(0),
    var skiltesDelAvAvdodesTP: Int = -99,
    var instOpphReduksjonsperiodeListe: MutableList<InstOpphReduksjonsperiode> = mutableListOf(),
    var instOpphFasteUtgifterperiodeListe: MutableList<InstOpphFasteUtgifterperiode> = mutableListOf(),
    var bosattLandEnum: LandkodeEnum? = null,
    var pensjonsbeholdning: PenPensjonsbeholdning? = null,
    var forstegangstjenestegrunnlag: Forstegangstjeneste? = null,
    var dagpengegrunnlagListe: MutableList<Dagpengegrunnlag> = mutableListOf(),
    var omsorgsgrunnlagListe: MutableList<PenOmsorgGrunnlag> = mutableListOf(),
    var arbeidsforholdsgrunnlagListe: MutableList<Arbeidsforholdsgrunnlag> = mutableListOf(),
    var arbeidsforholdEtterUforgrunnlagListe: MutableList<ArbeidsforholdEtterUforgrunnlag> = mutableListOf(),
    var overgangsInfoUPtilUT: OvergangsinfoUPtilUT? = null,
    var utbetalingsgradUTListe: MutableList<UtbetalingsgradUT> = mutableListOf(),
    var instOpphReduksjonsperiode: InstOpphReduksjonsperiode? = null,
    var instOpphFasteUtgifterperiode: InstOpphFasteUtgifterperiode? = null,
    var ektefellenMottarPensjon: Boolean = false,
    var personDetalj: PenPersonDetalj? = null,
    var poengtillegg: Double = 0.0,
    var boddEllerArbeidetIUtlandet: Boolean = false,
    var forsteVirk: Date? = null,
    var afpTpoUpGrunnlag: AfpTpoUpGrunnlag? = null,
    var normertPensjonsalderGrunnlag: NormertPensjonsalderGrunnlag? = null,

    var gjelderOmsorg: Boolean = false,
    var gjelderUforetrygd: Boolean = false,
    var barnetilleggVurderingsperioder: MutableList<BarnetilleggVurderingsperiode> = mutableListOf(),
    var beholdninger: MutableList<PenPensjonsbeholdning> = mutableListOf(), // not used; flatBeholdninger used instead
    var flatBeholdninger: MutableList<PenPensjonsbeholdning> = mutableListOf(),
    var trygdetider: MutableList<Trygdetid> = mutableListOf(),
    var uforegrunnlagList: MutableList<Uforegrunnlag> = mutableListOf(),
    var yrkesskadegrunnlagList: MutableList<Yrkesskadegrunnlag> = mutableListOf()
)
