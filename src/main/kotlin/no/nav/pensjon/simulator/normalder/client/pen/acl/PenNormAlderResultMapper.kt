package no.nav.pensjon.simulator.normalder.client.pen.acl

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.normalder.Aldersgrenser
import no.nav.pensjon.simulator.normalder.VerdiStatus

object PenNormalderResultMapper {

    fun fromDto(source: PenNormalderResult): List<Aldersgrenser> =
        source.normertPensjonsalderListe?.map(::normalder) ?: throw exception(source)

    private fun normalder(source: PenNormertPensjonsalder) =
        Aldersgrenser(
            aarskull = source.aarskull,
            alder = Alder(source.aar, source.maaned),
            nedreAlder = Alder(source.nedreAar, source.nedreMaaned),
            normalder = Alder(source.aar, source.maaned),
            oevreAlder = Alder(source.oevreAar, source.oevreMaaned),
            verdiStatus = VerdiStatus.valueOf(source.type)
        )

    private fun exception(source: PenNormalderResult) =
        RuntimeException("Normalder-feil for årskull ${source.aarskull}: ${source.message}")
}
