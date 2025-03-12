package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import java.util.*

/**
 * Persondetalj DTO (data transfer object) received from PEN.
 * See PersonDetalj for description of fields.
 * Corresponds with no.nav.pensjon.pen.domain.api.simulator.grunnlag.PersonDetalj in PEN.
 */
data class PenPersonDetalj(
    val grunnlagsrolleEnum: GrunnlagsrolleEnum? = null,
    val rolleFomDato: Date? = null,
    val rolleTomDato: Date? = null,
    val sivilstandTypeEnum: SivilstandEnum? = null,
    val sivilstandRelatertPerson: PenPenPerson? = null,
    val borMedEnum: BorMedTypeEnum? = null,
    val barnDetalj: PenBarnDetalj? = null,
    val tillegg: Boolean = false,
    val bruk: Boolean = true,
    val grunnlagKildeEnum: GrunnlagkildeEnum? = null,
    val serskiltSatsUtenET: Boolean? = null,
    val epsAvkallEgenPensjon: Boolean? = null,
    //--- Extra:
    val virkFom: Date? = null,
    val virkTom: Date? = null
)
