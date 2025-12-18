package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema

@JsonIgnoreProperties(ignoreUnknown = true)
class BrukerRegTjenestepensjonV3 {
    @Schema(description = "Brukeroppgitt navn på TPordningen")
    var navnOrdningTP: String? = null

    @Schema(description = "Brukeroppgitt uttaksgrad for TPordningen")
    private var utgTP: StillingsprOffCodeV3? = null

    @Schema(description = "Starttidspunkt for utbetaling av TPordningen")
    private var utbetStartTP: TpUtbetStartCodeV3? = null

    @Schema(description = "Sluttidspunkt for utbetaling av TPordningen")
    private var utbetSluttTP: TpUtbetSluttCodeV3? = null

    @Schema(description = "Beløp til utbetaling fra TPordningen")
    var belopTP: Int? = null

    fun getUtgTP(): StillingsprOffCodeV3? {
        return utgTP
    }

    fun setUtgTP(utgTP: String?) {
        this.utgTP = if (utgTP != null) StillingsprOffCodeV3.valueOf("P_" + utgTP) else null
    }

    fun getUtbetStartTP(): TpUtbetStartCodeV3? {
        return utbetStartTP
    }

    fun setUtbetStartTP(utbetStartTP: String?) {
        if (utbetStartTP == null) {
            this.utbetStartTP = null
        } else if (TpUtbetStartCodeV3.VED_PENSJONERING.toString().equals(utbetStartTP)) {
            this.utbetStartTP = TpUtbetStartCodeV3.VED_PENSJONERING
        } else {
            this.utbetStartTP = TpUtbetStartCodeV3.valueOf("P_" + utbetStartTP)
        }
    }

    fun getUtbetSluttTP(): TpUtbetSluttCodeV3? {
        return utbetSluttTP
    }

    fun setUtbetSluttTP(utbetSluttTP: String?) {
        if (utbetSluttTP == null) {
            this.utbetSluttTP = null
        } else if (TpUtbetSluttCodeV3.LIVSVARIG.toString().equals(utbetSluttTP)) {
            this.utbetSluttTP = TpUtbetSluttCodeV3.LIVSVARIG
        } else {
            this.utbetSluttTP = TpUtbetSluttCodeV3.valueOf("P_" + utbetSluttTP)
        }
    }
}
