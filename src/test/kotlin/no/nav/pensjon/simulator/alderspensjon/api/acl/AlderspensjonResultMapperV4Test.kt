package no.nav.pensjon.simulator.alderspensjon.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonFraFolketrygden
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonResult
import no.nav.pensjon.simulator.alderspensjon.ForslagVedForLavOpptjening
import no.nav.pensjon.simulator.alderspensjon.GradertUttak
import no.nav.pensjon.simulator.alderspensjon.PensjonDelytelse
import no.nav.pensjon.simulator.alderspensjon.PensjonSimuleringStatus
import no.nav.pensjon.simulator.alderspensjon.PensjonSimuleringStatusKode
import no.nav.pensjon.simulator.alderspensjon.PensjonType
import no.nav.pensjon.simulator.alderspensjon.Uttaksgrad
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.*
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
                forslagVedForLavOpptjening = null,
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
                    forslagVedForLavOpptjening = null,
                    harUttak = false
                )
    }

    test("resultV4 maps simuleringstatus 'none' to 'annet'") {
        AlderspensjonResultMapperV4.resultV4(
            AlderspensjonResult(
                simuleringSuksess = false,
                aarsakListeIkkeSuksess = listOf(
                    PensjonSimuleringStatus(
                        statusKode = PensjonSimuleringStatusKode.NONE,
                        statusBeskrivelse = "..."
                    )
                ),
                alderspensjon = listOf(
                    AlderspensjonFraFolketrygden(
                        fom = LocalDate.of(2026, 4, 1),
                        delytelseListe = listOf(
                            PensjonDelytelse(pensjonType = PensjonType.INNTEKTSPENSJON, beloep = 147026)
                        ),
                        uttaksgrad = Uttaksgrad.AATTI_PROSENT
                    )
                ),
                forslagVedForLavOpptjening = ForslagVedForLavOpptjening(
                    gradertUttak = GradertUttak(
                        fom = LocalDate.of(2026, 4, 1),
                        uttaksgrad = Uttaksgrad.AATTI_PROSENT
                    ),
                    heltUttakFom = LocalDate.of(2027, 2, 1)
                ),
                harUttak = false
            )
        ) shouldBe
                AlderspensjonResultV4(
                    simuleringSuksess = false,
                    aarsakListeIkkeSuksess = listOf(
                        PensjonSimuleringStatusV4(
                            statusKode = "ANNET",
                            statusBeskrivelse = "..."
                        )
                    ),
                    alderspensjon = listOf(
                        AlderspensjonFraFolketrygdenV4(
                            fraOgMedDato = LocalDate.of(2026, 4, 1),
                            delytelseListe = listOf(
                                PensjonDelytelseV4(
                                    pensjonsType = "inntektsPensjon",
                                    belop = 147026
                                )
                            ),
                            uttaksgrad = 80
                        )
                    ),
                    forslagVedForLavOpptjening = ForslagVedForLavOpptjeningV4(
                        gradertUttak = GradertUttakV4(
                            fraOgMedDato = LocalDate.of(2026, 4, 1),
                            uttaksgrad = 80
                        ),
                        heltUttakFraOgMedDato = LocalDate.of(2027, 2, 1)
                    ),
                    harUttak = false
                )
    }
})
