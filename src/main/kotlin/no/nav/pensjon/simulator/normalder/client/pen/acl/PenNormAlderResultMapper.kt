package no.nav.pensjon.simulator.normalder.client.pen.acl

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.normalder.NormertPensjonsalder
import no.nav.pensjon.simulator.normalder.PensjonsalderType

object PenNormAlderResultMapper {

    fun fromDto(source: PenNormAlderResult): List<NormertPensjonsalder> =
        source.normertPensjonsalderListe?.map(::normAlder) ?: throw exception(source)

    private fun normAlder(source: PenNormertPensjonsalder) =
        NormertPensjonsalder(
            aarskull = source.aarskull,
            alder = Alder(source.aar, source.maaned),
            nedreAlder = Alder(source.nedreAar, source.nedreMaaned),
            oevreAlder = Alder(source.oevreAar, source.oevreMaaned),
            type = PensjonsalderType.valueOf(source.type)
        )

    private fun exception(source: PenNormAlderResult) =
        RuntimeException("Normalder-feil for årskull ${source.aarskull}: ${source.message}")
}
