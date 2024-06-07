package no.nav.pensjon.simulator.alderspensjon.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.*
import java.time.LocalDate

class AlderspensjonResultMapperV4Test : FunSpec({

    test("resultV4 maps from domain to DTO version 4") {
        AlderspensjonResultMapperV4.resultV4(
            AlderspensjonResult(
                simuleringSuksess = true,
                aarsakListeIkkeSuksess = emptyList(),
                alderspensjon = listOf(
                    AlderspensjonFraFolketrygden(
                        fom = LocalDate.of(2031, 2, 3),
                        delytelseListe = listOf(
                            PensjonDelytelse(
                                pensjonType = PensjonType.INNTEKTSPENSJON,
                                beloep = 123000
                            )
                        ),
                        uttaksgrad = Uttaksgrad.FEMTI_PROSENT
                    )
                ),
                alternativerVedForLavOpptjening = null,
                harUttak = false
            )
        ) shouldBe
                AlderspensjonResultV4(
                    simuleringSuksess = true,
                    aarsakListeIkkeSuksess = emptyList(),
                    alderspensjon = listOf(
                        AlderspensjonFraFolketrygdenV4(
                            fraOgMedDato = LocalDate.of(2031, 2, 3),
                            delytelseListe = listOf(
                                PensjonDelytelseV4(
                                    pensjonsType = "inntektsPensjon",
                                    belop = 123000
                                )
                            ),
                            uttaksgrad = 50
                        )
                    ),
                    alternativerVedForLavOpptjening = null,
                    harUttak = false
                )
    }
})
