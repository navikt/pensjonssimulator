package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktAarsak
import no.nav.pensjon.simulator.core.krav.KravGjelder
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate
import java.util.*

data class AlderspensjonVilkaarsproeverBeregnerSpec(
    val kravhode: Kravhode,
    val knekkpunkter: SortedMap<LocalDate, MutableList<KnekkpunktAarsak>>,
    val simulering: SimuleringSpec,
    val sokerForsteVirk: LocalDate,
    val avdodForsteVirk: LocalDate? = null,
    val forrigeVilkarsvedtakListe: MutableList<VilkarsVedtak>,
    val forrigeAlderBeregningsresultat: AbstraktBeregningsResultat? = null,
    val sisteBeregning: SisteBeregning? = null,
    val afpPrivatBeregningsresultater: MutableList<BeregningsResultatAfpPrivat>,
    val gjeldendeAfpPrivatBeregningsresultat: BeregningsResultatAfpPrivat? = null,
    val forsteVirkAfpPrivat: LocalDate?,
    val brukBeregnetBeholdning: Boolean,
    val kravGjelder: KravGjelder,
    val sakId: Long?,
    val sakType: SakTypeEnum?,
    val afpOffentligLivsvarigBeregningsresultat: LivsvarigOffentligAfpResult?,
    val ignoreAvslag: Boolean,
    val onlyVilkaarsproeving: Boolean
)