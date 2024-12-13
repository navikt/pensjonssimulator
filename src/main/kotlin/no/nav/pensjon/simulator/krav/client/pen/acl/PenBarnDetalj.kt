package no.nav.pensjon.simulator.krav.client.pen.acl

/**
 * Barnedetalj DTO (data transfer object) received from PEN.
 * Corresponds to BarnDetaljDtoForSimulator in PEN.
 */
class PenBarnDetalj(
    var annenForelder: PenPenPerson? = null,
    var borMedBeggeForeldre: Boolean = false,
    var inntektOver1G: Boolean = false,
    var underUtdanning: Boolean = false
    // borFomDato, borTomDato ikke i PEN
)
