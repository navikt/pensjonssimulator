package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

// no.nav.service.pensjon.fpen.support.command.opprettsisteberegning.OpprettSisteBeregningParameter
data class SisteBeregningSpec(
    var beregningsresultat: AbstraktBeregningsResultat?,
    var regelverkKodePaNyttKrav: RegelverkTypeEnum?,
    var forrigeKravhode: Kravhode?,
    var filtrertVilkarsvedtakList: List<VilkarsVedtak>,
    val isRegelverk1967: Boolean = false,
    val vilkarsvedtakListe: List<VilkarsVedtak> = emptyList(),
    val kravhode: Kravhode? = null,
    val beregning: AbstraktBeregningsResultat? = null,
    val fomDato: LocalDate? = null,
    val tomDato: LocalDate? = null,
    val regelverk1967VirkToEarly: Boolean = false
)
