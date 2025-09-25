package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.AFPGrunnlagBeholdningPeriode
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.SimulerAFPBeholdningGrunnlagRequest

interface AFPBeholdningClient {
    fun simulerAFPBeholdningGrunnlag(request: SimulerAFPBeholdningGrunnlagRequest): List<AFPGrunnlagBeholdningPeriode>
}
