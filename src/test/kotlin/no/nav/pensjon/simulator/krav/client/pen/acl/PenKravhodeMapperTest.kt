package no.nav.pensjon.simulator.krav.client.pen.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import java.time.LocalDate

class PenKravhodeMapperTest : ShouldSpec({

    should("map persondetalj") {
        val source = PenKravhode().apply {
            persongrunnlagListe = mutableListOf(
                PenPersongrunnlag().apply {
                    personDetaljListe = mutableListOf(
                        PenPersonDetalj(
                            grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER,
                            rolleFomDatoLd = LocalDate.of(2021, 1, 1),
                            rolleTomDatoLd = LocalDate.of(2022, 2, 2),
                            sivilstandTypeEnum = SivilstandEnum.GIFT,
                            sivilstandRelatertPerson = PenPenPerson().apply { penPersonId = 1 },
                            borMedEnum = BorMedTypeEnum.SAMBOER3_2,
                            barnDetalj = PenBarnDetalj().apply { inntektOver1G = true },
                            tillegg = true,
                            bruk = false,
                            grunnlagKildeEnum = GrunnlagkildeEnum.PEN,
                            serskiltSatsUtenET = true,
                            epsAvkallEgenPensjon = false,
                            virkFomLd = LocalDate.of(2023, 3, 3),
                            virkTomLd = LocalDate.of(2024, 4, 4)
                        )
                    )
                })
        }

        val result: Kravhode = PenKravhodeMapper.kravhode(source)

        with(result) {
            with(persongrunnlagListe.first()) {
                with(personDetaljListe.first()) {
                    grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
                    penRolleFom shouldBe LocalDate.of(2021, 1, 1)
                    penRolleTom shouldBe LocalDate.of(2022, 2, 2)
                    rolleFomDatoLd shouldBe LocalDate.of(2023, 3, 3) // set to virkFom in mapper
                    rolleTomDatoLd shouldBe LocalDate.of(2024, 4, 4) // set to virkTom in mapper
                    virkFom shouldBe LocalDate.of(2023, 3, 3)
                    virkTom shouldBe LocalDate.of(2024, 4, 4)
                    sivilstandTypeEnum shouldBe SivilstandEnum.GIFT
                    with(sivilstandRelatertPerson!!) { penPersonId shouldBe 1 }
                    borMedEnum shouldBe BorMedTypeEnum.SAMBOER3_2
                    with(barnDetalj!!) { inntektOver1G shouldBe true }
                    tillegg shouldBe true
                    bruk shouldBe false
                    grunnlagKildeEnum shouldBe GrunnlagkildeEnum.PEN
                    serskiltSatsUtenET shouldBe true
                    epsAvkallEgenPensjon shouldBe false
                }
            }
        }
    }

    should("map kravlinje") {
        val source = PenKravhode().apply {
            kravlinjeListe = mutableListOf(
                PenKravlinje().apply {
                    kravlinjeStatus = KravlinjeStatus.FERDIG
                    land = "CZE_V1"
                    kravlinjeTypeEnum = KravlinjeTypeEnum.BP
                    hovedKravlinje = true
                    relatertPerson = PenPenPerson().apply { penPersonId = 1L }
                })
        }

        val result: Kravhode = PenKravhodeMapper.kravhode(source)

        with(result) {
            with(kravlinjeListe.first()) {
                kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.BP
                relatertPerson?.penPersonId shouldBe 1L
                hovedKravlinje shouldBe true
                kravlinjeStatus shouldBe KravlinjeStatus.FERDIG
                land shouldBe LandkodeEnum.CZE
            }
        }
    }
})