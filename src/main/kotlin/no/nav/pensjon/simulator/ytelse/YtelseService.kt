package no.nav.pensjon.simulator.ytelse

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.ytelse.client.YtelseClient
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Corresponds to those parts of AbstraktSimulerAPFra2011Command, SimulerFleksibelAPCommand, SimulerAFPogAPCommand
 * and SimulerEndringAvAPCommand that handle 'løpende ytelser'.
 * NB: It is assumed that forrigeVilkarsvedtakListe only contains 'norske vedtak'
 *     (ref. ReglerLoependeYtelserMapper in PEN).
 */
@Service
open class YtelseService(private val client: YtelseClient) {

    //@Cacheable(value = ["loependeYtelser"])
    open fun getLoependeYtelser(spec: SimuleringSpec): LoependeYtelser {
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
                        gjelderFpp = spec.type == SimuleringTypeEnum.AFP_FPP,
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
                forrigeVedtakListe = ytelser.alderspensjon.forrigeVilkarsvedtakListe.toMutableList()
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
                        inkluderPrivatAfp = spec.type == SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT
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
                forrigeVedtakListe = ytelser.alderspensjon.forrigeVilkarsvedtakListe.toMutableList()
            )
        }

        if (spec.type == SimuleringTypeEnum.AFP_FPP) { // ref. PEN: SimulerAFPogAPCommand.hentLopendeYtelser line 103
            return LoependeYtelser(
                soekerVirkningFom = LocalDate.of(1901, 1, 1),
                avdoedVirkningFom = null,
                privatAfpVirkningFom = null,
                sisteBeregning = null,
                forrigeAlderspensjonBeregningResultat = null,
                forrigePrivatAfpBeregningResultat = null,
                forrigeVedtakListe = mutableListOf()
            )
        }

        // SimulerFleksibelAPCommand
        val ytelser: LoependeYtelserResult = client.fetchLoependeYtelser(
            LoependeYtelserSpec(
                pid = spec.pid,
                foersteUttakDato = spec.foersteUttakDato!!,
                avdoed = spec.avdoed,
                alderspensjonFlags = AlderspensjonYtelserFlags(
                    inkluderPrivatAfp = spec.type == SimuleringTypeEnum.ALDER_M_AFP_PRIVAT
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
}
