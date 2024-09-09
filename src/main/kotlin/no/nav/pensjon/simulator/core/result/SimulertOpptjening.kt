package no.nav.pensjon.simulator.core.result

/**
 * Opptjening for både kapittel 19 og 20 for ett enkelt kalenderår.
 */
// no.nav.domain.pensjon.kjerne.simulering.SimulertOpptjening
data class SimulertOpptjening(
    /**
     * Kalenderåret opptjeningen gjelder for
     */
     val kalenderAar: Int? = null,

    /**
     * Pensjonsgivende inntekt aktuelt årstall
     */
     val pensjonsgivendeInntekt: Int? = null,

    /**
     * Opptjent pensjonspoeng fra pensjonsgivende inntekt aktuelt årstall.
     */
     val pensjonsgivendeInntektPensjonspoeng: Double? = null,

    /**
     * Opptjent omsorgspoeng aktuelt årstall.
     */
     val omsorgPensjonspoeng: Double? = null,

    /**
     * Opptjent pensjonsbeholdning til og med aktuelt årstall.
     */
     val pensjonBeholdning: Int? = null,

    /**
     * Angir om bruker er godskrevet opptjening for omsorg dette året.
     */
     val omsorg: Boolean? = null,

    /**
     * Angir om bruker er godskrevet opptjening for dagpenger dette året.
     */
     val dagpenger: Boolean? = null,

    /**
     * Angir om bruker er godskrevet opptjening for dagpenger dette året.
     * En person kan både være ordinær dagpengemottaker og motta dagpenger som fiskere og fangstmenn iløpet av ett år.
     */
     val dagpengerFiskere: Boolean? = null,

    /**
     * Angir om bruker er godskrevet opptjening for forstegangstjeneste dette året.
     */
     val foerstegangstjeneste: Boolean? = null,

    /**
     * Angir om bruker er godskrevet opptjening for uførepensjon dette året
     */
     val harUfoere: Boolean? = null,

    /**
     * Angir om bruker har mottatt offentlig AFP i dette kalenderåret
     */
     val harOffentligAfp: Boolean? = null
)
