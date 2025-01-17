package no.nav.pensjon.simulator.core

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.GjenlevendetilleggAP
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.GjenlevendetilleggAPKap19
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.PensjonUnderUtbetaling
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonOpptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidResponse
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovAlderpensjon2011Request
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovAlderpensjon2016Request
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovAlderpensjon2025Request
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.createDate
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.math.RoundingMode
import java.time.LocalDate
import java.util.*

object SimulatorContextUtil {

    private val log = KotlinLogging.logger {}

    fun finishOpptjeningInit(beholdningListe: ArrayList<Pensjonsbeholdning>) {
        beholdningListe.forEach {
            it.opptjening?.finishInit()
        }
    }

    // SimuleringEtter2011Context.updateBeholdningerWithFomAndTomDate
    fun tidsbegrensedeBeholdninger(beholdningListe: MutableList<Pensjonsbeholdning>): MutableList<Pensjonsbeholdning> {
        beholdningListe.forEach {
            it.fom = createDate(it.ar, Calendar.JANUARY, 1)
            it.tom = createDate(it.ar, Calendar.DECEMBER, 31)
        }

        return beholdningListe
    }

    // Ref. PEN: RequestToReglerMapper.mapToVilkarsprovAlderpensjon2011Request
    fun preprocess(spec: VilkarsprovAlderpensjon2011Request) {
        spec.fom = spec.fom?.noon()
        spec.tom = spec.tom?.noon()
        spec.afpVirkFom = spec.afpVirkFom?.noon()
        spec.kravhode?.uttaksgradListe.orEmpty().forEach { it.fomDato = it.fomDato?.noon() }
        spec.afpLivsvarig?.let(::roundNettoPerAar)
        spec.sisteBeregning?.pensjonUnderUtbetaling?.let(::preprocess)
    }

    // Ref. PEN: RequestToReglerMapper.mapToVilkarsprovAlderpensjon2016Request
    fun preprocess(spec: VilkarsprovAlderpensjon2016Request) {
        spec.virkFom = spec.virkFom?.noon()
        spec.afpVirkFom = spec.afpVirkFom?.noon()
        spec.kravhode?.uttaksgradListe.orEmpty().forEach { it.finishInit() }
        spec.afpLivsvarig?.let(::roundNettoPerAar)
        spec.sisteBeregning?.pensjonUnderUtbetaling?.let(::preprocess)
    }

    // Ref. PEN: RequestToReglerMapper.mapToVilkarsprovAlderpensjon2025Request
    fun preprocess(spec: VilkarsprovAlderpensjon2025Request) {
        spec.fom = spec.fom?.noon()
        spec.afpVirkFom = spec.afpVirkFom?.noon()
        spec.kravhode?.uttaksgradListe.orEmpty().forEach { it.fomDato = it.fomDato?.noon() }
        spec.afpLivsvarig?.let(::roundNettoPerAar)
        spec.sisteBeregning?.pensjonUnderUtbetaling?.let(::preprocess)
    }

    fun postprocess(result: BeregningsResultatAfpPrivat) {
        result.virkTom = null
        result.afpPrivatBeregning?.afpLivsvarig?.let(::roundNettoPerAar)
    }

    fun validerOgFerdigstillResponse(result: TrygdetidResponse, kravGjelderUfoeretrygd: Boolean) {
        validerResponse(result.pakkseddel)

        result.trygdetid?.apply {
            tt_67_75 = 0 // since this value is not mapped to PEN domain in original PEN code

            if (kravGjelderUfoeretrygd) {
                virkFom = null
                virkTom = null
            }
        }
    }

    fun personOpptjeningsgrunnlag(opptjeningGrunnlag: Opptjeningsgrunnlag, foedselsdato: LocalDate?) =
        PersonOpptjeningsgrunnlag().apply {
            opptjening = opptjeningGrunnlag
            fodselsdato = foedselsdato?.toNorwegianDateAtNoon()
        }

    // PEN: SimulatorContext.updatePersonOpptjeningsFieldFromPregResponse
    fun updatePersonOpptjeningsFieldFromReglerResponse(
        reglerInput: List<PersonOpptjeningsgrunnlag>,
        reglerOutput: List<PersonOpptjeningsgrunnlag>
    ) {
        val map: MutableMap<String, Opptjeningsgrunnlag> = HashMap()

        for (inputGrunnlag in reglerInput) {
            inputGrunnlag.opptjening?.let { map[pidOgOpptjeningAar(inputGrunnlag)] = it }
        }

        for (outputGrunnlag in reglerOutput) {
            outputGrunnlag.opptjening?.let {
                copyUpdatedData(
                    source = it,
                    target = map[pidOgOpptjeningAar(outputGrunnlag)]!!
                )
            }
        }
    }

    // RegelHelper.validateResponse
    fun validerResponse(pakkseddel: Pakkseddel) {
        val kontrollTjenesteOk = pakkseddel.kontrollTjenesteOk
        val annenTjenesteOk = pakkseddel.annenTjenesteOk
        if (kontrollTjenesteOk && annenTjenesteOk) return

        val message = pakkseddel.merknaderAsString()

        if (kontrollTjenesteOk) {
            log.error { "regler validering andre merknader - $message" }
            throw KanIkkeBeregnesException(message, pakkseddel.merknadListe)
        } else {
            log.error { "regler validering kontroll merknader - $message" }
            throw RegelmotorValideringException(message, pakkseddel.merknadListe)
        }
    }

    fun validerResponse(pakkseddel: Pakkseddel, spec: Any, objectMapper: ObjectMapper, call: String) {
        val kontrollTjenesteOk = pakkseddel.kontrollTjenesteOk
        val annenTjenesteOk = pakkseddel.annenTjenesteOk
        if (kontrollTjenesteOk && annenTjenesteOk) return

        val message = pakkseddel.merknaderAsString()
        log.error { "regler validering error for $call - " + objectMapper.writeValueAsString(spec) }

        if (kontrollTjenesteOk) {
            log.error { "regler validering andre merknader - $message" }
            throw KanIkkeBeregnesException(message, pakkseddel.merknadListe)
        } else {
            log.error { "regler validering kontroll merknader - $message" }
            throw RegelmotorValideringException(message, pakkseddel.merknadListe)
        }
    }

    private fun preprocess(pensjon: PensjonUnderUtbetaling) {
        pensjon.ytelseskomponenter.forEach {
            (it as? Tilleggspensjon)?.let { it.formelMap = HashMap() }
            (it as? GjenlevendetilleggAP)?.let { it.formelMap = HashMap() }
            (it as? GjenlevendetilleggAPKap19)?.let { it.formelMap = HashMap() }
            roundNettoPerAar(it)
        }
    }

    /**
     * PEN's nettoPerAr is integer, whereas regler's is double;
     * therefore need to round when calling regler from PEN.
     */
    private fun roundNettoPerAar(afp: AfpLivsvarig) {
        afp.nettoPerAr = afp.nettoPerAr.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()
    }

    private fun roundNettoPerAar(ytelse: Ytelseskomponent) {
        ytelse.nettoPerAr = ytelse.nettoPerAr.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()
    }

    private fun copyUpdatedData(source: Opptjeningsgrunnlag, target: Opptjeningsgrunnlag) {
        target.pp = source.pp
        target.pi = source.pi
        target.pia = source.pia
        target.ar = source.ar
        target.opptjeningType = source.opptjeningType
        target.bruk = source.bruk
        target.grunnlagKilde = source.grunnlagKilde
    }

    /**
     * Format: "pid:år"
     */
    private fun pidOgOpptjeningAar(grunnlag: PersonOpptjeningsgrunnlag): String {
        val key = StringBuilder()
        grunnlag.fnr?.let { key.append(it) }
        key.append(":").append(grunnlag.opptjening?.ar)
        return key.toString()
    }
}
