package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class SakTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(typeCti: TypeCti) : super(typeCti)
    constructor(kode: String) : super(kode)
}

//public SatsTypeCti(TypeCti typeCti) {
//    super(typeCti);
//}
//
//public SatsTypeCti() {
//}
//
//public SatsTypeCti(String kode) {
//    super(kode);
//}
