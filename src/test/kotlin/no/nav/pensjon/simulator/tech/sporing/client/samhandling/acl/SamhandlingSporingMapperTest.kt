package no.nav.pensjon.simulator.tech.sporing.client.samhandling.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.sporing.Sporing
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SamhandlingSporingMapperTest : FunSpec({

    context("toDto") {

        test("mapper alle felter korrekt") {
            val sporing = Sporing(
                pid = Pid("12345678901"),
                mottaker = Organisasjonsnummer("987654321"),
                tema = "PEN",
                behandlingGrunnlag = "Behandlingsgrunnlag for pensjon",
                uthentingTidspunkt = LocalDateTime.of(2025, 6, 15, 10, 30, 45),
                dataForespoersel = "Forespørsel om pensjonsdata",
                leverteData = "Pensjonsdata levert"
            )

            val result = SamhandlingSporingMapper.toDto(sporing)

            result.person shouldBe "12345678901"
            result.mottaker shouldBe "987654321"
            result.tema shouldBe "PEN"
            result.behandlingsGrunnlag shouldBe "Behandlingsgrunnlag for pensjon"
            result.uthentingsTidspunkt shouldBe "2025-06-15T10:30:45"
            result.dataForespoersel shouldBe "Forespørsel om pensjonsdata"
            result.leverteData shouldBe "Pensjonsdata levert"
        }

        test("formaterer uthentingTidspunkt med ISO_LOCAL_DATE_TIME format") {
            val tidspunkt = LocalDateTime.of(2024, 1, 5, 8, 15, 30)
            val sporing = createSporing(uthentingTidspunkt = tidspunkt)

            val result = SamhandlingSporingMapper.toDto(sporing)

            result.uthentingsTidspunkt shouldBe DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(tidspunkt)
            result.uthentingsTidspunkt shouldBe "2024-01-05T08:15:30"
        }

        test("håndterer tidspunkt ved midnatt") {
            val tidspunkt = LocalDateTime.of(2025, 12, 31, 0, 0, 0)
            val sporing = createSporing(uthentingTidspunkt = tidspunkt)

            val result = SamhandlingSporingMapper.toDto(sporing)

            result.uthentingsTidspunkt shouldBe "2025-12-31T00:00:00"
        }

        test("håndterer tidspunkt ved slutten av dagen") {
            val tidspunkt = LocalDateTime.of(2025, 6, 30, 23, 59, 59)
            val sporing = createSporing(uthentingTidspunkt = tidspunkt)

            val result = SamhandlingSporingMapper.toDto(sporing)

            result.uthentingsTidspunkt shouldBe "2025-06-30T23:59:59"
        }

        test("mapper pid value korrekt") {
            val sporing = createSporing(pid = Pid("11111111111"))

            val result = SamhandlingSporingMapper.toDto(sporing)

            result.person shouldBe "11111111111"
        }

        test("mapper organisasjonsnummer value korrekt") {
            val sporing = createSporing(mottaker = Organisasjonsnummer("123456789"))

            val result = SamhandlingSporingMapper.toDto(sporing)

            result.mottaker shouldBe "123456789"
        }

        test("håndterer tomme strenger for tekst-felter") {
            val sporing = createSporing(
                tema = "",
                behandlingGrunnlag = "",
                dataForespoersel = "",
                leverteData = ""
            )

            val result = SamhandlingSporingMapper.toDto(sporing)

            result.tema shouldBe ""
            result.behandlingsGrunnlag shouldBe ""
            result.dataForespoersel shouldBe ""
            result.leverteData shouldBe ""
        }

        test("håndterer spesialtegn i tekst-felter") {
            val sporing = createSporing(
                tema = "Tema med ÆØÅ og æøå",
                behandlingGrunnlag = "Grunnlag: §1-2 \"test\"",
                dataForespoersel = "<xml>data</xml>",
                leverteData = "JSON: {\"key\": \"value\"}"
            )

            val result = SamhandlingSporingMapper.toDto(sporing)

            result.tema shouldBe "Tema med ÆØÅ og æøå"
            result.behandlingsGrunnlag shouldBe "Grunnlag: §1-2 \"test\""
            result.dataForespoersel shouldBe "<xml>data</xml>"
            result.leverteData shouldBe "JSON: {\"key\": \"value\"}"
        }

        test("håndterer tidspunkt med nanosekunder (ignoreres i output)") {
            val tidspunkt = LocalDateTime.of(2025, 3, 15, 14, 30, 45, 123456789)
            val sporing = createSporing(uthentingTidspunkt = tidspunkt)

            val result = SamhandlingSporingMapper.toDto(sporing)

            // ISO_LOCAL_DATE_TIME inkluderer nanosekunder hvis de finnes
            result.uthentingsTidspunkt shouldBe "2025-03-15T14:30:45.123456789"
        }
    }
})

private fun createSporing(
    pid: Pid = Pid("12345678901"),
    mottaker: Organisasjonsnummer = Organisasjonsnummer("987654321"),
    tema: String = "PEN",
    behandlingGrunnlag: String = "Behandlingsgrunnlag",
    uthentingTidspunkt: LocalDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0),
    dataForespoersel: String = "Forespørsel",
    leverteData: String = "Data"
) = Sporing(
    pid = pid,
    mottaker = mottaker,
    tema = tema,
    behandlingGrunnlag = behandlingGrunnlag,
    uthentingTidspunkt = uthentingTidspunkt,
    dataForespoersel = dataForespoersel,
    leverteData = leverteData
)
