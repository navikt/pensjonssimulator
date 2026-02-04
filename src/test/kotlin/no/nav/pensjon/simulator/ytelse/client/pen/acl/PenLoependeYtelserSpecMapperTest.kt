package no.nav.pensjon.simulator.ytelse.client.pen.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.ytelse.AlderspensjonYtelserFlags
import no.nav.pensjon.simulator.ytelse.EndringAlderspensjonYtelserFlags
import no.nav.pensjon.simulator.ytelse.LoependeYtelserSpec
import no.nav.pensjon.simulator.ytelse.Pre2025OffentligAfpYtelserFlags
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
            pre2025OffentligAfpYtelserFlags = Pre2025OffentligAfpYtelserFlags(
                gjelderFpp = true,
                sivilstatusUdefinert = false
            )
        )

        val result = PenLoependeYtelserSpecMapper.toDto(spec)

        result.pid shouldBe "12345678901"
        result.foersteUttakDato shouldBe LocalDate.of(2024, 6, 1)
        result.avdoed?.pid shouldBe "98765432109"
        result.avdoed?.doedDato shouldBe LocalDate.of(2023, 3, 15)
        result.alderspensjonFlags?.inkluderPrivatAfp shouldBe true
        result.endringAlderspensjonFlags?.inkluderPrivatAfp shouldBe false
        result.pre2025OffentligAfpYtelserFlags?.gjelderFpp shouldBe true
        result.pre2025OffentligAfpYtelserFlags?.sivilstatusUdefinert shouldBe false
    }

    should("mappe spec med null pid") {
        val spec = LoependeYtelserSpec(
            pid = null,
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = null,
            endringAlderspensjonFlags = null,
            pre2025OffentligAfpYtelserFlags = null
        )

        val result = PenLoependeYtelserSpecMapper.toDto(spec)

        result.pid shouldBe null
        result.foersteUttakDato shouldBe LocalDate.of(2024, 6, 1)
        result.avdoed shouldBe null
        result.alderspensjonFlags shouldBe null
        result.endringAlderspensjonFlags shouldBe null
        result.pre2025OffentligAfpYtelserFlags shouldBe null
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
            pre2025OffentligAfpYtelserFlags = null
        )

        val result = PenLoependeYtelserSpecMapper.toDto(spec)

        result.avdoed?.pid shouldBe "98765432109"
        result.avdoed?.doedDato shouldBe LocalDate.of(2023, 3, 15)
        result.alderspensjonFlags shouldBe null
        result.endringAlderspensjonFlags shouldBe null
        result.pre2025OffentligAfpYtelserFlags shouldBe null
    }

    should("mappe spec med kun alderspensjonFlags utfylt") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = AlderspensjonYtelserFlags(inkluderPrivatAfp = true),
            endringAlderspensjonFlags = null,
            pre2025OffentligAfpYtelserFlags = null
        )

        val result = PenLoependeYtelserSpecMapper.toDto(spec)

        result.avdoed shouldBe null
        result.alderspensjonFlags?.inkluderPrivatAfp shouldBe true
        result.endringAlderspensjonFlags shouldBe null
        result.pre2025OffentligAfpYtelserFlags shouldBe null
    }

    should("mappe spec med kun endringAlderspensjonFlags utfylt") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = null,
            endringAlderspensjonFlags = EndringAlderspensjonYtelserFlags(inkluderPrivatAfp = false),
            pre2025OffentligAfpYtelserFlags = null
        )

        val result = PenLoependeYtelserSpecMapper.toDto(spec)

        result.avdoed shouldBe null
        result.alderspensjonFlags shouldBe null
        result.endringAlderspensjonFlags?.inkluderPrivatAfp shouldBe false
        result.pre2025OffentligAfpYtelserFlags shouldBe null
    }

    should("mappe spec med kun pre2025OffentligAfpYtelserFlags utfylt") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = null,
            endringAlderspensjonFlags = null,
            pre2025OffentligAfpYtelserFlags = Pre2025OffentligAfpYtelserFlags(
                gjelderFpp = false,
                sivilstatusUdefinert = true
            )
        )

        val result = PenLoependeYtelserSpecMapper.toDto(spec)

        result.avdoed shouldBe null
        result.alderspensjonFlags shouldBe null
        result.endringAlderspensjonFlags shouldBe null
        result.pre2025OffentligAfpYtelserFlags?.gjelderFpp shouldBe false
        result.pre2025OffentligAfpYtelserFlags?.sivilstatusUdefinert shouldBe true
    }

    should("mappe alderspensjonFlags med inkluderPrivatAfp false") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = AlderspensjonYtelserFlags(inkluderPrivatAfp = false),
            endringAlderspensjonFlags = null,
            pre2025OffentligAfpYtelserFlags = null
        )

        val result = PenLoependeYtelserSpecMapper.toDto(spec)

        result.alderspensjonFlags?.inkluderPrivatAfp shouldBe false
    }

    should("mappe endringAlderspensjonFlags med inkluderPrivatAfp true") {
        val spec = LoependeYtelserSpec(
            pid = Pid("12345678901"),
            foersteUttakDato = LocalDate.of(2024, 6, 1),
            avdoed = null,
            alderspensjonFlags = null,
            endringAlderspensjonFlags = EndringAlderspensjonYtelserFlags(inkluderPrivatAfp = true),
            pre2025OffentligAfpYtelserFlags = null
        )

        val result = PenLoependeYtelserSpecMapper.toDto(spec)

        result.endringAlderspensjonFlags?.inkluderPrivatAfp shouldBe true
    }
})
