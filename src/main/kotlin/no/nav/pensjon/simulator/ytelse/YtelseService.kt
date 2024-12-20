package no.nav.pensjon.simulator.ytelse

import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.ytelse.client.YtelseClient
import org.springframework.stereotype.Service

/**
 * Corresponds to those parts of AbstraktSimulerAPFra2011Command, SimulerFleksibelAPCommand, SimulerAFPogAPCommand
 * and SimulerEndringAvAPCommand that handle 'løpende ytelser'
 */
@Service
class YtelseService(private val client: YtelseClient) {

    fun getLoependeYtelser(spec: SimuleringSpec): LoependeYtelser {
        if (spec.gjelderPre2025OffentligAfp()) {
            // SimulerAFPogAPCommand
            val ytelser: LoependeYtelserResult = client.fetchLoependeYtelser(
                LoependeYtelserSpec(
                    pid = spec.pid!!,
                    foersteUttakDato = spec.foersteUttakDato!!,
                    avdoed = spec.avdoed,
                    alderspensjonFlags = null,
                    endringAlderspensjonFlags = null,
                    pre2025OffentligAfpYtelserFlags = Pre2025OffentligAfpYtelserFlags(
                        gjelderFpp = spec.type == SimuleringType.AFP_FPP,
                        sivilstatusUdefinert = false //TODO check if this can happen: spec.sivilstatus == null
                    )
                )
            )

            return LoependeYtelser(
                soekerVirkningFom = ytelser.alderspensjon?.sokerVirkningFom!!,
                avdoedVirkningFom = ytelser.alderspensjon.avdodVirkningFom,
                privatAfpVirkningFom = ytelser.afpPrivat?.virkningFom,
                sisteBeregning = ytelser.alderspensjon.sisteBeregning,
                forrigeAlderspensjonBeregningResultat = ytelser.alderspensjon.forrigeBeregningsresultat,
                forrigePrivatAfpBeregningResultat = ytelser.afpPrivat?.forrigeBeregningsresultat,
                forrigeVedtakListe = norskeVedtak(ytelser.alderspensjon.forrigeVilkarsvedtakListe)
            )
        }

        if (spec.gjelderEndring()) {
            // SimulerEndringAvAPCommand
            val ytelser: LoependeYtelserResult = client.fetchLoependeYtelser(
                LoependeYtelserSpec(
                    pid = spec.pid!!,
                    foersteUttakDato = spec.foersteUttakDato!!,
                    avdoed = spec.avdoed,
                    alderspensjonFlags = null,
                    endringAlderspensjonFlags = EndringAlderspensjonYtelserFlags(
                        inkluderPrivatAfp = spec.type == SimuleringType.ENDR_AP_M_AFP_PRIVAT
                    ),
                    pre2025OffentligAfpYtelserFlags = null
                )
            )

            return LoependeYtelser(
                soekerVirkningFom = ytelser.alderspensjon?.sokerVirkningFom!!,
                avdoedVirkningFom = ytelser.alderspensjon.avdodVirkningFom,
                privatAfpVirkningFom = ytelser.afpPrivat?.virkningFom,
                sisteBeregning = ytelser.alderspensjon.sisteBeregning,
                forrigeAlderspensjonBeregningResultat = ytelser.alderspensjon.forrigeBeregningsresultat,
                forrigePrivatAfpBeregningResultat = ytelser.afpPrivat?.forrigeBeregningsresultat,
                forrigeVedtakListe = norskeVedtak(ytelser.alderspensjon.forrigeVilkarsvedtakListe)
            )
        }

        // SimulerFleksibelAPCommand
        val ytelser: LoependeYtelserResult = client.fetchLoependeYtelser(
            LoependeYtelserSpec(
                pid = spec.pid,
                foersteUttakDato = spec.foersteUttakDato!!,
                avdoed = spec.avdoed,
                alderspensjonFlags = AlderspensjonYtelserFlags(
                    inkluderPrivatAfp = spec.type == SimuleringType.ALDER_M_AFP_PRIVAT
                ),
                endringAlderspensjonFlags = null,
                pre2025OffentligAfpYtelserFlags = null
            )
        )

        return LoependeYtelser(
            soekerVirkningFom = ytelser.alderspensjon?.sokerVirkningFom!!,
            avdoedVirkningFom = ytelser.alderspensjon.avdodVirkningFom,
            privatAfpVirkningFom = ytelser.afpPrivat?.virkningFom,
            sisteBeregning = null,
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            forrigeVedtakListe = mutableListOf() //TODO use value in ytelser?
        )
    }

    private companion object {
        // AbstraktSimulerAPFra2011Command.filterVilkarsVedtakListOnNOR
        private fun norskeVedtak(vedtakListe: List<VilkarsVedtak>): MutableList<VilkarsVedtak> =
            vedtakListe.filter { Land.NOR == it.kravlinje?.land }.toMutableList()
    }
}
