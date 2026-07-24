package no.nav.pensjon.simulator.ytelse.client.pen.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.ytelse.AlderspensjonYtelserFlags
import no.nav.pensjon.simulator.ytelse.EndringAlderspensjonYtelserFlags
import no.nav.pensjon.simulator.ytelse.LoependeYtelserSpec
import no.nav.pensjon.simulator.ytelse.TidsbegrensetOffentligAfpYtelserFlags
import java.time.LocalDate

class PenLoependeYtelserSpecMapperTest : ShouldSpec({

    should("mappe full spec med alle felter utfylt") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = Avdoed(
                pid = Pid("98765432109"),
                antallAarUtenlands = 5,
                inntektFoerDoed = 500000,
                doedDato = LocalDate.of(2023, 3, 15),
                erMedlemAvFolketrygden = true,
                harInntektOver1G = true
            ),
            alderspensjonFlags = AlderspensjonYtelserFlags(inkluderPrivatAfp = true),
            endringAlderspensjonFlags = EndringAlderspensjonYtelserFlags(inkluderPrivatAfp = false),
            tidsbegrensetOffentligAfpYtelserFlags = TidsbegrensetOffentligAfpYtelserFlags(
                gjelderFpp = true,
                sivilstatusUdefinert = false
            )
        )

        PenLoependeYtelserSpecMapper.toDto(spec) shouldBe
                PenLoependeYtelserSpec(
                    pid = "12345678901",
                    foersteUttakDato = LocalDate.of(2024, 6, 1),
                    avdoed = PenAvdoedYtelserSpec(
                        pid = "98765432109",
                        doedDato = LocalDate.of(2023, 3, 15)
                    ),
                    alderspensjonFlags = PenAlderspensjonYtelserFlags(inkluderPrivatAfp = true),
                    endringAlderspensjonFlags = PenEndringAlderspensjonYtelserFlags(inkluderPrivatAfp = false),
                    pre2025OffentligAfpYtelserFlags = PenTidsbegrensetOffentligAfpYtelserFlags(
                        gjelderFpp = true,
                        sivilstatusUdefinert = false
                    )
                )
    }

    should("mappe spec med null pid") {
        val spec = LoependeYtelserSpec(
            pid = null,
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = null,
            endringAlderspensjonFlags = null,
            tidsbegrensetOffentligAfpYtelserFlags = null
        )

        PenLoependeYtelserSpecMapper.toDto(spec) shouldBe
                PenLoependeYtelserSpec(
                    pid = null,
                    foersteUttakDato = LocalDate.of(2024, 6, 1),
                    avdoed = null,
                    alderspensjonFlags = null,
                    endringAlderspensjonFlags = null,
                    pre2025OffentligAfpYtelserFlags = null
                )
    }

    should("mappe spec med kun avdoed utfylt") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = Avdoed(
                pid = Pid("98765432109"),
                antallAarUtenlands = 0,
                inntektFoerDoed = 0,
                doedDato = LocalDate.of(2023, 3, 15)
            ),
            alderspensjonFlags = null,
            endringAlderspensjonFlags = null,
            tidsbegrensetOffentligAfpYtelserFlags = null
        )

        PenLoependeYtelserSpecMapper.toDto(spec) shouldBe
                PenLoependeYtelserSpec(
                    pid = "12345678901",
                    foersteUttakDato = LocalDate.of(2024, 6, 1),
                    avdoed = PenAvdoedYtelserSpec(
                        pid = "98765432109",
                        doedDato = LocalDate.of(2023, 3, 15)
                    ),
                    alderspensjonFlags = null,
                    endringAlderspensjonFlags = null,
                    pre2025OffentligAfpYtelserFlags = null
                )
    }

    should("mappe spec med kun alderspensjonFlags utfylt") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = AlderspensjonYtelserFlags(inkluderPrivatAfp = true),
            endringAlderspensjonFlags = null,
            tidsbegrensetOffentligAfpYtelserFlags = null
        )

        PenLoependeYtelserSpecMapper.toDto(spec) shouldBe
                PenLoependeYtelserSpec(
                    pid = "12345678901",
                    foersteUttakDato = LocalDate.of(2024, 6, 1),
                    avdoed = null,
                    alderspensjonFlags = PenAlderspensjonYtelserFlags(inkluderPrivatAfp = true),
                    endringAlderspensjonFlags = null,
                    pre2025OffentligAfpYtelserFlags = null
                )
    }

    should("mappe spec med kun endringAlderspensjonFlags utfylt") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = null,
            endringAlderspensjonFlags = EndringAlderspensjonYtelserFlags(inkluderPrivatAfp = false),
            tidsbegrensetOffentligAfpYtelserFlags = null
        )

        PenLoependeYtelserSpecMapper.toDto(spec) shouldBe
                PenLoependeYtelserSpec(
                    pid = "12345678901",
                    foersteUttakDato = LocalDate.of(2024, 6, 1),
                    avdoed = null,
                    alderspensjonFlags = null,
                    endringAlderspensjonFlags = PenEndringAlderspensjonYtelserFlags(inkluderPrivatAfp = false),
                    pre2025OffentligAfpYtelserFlags = null
                )
    }

    should("mappe spec med kun 'tidsbegrenset offentlig AFP'-flagg utfylt") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = null,
            endringAlderspensjonFlags = null,
            tidsbegrensetOffentligAfpYtelserFlags = TidsbegrensetOffentligAfpYtelserFlags(
                gjelderFpp = false,
                sivilstatusUdefinert = true
            )
        )

        PenLoependeYtelserSpecMapper.toDto(spec) shouldBe
                PenLoependeYtelserSpec(
                    pid = "12345678901",
                    foersteUttakDato = LocalDate.of(2024, 6, 1),
                    avdoed = null,
                    alderspensjonFlags = null,
                    endringAlderspensjonFlags = null,
                    pre2025OffentligAfpYtelserFlags = PenTidsbegrensetOffentligAfpYtelserFlags(
                        gjelderFpp = false,
                        sivilstatusUdefinert = true
                    )
                )
    }

    should("mappe alderspensjonFlags med inkluderPrivatAfp false") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = AlderspensjonYtelserFlags(inkluderPrivatAfp = false),
            endringAlderspensjonFlags = null,
            tidsbegrensetOffentligAfpYtelserFlags = null
        )

        PenLoependeYtelserSpecMapper.toDto(spec).alderspensjonFlags?.inkluderPrivatAfp shouldBe false
    }

    should("mappe endringAlderspensjonFlags med inkluderPrivatAfp true") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = null,
            endringAlderspensjonFlags = EndringAlderspensjonYtelserFlags(inkluderPrivatAfp = true),
            tidsbegrensetOffentligAfpYtelserFlags = null
        )

        PenLoependeYtelserSpecMapper.toDto(spec).endringAlderspensjonFlags?.inkluderPrivatAfp shouldBe true
    }
})