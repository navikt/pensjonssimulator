package no.nav.pensjon.simulator.core.virkning

import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.krav.KravlinjeType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
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
            .filter { gjelderAvdod(it) }
            .forEach { addAvdoedDatoGrunnlag(it, soekerDatoGrunnlagListe) }
    }

    private fun addAvdoedDatoGrunnlag(
        avdoedPersongrunnlag: Persongrunnlag,
        soekerDatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
    ) {
        val transferList = mutableListOf<ForsteVirkningsdatoGrunnlag>()
        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe.forEach { transferList.add(copy(it)) }

        soekerDatoGrunnlagListe
            .filter { gjelderAvdod(it) }
            .forEach { transferList.add(it) }

        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe = transferList
    }

    private fun copy(grunnlag: ForsteVirkningsdatoGrunnlag) =
        ForsteVirkningsdatoGrunnlag(
            grunnlag.virkningsdato,
            grunnlag.kravFremsattDato,
            grunnlag.bruker, // NB in ForsteVirkningsDatoMapper this is grunnlag.sak.penPerson
            grunnlag.annenPerson,
            grunnlag.kravlinjeType
        )

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
            .filter { it.kravlinjeType == kravlinje.kravlinjeType }
            .filter { soeker.penPersonId == it.bruker!!.penPersonId }
            .any { hasMatchingAnnenPerson(annenPerson, it) }

        if (alreadyExists) return

        transferList.add(datoGrunnlag(kravlinje.kravlinjeType!!, kravhode, soeker, annenPerson))
    }

    private fun datoGrunnlag(
        kravlinjeType: KravlinjeTypeCti,
        kravhode: Kravhode,
        soeker: PenPerson,
        annenPerson: PenPerson?
    ) =
        ForsteVirkningsdatoGrunnlag(
            virkningsdato = kravhode.onsketVirkningsdato?.noon(),
            kravFremsattDato = kravhode.kravFremsattDato?.noon(),
            bruker = soeker,
            annenPerson = if (gjelderForsorgingstillegg(kravlinjeType)) annenPerson else null,
            kravlinjeType = kravlinjeType
        )

    private fun hasMatchingAnnenPerson(annenPerson: PenPerson?, grunnlag: ForsteVirkningsdatoGrunnlag) =
        if (gjelderForsorgingstillegg(grunnlag.kravlinjeType!!)) {
            personId(annenPerson) == personId(grunnlag.annenPerson)
        } else {
            true
        }

    private fun personId(person: PenPerson?) = person?.penPersonId ?: 0L

    private fun gjelderAvdod(datogrunnlag: ForsteVirkningsdatoGrunnlag) =
        kravlinjetyperForAvdod.contains(KravlinjeType.valueOf(datogrunnlag.kravlinjeType!!.kode))

    private fun gjelderAvdod(persongrunnlag: Persongrunnlag) =
        persongrunnlag.personDetaljListe
            .filter { it.bruk }
            .any { grunnlagsrollerForAvdod.contains(GrunnlagRolle.valueOf(it.grunnlagsrolle!!.kode)) }

    private fun gjelderForsorgingstillegg(kravlinjeType: KravlinjeTypeCti) =
        kravlinjetyperForForsorger.contains(KravlinjeType.valueOf(kravlinjeType.kode))

    private val grunnlagsrollerForAvdod = EnumSet.of(
        GrunnlagRolle.AVDOD,
        GrunnlagRolle.FAR,
        GrunnlagRolle.MOR
    )

    private val kravlinjetyperForAvdod = EnumSet.of(
        KravlinjeType.BP,
        KravlinjeType.GJP,
        KravlinjeType.GJR,
        KravlinjeType.UT_GJT
    )

    private val kravlinjetyperForForsorger = EnumSet.of(
        KravlinjeType.ET,
        KravlinjeType.BT
    )
}
