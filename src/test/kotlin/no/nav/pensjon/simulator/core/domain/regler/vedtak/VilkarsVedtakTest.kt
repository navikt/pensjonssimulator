package no.nav.pensjon.simulator.core.domain.regler.vedtak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class VilkarsVedtakTest : FunSpec({

    test("fastsettForstevirkKravlinje for innvilget vedtak-liste med kravlinjeType-match => finner tidligste virkningsdato") {
        val vedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.KLAGE }

        vedtak.fastsettForstevirkKravlinje(
            vedtakListe = mutableListOf(
                vedtak(KravlinjeTypeEnum.KLAGE, VedtakResultatEnum.INNV, 2026),
                vedtak(KravlinjeTypeEnum.KLAGE, VedtakResultatEnum.INNV, 2024), // tidligste
                vedtak(KravlinjeTypeEnum.KLAGE, VedtakResultatEnum.INNV, 2025)
            ),
            virkningListe = emptyList()
        )

        vedtak.kravlinjeForsteVirk shouldBe dateAtNoon(2024, Calendar.JUNE, 15)
    }

    test("fastsettForstevirkKravlinje for vedtak-liste uten innvilget vedtak => finner ikke virkningsdato") {
        val vedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AFP }

        vedtak.fastsettForstevirkKravlinje(
            vedtakListe = mutableListOf(vedtak(KravlinjeTypeEnum.AFP, VedtakResultatEnum.VETIKKE, 2025)),
            virkningListe = emptyList()
        )

        vedtak.kravlinjeForsteVirk.shouldBeNull()
    }

    test("fastsettForstevirkKravlinje for virkning-liste med kravlinjeType-match og person-match og barne-/ektefelletillegg => finner virkningsdato") {
        val person = PenPerson()

        val vedtak = VilkarsVedtak().apply {
            kravlinjeTypeEnum = KravlinjeTypeEnum.BT // barnetillegg
            gjelderPerson = person
        }

        vedtak.fastsettForstevirkKravlinje(
            vedtakListe = mutableListOf(),
            virkningListe = listOf(
                FoersteVirkningDato(
                    sakType = null,
                    kravlinjeType = KravlinjeTypeEnum.BT,
                    virkningDato = LocalDate.of(2025, 6, 15),
                    annenPerson = person // samme person som vedtakets 'gjelderPerson'
                )
            )
        )

        vedtak.kravlinjeForsteVirk shouldBe dateAtNoon(2025, Calendar.JUNE, 15)
    }

    test("fastsettForstevirkKravlinje for virkning-liste uten kravlinjetype-match => finner ikke virkningsdato") {
        val person = PenPerson()

        val vedtak = VilkarsVedtak().apply {
            kravlinjeTypeEnum = KravlinjeTypeEnum.AP
            gjelderPerson = person
        }

        vedtak.fastsettForstevirkKravlinje(
            vedtakListe = mutableListOf(),
            virkningListe = listOf(
                FoersteVirkningDato(
                    sakType = null,
                    kravlinjeType = KravlinjeTypeEnum.UT, // annen kravlinjetype
                    virkningDato = LocalDate.of(2025, 6, 15),
                    annenPerson = person
                )
            )
        )

        vedtak.kravlinjeForsteVirk.shouldBeNull()
    }

    test("fastsettForstevirkKravlinje for virkning-liste med kravlinjeType-match og ikke barne-/ektefelletillegg => bruker første forekommende 'første virkningsdato' i listen") {
        val vedtak = VilkarsVedtak().apply {
            kravlinjeTypeEnum = KravlinjeTypeEnum.UT // ikke barne-/ektefelletillegg
            gjelderPerson = PenPerson()
        }

        vedtak.fastsettForstevirkKravlinje(
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
            )
        )

        vedtak.kravlinjeForsteVirk shouldBe dateAtNoon(2025, Calendar.JUNE, 7)
    }


    test("fastsettForstevirkKravlinje finner dato fra vedtak hvis tidligere enn virkning-datoer") {
        val vedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AFP }

        vedtak.fastsettForstevirkKravlinje(
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
            )
        )

        vedtak.kravlinjeForsteVirk shouldBe dateAtNoon(2024, Calendar.JUNE, 15)
    }

    test("fastsettForstevirkKravlinje finner dato fra virkning hvis tidligere enn vedtak-datoer") {
        val vedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AFP_PRIVAT }

        vedtak.fastsettForstevirkKravlinje(
            vedtakListe = mutableListOf(vedtak(KravlinjeTypeEnum.AFP_PRIVAT, VedtakResultatEnum.INNV, 2025)),
            virkningListe = listOf(
                FoersteVirkningDato(
                    sakType = null,
                    kravlinjeType = KravlinjeTypeEnum.AFP_PRIVAT,
                    virkningDato = LocalDate.of(2024, 12, 31), // tidligere enn i vedtak-liste
                    annenPerson = null
                )
            )
        )

        vedtak.kravlinjeForsteVirk shouldBe dateAtNoon(2024, Calendar.DECEMBER, 31)
    }
})

fun vedtak(kravlinjeType: KravlinjeTypeEnum, vedtakResultat: VedtakResultatEnum, aar: Int) =
    VilkarsVedtak().apply {
        kravlinjeTypeEnum = kravlinjeType
        vilkarsvedtakResultatEnum = vedtakResultat
        virkFom = dateAtNoon(aar, Calendar.JUNE, 15)
    }

