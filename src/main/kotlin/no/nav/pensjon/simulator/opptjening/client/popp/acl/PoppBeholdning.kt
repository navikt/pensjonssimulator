package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate
import java.util.*

data class PoppBeholdning(
    val belop: Double? = null,
    val fomDato: Date? = null,
    val tomDato: Date? = null,
    val beholdningGrunnlag: Double? = null,
    val beholdningGrunnlagAvkortet: Double? = null,
    val beholdningInnskudd: Double? = null,
    val lonnsvekstregulering: PoppLoennsvekstregulering? = null,
    val inntektOpptjeningBelop: PoppInntektOpptjeningBelop? = null,
    val omsorgOpptjeningBelop: PoppOmsorgsopptjeningBeloep? = null,
    val dagpengerOpptjeningBelop: PoppDagpengerOpptjeningBeloep? = null,
    val forstegangstjenesteOpptjeningBelop: PoppFoerstegangstjenesteOpptjeningBeloep? = null,
    val uforeOpptjeningBelop: PoppUfoereOpptjeningBeloep? = null
) {
    private val fom: LocalDate? = fomDato?.toNorwegianLocalDate()
    private val aar: Int = fom?.year ?: 0

    fun toPensjonsbeholdning() =
        Pensjonsbeholdning().apply {
            ar = aar
            fomLd = fom
            tomLd = tomDato?.toNorwegianLocalDate()
            totalbelop = belop ?: 0.0
            opptjening = opptjening()

            lonnsvekstregulering?.let {
                lonnsvekstInformasjon = it.loennsvekstinformasjon()
                reguleringsInformasjon = it.reguleringsinformasjon()
            }
        }

    private fun opptjening() =
        Opptjening().apply {
            ar = aar - OPPTJENING_ETTERSLEP_ANTALL_AAR
            opptjeningsgrunnlag = beholdningGrunnlag ?: 0.0
            anvendtOpptjeningsgrunnlag = beholdningGrunnlagAvkortet ?: 0.0
            arligOpptjening = beholdningInnskudd ?: 0.0
            forstegangstjeneste = forstegangstjenesteOpptjeningBelop?.belop ?: 0.0
            inntektUtenDagpenger = inntektOpptjeningBelop?.belop ?: 0.0
            omsorg = omsorgOpptjeningBelop?.belop ?: 0.0
            uforeOpptjening = uforeOpptjeningBelop?.toUfoereopptjening()
            poengtall = Poengtall() // NB: Tomt objekt, ref. CommonToReglerMapper.mapOpptjeningToPenRegler i PEN

            dagpengerOpptjeningBelop?.let {
                dagpenger = it.belopOrdinar ?: 0.0
                dagpengerFiskerOgFangstmenn = it.belopFiskere ?: 0.0
            }
        }
}