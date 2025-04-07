package no.nav.pensjon.simulator.core.afp.offentlig.livsvarig

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarig
import no.nav.pensjon.simulator.core.util.isBeforeOrOn
import java.time.LocalDate

object LivsvarigOffentligAfpUtil {

    fun getLivsvarigOffentligAfp(
        resultatListe: List<LivsvarigOffentligAfpYtelseMedDelingstall>,
        knekkpunktDato: LocalDate
    ): AfpOffentligLivsvarig? =
        resultatListe
            .filter { it.gjelderFom.isBeforeOrOn(knekkpunktDato) }
            .maxByOrNull { it.gjelderFom }
            ?.let {
                AfpOffentligLivsvarig().apply {
                    bruttoPerAr = it.afpYtelsePerAar
                    uttaksdato = it.gjelderFom
                }
            }
}
