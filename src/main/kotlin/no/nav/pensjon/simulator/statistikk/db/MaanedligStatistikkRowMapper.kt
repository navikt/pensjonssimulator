package no.nav.pensjon.simulator.statistikk.db

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

internal class MaanedligStatistikkRowMapper : RowMapper<MaanedligStatistikkDto> {

    override fun mapRow(result: ResultSet, rowNum: Int) =
        MaanedligStatistikkDto(
            aarMaaned = result.getInt("AAR_MAANED"),
            statistikk = result.getString("STATISTIKK")
        )
}
