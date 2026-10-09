package no.nav.pensjon.simulator.opptjening.client.popp

import com.github.benmanes.caffeine.cache.Cache
import no.nav.pensjon.simulator.opptjening.BeholdningSpec
import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSpec
import no.nav.pensjon.simulator.opptjening.PensjonspoengSpec
import no.nav.pensjon.simulator.opptjening.client.popp.acl.PoppBeholdningResult
import no.nav.pensjon.simulator.opptjening.client.popp.acl.PoppOpptjeningsgrunnlag
import no.nav.pensjon.simulator.opptjening.client.popp.acl.PoppOpptjeningsgrunnlagResult
import no.nav.pensjon.simulator.opptjening.client.popp.acl.PoppPensjonspoengResult
import no.nav.pensjon.simulator.tech.cache.CacheConfigurator.createCache
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.stereotype.Component

@Component
class PoppOpptjeningCache(cacheManager: CaffeineCacheManager) {

    private val beholdningCache: Cache<BeholdningSpec, PoppBeholdningResult> =
        createCache("popp-beholdninger", cacheManager)

    private val grunnlagCache: Cache<OpptjeningsgrunnlagSpec, PoppOpptjeningsgrunnlagResult> =
        createCache("popp-opptjeningsgrunnlag", cacheManager)

    private val pensjonspoengCache: Cache<PensjonspoengSpec, PoppPensjonspoengResult> =
        createCache("popp-pensjonspoeng", cacheManager)

    fun cachedOrFreshBeholdninger(
        spec: BeholdningSpec,
        fetcher: (BeholdningSpec) -> PoppBeholdningResult?
    ): PoppBeholdningResult =
        beholdningCache.getIfPresent(spec)
            ?: (fetcher.invoke(spec) ?: PoppBeholdningResult(beholdninger = emptyList()))
                .also { beholdningCache.put(spec, it) }

    fun cachedOrFreshOpptjeningsgrunnlag(
        spec: OpptjeningsgrunnlagSpec,
        fetcher: (OpptjeningsgrunnlagSpec) -> PoppOpptjeningsgrunnlagResult?
    ): PoppOpptjeningsgrunnlagResult =
        grunnlagCache.getIfPresent(spec)
            ?: (fetcher.invoke(spec) ?: defaultResult(spec))
                .also { grunnlagCache.put(spec, it) }

    fun cachedOrFreshPensjonspoeng(
        spec: PensjonspoengSpec,
        fetcher: (PensjonspoengSpec) -> PoppPensjonspoengResult?
    ): PoppPensjonspoengResult =
        pensjonspoengCache.getIfPresent(spec)
            ?: (fetcher.invoke(spec) ?: PoppPensjonspoengResult(pensjonspoeng = emptyList()))
                .also { pensjonspoengCache.put(spec, it) }

    private companion object {
        private fun defaultResult(spec: OpptjeningsgrunnlagSpec): PoppOpptjeningsgrunnlagResult =
            PoppOpptjeningsgrunnlagResult(opptjeningsGrunnlag = PoppOpptjeningsgrunnlag(fnr = spec.pid.value))
    }
}