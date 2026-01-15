package no.nav.pensjon.simulator.afp.privat

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.TestObjects.persongrunnlag
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate
import java.util.*

class PrivatAfpBeregnerTest : ShouldSpec({

    should("return forrigePrivatAfpBeregningResult when no knekkpunktDatoer") {
        val soekerGrunnlag = Persongrunnlag().apply {
            personDetaljListe = mutableListOf(PersonDetalj().apply {
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                bruk = true
            })
        }
        val beregningResultat = BeregningsResultatAfpPrivat().apply {
            afpPrivatBeregning = AfpPrivatBeregning().apply {
                afpPrivatLivsvarig = AfpPrivatLivsvarig().apply {
                    justeringsbelop = 123
                }
            }
        }
        val knekkpunktFinder = mockk<PrivatAfpKnekkpunktFinder>().apply {
            every { findKnekkpunktDatoer(any(), any(), any(), any()) } returns TreeSet() // no knekkpunktDatoer
        }

        val result = PrivatAfpBeregner(
            context = mockk(),
            generelleDataHolder = mockk(),
            knekkpunktFinder,
            time = mockk()
        ).beregnPrivatAfp(
            PrivatAfpSpec(
                kravhode = Kravhode().apply { persongrunnlagListe.add(soekerGrunnlag) },
                virkningFom = LocalDate.of(2021, 1, 1),
                foersteUttakDato = LocalDate.of(2022, 1, 1),
                forrigePrivatAfpBeregningResult = beregningResultat,
                gjelderOmsorg = false,
                sakId = null
            )
        )

        result.gjeldendeBeregningsresultatAfpPrivat?.afpPrivatBeregning
            ?.afpPrivatLivsvarig?.justeringsbelop shouldBe 123
    }

    should("returnere forrige beregningsresultat hvis ingen knekkpunkter") {
        val result = PrivatAfpBeregner(
            context = mockk(),
            generelleDataHolder = mockk(),
            knekkpunktFinder = utenKnekkpunkter(),
            time = mockk()
        ).beregnPrivatAfp(
            PrivatAfpSpec(
                kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag) },
                virkningFom = LocalDate.of(2025, 1, 1),
                foersteUttakDato = LocalDate.of(2024, 1, 1),
                forrigePrivatAfpBeregningResult = BeregningsResultatAfpPrivat().apply {
                    afpPrivatBeregning =
                        AfpPrivatBeregning().apply {
                            afpPrivatLivsvarig = AfpPrivatLivsvarig().apply { justeringsbelop = 1 }
                        }
                },
                gjelderOmsorg = false,
                sakId = null
            )
        )

        result.gjeldendeBeregningsresultatAfpPrivat?.afpPrivatBeregning?.afpPrivatLivsvarig?.justeringsbelop shouldBe 1
        result.afpPrivatBeregningsresultatListe[0] shouldBe result.gjeldendeBeregningsresultatAfpPrivat
    }

    should("gi klar feilmelding når ugyldig trygdetid ved framtidig vedtaksstart") {
        val context = mockk<SimulatorContext>(relaxed = true).apply {
            every {
                beregnPrivatAfp(any(), any())
            } throws RegelmotorValideringException(
                message = "",
                merknadListe = listOf(
                    logiskSammenhengMerknad(aarsak = "TrygdetidenErIkkeGyldigIBeregningsperioden"),
                    logiskSammenhengMerknad(aarsak = "TrygdetidKapittel20ErIkkeGyldigIBeregningsperioden"),
                )
            )
        }

        shouldThrow<BadSpecException> {
            PrivatAfpBeregner(
                context,
                generelleDataHolder = mockk(relaxed = true),
                knekkpunktFinder = arrangeFramtidigKnekkpunkt(),
                time = mockk<Time>().apply {
                    every { today() } returns today
                }
            ).beregnPrivatAfp(
                PrivatAfpSpec(
                    kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag) },
                    virkningFom = LocalDate.of(2023, 1, 1),
                    foersteUttakDato = LocalDate.of(2024, 1, 1),
                    forrigePrivatAfpBeregningResult = BeregningsResultatAfpPrivat().apply {
                        afpPrivatBeregning =
                            AfpPrivatBeregning().apply {
                                afpPrivatLivsvarig = AfpPrivatLivsvarig().apply { justeringsbelop = 1 }
                            }
                    },
                    gjelderOmsorg = false,
                    sakId = null
                )
            )
        }.message shouldBe "Personen har et vedtak med virkning f.o.m. 2026-02-01;" +
                " uttaksdato må være etter denne datoen"
    }
})

private val today = LocalDate.of(2026, 1, 1)

private fun arrangeFramtidigKnekkpunkt(): PrivatAfpKnekkpunktFinder =
    mockk<PrivatAfpKnekkpunktFinder>().apply {
        every {
            findKnekkpunktDatoer(any(), any(), any(), any())
        } returns TreeSet<LocalDate>().apply { add(today.plusMonths(1)) }
    }

private fun utenKnekkpunkter(): PrivatAfpKnekkpunktFinder =
    mockk<PrivatAfpKnekkpunktFinder>().apply {
        every {
            findKnekkpunktDatoer(any(), any(), any(), any())
        } returns TreeSet()
    }

private fun logiskSammenhengMerknad(aarsak: String) =
    Merknad().apply {
        kode = "TRYGDETID_KontrollerTrygdetidLogiskSammenhengBeregningRS.$aarsak"
    }
