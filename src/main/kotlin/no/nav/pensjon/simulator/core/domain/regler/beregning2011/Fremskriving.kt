package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FremskrevetMPNTypeCti

interface Fremskriving {

    var teller: Int

    var nevner: Int

    var brok: Double

    var type: FremskrevetMPNTypeCti
}
