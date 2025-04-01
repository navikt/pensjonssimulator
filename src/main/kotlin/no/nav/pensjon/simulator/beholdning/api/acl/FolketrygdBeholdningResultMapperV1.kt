package no.nav.pensjon.simulator.beholdning.api.acl

import no.nav.pensjon.simulator.beholdning.BeholdningPeriode
import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdning
import no.nav.pensjon.simulator.beholdning.GarantipensjonNivaa

object FolketrygdBeholdningResultMapperV1 {

    fun resultV1(source: FolketrygdBeholdning) =
        FolketrygdBeholdningResultV1(
            pensjonsBeholdningsPeriodeListe = source.pensjonBeholdningPeriodeListe.map(::beholdningPeriode)
        )

    private fun beholdningPeriode(source: BeholdningPeriode) =
        PensjonsbeholdningPeriodeV1(
            pensjonsBeholdning = source.pensjonBeholdning,
            garantiPensjonsBeholdning = source.garantipensjonBeholdning,
            garantitilleggsbeholdning = source.garantitilleggBeholdning,
            garantiPensjonsNiva = garantipensjonNivaa(source.garantipensjonNivaa),
            fraOgMedDato = source.fom
        )

    private fun garantipensjonNivaa(source: GarantipensjonNivaa) =
        GarantipensjonNivaaV1(
            belop = source.beloep,
            satsType = SatsTypeV1.fromInternalValue(source.satsType).externalValue,
            sats = source.sats,
            anvendtTrygdetid = source.anvendtTrygdetid
        )
}
