package no.nav.pensjon.simulator.core.ytelse

import java.time.LocalDate

// no.nav.service.pensjon.simulering.abstractsimulerapfra2011.HentLopendeYtelserResponse
/**
 * NB: The following fields are included in HentLopendeYtelserResponse,
 *     but are irrelevant when simulering fleksibel AP (with or without AFP privat):
 * forrigeAlderBeregningsresultat, forrigeVilkarsvedtakListe, forrigeAfpPrivatBeregningsresultat, sisteBeregning
 * (Check response in SimulerFleksibelAPCommand.hentLopendeYtelser to verify)
 */
data class LoependeYtelseResult(
    val soekerFoersteVirkningDato: LocalDate?,
    val avdoedFoersteVirkningDato: LocalDate?,
    val privatAfpFoersteVirkningDato: LocalDate?
)
