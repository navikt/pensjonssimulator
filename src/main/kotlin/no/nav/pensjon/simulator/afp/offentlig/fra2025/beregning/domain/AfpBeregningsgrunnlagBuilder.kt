package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain

import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsperiode
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.AlderForDelingstallBeregner.bestemAldreForDelingstall
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.sats.Delingstall
import no.nav.pensjon.simulator.core.domain.regler.to.HentDelingstallRequest
import no.nav.pensjon.simulator.core.domain.regler.to.HentDelingstallResponse

class AfpBeregningsgrunnlagBuilder {
    private lateinit var spec: LivsvarigOffentligAfpSpec
    private lateinit var alderForDelingstall: List<AlderForDelingstall>
    private lateinit var pensjonsbeholdningMedDelingstallAlder: List<PensjonsbeholdningMedDelingstallAlder>
    private lateinit var delingstallListe: List<Delingstall>

    fun medSpec(spec: LivsvarigOffentligAfpSpec) = apply {
        this.spec = spec
    }

    fun leggTilAlderForDelingstall() = apply {
        this.alderForDelingstall = bestemAldreForDelingstall(spec.foedselsdato, spec.fom)
    }

    fun hentRelevanteDelingstall(func: (HentDelingstallRequest) -> HentDelingstallResponse) = apply {
        func.invoke(
            HentDelingstallRequest(
                arskull = spec.foedselsdato.year,
                alder = pensjonsbeholdningMedDelingstallAlder.map {
                    it.alderForDelingstall.alder.min(
                        HOYESTE_ALDER_FOR_DELINGSTALL
                    )
                }
            ))
            .also {
                this.delingstallListe = it.delingstall
            }
    }

    fun hentSimulerteAfpBeholdninger(func: (LivsvarigOffentligAfpSpec) -> List<SimulerLivsvarigOffentligAfpBeholdningsperiode>) =
        apply {
            func.invoke(spec)
                .also { beholdinger ->
                    this.pensjonsbeholdningMedDelingstallAlder = beholdinger
                        .map { periode ->
                            PensjonsbeholdningMedDelingstallAlder(
                                periode.pensjonsbeholdning,
                                alderForDelingstall.first { it.datoVedAlder.year == periode.fom.year })
                        }
                }
        }

    private fun bestemDelingstall(delingstallListe: List<Delingstall>, alder: Alder): Double =
        delingstallListe
            .firstOrNull { dt -> haveEqualAlder(alder, dt) }?.delingstall
            ?: delingstallListe.first { dt ->
                haveEqualAlder(
                    HOYESTE_ALDER_FOR_DELINGSTALL,
                    dt
                )
            }.delingstall

    private fun haveEqualAlder(alder: Alder, delingstall: Delingstall): Boolean =
        alder.aar == delingstall.alder.aar && alder.maaneder == delingstall.alder.maaneder

    fun build(): List<AfpBeregningsgrunnlag> {
        check(::spec.isInitialized)
        check(::alderForDelingstall.isInitialized)
        check(::pensjonsbeholdningMedDelingstallAlder.isInitialized)
        check(::delingstallListe.isInitialized)

        return pensjonsbeholdningMedDelingstallAlder
            .map {
                AfpBeregningsgrunnlag(
                    pensjonsbeholdning = it.pensjonsbeholdning,
                    alderForDelingstall = it.alderForDelingstall,
                    delingstall = bestemDelingstall(delingstallListe, it.alderForDelingstall.alder)
                )
            }
    }

    companion object {
        val HOYESTE_ALDER_FOR_DELINGSTALL = Alder(70, 0)
    }
}