package no.nav.pensjon.simulator.uttak.api.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.uttak.TidligstMuligUttak
import no.nav.pensjon.simulator.uttak.Uttaksgrad
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import java.time.LocalDate

class UttakResultMapperV1Test : ShouldSpec({

    should("map domain object to data transfer object version 1") {
        UttakResultMapperV1.resultV1(
            source = TidligstMuligUttak(
                uttaksdato = LocalDate.of(2021, 1, 1),
                uttaksgrad = Uttaksgrad.AATTI_PROSENT,
                problem = Problem(
                    type = ProblemType.SERVERFEIL,
                    beskrivelse = "Feil"
                )
            )
        ) shouldBe TidligstMuligUttakResultV1(
            tidligstMuligeUttakstidspunktListe = listOf(
                TidligstMuligUttakV1(
                    uttaksgrad = 80,
                    tidligstMuligeUttaksdato = LocalDate.of(2021, 1, 1)
                )
            ),
            feil = TidligstMuligUttakFeilV1(
                type = TidligstMuligUttakFeilTypeV1.TEKNISK_FEIL,
                beskrivelse = "Feil"
            )
        )
    }
})
