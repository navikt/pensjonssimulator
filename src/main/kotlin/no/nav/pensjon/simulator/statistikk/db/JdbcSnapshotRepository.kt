package no.nav.pensjon.simulator.statistikk.db

import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.statistikk.MaanedligStatistikk
import no.nav.pensjon.simulator.statistikk.SimuleringHendelse
import no.nav.pensjon.simulator.statistikk.SimuleringStatistikk
import no.nav.pensjon.simulator.statistikk.SnapshotRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Repository
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper

@Repository
class JdbcSnapshotRepository(
    private val db: NamedParameterJdbcOperations,
    private val jsonMapper: JsonMapper
) : SnapshotRepository {

    private val rowMapper = MaanedligStatistikkRowMapper()

    override fun create(snapshot: MaanedligStatistikk) {
        db.update(INSERT_SQL, dataMap(dto(snapshot)))
    }

    override fun read(aarMaaned: Int): List<SimuleringStatistikk> {
        val statistikk = db.query(SELECT_SQL, dataMap(aarMaaned), rowMapper)[0].statistikk
        return jsonMapper.readValue(statistikk, object : TypeReference<List<SimuleringStatistikkDto>>() {})
            .map(::fromDto)
    }

    private fun dataMap(snapshot: StatistikkSnapshotDto): Map<String, Any> =
        mapOf(
            "aar_maaned" to snapshot.aarMaaned,
            "statistikk" to jsonMapper.writeValueAsString(snapshot.statistikk)
        )

    private companion object {
        const val INSERT_SQL: String =
            "INSERT INTO PENSJONSSIMULATOR.SIMULERING_TELLER_SNAPSHOT (AAR_MAANED, TIDSPUNKT, STATISTIKK)" +
                    " VALUES(:aar_maaned, NOW(), :statistikk) ON CONFLICT DO NOTHING"

        const val SELECT_SQL: String =
            "SELECT AAR_MAANED, STATISTIKK FROM PENSJONSSIMULATOR.SIMULERING_TELLER_SNAPSHOT" +
                    " WHERE AAR_MAANED = :aar_maaned"

        private fun dataMap(aarMaaned: Int): Map<String, Any> =
            mapOf("aar_maaned" to aarMaaned)

        private fun dto(source: MaanedligStatistikk) =
            StatistikkSnapshotDto(
                aarMaaned = source.aarMaaned,
                statistikk = source.statistikk.map(::dto)
            )

        private fun dto(source: SimuleringStatistikk) =
            SimuleringStatistikkDto(
                hendelse = dto(source.hendelse),
                antall = source.antall
            )

        private fun dto(source: SimuleringHendelse) =
            SimuleringHendelseDto(
                organisasjonsnummer = source.organisasjonsnummer.value,
                simuleringstype = source.simuleringstype
            )

        private fun fromDto(source: SimuleringStatistikkDto) =
            SimuleringStatistikk(
                hendelse = fromDto(source.hendelse),
                antall = source.antall
            )

        private fun fromDto(source: SimuleringHendelseDto) =
            SimuleringHendelse(
                organisasjonsnummer = Organisasjonsnummer(value = source.organisasjonsnummer),
                simuleringstype = source.simuleringstype
            )
    }
}
