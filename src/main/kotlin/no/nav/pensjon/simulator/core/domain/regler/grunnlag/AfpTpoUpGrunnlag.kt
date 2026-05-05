package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.time.LocalDate

// 2026-04-23
/**
 * Informasjon om tjenestepensjonsordningers uførepensjonsgrunnlag.
 * Dette er manuelt registrerte data og ikke hentet fra TP-registeret eller andre eksterne kilder.
 */
class AfpTpoUpGrunnlag {
    /**
     * Beløp som skal benyttes i AFP-beregning dersom bruker har hatt uførepensjon fra TPO
     */
    var belop = 0

    /**
     * Dato som beløpet ovenfor var gyldig
     */
    var virkFomLd: LocalDate? = null
}
