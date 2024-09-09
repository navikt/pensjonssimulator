package no.nav.pensjon.simulator.core.out

import no.nav.pensjon.simulator.core.ytelse.YtelseKomponentType
import java.time.LocalDate

data class OutputAlderspensjon(
    val alderAar: Int,
    val beloep: Int
)

class OutputAlderspensjonFraFolketrygden(
    val datoFom: LocalDate,
    val delytelseListe: List<OutputDelytelse>,
    val uttakGrad: Int
)

data class OutputDelytelse(
    val pensjonType: YtelseKomponentType,
    val beloep: Int
)
