package no.nav.pensjon.simulator.tech.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
open class DataSourceConfig {

    @Bean
    open fun dataSource(
        @Value("\${psdb.url}") dbUrl: String,
        @Value("\${psdb.user}") dbUsername: String,
        @Value("\${psdb.password}") dbPassword: String
    ): DataSource =
        HikariDataSource(
            HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = dbUrl
                username = dbUsername
                password = dbPassword
                maximumPoolSize = 5
            }
        )
}
