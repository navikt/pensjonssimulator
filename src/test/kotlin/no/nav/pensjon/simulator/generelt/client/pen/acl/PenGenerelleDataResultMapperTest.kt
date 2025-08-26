package no.nav.pensjon.simulator.generelt.client.pen.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.generelt.GenerelleData
import no.nav.pensjon.simulator.generelt.Person
import java.time.LocalDate

class PenGenerelleDataResultMapperTest : FunSpec({

    test("fromDto with empty lists") {
        PenGenerelleDataResultMapper.fromDto(
            PenGenerelleDataResult(
                person = PenPersonData(
                    foedselDato = LocalDate.of(1964, 5, 6),
                    statsborgerskap = "ABW"
                ),
                privatAfpSatser = PenPrivatAfpSatser(forholdstall = null),
                delingstallUtvalg = PenDelingstallUtvalg(
                    dt = 1.2,
                    dt67soker = 3.4,
                    dt67virk = 5.6,
                    delingstallListe = mutableListOf()
                ),
                forholdstallUtvalg = PenForholdstallUtvalg(
                    ft = 1.2,
                    forholdstallListe = mutableListOf(),
                    ft67soeker = 3.4,
                    ft67virkning = 5.6,
                    reguleringFaktor = 7.8
                ),
                satsResultatListe = emptyList()
            )
        ) shouldBeEqualToComparingFields
                GenerelleData(
                    person = Person(
                        foedselDato = LocalDate.of(1964, 5, 6),
                        statsborgerskap = LandkodeEnum.ABW
                    ),
                    privatAfpSatser = PrivatAfpSatser(
                        forholdstall = 0.0,
                        kompensasjonstilleggForholdstall = 0.0,
                        justeringsbeloep = 0,
                        referansebeloep = 0
                    ),
                    satsResultatListe = emptyList()
                )
    }

    test("fromDto lists") {
        val result = PenGenerelleDataResultMapper.fromDto(
            PenGenerelleDataResult(
                person = PenPersonData(
                    foedselDato = LocalDate.of(1964, 5, 6),
                    statsborgerskap = "ABW"
                ),
                privatAfpSatser = PenPrivatAfpSatser(forholdstall = null),
                delingstallUtvalg = PenDelingstallUtvalg(
                    dt = 1.2,
                    dt67soker = 3.4,
                    dt67virk = 5.6,
                    delingstallListe = mutableListOf(
                        PenAarskullTall(
                            aarskull = 1970L,
                            alderAar = 55,
                            maaneder = 9,
                            tall = 6.71
                        )
                    )
                ),
                forholdstallUtvalg = PenForholdstallUtvalg(
                    ft = 1.2,
                    forholdstallListe = mutableListOf(
                        PenAarskullTall(
                            aarskull = 1960L,
                            alderAar = 65,
                            maaneder = 11,
                            tall = 6.7
                        )
                    ),
                    ft67soeker = 3.4,
                    ft67virkning = 5.6,
                    reguleringFaktor = 7.8
                ),
                satsResultatListe = listOf(
                    PenVeietSatsResultat(
                        aar = 2025,
                        verdi = 8.9
                    )
                )
            )
        )

        with(result.satsResultatListe[0]) {
            ar shouldBe 2025
            verdi shouldBe 8.9
        }
    }

    /* TODO does currently not work with filled lists
    test("fromDto with all fields defined") {
        PenGenerelleDataResultMapper.fromDto(
            PenGenerelleDataResult(
                person = PenPersonData(
                    foedselDato = LocalDate.of(1964, 5, 6),
                    statsborgerskap = "ABW"
                ),
                privatAfpSatser = PenPrivatAfpSatser(
                    forholdstall = PenAarskullTall(
                        aarskull = 1964L,
                        alderAar = 62,
                        maaneder = 7,
                        tall = 12.34
                    )
                ),
                delingstallUtvalg = PenDelingstallUtvalg(
                    dt = 1.2,
                    dt67soker = 3.4,
                    dt67virk = 5.6,
                    delingstallListe = mutableListOf(
                        PenAarskullTall(
                            aarskull = 1965L,
                            alderAar = 63,
                            maaneder = 8,
                            tall = 123.45
                        ),
                        PenAarskullTall(
                            aarskull = 1966L,
                            alderAar = 64,
                            maaneder = 9,
                            tall = 0.12
                        )
                    )
                ),
                forholdstallUtvalg = PenForholdstallUtvalg(
                    ft = 1.2,
                    forholdstallListe = mutableListOf(
                        PenAarskullTall(
                            aarskull = 1965L,
                            alderAar = 63,
                            maaneder = 8,
                            tall = 123.45
                        )
                    ),
                    ft67soeker = 3.4,
                    ft67virkning = 5.6,
                    reguleringFaktor = 7.8
                ),
                satsResultatListe = listOf(PenVeietSatsResultat(aar = 2024, verdi = 43.21))
            )
        ) shouldBeEqualToComparingFields
                GenerelleData(
                    person = Person(
                        foedselDato = LocalDate.of(1964, 5, 6),
                        statsborgerskap = LandkodeEnum.ABW
                    ),
                    privatAfpSatser = PrivatAfpSatser(
                        ft = Forholdstall(
                            arskull = 1964,
                            alder = 62,
                            maned = 7,
                            forholdstall = 12.34
                        )
                    ),
                    delingstallUtvalg = DelingstallUtvalg().apply {
                        dt = 1.2
                        dt67soker = 0.0 // not mapped, so not 3.4
                        dt67virk = 0.0 // not mapped, so not 5.6
                        delingstallListe = mutableListOf(
                            Delingstall().apply {
                                arskull = 1965L
                                alder = 63
                                maned = 8
                                delingstall = 123.45
                            },
                            Delingstall().apply {
                                arskull = 1966L
                                alder = 64
                                maned = 9
                                delingstall = 0.12
                            }
                        )
                    },
                    forholdstallUtvalg = ForholdstallUtvalg().apply {
                        ft = 1.2
                        forholdstallListe = mutableListOf(
                            Forholdstall().apply {
                                arskull = 1965L
                                alder = 63
                                maned = 8
                                forholdstall = 123.45
                            }
                        )
                        ft67soker = 0.0 // not mapped, so not 3.4
                        ft67virk = 0.0 // not mapped, so not 5.6
                        reguleringsfaktor = 0.0 // not mapped, so not 7.8
                    },
                    satsResultatListe = listOf(VeietSatsResultat(ar = 2024, verdi = 43.21))
                )
    }
    */
})
