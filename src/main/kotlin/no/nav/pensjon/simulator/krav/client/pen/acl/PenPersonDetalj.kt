package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import java.util.*

/**
 * Persondetalj DTO (data transfer object) received from PEN.
 * Corresponds to no.nav.pensjon.pen.domain.api.simulator.grunnlag.PersonDetalj in PEN.
 */
class PenPersonDetalj(
    var grunnlagsrolleEnum: GrunnlagsrolleEnum? = null,
    var rolleFomDato: Date? = null,
    var rolleTomDato: Date? = null,
    var sivilstandTypeEnum: SivilstandEnum? = null,
    var sivilstandRelatertPerson: PenPenPerson? = null,
    var borMedTypeEnum: BorMedTypeEnum? = null,
    var barnDetalj: PenBarnDetalj? = null,
    var tillegg: Boolean = false,
    var bruk: Boolean = true,
    var grunnlagKildeEnum: GrunnlagkildeEnum? = null,
    var serskiltSatsUtenET: Boolean? = null,
    var epsAvkallEgenPensjon: Boolean? = null,
    var virkFom: Date? = null,
    var virkTom: Date? = null
)
