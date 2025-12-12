package no.nav.pensjon.simulator.statistikk.db

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.statistikk.SimuleringHendelse
import no.nav.pensjon.simulator.statistikk.SimuleringStatistikk
import no.nav.pensjon.simulator.statistikk.StatistikkRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.Map

@Repository
open class JdbcStatistikkRepository(private val db: NamedParameterJdbcOperations) : StatistikkRepository {

    private val log = KotlinLogging.logger { }
    private val rowMapper = SimuleringStatistikkRowMapper()

    override fun update(hendelse: SimuleringHendelse) {
        db.update(UPDATE_SQL, dataMap(hendelse))
            .also { log.info { "Rows updated in SIMULERING_TELLER: $it" } }
    }

    override fun read(): List<SimuleringStatistikk> =
        db.query(SELECT_SQL, rowMapper)

    private companion object {
        const val UPDATE_SQL: String =
            "UPDATE PENSJONSSIMULATOR.SIMULERING_TELLER SET ANTALL = ANTALL + 1" +
                    " WHERE ORG_NR = :org_nr AND SIMULERINGSTYPE = :simuleringstype"

        const val SELECT_SQL: String =
            "SELECT ORG_NR, SIMULERINGSTYPE, ANTALL FROM PENSJONSSIMULATOR.SIMULERING_TELLER ORDER BY ANTALL DESC"

        private fun dataMap(hendelse: SimuleringHendelse): MutableMap<String, Any> =
            Map.of<String, Any>(
                "org_nr", hendelse.organisasjonsnummer.value,
                "simuleringstype", hendelse.simuleringstype.name
            )
    }
}

internal class SimuleringStatistikkRowMapper : RowMapper<SimuleringStatistikk> {

    override fun mapRow(result: ResultSet, rowNum: Int) =
        SimuleringStatistikk(
            hendelse = hendelse(result),
            antall = result.getInt("ANTALL")
        )

    private fun hendelse(result: ResultSet) =
        SimuleringHendelse(
            organisasjonsnummer = Organisasjonsnummer(result.getString("ORG_NR")),
            simuleringstype = SimuleringTypeEnum.valueOf(result.getString("SIMULERINGSTYPE"))
        )
}
