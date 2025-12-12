package no.nav.pensjon.simulator.tech.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.configuration.ClassicConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

@Configuration
@Profile("!test")
open class DataSourceConfig {

    @Bean
    open fun dataSource(
        @Value("\${psdb.jdbc.url}") dbUrl: String,
        @Value("\${psdb.username}") dbUsername: String,
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

    /**
     * Temporary bean for wiping the database.
     */
    @Bean(initMethod = "migrate")
    open fun flyway(datasource: DataSource): Flyway {
        val config = ClassicConfiguration().apply {
            this.dataSource = datasource
            this.isCleanDisabled = false
            this.setLocations(Location.fromPath("classpath:", "db/migration"))
        }

        return Flyway(config).apply { clean() }
    }
}
