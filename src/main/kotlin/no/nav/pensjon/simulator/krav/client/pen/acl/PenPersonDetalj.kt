package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.TypeCti
import java.io.Serializable
import java.util.*

/**
 * Persondetalj DTO (data transfer object) received from PEN.
 * Corresponds to PersonDetaljDtoForSimulator in PEN.
 */
class PenPersonDetalj(
    var grunnlagsrolle: PenGrunnlagsrolleCti? = null,
    var grunnlagsrolleEnum: GrunnlagsrolleEnum? = null,
    var rolleFomDato: Date? = null,
    var rolleTomDato: Date? = null,
    var sivilstandType: PenSivilstandTypeCti? = null,
    var sivilstandTypeEnum: SivilstandEnum? = null,
    var sivilstandRelatertPerson: PenPenPerson? = null,
    var borMed: PenBorMedTypeCti? = null,
    var borMedTypeEnum: BorMedTypeEnum? = null,
    var barnDetalj: PenBarnDetalj? = null,
    var tillegg: Boolean = false,
    var bruk: Boolean = true,
    var grunnlagKilde: PenGrunnlagKildeCti? = null,
    var grunnlagKildeEnum: GrunnlagkildeEnum? = null,
    var serskiltSatsUtenET: Boolean? = null,
    var epsAvkallEgenPensjon: Boolean? = null,
    var virkFom: Date? = null,
    var virkTom: Date? = null
)

class PenBorMedTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(borMedTypeCti: PenBorMedTypeCti?) : super(borMedTypeCti!!)
}

class PenGrunnlagKildeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(grunnlagKildeCti: PenGrunnlagKildeCti?) : super(grunnlagKildeCti!!)
}

class PenGrunnlagsrolleCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(grunnlagsrolleCti: PenGrunnlagsrolleCti?) : super(grunnlagsrolleCti!!)
}

class PenSivilstandTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(sivilstandTypeCti: PenSivilstandTypeCti?) : super(sivilstandTypeCti!!)
}
