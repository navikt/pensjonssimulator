package no.nav.pensjon.simulator.generelt

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class GenerelleDataSpec(
    val pid: Pid?,
    val virkningFom: LocalDate?,
    val foedselDato: LocalDate?,
    val satsPeriode: PeriodeSpec?,
    val inkludering: InkluderingSpec
) {
    companion object {
        fun forPerson(pid: Pid) =
            GenerelleDataSpec(
                pid,
                virkningFom = null,
                foedselDato = null,
                satsPeriode = null,
                inkludering = InkluderingSpec.none()
            )

        fun forPrivatAfp(virkningFom: LocalDate, foedselDato: LocalDate) =
            GenerelleDataSpec(
                pid = null,
                virkningFom,
                foedselDato,
                satsPeriode = null,
                inkludering = InkluderingSpec.forAfpSatser()
            )

        fun forDelingstall(virkningFom: LocalDate, foedselDato: LocalDate) =
            GenerelleDataSpec(
                pid = null,
                virkningFom,
                foedselDato,
                satsPeriode = null,
                inkludering = InkluderingSpec.forDelingstall()
            )

        fun forForholdstall(virkningFom: LocalDate, foedselDato: LocalDate) =
            GenerelleDataSpec(
                pid = null,
                virkningFom,
                foedselDato,
                satsPeriode = null,
                inkludering = InkluderingSpec.forForholdstall()
            )

        fun forVeietGrunnbeloep(fomAar: Int?, tomAar: Int?) =
            GenerelleDataSpec(
                pid = null,
                virkningFom = null,
                foedselDato = null,
                satsPeriode = PeriodeSpec(fomAar, tomAar),
                inkludering = InkluderingSpec.none()
            )
    }
}

data class PeriodeSpec(
    val fomAar: Int?,
    val tomAar: Int?,
)

data class InkluderingSpec(
    val afpSatser: Boolean,
    val delingstall: Boolean,
    val forholdstall: Boolean
) {
    companion object {
        fun forAfpSatser() =
            InkluderingSpec(afpSatser = true, delingstall = false, forholdstall = false)

        fun forDelingstall() =
            InkluderingSpec(afpSatser = false, delingstall = true, forholdstall = false)

        fun forForholdstall() =
            InkluderingSpec(afpSatser = false, delingstall = false, forholdstall = true)

        fun none() =
            InkluderingSpec(afpSatser = false, delingstall = false, forholdstall = false)
    }
}
