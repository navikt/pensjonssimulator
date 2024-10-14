package no.nav.pensjon.simulator.core.ytelse

import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getFirstDayOfMonth2
import java.time.LocalDate

// SimulerFleksibelAPCommand.hentLopendeYtelser + SimulerApCommonHelper.avdodesForsteVirkBasedOnLopendeYtelser
object LoependeYtelseGetter {

    // SimulerFleksibelAPCommand.hentLopendeYtelser + findForsteVirkDate + findBrukersForsteVirkBasedOnLopendeYtelser
    fun finnForsteVirkningsdatoer(
        spec: SimuleringSpec,
        soekerFoersteVirkningDatoListe: List<FoersteVirkningDato>,
        avdoedFoersteVirkningDatoListe: List<FoersteVirkningDato>
    ): LoependeYtelseResult {
        val soekerEldsteFoersteVirkningDato: LocalDate? =
            if (spec.erAnonym)
                spec.foersteUttakDato
            else
                finnEldsteForsteVirkningsdato(soekerFoersteVirkningDatoListe) ?: spec.foersteUttakDato

        val avdoedEldsteFoersteVirkningDato: LocalDate? =
            spec.avdoed?.let {
                if (spec.erAnonym)
                    forsteDagIManedenEtter(it.doedDato)
                else
                    finnEldsteForsteVirkningsdato(avdoedFoersteVirkningDatoListe) ?: forsteDagIManedenEtter(it.doedDato)
            }

        return LoependeYtelseResult(
            soekerFoersteVirkningDato = soekerEldsteFoersteVirkningDato,
            avdoedFoersteVirkningDato = avdoedEldsteFoersteVirkningDato,
            privatAfpFoersteVirkningDato = if (spec.gjelderPrivatAfpFoersteUttak()) spec.foersteUttakDato
            else null // ref. SimulerFleksibelAPCommand.hentLopendeYtelser
        )
    }

    // Extracted from HentGenerellHistorikkCommand.findForsteVirkningsdato
    private fun relevanteDatoer(datoListe: List<FoersteVirkningDato>) =
        datoListe.filter { !ekskluderteSakTyper.contains(it.sakType) }

    private fun finnEldsteForsteVirkningsdato(datoListe: List<FoersteVirkningDato>): LocalDate? =
        finnEldste(relevanteDatoer(datoListe))?.virkningDato

    private fun forsteDagIManedenEtter(dato: LocalDate?) =
        dato?.let { getFirstDayOfMonth2(dato).plusMonths(1) }

    // HentGenerellHistorikkCommand.findForsteVirkningsdato
    private fun finnEldste(datoListe: List<FoersteVirkningDato>): FoersteVirkningDato? {
        var eldst: FoersteVirkningDato? = null

        for (dato in datoListe) {
            if (eldst == null) {
                eldst = dato
            } else {
                // NB: null virkningsdato is not considered to be eldst
                if (eldst.virkningDato == null || eldst.virkningDato!!.isAfter(dato.virkningDato)) {
                    eldst = dato
                }
            }
        }

        return eldst
    }

    // Extracted from HentGenerellHistorikkCommand.findForsteVirkningsdato
    private val ekskluderteSakTyper = listOf(
        SakType.OMSORG,
        SakType.GENRL,
        SakType.GRBL,
        SakType.KRIGSP,
        SakType.GAM_YRK
    )
}
