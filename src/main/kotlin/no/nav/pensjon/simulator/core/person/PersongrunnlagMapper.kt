package no.nav.pensjon.simulator.core.person

import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.SimuleringSpec
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.domain.*
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InngangOgEksportGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.*
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import org.springframework.stereotype.Component
import java.time.LocalDate

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.PersongrunnlagMapper
@Component
class PersongrunnlagMapper(val context: SimulatorContext) {

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
            statsborgerskap = norge
            flyktning = false
            bosattLand = norge
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
            statsborgerskap =
                norge //ANON person.pid?.let(context::getStatsborgerskap)?.let { LandCti(it.name) } ?: norge
            bosattLand = norge
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
        private val norge = LandCti(Land.NOR.name)

        private fun createPersonDetalj(spec: SimuleringSpec) =
            PersonDetalj().apply {
                grunnlagsrolle = GrunnlagsrolleCti(GrunnlagRolle.SOKER.name)
                /* AFP_FPP is irrelevant for pensjonskalkulator
                if (SimuleringTypeCode.AFP_FPP.equals(simulering.type)) {
                    rolleFomDato = DateUtils.getLastDateOfPreviousMonth(simulering.forsteUttakDato)
                } else {
                    rolleFomDato = DateProvider.getToday()
                }*/
                rolleFomDato = fromLocalDate(LocalDate.now())
                sivilstandType = SivilstandTypeCti(mapToSivilstand(spec).name)
                bruk = true
                grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
            }.also { it.finishInit() }

        private fun mapToSivilstand(spec: SimuleringSpec): SivilstandType {
            if (spec.type == SimuleringType.ALDER_M_GJEN || spec.type == SimuleringType.ENDR_ALDER_M_GJEN) {
                return SivilstandType.ENKE
            }

            return when (spec.sivilstatus) {
                SivilstatusType.GIFT -> SivilstandType.GIFT
                SivilstatusType.REPA -> SivilstandType.REPA
                else -> SivilstandType.UGIF
            }
        }

        // PersongrunnlagMapper.mapToPersonDetaljEPS
        private fun mapToEpsPersonDetalj(sivilstatus: SivilstatusType, foedselsdato: LocalDate?) =
            PersonDetalj(null, null).apply {
                grunnlagsrolle = mapToEpsGrunnlagRolle(sivilstatus)?.let { GrunnlagsrolleCti(it.name) }
                rolleFomDato = fromLocalDate(foedselsdato)
                borMed = mapToEpsBorMedType(sivilstatus)?.let { BorMedTypeCti(it.name) }
                bruk = true
                grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
            }.also { it.finishInit() }

        private fun mapToEpsGrunnlagRolle(sivilstatus: SivilstatusType) =
            when (sivilstatus) {
                SivilstatusType.SAMB -> GrunnlagRolle.SAMBO
                SivilstatusType.GIFT -> GrunnlagRolle.EKTEF
                SivilstatusType.REPA -> GrunnlagRolle.PARTNER
                else -> null
            }

        // PersongrunnlagMapper.mapToBorMedTypeEPS
        private fun mapToEpsBorMedType(sivilstatus: SivilstatusType) =
            when (sivilstatus) {
                SivilstatusType.SAMB -> BorMedType.SAMBOER1_5
                SivilstatusType.GIFT -> BorMedType.J_EKTEF
                SivilstatusType.REPA -> BorMedType.J_PARTNER
                else -> null
            }
    }
}
