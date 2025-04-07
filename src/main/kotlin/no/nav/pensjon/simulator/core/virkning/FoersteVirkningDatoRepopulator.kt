package no.nav.pensjon.simulator.core.virkning

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.util.*

/**
 * Repopulates søker's forsteVirkningsdatoGrunnlagListe with:
 * - copy of existing forsteVirkningsdatoGrunnlag entries
 * - additional forsteVirkningsdatoGrunnlag entries based on kravlinjer not already present in list
 *
 * Repopulates avdøde's forsteVirkningsdatoGrunnlagListe with:
 * - copy of existing forsteVirkningsdatoGrunnlag entries
 * - additional forsteVirkningsdatoGrunnlag entries based on søker's entries concerning avdøde
 */
// Corresponds to ForsteVirkningsDatoMapper
object FoersteVirkningDatoRepopulator {

    // ForsteVirkningsDatoMapper.mapForstevirkningsdatoGrunnlagTransfer
    fun mapFoersteVirkningDatoGrunnlagTransfer(kravhode: Kravhode) {
        val persongrunnlagListe = kravhode.persongrunnlagListe
        addSoekerDatoGrunnlag(kravhode, persongrunnlagListe)
        addAvdoedDatoGrunnlag(persongrunnlagListe)
    }

    // ForsteVirkningsDatoMapper.addForstevirkningsDatoGrunnlagTransferForSoker
    private fun addSoekerDatoGrunnlag(kravhode: Kravhode, persongrunnlagList: MutableList<Persongrunnlag>) {
        val persongrunnlag = persongrunnlagList.firstOrNull { it.isSoker() } ?: return
        val transferList = mutableListOf<ForsteVirkningsdatoGrunnlag>()
        persongrunnlag.forsteVirkningsdatoGrunnlagListe.forEach { transferList.add(copy(it)) }

        kravhode.kravlinjeListe.forEach {
            addDatogrunnlagBasedOnKrav(it, kravhode, persongrunnlag, transferList)
        }

        persongrunnlag.forsteVirkningsdatoGrunnlagListe = transferList
    }

    private fun addAvdoedDatoGrunnlag(persongrunnlagList: MutableList<Persongrunnlag>) {
        val soekerDatoGrunnlagListe = persongrunnlagList
            .filter { it.isSoker() }
            .flatMap { it.forsteVirkningsdatoGrunnlagListe }

        persongrunnlagList
            .filter { gjelderAvdoed(it) }
            .forEach { addAvdoedDatoGrunnlag(it, soekerDatoGrunnlagListe) }
    }

    private fun addAvdoedDatoGrunnlag(
        avdoedPersongrunnlag: Persongrunnlag,
        soekerDatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ) {
        val transferList = mutableListOf<ForsteVirkningsdatoGrunnlag>()
        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe.forEach { transferList.add(copy(it)) }

        soekerDatoGrunnlagListe
            .filter { gjelderAvdoed(it) }
            .forEach { transferList.add(it) }

        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe = transferList
    }

    private fun copy(grunnlag: ForsteVirkningsdatoGrunnlag) =
        ForsteVirkningsdatoGrunnlag().apply {
            virkningsdato = grunnlag.virkningsdato
            kravFremsattDato = grunnlag.kravFremsattDato
            bruker = grunnlag.bruker // NB in ForsteVirkningsDatoMapper this is grunnlag.sak.penPerson
            annenPerson = grunnlag.annenPerson
            kravlinjeTypeEnum = grunnlag.kravlinjeTypeEnum
        }

    private fun addDatogrunnlagBasedOnKrav(
        kravlinje: Kravlinje,
        kravhode: Kravhode,
        persongrunnlag: Persongrunnlag,
        transferList: MutableList<ForsteVirkningsdatoGrunnlag>
    ) {
        val soeker = persongrunnlag.penPerson!!
        val annenPerson =
            if (soeker.penPersonId == kravlinje.relatertPerson?.penPersonId) null else kravlinje.relatertPerson

        val alreadyExists = transferList
            .filter { it.kravlinjeTypeEnum == kravlinje.kravlinjeTypeEnum }
            .filter { soeker.penPersonId == it.bruker!!.penPersonId }
            .any { hasMatchingAnnenPerson(annenPerson, it) }

        if (alreadyExists) return

        kravlinje.kravlinjeTypeEnum?.let { transferList.add(datoGrunnlag(it, kravhode, soeker, annenPerson)) }
    }

    private fun datoGrunnlag(
        kravlinjeType: KravlinjeTypeEnum,
        kravhode: Kravhode,
        soeker: PenPerson,
        annenPerson: PenPerson?
    ) =
        ForsteVirkningsdatoGrunnlag().apply {
            this.virkningsdato = kravhode.onsketVirkningsdato?.toNorwegianDateAtNoon()
            this.kravFremsattDato = kravhode.kravFremsattDato?.noon()
            this.bruker = soeker
            this.annenPerson = if (gjelderForsorgingstillegg(kravlinjeType)) annenPerson else null
            this.kravlinjeTypeEnum = kravlinjeType
        }

    private fun hasMatchingAnnenPerson(annenPerson: PenPerson?, grunnlag: ForsteVirkningsdatoGrunnlag) =
        if (grunnlag.kravlinjeTypeEnum?.let(::gjelderForsorgingstillegg) == true) {
            personId(annenPerson) == personId(grunnlag.annenPerson)
        } else {
            true
        }

    private fun personId(person: PenPerson?) = person?.penPersonId ?: 0L

    private fun gjelderAvdoed(datogrunnlag: ForsteVirkningsdatoGrunnlag) =
        avdoedKravlinjeTyper.contains(datogrunnlag.kravlinjeTypeEnum)

    private fun gjelderAvdoed(persongrunnlag: Persongrunnlag) =
        persongrunnlag.personDetaljListe
            .filter { it.bruk == true }
            .any { avdoedGrunnlagRoller.contains(it.grunnlagsrolleEnum) }

    private fun gjelderForsorgingstillegg(kravlinjeType: KravlinjeTypeEnum) =
        forsoergerKravlinjeTyper.contains(kravlinjeType)

    private val avdoedGrunnlagRoller =
        EnumSet.of(
            GrunnlagsrolleEnum.AVDOD,
            GrunnlagsrolleEnum.FAR,
            GrunnlagsrolleEnum.MOR
        )

    private val avdoedKravlinjeTyper =
        EnumSet.of(
            KravlinjeTypeEnum.BP,
            KravlinjeTypeEnum.GJP,
            KravlinjeTypeEnum.GJR,
            KravlinjeTypeEnum.UT_GJT
        )

    private val forsoergerKravlinjeTyper =
        EnumSet.of(
            KravlinjeTypeEnum.ET,
            KravlinjeTypeEnum.BT
        )
}
