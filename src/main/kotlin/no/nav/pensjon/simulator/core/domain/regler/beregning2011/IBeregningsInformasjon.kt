package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningMetodeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.JustertPeriodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ResultatTypeCti

@JsonSubTypes(
    JsonSubTypes.Type(value = BeregningsInformasjon::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
interface IBeregningsInformasjon {
    var penPerson: PenPerson?
    var beregningsMetode: BeregningMetodeTypeCti?
    var ensligPensjonInstOpph: Boolean
    var instOppholdType: JustertPeriodeCti?
    var instOpphAnvendt: Boolean
    var resultatType: ResultatTypeCti?
    var tapendeBeregningsmetodeListe: MutableList<TapendeBeregningsmetode>
    var trygdetid: Int
    var tt_anv: Int
    var vurdertBosattland: LandCti?
    var eksport: Boolean

    var avdodesTilleggspensjonBrukt: Boolean
    var avdodesTrygdetidBrukt: Boolean
    var forholdstall67: Double
    var forholdstallUttak: Double
    var gjenlevenderettAnvendt: Boolean
    var gpAvkortingsArsakList: MutableList<Merknad>
    var grunnpensjonAvkortet: Boolean
    var mottarMinstePensjonsniva: Boolean
    var minstepensjonArsak: String?
    var opt: Sluttpoengtall?
    var rettPaGjenlevenderett: Boolean
    var spt: Sluttpoengtall?
    var ungUfor: Boolean
    var ungUforAnvendt: Boolean
    var ypt: Sluttpoengtall?
    var yrkesskadeAnvendt: Boolean
    var yrkesskadegrad: Int
    var yrkesskadeRegistrert: Boolean
}
