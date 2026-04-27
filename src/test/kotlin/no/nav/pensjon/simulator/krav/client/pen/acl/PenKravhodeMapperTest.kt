package no.nav.pensjon.simulator.krav.client.pen.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class PenKravhodeMapperTest : FunSpec({

    test("'kravhode' maps persondetalj") {
        val source = PenKravhode().apply {
            persongrunnlagListe = mutableListOf(
                PenPersongrunnlag().apply {
                    personDetaljListe = mutableListOf(
                        PenPersonDetalj(
                            grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER,
                            rolleFomDato = LocalDate.of(2021, 1, 1).toNorwegianDateAtNoon(),
                            rolleTomDato = LocalDate.of(2022, 2, 2).toNorwegianDateAtNoon(),
                            sivilstandTypeEnum = SivilstandEnum.GIFT,
                            sivilstandRelatertPerson = PenPenPerson().apply { penPersonId = 1 },
                            borMedEnum = BorMedTypeEnum.SAMBOER3_2,
                            barnDetalj = PenBarnDetalj().apply { inntektOver1G = true },
                            tillegg = true,
                            bruk = false,
                            grunnlagKildeEnum = GrunnlagkildeEnum.PEN,
                            serskiltSatsUtenET = true,
                            epsAvkallEgenPensjon = false,
                            virkFom = LocalDate.of(2023, 3, 3).toNorwegianDateAtNoon(),
                            virkTom = LocalDate.of(2024, 4, 4).toNorwegianDateAtNoon()
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
})
