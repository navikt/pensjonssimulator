package no.nav.pensjon.simulator.vedtak

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.util.LocalDateUtil.earliest
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import java.time.LocalDate

object VedtakUtil {

    private val kravlinjeTyperForTillegg: List<KravlinjeTypeEnum> =
        listOf(
            KravlinjeTypeEnum.BT, // barnetillegg
            KravlinjeTypeEnum.ET // ektefelletillegg
        )

    fun foersteVirkningsdato(
        vedtakListe: MutableList<VilkarsVedtak>,
        virkningListe: List<FoersteVirkningDato>,
        kravlinjetype: KravlinjeTypeEnum?,
        gjelderPerson: PenPerson?
    ): LocalDate? =
        earliest(
            foersteVirkningsdatoForRelevanteVirkninger(virkningListe, kravlinjetype, gjelderPerson),
            foersteVirkningsdatoForInnvilgedeVedtak(vedtakListe, kravlinjetype)
        )

    // PEN: kjerne.Vilkarsvedtak.findFirstInnvilgetDateForKravlinjeOnSakForKravlinjePerson
    private fun foersteVirkningsdatoForRelevanteVirkninger(
        virkningListe: List<FoersteVirkningDato>,
        kravlinjetype: KravlinjeTypeEnum?,
        gjelderPerson: PenPerson?
    ): LocalDate? =
        virkningListe
            .filter { erRelevantVirkning(it, kravlinjetype, gjelderPerson) }
            .map { it.virkningDato }
            .firstOrNull()

    // PEN: kjerne.Vilkarsvedtak.findFirstInnvilgetDateForKravlinjeOnVedtak
    private fun foersteVirkningsdatoForInnvilgedeVedtak(
        vedtakListe: List<VilkarsVedtak>,
        kravlinjetype: KravlinjeTypeEnum?
    ): LocalDate? =
        vedtakListe
            .filter { it.kravlinjeTypeEnum == kravlinjetype && VedtakResultatEnum.INNV == it.vilkarsvedtakResultatEnum }
            .mapNotNull { it.virkFomLd }
            .minByOrNull { it } // legacy uses 'nullsLast' i.e. nulls > non-nulls. Hence, mapNotNull + minByOrNull ought to be equivalent

    // PEN: kjerne.Vilkarsvedtak.hasSamePersonAndKravlinjetypeAsVilkarsvedtak
    //      + isEtOrBtAndCorrectPerson + isEktefelleOrBarnetillegg
    private fun erRelevantVirkning(
        virkning: FoersteVirkningDato,
        kravlinjetype: KravlinjeTypeEnum?,
        gjelderPerson: PenPerson?
    ): Boolean {
        val erTillegg = kravlinjeTyperForTillegg.contains(kravlinjetype)

        return ((erTillegg.not() || erTillegg && gjelderPerson == virkning.annenPerson)
                && virkning.kravlinjeType == kravlinjetype)
    }
}