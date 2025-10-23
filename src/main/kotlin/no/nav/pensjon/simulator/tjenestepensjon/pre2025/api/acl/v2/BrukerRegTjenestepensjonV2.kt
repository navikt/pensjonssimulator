package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema

@JsonIgnoreProperties(ignoreUnknown = true)
class BrukerRegTjenestepensjonV2 {
    @Schema(description = "Brukeroppgitt navn på TPordningen")
    var navnOrdningTP: String? = null

    @Schema(description = "Brukeroppgitt uttaksgrad for TPordningen")
    private var utgTP: StillingsprOffCodeV2? = null

    @Schema(description = "Starttidspunkt for utbetaling av TPordningen")
    private var utbetStartTP: TpUtbetStartCodeV2? = null

    @Schema(description = "Sluttidspunkt for utbetaling av TPordningen")
    private var utbetSluttTP: TpUtbetSluttCodeV2? = null

    @Schema(description = "Beløp til utbetaling fra TPordningen")
    var belopTP: Int? = null

    fun getUtgTP(): StillingsprOffCodeV2? {
        return utgTP
    }

    fun setUtgTP(utgTP: String?) {
        this.utgTP = if (utgTP != null) StillingsprOffCodeV2.valueOf("P_" + utgTP) else null
    }

    fun getUtbetStartTP(): TpUtbetStartCodeV2? {
        return utbetStartTP
    }

    fun setUtbetStartTP(utbetStartTP: String?) {
        if (utbetStartTP == null) {
            this.utbetStartTP = null
        } else if (TpUtbetStartCodeV2.VED_PENSJONERING.toString().equals(utbetStartTP)) {
            this.utbetStartTP = TpUtbetStartCodeV2.VED_PENSJONERING
        } else {
            this.utbetStartTP = TpUtbetStartCodeV2.valueOf("P_" + utbetStartTP)
        }
    }

    fun getUtbetSluttTP(): TpUtbetSluttCodeV2? {
        return utbetSluttTP
    }

    fun setUtbetSluttTP(utbetSluttTP: String?) {
        if (utbetSluttTP == null) {
            this.utbetSluttTP = null
        } else if (TpUtbetSluttCodeV2.LIVSVARIG.toString().equals(utbetSluttTP)) {
            this.utbetSluttTP = TpUtbetSluttCodeV2.LIVSVARIG
        } else {
            this.utbetSluttTP = TpUtbetSluttCodeV2.valueOf("P_" + utbetSluttTP)
        }
    }
}
