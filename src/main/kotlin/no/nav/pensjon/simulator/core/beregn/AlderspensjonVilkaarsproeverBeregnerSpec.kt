package no.nav.pensjon.simulator.core.beregn
//TODO move to beregn

import no.nav.pensjon.simulator.core.SimuleringSpec
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktAarsak
import no.nav.pensjon.simulator.core.krav.KravGjelder
import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.abstractsimulerapfra2011.VilkarsprovOgBeregnAlderspensjonRequest
data class AlderspensjonVilkaarsproeverBeregnerSpec(
    val kravhode: Kravhode,
    var knekkpunkter: SortedMap<LocalDate, MutableList<KnekkpunktAarsak>>,
    var simulering: SimuleringSpec,
    var sokerForsteVirk: LocalDate,
    var avdodForsteVirk: LocalDate? = null,
    var forrigeVilkarsvedtakListe: MutableList<VilkarsVedtak>,
    var forrigeAlderBeregningsresultat: AbstraktBeregningsResultat? = null,
    var sisteBeregning: SisteBeregning? = null,
    var afpPrivatBeregningsresultater: MutableList<BeregningsResultatAfpPrivat>,
    var gjeldendeAfpPrivatBeregningsresultat: BeregningsResultatAfpPrivat? = null,
    val forsteVirkAfpPrivat: LocalDate?,
    val isHentPensjonsbeholdninger: Boolean,
    val kravGjelder: KravGjelder,
    val sakId: Long?,
    val sakType: SakType?,
    val afpOffentligLivsvarigBeregningsresultat: LivsvarigOffentligAfpResult?,
    val ignoreAvslag: Boolean = false
)