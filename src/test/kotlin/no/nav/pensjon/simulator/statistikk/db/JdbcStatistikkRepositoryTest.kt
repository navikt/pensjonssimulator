package no.nav.pensjon.simulator.statistikk.db

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.statistikk.SimuleringHendelse
import no.nav.pensjon.simulator.statistikk.SimuleringStatistikk
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.sql.ResultSet

class JdbcStatistikkRepositoryTest : FunSpec({

    context("update") {

        test("kaller db.update med korrekt SQL") {
            val db = mockk<NamedParameterJdbcOperations>(relaxed = true)
            val repository = JdbcStatistikkRepository(db)
            val hendelse = SimuleringHendelse(
                organisasjonsnummer = Organisasjonsnummer("123456789"),
                simuleringstype = SimuleringTypeEnum.ALDER
            )

            repository.update(hendelse)

            val sqlSlot = slot<String>()
            verify { db.update(capture(sqlSlot), any<Map<String, Any>>()) }
            sqlSlot.captured shouldBe "UPDATE PENSJONSSIMULATOR.SIMULERING_TELLER SET ANTALL = ANTALL + 1 WHERE ORG_NR = :org_nr AND SIMULERINGSTYPE = :simuleringstype"
        }

        test("sender korrekte parametere til db.update") {
            val db = mockk<NamedParameterJdbcOperations>(relaxed = true)
            val repository = JdbcStatistikkRepository(db)
            val hendelse = SimuleringHendelse(
                organisasjonsnummer = Organisasjonsnummer("987654321"),
                simuleringstype = SimuleringTypeEnum.AFP
            )

            repository.update(hendelse)

            val paramsSlot = slot<Map<String, Any>>()
            verify { db.update(any(), capture(paramsSlot)) }
            paramsSlot.captured["org_nr"] shouldBe "987654321"
            paramsSlot.captured["simuleringstype"] shouldBe "AFP"
        }

        test("håndterer ulike simuleringstyper") {
            listOf(
                SimuleringTypeEnum.ALDER,
                SimuleringTypeEnum.AFP,
                SimuleringTypeEnum.GJENLEVENDE,
                SimuleringTypeEnum.BARN
            ).forEach { type ->
                val db = mockk<NamedParameterJdbcOperations>(relaxed = true)
                val repository = JdbcStatistikkRepository(db)
                val hendelse = SimuleringHendelse(
                    organisasjonsnummer = Organisasjonsnummer("123456789"),
                    simuleringstype = type
                )

                repository.update(hendelse)

                val paramsSlot = slot<Map<String, Any>>()
                verify { db.update(any(), capture(paramsSlot)) }
                paramsSlot.captured["simuleringstype"] shouldBe type.name
            }
        }
    }

    context("read") {

        test("kaller db.query med korrekt SQL") {
            val db = mockk<NamedParameterJdbcOperations>()
            every { db.query(any<String>(), any<RowMapper<SimuleringStatistikk>>()) } returns emptyList()
            val repository = JdbcStatistikkRepository(db)

            repository.read()

            val sqlSlot = slot<String>()
            verify { db.query(capture(sqlSlot), any<RowMapper<SimuleringStatistikk>>()) }
            sqlSlot.captured shouldBe "SELECT ORG_NR, SIMULERINGSTYPE, ANTALL FROM PENSJONSSIMULATOR.SIMULERING_TELLER ORDER BY ANTALL DESC"
        }

        test("returnerer tom liste når ingen data finnes") {
            val db = mockk<NamedParameterJdbcOperations>()
            every { db.query(any<String>(), any<RowMapper<SimuleringStatistikk>>()) } returns emptyList()
            val repository = JdbcStatistikkRepository(db)

            val result = repository.read()

            result.size shouldBe 0
        }

        test("returnerer data fra database") {
            val db = mockk<NamedParameterJdbcOperations>()
            val expectedData = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 100
                )
            )
            every { db.query(any<String>(), any<RowMapper<SimuleringStatistikk>>()) } returns expectedData
            val repository = JdbcStatistikkRepository(db)

            val result = repository.read()

            result shouldBe expectedData
        }
    }
})

class SimuleringStatistikkRowMapperTest : FunSpec({

    test("mapper ResultSet til SimuleringStatistikk") {
        val rowMapper = SimuleringStatistikkRowMapper()
        val resultSet = mockk<ResultSet>()
        every { resultSet.getString("ORG_NR") } returns "123456789"
        every { resultSet.getString("SIMULERINGSTYPE") } returns "ALDER"
        every { resultSet.getInt("ANTALL") } returns 42

        val result = rowMapper.mapRow(resultSet, 0)

        result?.hendelse?.organisasjonsnummer shouldBe Organisasjonsnummer("123456789")
        result?.hendelse?.simuleringstype shouldBe SimuleringTypeEnum.ALDER
        result?.antall shouldBe 42
    }

    test("mapper ulike simuleringstyper") {
        val rowMapper = SimuleringStatistikkRowMapper()

        listOf(
            "AFP" to SimuleringTypeEnum.AFP,
            "ALDER" to SimuleringTypeEnum.ALDER,
            "GJENLEVENDE" to SimuleringTypeEnum.GJENLEVENDE,
            "BARN" to SimuleringTypeEnum.BARN,
            "ALDER_M_AFP_PRIVAT" to SimuleringTypeEnum.ALDER_M_AFP_PRIVAT
        ).forEach { (typeString, expectedType) ->
            val resultSet = mockk<ResultSet>()
            every { resultSet.getString("ORG_NR") } returns "111111111"
            every { resultSet.getString("SIMULERINGSTYPE") } returns typeString
            every { resultSet.getInt("ANTALL") } returns 1

            val result = rowMapper.mapRow(resultSet, 0)

            result?.hendelse?.simuleringstype shouldBe expectedType
        }
    }

    test("mapper ulike antall-verdier") {
        val rowMapper = SimuleringStatistikkRowMapper()

        listOf(0, 1, 100, 999999).forEach { antall ->
            val resultSet = mockk<ResultSet>()
            every { resultSet.getString("ORG_NR") } returns "123456789"
            every { resultSet.getString("SIMULERINGSTYPE") } returns "ALDER"
            every { resultSet.getInt("ANTALL") } returns antall

            val result = rowMapper.mapRow(resultSet, 0)

            result?.antall shouldBe antall
        }
    }

    test("mapper ulike organisasjonsnumre") {
        val rowMapper = SimuleringStatistikkRowMapper()

        listOf("111111111", "222222222", "987654321").forEach { orgNr ->
            val resultSet = mockk<ResultSet>()
            every { resultSet.getString("ORG_NR") } returns orgNr
            every { resultSet.getString("SIMULERINGSTYPE") } returns "ALDER"
            every { resultSet.getInt("ANTALL") } returns 10

            val result = rowMapper.mapRow(resultSet, 0)

            result?.hendelse?.organisasjonsnummer shouldBe Organisasjonsnummer(orgNr)
        }
    }
})
