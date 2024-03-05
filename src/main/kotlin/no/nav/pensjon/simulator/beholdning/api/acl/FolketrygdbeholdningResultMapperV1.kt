package no.nav.pensjon.simulator.beholdning.api.acl

import java.time.LocalDate

object FolketrygdbeholdningResultMapperV1 {

    fun resultV1() =
        SimulerFolketrygdbeholdningResultV1(
            pensjonsBeholdningsPeriodeListe = listOf(
                pensjonsbeholdningPeriodeV1(number = 1),
                pensjonsbeholdningPeriodeV1(number = 2)
            )
        )

    private fun pensjonsbeholdningPeriodeV1(number: Int) =
        PensjonsbeholdningPeriodeV1(
            pensjonsBeholdning = 1000000 * number,
            garantiPensjonsBeholdning = 200000 * number,
            garantiPensjonsNiva = garantipensjonNivaaV1(number),
            fraOgMedDato = LocalDate.of(2000 + number, 15 + number, 29 - number)
        )

    private fun garantipensjonNivaaV1(number: Int) =
        GarantipensjonNivaaV1(
            belop = 100000 * number,
            satsType = if (number == 1) SatsTypeV1.ORDINAER else SatsTypeV1.HOY,
            sats = number,
            anvendtTrygdetid = 20 * number
        )
}
