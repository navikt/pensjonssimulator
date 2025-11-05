package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simuleringsperioder

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec
import java.time.LocalDate

data class SimuleringsperioderSpec(
    val afpEtterfulgtAvAlder: Boolean,
    val foedselsdato: LocalDate,
    val stillingsprosentSpec: StillingsprosentSpec,
    val folketrygdUttaksgrad: Int,
    val simuleringType: SimuleringTypeEnum,
    val foersteUttakDato: LocalDate,
    val heltUttakDato: LocalDate?,
    val inntektEtterHeltUttakAntallAar: Long,
)