package no.nav.pensjon.simulator.krav.client.pen.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.util.Calendar

class PenKravhodeMapperTest : FunSpec({

    test("'kravhode' maps persondetalj") {
        val source = PenKravhode().apply {
            persongrunnlagListe = mutableListOf(
                PenPersongrunnlag().apply {
                    personDetaljListe = mutableListOf(
                        PenPersonDetalj(
                            grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER,
                            rolleFomDato = dateAtNoon(2021, Calendar.JANUARY, 1),
                            rolleTomDato = dateAtNoon(2022, Calendar.FEBRUARY, 2),
                            sivilstandTypeEnum = SivilstandEnum.GIFT,
                            sivilstandRelatertPerson = PenPenPerson().apply { penPersonId = 1 },
                            borMedEnum = BorMedTypeEnum.SAMBOER3_2,
                            barnDetalj = PenBarnDetalj().apply { inntektOver1G = true },
                            tillegg = true,
                            bruk = false,
                            grunnlagKildeEnum = GrunnlagkildeEnum.PEN,
                            serskiltSatsUtenET = true,
                            epsAvkallEgenPensjon = false,
                            virkFom = dateAtNoon(2023, Calendar.MARCH, 3),
                            virkTom = dateAtNoon(2024, Calendar.APRIL, 4)
                        )
                    )
                })
        }

        val result: Kravhode = PenKravhodeMapper.kravhode(source)

        with(result) {
            with(persongrunnlagListe.first()) {
                with(personDetaljListe.first()) {
                    grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
                    penRolleFom shouldBe dateAtNoon(2021, Calendar.JANUARY, 1)
                    penRolleTom shouldBe dateAtNoon(2022, Calendar.FEBRUARY, 2)
                    rolleFomDato shouldBe dateAtNoon(2023, Calendar.MARCH, 3) // set to virkFom in mapper
                    rolleTomDato shouldBe dateAtNoon(2024, Calendar.APRIL, 4) // set to virkTom in mapper
                    virkFom shouldBe dateAtNoon(2023, Calendar.MARCH, 3)
                    virkTom shouldBe dateAtNoon(2024, Calendar.APRIL, 4)
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
