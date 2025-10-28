package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.trygdetid.cache.RollebasertTrygdetidCache
import org.springframework.stereotype.Component
import java.util.Date

/**
 * Delegerer beregning av trygdetid til pensjon-regler.
 */
@Component
class TrygdetidBeregnerProxy(private val context: SimulatorContext) {

    private var trygdetidCache: RollebasertTrygdetidCache? = null

    // VilkarsprovOgBeregnAlderHelper.fastsettTrygdetidForPeriode + FastsettTrygdetidCache.updateSisteGyldigeOpptjeningsaar
    fun fastsettTrygdetidForPeriode(
        spec: TrygdetidRequest,
        rolle: GrunnlagsrolleEnum,
        kravIsUforetrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo {
        spec.persongrunnlag?.let {
            it.sisteGyldigeOpptjeningsAr = getYear(spec.virkFom!!) - OPPTJENING_ETTERSLEP_ANTALL_AAR
        } // updateSisteGyldigeOpptjeningsaar

        try {
            return fastsettTrygdetid(spec, rolle, kravIsUforetrygd, sakId)
        } catch (e: RegelmotorValideringException) {
            handle(e, spec)
        }
    }

    private fun handle(
        e: RegelmotorValideringException,
        spec: TrygdetidRequest
    ): Nothing {
        if (e.merknadListe.any { it.kode.equals("InputdataKontrollTTPeriodeRS.FomIkkeMindreEnnVirk") }
            && kapittel20TrygdetidStarterFoerUttak(spec).not())
            throw BadSpecException("Trygdetiden må starte før uttak (oppnås ved å utsette uttaket eller redusere antall år i utlandet)")
        else
            throw e
    }

    private fun kapittel20TrygdetidStarterFoerUttak(spec: TrygdetidRequest): Boolean =
        spec.persongrunnlag?.trygdetidPerioderKapittel20.orEmpty().all {
            it.fom?.isBefore(spec.virkFom) == true
        }

    private fun Date.isBefore(other: Date?): Boolean? =
        other?.let { this.toNorwegianLocalDate().isBefore(it.toNorwegianLocalDate()) }

    // SimuleringEtter2011Context.fastsettTrygdetid
    private fun fastsettTrygdetid(
        spec: TrygdetidRequest,
        rolle: GrunnlagsrolleEnum,
        gjelderUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo {
        /*
        TODO restore cache?
        NB: This cache cannot be used by consecutive simuleringer
        if (trygdetidCache == null) {
            trygdetidCache = TrygdetidCache(context = context)
                .also { it.createCacheForGrunnlagsroller(GrunnlagsrolleEnum.SOKER, GrunnlagsrolleEnum.AVDOD) }
        }

        return trygdetidCache!!.fastsettTrygdetid(spec, rolle, gjelderUfoeretrygd, sakId)
        */
        return RollebasertTrygdetidCache(context).fastsettTrygdetid(spec, rolle, gjelderUfoeretrygd, sakId)
    }
}
