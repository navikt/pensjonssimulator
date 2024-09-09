package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class AFPetteroppgjorGruppeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(afpetteroppgjorGruppeCti: AFPetteroppgjorGruppeCti?) : super(afpetteroppgjorGruppeCti!!)

}
