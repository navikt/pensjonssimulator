package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagsrolleCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.util.*

/**
 * Persondetalj DTO (data transfer object) received from PEN.
 * Corresponds to PersonDetaljDtoForSimulator in PEN.
 */
class PenPersonDetalj(
    var grunnlagsrolle: GrunnlagsrolleCti? = null,
    var rolleFomDato: Date? = null,
    var rolleTomDato: Date? = null,
    var sivilstandType: SivilstandTypeCti? = null,
    var sivilstandRelatertPerson: PenPenPerson? = null,
    var borMed: BorMedTypeCti? = null,
    var barnDetalj: PenBarnDetalj? = null,
    var tillegg: Boolean = false,
    var bruk: Boolean = true,
    var grunnlagKilde: GrunnlagKildeCti? = null,
    var serskiltSatsUtenET: Boolean? = null,
    var epsAvkallEgenPensjon: Boolean? = null,
    var virkFom: Date? = null,
    var virkTom: Date? = null
)
