package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2

import no.nav.pensjon.simulator.alder.Alder

data class SimulerOffentligTjenestepensjonFra2025ResultV2(
    val simuleringsResultatStatus: SimuleringsResultatStatusDtoV2,
    val simuleringsResultat: SimuleringsResultatDtoV2? = null,
    val relevanteTpOrdninger: List<RelevantOrdningV2> = emptyList(),
    var serviceData: List<String> = emptyList(),
) {
    constructor(resultatTypeDto: ResultatTypeDtoV2, feilmelding: String?, tpOrdninger: List<RelevantOrdningV2>) : this(
        SimuleringsResultatStatusDtoV2(resultatTypeDto, feilmelding), null, tpOrdninger
    )
}

data class SimuleringsResultatStatusDtoV2(
    val resultatType: ResultatTypeDtoV2,
    val feilmelding: String? = null,
)

enum class ResultatTypeDtoV2 {
    SUCCESS,
    BRUKER_ER_IKKE_MEDLEM_HOS_TP_ORDNING,
    TP_ORDNING_ER_IKKE_STOTTET,
    INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING,
    TEKNISK_FEIL_FRA_TP_ORDNING,
}

data class SimuleringsResultatDtoV2(
    val tpLeverandoer: String,
    val tpNummer: String,
    val utbetalingsperioder: List<UtbetalingPerAlderV2>,
    val betingetTjenestepensjonErInkludert: Boolean,
)

data class UtbetalingPerAlderV2(
    val startAlder: Alder,
    val sluttAlder: Alder?,
    val maanedligBeloep: Int,
)

data class RelevantOrdningV2(
    val navn: String,
    val tpNummer: String,
)