package no.nav.pensjon.simulator.core.person

import no.nav.pensjon.simulator.core.SimuleringSpec
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InngangOgEksportGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import org.springframework.stereotype.Component
import java.time.LocalDate

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.PersongrunnlagMapper
@Component
class PersongrunnlagMapper(private val generelleDataHolder: GenerelleDataHolder) {

    fun mapToPersongrunnlag(person: PenPerson, spec: SimuleringSpec) =
        createPersongrunnlag(
            person = person,
            personDetalj = createPersonDetalj(spec),
            utlandAntallAar = spec.utlandAntallAar,
            erFlyktning = spec.flyktning
        ).also {
            it.over60ArKanIkkeForsorgesSelv = false
            it.sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
            it.inngangOgEksportGrunnlag = InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
        }

    fun mapToEpsPersongrunnlag(sivilstatus: SivilstatusType, foedselsdato: LocalDate) =
        Persongrunnlag().apply {
            gjelderOmsorg = false
            gjelderUforetrygd = false
            penPerson = PenPerson(EPS_PERSON_ID)
            this.fodselsdato = fromLocalDate(foedselsdato)
            antallArUtland = 0
            dodsdato = null
            statsborgerskapEnum = norge
            flyktning = false
            bosattLandEnum = norge
            afpHistorikkListe = mutableListOf()
            uforeHistorikk = null
            generellHistorikk = null
            personDetaljListe.add(mapToEpsPersonDetalj(sivilstatus, foedselsdato))
        }.also { it.finishInit() }

    private fun createPersongrunnlag(
        person: PenPerson,
        personDetalj: PersonDetalj,
        utlandAntallAar: Int,
        erFlyktning: Boolean?
    ) =
        Persongrunnlag().apply {
            gjelderOmsorg = false
            gjelderUforetrygd = false
            penPerson = person
            fodselsdato = person.fodselsdato
            statsborgerskapEnum = person.pid?.let { generelleDataHolder.getPerson(it).statsborgerskap }
            bosattLandEnum = norge
            afpHistorikkListe = person.afpHistorikkListe ?: mutableListOf()
            uforeHistorikk = person.uforehistorikk
            generellHistorikk = person.generellHistorikk
            personDetaljListe.add(personDetalj)
            medlemIFolketrygdenSiste3Ar = true
            antallArUtland = utlandAntallAar
            flyktning = erFlyktning
        }.also { it.finishInit() }

    private companion object {
        private const val EPS_PERSON_ID = -2L
        private val norge = LandkodeEnum.NOR

        private fun createPersonDetalj(spec: SimuleringSpec) =
            PersonDetalj().apply {
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                /* AFP_FPP is irrelevant for pensjonskalkulator
                if (SimuleringTypeCode.AFP_FPP.equals(simulering.type)) {
                    rolleFomDato = DateUtils.getLastDateOfPreviousMonth(simulering.forsteUttakDato)
                } else {
                    rolleFomDato = DateProvider.getToday()
                }*/
                rolleFomDato = fromLocalDate(LocalDate.now())
                sivilstandTypeEnum = mapToSivilstand(spec)
                bruk = true
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            }.also { it.finishInit() }

        private fun mapToSivilstand(spec: SimuleringSpec): SivilstandEnum {
            if (spec.type == SimuleringType.ALDER_M_GJEN || spec.type == SimuleringType.ENDR_ALDER_M_GJEN) {
                return SivilstandEnum.ENKE
            }

            return when (spec.sivilstatus) {
                SivilstatusType.GIFT -> SivilstandEnum.GIFT
                SivilstatusType.REPA -> SivilstandEnum.REPA
                else -> SivilstandEnum.UGIF
            }
        }

        // PersongrunnlagMapper.mapToPersonDetaljEPS
        private fun mapToEpsPersonDetalj(sivilstatus: SivilstatusType, foedselsdato: LocalDate?) =
            PersonDetalj().apply {
                grunnlagsrolleEnum = mapToEpsGrunnlagRolle(sivilstatus)
                rolleFomDato = fromLocalDate(foedselsdato)
                borMedEnum = mapToEpsBorMedType(sivilstatus)
                bruk = true
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            }.also { it.finishInit() }

        private fun mapToEpsGrunnlagRolle(sivilstatus: SivilstatusType): GrunnlagsrolleEnum? =
            when (sivilstatus) {
                SivilstatusType.SAMB -> GrunnlagsrolleEnum.SAMBO
                SivilstatusType.GIFT -> GrunnlagsrolleEnum.EKTEF
                SivilstatusType.REPA -> GrunnlagsrolleEnum.PARTNER
                else -> null
            }

        // PersongrunnlagMapper.mapToBorMedTypeEPS
        private fun mapToEpsBorMedType(sivilstatus: SivilstatusType): BorMedTypeEnum? =
            when (sivilstatus) {
                SivilstatusType.SAMB -> BorMedTypeEnum.SAMBOER1_5
                SivilstatusType.GIFT -> BorMedTypeEnum.J_EKTEF
                SivilstatusType.REPA -> BorMedTypeEnum.J_PARTNER
                else -> null
            }
    }
}
