package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelsetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.io.Serializable

class InfoPavirkendeYtelse : Serializable {
    /**
     * Liste av alle vilkårsvedtak for hovedytelse som EPS har løpende.
     */
    var vilkarsvedtakEPSListe: MutableList<VilkarsVedtak> = mutableListOf()

    /**
     * EPS uforegrad dersom EPS har uførepensjon.
     */
    var uforegradEPS = 0

    /**
     * Hvis vilkarsvedtakEPSListen er tom og det finnes en tjenestepensjon for ektefellen som
     * ikke blir beregnet av PESYS skal denne fylles ut.
     */
    var tjenestepensjonsordningEpsEnum: YtelsetypeEnum? = null

    /**
     * Satt til true dersom EPS mottar omstillingsstonad (utbetalt beløp > 0).
     */
    var mottarOmstillingsstonadEps: Boolean = false
}
