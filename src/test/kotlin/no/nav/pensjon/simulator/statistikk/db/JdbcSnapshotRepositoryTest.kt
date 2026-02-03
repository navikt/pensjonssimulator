package no.nav.pensjon.simulator.statistikk.db

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.statistikk.MaanedligStatistikk
import no.nav.pensjon.simulator.statistikk.SimuleringHendelse
import no.nav.pensjon.simulator.statistikk.SimuleringStatistikk
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import tools.jackson.databind.json.JsonMapper
import java.sql.ResultSet

class JdbcSnapshotRepositoryTest : FunSpec({

    context("create") {

        test("kaller db.update med korrekt SQL") {
            val db = mockk<NamedParameterJdbcOperations>(relaxed = true)
            val jsonMapper = JsonMapper.builder().build()
            val repository = JdbcSnapshotRepository(db, jsonMapper)
            val snapshot = MaanedligStatistikk(
                aarMaaned = 202406,
                statistikk = emptyList()
            )

            repository.create(snapshot)

            val sqlSlot = slot<String>()
            verify { db.update(capture(sqlSlot), any<Map<String, Any>>()) }
            sqlSlot.captured shouldContain "INSERT INTO PENSJONSSIMULATOR.SIMULERING_TELLER_SNAPSHOT"
            sqlSlot.captured shouldContain "ON CONFLICT DO NOTHING"
        }

        test("sender korrekt aarMaaned til db.update") {
            val db = mockk<NamedParameterJdbcOperations>(relaxed = true)
            val jsonMapper = JsonMapper.builder().build()
            val repository = JdbcSnapshotRepository(db, jsonMapper)
            val snapshot = MaanedligStatistikk(
                aarMaaned = 202506,
                statistikk = emptyList()
            )

            repository.create(snapshot)

            val paramsSlot = slot<Map<String, Any>>()
            verify { db.update(any(), capture(paramsSlot)) }
            paramsSlot.captured["aar_maaned"] shouldBe 202506
        }

        test("serialiserer statistikk til JSON") {
            val db = mockk<NamedParameterJdbcOperations>(relaxed = true)
            val jsonMapper = JsonMapper.builder().build()
            val repository = JdbcSnapshotRepository(db, jsonMapper)
            val snapshot = MaanedligStatistikk(
                aarMaaned = 202406,
                statistikk = listOf(
                    SimuleringStatistikk(
                        hendelse = SimuleringHendelse(
                            organisasjonsnummer = Organisasjonsnummer("123456789"),
                            simuleringstype = SimuleringTypeEnum.ALDER
                        ),
                        antall = 100
                    )
                )
            )

            repository.create(snapshot)

            val paramsSlot = slot<Map<String, Any>>()
            verify { db.update(any(), capture(paramsSlot)) }
            val statistikkJson = paramsSlot.captured["statistikk"] as String
            statistikkJson shouldContain "123456789"
            statistikkJson shouldContain "ALDER"
            statistikkJson shouldContain "100"
        }

        test("håndterer tom statistikkliste") {
            val db = mockk<NamedParameterJdbcOperations>(relaxed = true)
            val jsonMapper = JsonMapper.builder().build()
            val repository = JdbcSnapshotRepository(db, jsonMapper)
            val snapshot = MaanedligStatistikk(
                aarMaaned = 202406,
                statistikk = emptyList()
            )

            repository.create(snapshot)

            val paramsSlot = slot<Map<String, Any>>()
            verify { db.update(any(), capture(paramsSlot)) }
            paramsSlot.captured["statistikk"] shouldBe "[]"
        }

        test("serialiserer flere statistikk-elementer") {
            val db = mockk<NamedParameterJdbcOperations>(relaxed = true)
            val jsonMapper = JsonMapper.builder().build()
            val repository = JdbcSnapshotRepository(db, jsonMapper)
            val snapshot = MaanedligStatistikk(
                aarMaaned = 202406,
                statistikk = listOf(
                    SimuleringStatistikk(
                        hendelse = SimuleringHendelse(
                            organisasjonsnummer = Organisasjonsnummer("111111111"),
                            simuleringstype = SimuleringTypeEnum.ALDER
                        ),
                        antall = 50
                    ),
                    SimuleringStatistikk(
                        hendelse = SimuleringHendelse(
                            organisasjonsnummer = Organisasjonsnummer("222222222"),
                            simuleringstype = SimuleringTypeEnum.AFP
                        ),
                        antall = 30
                    )
                )
            )

            repository.create(snapshot)

            val paramsSlot = slot<Map<String, Any>>()
            verify { db.update(any(), capture(paramsSlot)) }
            val statistikkJson = paramsSlot.captured["statistikk"] as String
            statistikkJson shouldContain "111111111"
            statistikkJson shouldContain "222222222"
            statistikkJson shouldContain "ALDER"
            statistikkJson shouldContain "AFP"
        }
    }

    context("read") {

        test("kaller db.query med korrekt SQL og aarMaaned") {
            val db = mockk<NamedParameterJdbcOperations>()
            val jsonMapper = JsonMapper.builder().build()
            every {
                db.query(any<String>(), any<Map<String, Any>>(), any<RowMapper<MaanedligStatistikkDto>>())
            } returns listOf(MaanedligStatistikkDto(202406, "[]"))
            val repository = JdbcSnapshotRepository(db, jsonMapper)

            repository.read(202406)

            val sqlSlot = slot<String>()
            val paramsSlot = slot<Map<String, Any>>()
            verify { db.query(capture(sqlSlot), capture(paramsSlot), any<RowMapper<MaanedligStatistikkDto>>()) }
            sqlSlot.captured shouldContain "SELECT"
            sqlSlot.captured shouldContain "SIMULERING_TELLER_SNAPSHOT"
            paramsSlot.captured["aar_maaned"] shouldBe 202406
        }

        test("deserialiserer JSON til SimuleringStatistikk liste") {
            val db = mockk<NamedParameterJdbcOperations>()
            val jsonMapper = JsonMapper.builder().build()
            val jsonData = """[{"hendelse":{"organisasjonsnummer":"123456789","simuleringstype":"ALDER"},"antall":100}]"""
            every {
                db.query(any<String>(), any<Map<String, Any>>(), any<RowMapper<MaanedligStatistikkDto>>())
            } returns listOf(MaanedligStatistikkDto(202406, jsonData))
            val repository = JdbcSnapshotRepository(db, jsonMapper)

            val result = repository.read(202406)

            result.size shouldBe 1
            result[0].hendelse.organisasjonsnummer shouldBe Organisasjonsnummer("123456789")
            result[0].hendelse.simuleringstype shouldBe SimuleringTypeEnum.ALDER
            result[0].antall shouldBe 100
        }

        test("returnerer tom liste når JSON er tom array") {
            val db = mockk<NamedParameterJdbcOperations>()
            val jsonMapper = JsonMapper.builder().build()
            every {
                db.query(any<String>(), any<Map<String, Any>>(), any<RowMapper<MaanedligStatistikkDto>>())
            } returns listOf(MaanedligStatistikkDto(202406, "[]"))
            val repository = JdbcSnapshotRepository(db, jsonMapper)

            val result = repository.read(202406)

            result.size shouldBe 0
        }

        test("deserialiserer flere elementer") {
            val db = mockk<NamedParameterJdbcOperations>()
            val jsonMapper = JsonMapper.builder().build()
            val jsonData = """[
                {"hendelse":{"organisasjonsnummer":"111111111","simuleringstype":"ALDER"},"antall":50},
                {"hendelse":{"organisasjonsnummer":"222222222","simuleringstype":"AFP"},"antall":30}
            ]"""
            every {
                db.query(any<String>(), any<Map<String, Any>>(), any<RowMapper<MaanedligStatistikkDto>>())
            } returns listOf(MaanedligStatistikkDto(202406, jsonData))
            val repository = JdbcSnapshotRepository(db, jsonMapper)

            val result = repository.read(202406)

            result.size shouldBe 2
            result[0].hendelse.organisasjonsnummer shouldBe Organisasjonsnummer("111111111")
            result[0].hendelse.simuleringstype shouldBe SimuleringTypeEnum.ALDER
            result[0].antall shouldBe 50
            result[1].hendelse.organisasjonsnummer shouldBe Organisasjonsnummer("222222222")
            result[1].hendelse.simuleringstype shouldBe SimuleringTypeEnum.AFP
            result[1].antall shouldBe 30
        }
    }
})

class MaanedligStatistikkRowMapperTest : FunSpec({

    test("mapper ResultSet til MaanedligStatistikkDto") {
        val rowMapper = MaanedligStatistikkRowMapper()
        val resultSet = mockk<ResultSet>()
        every { resultSet.getInt("AAR_MAANED") } returns 202406
        every { resultSet.getString("STATISTIKK") } returns """[{"hendelse":{"organisasjonsnummer":"123456789","simuleringstype":"ALDER"},"antall":100}]"""

        val result = rowMapper.mapRow(resultSet, 0)

        result?.aarMaaned shouldBe 202406
        result?.statistikk shouldContain "123456789"
    }

    test("mapper ulike aarMaaned verdier") {
        val rowMapper = MaanedligStatistikkRowMapper()

        listOf(202401, 202406, 202412, 202501).forEach { aarMaaned ->
            val resultSet = mockk<ResultSet>()
            every { resultSet.getInt("AAR_MAANED") } returns aarMaaned
            every { resultSet.getString("STATISTIKK") } returns "[]"

            val result = rowMapper.mapRow(resultSet, 0)

            result?.aarMaaned shouldBe aarMaaned
        }
    }

    test("mapper statistikk JSON string") {
        val rowMapper = MaanedligStatistikkRowMapper()
        val expectedJson = """[{"hendelse":{"organisasjonsnummer":"987654321","simuleringstype":"AFP"},"antall":42}]"""
        val resultSet = mockk<ResultSet>()
        every { resultSet.getInt("AAR_MAANED") } returns 202406
        every { resultSet.getString("STATISTIKK") } returns expectedJson

        val result = rowMapper.mapRow(resultSet, 0)

        result?.statistikk shouldBe expectedJson
    }
})
