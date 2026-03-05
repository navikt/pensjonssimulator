package no.nav.pensjon.simulator.vedtak

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import java.time.LocalDate

class VedtakUtilTest : ShouldSpec({

    context("innvilget vedtak-liste med kravlinjetype-match") {
        should("finne tidligste virkningsdato") {
            VedtakUtil.foersteVirkningsdato(
                vedtakListe = mutableListOf(
                    vedtak(KravlinjeTypeEnum.KLAGE, VedtakResultatEnum.INNV, 2026),
                    vedtak(KravlinjeTypeEnum.KLAGE, VedtakResultatEnum.INNV, 2024), // tidligste
                    vedtak(KravlinjeTypeEnum.KLAGE, VedtakResultatEnum.INNV, 2025)
                ),
                virkningListe = emptyList(),
                kravlinjetype = KravlinjeTypeEnum.KLAGE,
                gjelderPerson = null
            ) shouldBe LocalDate.of(2024, 6, 15)
        }
    }

    context("vedtak-liste uten innvilget vedtak") {
        should("ikke finne virkningsdato") {
            VedtakUtil.foersteVirkningsdato(
                vedtakListe = mutableListOf(vedtak(KravlinjeTypeEnum.AFP, VedtakResultatEnum.VETIKKE, 2025)),
                virkningListe = emptyList(),
                kravlinjetype = KravlinjeTypeEnum.AFP,
                gjelderPerson = null
            ).shouldBeNull()
        }
    }

    context("virkning-liste med kravlinjeType-match og person-match og barne-/ektefelletillegg") {
        should("finne virkningsdato") {
            val person = PenPerson()

            VedtakUtil.foersteVirkningsdato(
                vedtakListe = mutableListOf(),
                virkningListe = listOf(
                    FoersteVirkningDato(
                        sakType = null,
                        kravlinjeType = KravlinjeTypeEnum.BT,
                        virkningDato = LocalDate.of(2025, 6, 15),
                        annenPerson = person // samme person som vedtakets 'gjelderPerson'
                    )
                ),
                kravlinjetype = KravlinjeTypeEnum.BT, // barnetillegg
                gjelderPerson = person
            ) shouldBe LocalDate.of(2025, 6, 15)
        }
    }

    context("virkning-liste uten kravlinjetype-match") {
        should("ikke finne virkningsdato") {
            val person = PenPerson()

            VedtakUtil.foersteVirkningsdato(
                vedtakListe = mutableListOf(),
                virkningListe = listOf(
                    FoersteVirkningDato(
                        sakType = null,
                        kravlinjeType = KravlinjeTypeEnum.UT, // annen kravlinjetype
                        virkningDato = LocalDate.of(2025, 6, 15),
                        annenPerson = person
                    )
                ),
                kravlinjetype = KravlinjeTypeEnum.AP,
                gjelderPerson = person
            ).shouldBeNull()
        }
    }

    context("virkning-liste med kravlinjeType-match og ikke barne-/ektefelletillegg") {
        should("bruke første forekommende 'første virkningsdato' i listen") {
            VedtakUtil.foersteVirkningsdato(
                vedtakListe = mutableListOf(),
                virkningListe = listOf(
                    FoersteVirkningDato(
                        sakType = null,
                        kravlinjeType = KravlinjeTypeEnum.UT,
                        virkningDato = LocalDate.of(2025, 6, 7), // første forekommende i listen...
                        // ...selv om datoen er senere enn neste i listen
                        annenPerson = PenPerson() // ikke samme person som vedtakets 'gjelderPerson'
                    ),
                    FoersteVirkningDato(
                        sakType = null,
                        kravlinjeType = KravlinjeTypeEnum.UT,
                        virkningDato = LocalDate.of(2024, 5, 6),
                        annenPerson = PenPerson() // ikke samme person som vedtakets 'gjelderPerson'
                    )
                ),
                kravlinjetype = KravlinjeTypeEnum.UT, // ikke barne-/ektefelletillegg
                gjelderPerson = PenPerson()
            ) shouldBe LocalDate.of(2025, 6, 7)
        }
    }

    should("finne dato fra vedtak hvis tidligere enn virkning-datoer") {
        VedtakUtil.foersteVirkningsdato(
            vedtakListe = mutableListOf(
                vedtak(KravlinjeTypeEnum.AFP, VedtakResultatEnum.INNV, 2024), // tidligere enn i virkning-liste
            ),
            virkningListe = listOf(
                FoersteVirkningDato(
                    sakType = null,
                    kravlinjeType = KravlinjeTypeEnum.AFP,
                    virkningDato = LocalDate.of(2025, 1, 1),
                    annenPerson = null
                )
            ),
            kravlinjetype = KravlinjeTypeEnum.AFP,
            gjelderPerson = null
        ) shouldBe LocalDate.of(2024, 6, 15)
    }

    should("finne dato fra virkning hvis tidligere enn vedtak-datoer") {
        VedtakUtil.foersteVirkningsdato(
            vedtakListe = mutableListOf(vedtak(KravlinjeTypeEnum.AFP_PRIVAT, VedtakResultatEnum.INNV, 2025)),
            virkningListe = listOf(
                FoersteVirkningDato(
                    sakType = null,
                    kravlinjeType = KravlinjeTypeEnum.AFP_PRIVAT,
                    virkningDato = LocalDate.of(2024, 12, 31), // tidligere enn i vedtak-liste
                    annenPerson = null
                )
            ),
            kravlinjetype = KravlinjeTypeEnum.AFP_PRIVAT,
            gjelderPerson = null
        ) shouldBe LocalDate.of(2024, 12, 31)
    }
})

fun vedtak(kravlinjeType: KravlinjeTypeEnum, vedtakResultat: VedtakResultatEnum, aar: Int) =
    VilkarsVedtak().apply {
        kravlinjeTypeEnum = kravlinjeType
        vilkarsvedtakResultatEnum = vedtakResultat
        virkFom = LocalDate.of(aar, 6, 15).toNorwegianDateAtNoon()
    }