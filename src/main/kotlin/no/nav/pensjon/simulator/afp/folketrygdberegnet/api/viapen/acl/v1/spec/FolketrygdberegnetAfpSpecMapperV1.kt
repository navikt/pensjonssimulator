package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec

import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.Pid

/**
 * Maps from received DTO to domain object for specification of 'simulering av folketrygdberegnet AFP'.
 * V1 = Versipn 1 of the API (application programming interface) and DTO (data transfer object)
 * AFP = Avtalefestet pensjon
 */
object FolketrygdberegnetAfpSpecMapperV1 {

    fun fromSimuleringSpecV1(source: FolketrygdberegnetAfpSpecV1) =
        SimuleringSpec(
            type = source.simuleringType?.let { FolketrygdberegnetAfpSimuleringTypeSpecV1.fromExternalValue(it.name).internalValue }
                ?: SimuleringType.ALDER,
            sivilstatus = source.sivilstatus?.let { FolketrygdberegnetAfpSivilstandSpecV1.fromExternalValue(it.name).internalValue }
                ?: SivilstatusType.UGIF,
            epsHarPensjon = source.epsPensjon == true,
            foersteUttakDato = source.forsteUttakDato?.toNorwegianLocalDate(),
            heltUttakDato = null, // not relevant in this context
            pid = source.fnr?.let(::Pid),
            foedselDato = null, // used for anonym only
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100, // not relevant in this context
            forventetInntektBeloep = source.forventetInntekt ?: 0,
            inntektUnderGradertUttakBeloep = source.inntektUnderGradertUttak ?: 0,
            inntektEtterHeltUttakBeloep = source.inntektEtterHeltUttak ?: 0,
            inntektEtterHeltUttakAntallAar = source.antallArInntektEtterHeltUttak ?: 0,
            foedselAar = 0,
            utlandAntallAar = source.utenlandsopphold ?: 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0, // used for anonym only
            flyktning = null,
            epsHarInntektOver2G = source.eps2G == true,
            rettTilOffentligAfpFom = null, // not relevant in this context
            pre2025OffentligAfp = pre2025OffentligAfpSpec(source),
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )

    private fun pre2025OffentligAfpSpec(spec: FolketrygdberegnetAfpSpecV1): Pre2025OffentligAfpSpec =
        spec.afpOrdning?.let {
            Pre2025OffentligAfpSpec(
                afpOrdning = AfpOrdningType.valueOf(it),
                inntektMaanedenFoerAfpUttakBeloep = spec.afpInntektMndForUttak ?: 0,
                inntektUnderAfpUttakBeloep = 0 // ref. PEN ForetaFolketrygdBeregnetAfpHelper
            )
        } ?: throw BadSpecException("afpOrdning mangler")

    /* The mapping is based on code in PEN: ForetaFolketrygdBeregnetAfpHelper.folketrygdberegnetAfpSimuleringSpec
       (in turn based on deleted PEN legacy code - see github.com/navikt/pensjon-pen/pull/15225)
        simuleringType                = SimuleringTypeCode.AFP_FPP,
        fnr                           = sak.getPenPerson().getFnr(),
        forventetInntekt              = grunnlag.getInntektArForUttak().intValue(),
        forsteUttakDato               = grunnlag.getVirkFom(),
        inntektUnderGradertUttak      = 0,
        inntektEtterHeltUttak         = 0,
        antallArInntektEtterHeltUttak = 0,
        utenlandsopphold              = grunnlag.getAntallArUtland(),
        sivilstatus                   = sivilstand,
        epsPensjon                    = grunnlag.getEpsMottarYtelse(),
        eps2G                         = grunnlag.getEpsOver2G(),
        afpOrdning                    = grunnlag.getAfpOrdning(),
        afpInntektMndForUttak         = grunnlag.getInntektMndForUttak().intValue());
    */
}
