package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Feilkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringsresultatVedFeil
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode
import java.time.LocalDate
import java.time.Month

class SpkFeilhaandteringTest : ShouldSpec({

    context("beregning gir 0 i utbetaling") {
        should("gi feilkode 'beregning gir 0 utbetaling' og to 'default' utbetalingsperioder") {
            val exception = shouldThrow<TjenestepensjonException> {
                SpkFeilhaandtering().haandterFeil(
                    EgressException(
                        """{"errorCode":"CALC002","message":"Validation problem: Beregning gir 0 i utbetaling."}"""
                    ),
                    foedselsdato
                )
            }

            with(exception) {
                message shouldBe "prognosekall til SPK feilet"
                resultat shouldBe TjenestepensjonSimuleringsresultatVedFeil(
                    utbetalingsperiodeListe = listOf(
                        Utbetalingsperiode(
                            datoFom = LocalDate.of(2030, Month.FEBRUARY, 1),
                            datoTom = LocalDate.of(2032, Month.FEBRUARY, 1),
                            uttaksgrad = 100,
                            arligUtbetaling = 0.0,
                            ytelsekode = YtelseCode.AFP
                        ),
                        Utbetalingsperiode(
                            datoFom = LocalDate.of(2032, Month.FEBRUARY, 1),
                            datoTom = null,
                            uttaksgrad = 100,
                            arligUtbetaling = 0.0,
                            ytelsekode = YtelseCode.AP
                        )
                    ),
                    feilkode = Feilkode.BEREGNING_GIR_NULL_UTBETALING
                )
            }
        }
    }

    context("tjenestetid mindre enn 3 år") {
        should("gi feilkode 'beregning gir null utbetaling' og ingen utbetalingsperioder") {
            shouldThrow<TjenestepensjonException> {
                SpkFeilhaandtering().haandterFeil(
                    EgressException(
                        """{"errorCode":"CALC002","message":"Validation problem: Tjenestetid mindre enn 3 år."}"""
                    ),
                    foedselsdato
                )
            }.resultat shouldBe TjenestepensjonSimuleringsresultatVedFeil(
                utbetalingsperiodeListe = emptyList(),
                feilkode = Feilkode.OPPFYLLER_IKKE_INNGANGSVILKAAR
            )
        }
    }

    context("flere samtidige stillinger") {
        should("gi feilkode 'teknisk feil' og ingen utbetalingsperioder") { // NB: 'teknisk feil'
            shouldThrow<TjenestepensjonException> {
                SpkFeilhaandtering().haandterFeil(
                    EgressException(
                        """{"errorCode":"CALC002","message":"Validation problem: Flere samtidige stillinger er ikke støttet."}""",
                    ),
                    foedselsdato
                )
            }.resultat shouldBe TjenestepensjonSimuleringsresultatVedFeil(
                utbetalingsperiodeListe = emptyList(),
                feilkode = Feilkode.TEKNISK_FEIL
            )
        }
    }

    context("teknisk feil") {
        should("gi feilkode 'teknisk feil' og ingen utbetalingsperioder") {
            shouldThrow<TjenestepensjonException> {
                SpkFeilhaandtering().haandterFeil(
                    EgressException("""{"errorCode":"CALC001","message":"Some technical error"}"""),
                    foedselsdato
                )
            }.resultat shouldBe TjenestepensjonSimuleringsresultatVedFeil(
                utbetalingsperiodeListe = emptyList(),
                feilkode = Feilkode.TEKNISK_FEIL
            )
        }
    }

    context("ukjent feilkode") {
        should("gi feilkode 'teknisk feil' og ingen utbetalingsperioder") {
            shouldThrow<TjenestepensjonException> {
                SpkFeilhaandtering().haandterFeil(
                    EgressException("""{"errorCode":"UNKNOWN","message":"Unknown error"}"""),
                    foedselsdato
                )
            }.resultat shouldBe TjenestepensjonSimuleringsresultatVedFeil(
                utbetalingsperiodeListe = emptyList(),
                feilkode = Feilkode.TEKNISK_FEIL
            )
        }
    }
})

private val foedselsdato = LocalDate.of(1965, Month.JANUARY, 15)