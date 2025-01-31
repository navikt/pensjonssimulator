package no.nav.pensjon.simulator.tech.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.temporal.ChronoUnit.DAYS

@Configuration
open class CacheConfiguration {

    @Bean
    open fun cacheManager() = CaffeineCacheManager()
}

object CacheConfigurator {

    fun <K : Any, V> createCache(name: String, manager: CaffeineCacheManager): Cache<K, V> =
        Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(Duration.of(1, DAYS))
            .recordStats()
            .build<K, V>()
            .also {
                @Suppress("UNCHECKED_CAST")
                manager.registerCustomCache(name, it as Cache<Any, Any>)
            }
}
