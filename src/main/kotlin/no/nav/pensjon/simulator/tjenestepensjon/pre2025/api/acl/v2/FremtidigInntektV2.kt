package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
class FremtidigInntektV2 {
    @Schema(description = "Fra og med dato for inntekten")
    val datoFom: LocalDate? = null

    @Schema(
        description = ("Årlig inntekt for perioden, dersom perioden er kortere enn et helt år, gjøres den om til årlig inntekt ved å kalkulere fra x/12 deler der x er antall "
                + "måneder inntekten er angitt for")
    )
    val arliginntekt: Int? = null
}
