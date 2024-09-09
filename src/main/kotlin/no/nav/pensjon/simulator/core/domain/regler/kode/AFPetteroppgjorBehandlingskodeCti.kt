package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class AFPetteroppgjorBehandlingskodeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(afpEtteroppgjorBehandlingskodeCti: AFPetteroppgjorBehandlingskodeCti?) : super(
        afpEtteroppgjorBehandlingskodeCti!!
    )
}
