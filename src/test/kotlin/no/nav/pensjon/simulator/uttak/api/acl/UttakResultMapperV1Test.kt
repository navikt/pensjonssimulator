package no.nav.pensjon.simulator.uttak.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.uttak.TidligstMuligUttak
import no.nav.pensjon.simulator.uttak.TidligstMuligUttakFeil
import no.nav.pensjon.simulator.uttak.TidligstMuligUttakFeilType
import no.nav.pensjon.simulator.uttak.Uttaksgrad
import java.time.LocalDate

class UttakResultMapperV1Test : FunSpec({

    test("resultV1 should map domain object to DTO version 1") {
        UttakResultMapperV1.resultV1(
            source = TidligstMuligUttak(
                uttakDato = LocalDate.of(2021, 1, 1),
                uttaksgrad = Uttaksgrad.AATTI_PROSENT,
                feil = TidligstMuligUttakFeil(
                    type = TidligstMuligUttakFeilType.TEKNISK_FEIL,
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
