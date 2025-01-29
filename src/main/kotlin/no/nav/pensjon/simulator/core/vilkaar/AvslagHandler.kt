package no.nav.pensjon.simulator.core.vilkaar

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.domain.regler.enum.BegrunnelseTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException

/**
 * Håndterer avslag i vilkårsprøvingen av krav om pensjon.
 */
object AvslagHandler {
    private val log = KotlinLogging.logger {}

    // PEN: VilkarsprovOgBeregnAlderHelper.createFunctionalExceptionsFromVilkarsprovingIfNecesseary
    fun handleAvslag(vedtakListe: List<VilkarsVedtak>) {
        vedtakListe.filter {
            VedtakResultatEnum.AVSL == it.anbefaltResultatEnum
        }.forEach {
            it.begrunnelseEnum?.let(::handleAvslag) ?: throw UtilstrekkeligTrygdetidException()
            // NB: begrunnelseEnum = null => UtilstrekkeligTrygdetidException
        }
    }

    private fun handleAvslag(begrunnelse: BegrunnelseTypeEnum?) {
        when (begrunnelse) {
            BegrunnelseTypeEnum.UNDER_1_AR_TT -> throw UtilstrekkeligTrygdetidException()
            BegrunnelseTypeEnum.UNDER_3_AR_TT -> throw UtilstrekkeligTrygdetidException()
            BegrunnelseTypeEnum.UNDER_5_AR_TT -> throw UtilstrekkeligTrygdetidException()
            BegrunnelseTypeEnum.UNDER_20_AR_TT_2025 -> throw UtilstrekkeligTrygdetidException()
            BegrunnelseTypeEnum.LAVT_TIDLIG_UTTAK -> throw UtilstrekkeligOpptjeningException()

            BegrunnelseTypeEnum.UTG_MINDRE_ETT_AR ->
                throw InvalidArgumentException("Mindre enn ett år fra gradsendring")

            else -> log.warn { "vilkårsprøving ga avslag grunnet $begrunnelse" }
        }
    }
}
