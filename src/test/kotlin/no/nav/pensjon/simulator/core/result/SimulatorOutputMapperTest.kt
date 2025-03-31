package no.nav.pensjon.simulator.core.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag

class SimulatorOutputMapperTest : FunSpec({

    test("mapToSimulertOpptjening should map poengtall.pp to pensjonsgivendeInntektPensjonspoeng") {
        SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = listOf(
                Poengtall().apply {
                    ar = 2024
                    pp = 1.23
                },
            ),
            useNullAsDefaultPensjonspoeng = false
        ).pensjonsgivendeInntektPensjonspoeng shouldBe 1.23
    }

    test("mapToSimulertOpptjening should return undefined pensjonpoeng when no poengtall-liste is present and useNullAsDefaultPensjonspoeng is true") {
        SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2023, // not present in poengtallListe
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).pensjonsgivendeInntektPensjonspoeng shouldBe null
    }

    test("mapToSimulertOpptjening should return zero pensjonpoeng when no poengtall for angitt aar and useNullAsDefaultPensjonspoeng is false") {
        SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2023, // not present in poengtallListe
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = listOf(
                Poengtall().apply {
                    ar = 2024
                    pp = 1.23
                },
            ),
            useNullAsDefaultPensjonspoeng = false
        ).pensjonsgivendeInntektPensjonspoeng shouldBe 0.0
    }
})
