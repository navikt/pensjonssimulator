package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.enum.BeregningsmetodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.JustertPeriodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ResultattypeEnum

// 2025-03-10
class BeregningsInformasjon {
    var forholdstallUttak = 0.0
    var forholdstall67 = 0.0
    var delingstallUttak = 0.0
    var delingstall67 = 0.0
    var spt: Sluttpoengtall? = null
    var opt: Sluttpoengtall? = null
    var ypt: Sluttpoengtall? = null
    var grunnpensjonAvkortet = false
    var merknadListe: List<Merknad> = mutableListOf()
    var mottarMinstePensjonsniva = false
    var minstepensjonArsak: String? = null
    var rettPaGjenlevenderett = false
    var gjenlevenderettAnvendt = false
    var avdodesTilleggspensjonBrukt = false
    var avdodesTrygdetidBrukt = false
    var ungUfor = false
    var ungUforAnvendt = false
    var yrkesskadeRegistrert = false
    var yrkesskadeAnvendt = false
    var yrkesskadegrad = 0
    var penPerson: PenPerson? = null
    var beregningsMetodeEnum: BeregningsmetodeEnum? = null
    var eksport = false
    var resultatTypeEnum: ResultattypeEnum? = null
    var tapendeBeregningsmetodeListe: List<TapendeBeregningsmetode> = mutableListOf()
    var trygdetid: Int? = null
    var tt_anv = 0
    var vurdertBosattlandEnum: LandkodeEnum? = null
    var ensligPensjonInstOpph = false
    var instOppholdTypeEnum: JustertPeriodeEnum? = null
    var instOpphAnvendt = false
    var tp = 0.0 // tilleggspensjon
    var ttBeregnetForGrunnlagsrolle = 0
    var ungUforGarantiFrafalt = false

    //--- Extra:

    // from PEN no.nav.domain.pensjon.kjerne.beregning2011.BeregningsInformasjon
    var epsMottarPensjon: Boolean = false
    var epsOver2G: Boolean = false

    @JsonIgnore
    var unclearedDelingstallUttak: Double? = null

    @JsonIgnore
    var unclearedDelingstall67: Double? = null

    val internDelingstallUttak: Double
        @JsonIgnore get() = unclearedDelingstallUttak ?: delingstallUttak

    val internDelingstall67: Double
        @JsonIgnore get() = unclearedDelingstall67 ?: delingstall67

    // delingstallUttak, delingstall67 are not mapped in legacy simulering, hence set to zero:
    fun clearDelingstall() {
        unclearedDelingstallUttak = delingstallUttak
        delingstallUttak = 0.0
        unclearedDelingstall67 = delingstall67
        delingstall67 = 0.0
    }
}
