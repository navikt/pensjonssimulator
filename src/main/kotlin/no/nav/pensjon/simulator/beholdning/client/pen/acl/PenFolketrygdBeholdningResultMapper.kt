package no.nav.pensjon.simulator.beholdning.client.pen.acl

import no.nav.pensjon.simulator.beholdning.*

object PenFolketrygdBeholdningResultMapper {
    fun fromDto(dto: PenFolketrygdBeholdningResult) =
        FolketrygdBeholdning(
            pensjonBeholdningPeriodeListe = dto.pensjonBeholdningPeriodeListe.orEmpty().map(::beholdningPeriode)
        )

    private fun beholdningPeriode(dto: PenPensjonsbeholdningPeriode) =
        BeholdningPeriode(
            pensjonBeholdning = dto.pensjonBeholdning ?: 0,
            garantipensjonBeholdning = dto.garantipensjonBeholdning ?: 0,
            garantipensjonNivaa = dto.garantipensjonNivaa?.let(::garantipensjonNivaa)!!,
            fom = dto.fom!!
        )

    private fun garantipensjonNivaa(dto: PenGarantipensjonNivaa) =
        GarantipensjonNivaa(
            beloep = dto.beloep ?: 0,
            satsType = PenSatsType.fromExternalValue(dto.satsType).internalValue,
            sats = dto.sats ?: 0,
            anvendtTrygdetid = dto.anvendtTrygdetid ?: 0
        )
}
