package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.IBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.Beregning2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.Uforetrygdberegning
import java.io.Serializable
import java.lang.reflect.InvocationTargetException

class BeregningRelasjon : Serializable {
    /**
     * 1967 beregningen som det relateres til
     */
    var beregning: Beregning? = null
        set(ber) {
            if (ber != null) {
                if (this.beregning2011 != null) {
                    throw RuntimeException("Kan ikke sette beregning på BeregningRelasjon dersom beregning2011 er satt")
                } else {
                    ber.beregningsrelasjon = this
                    field = ber
                }
            }
        }

    /**
     * Beregning 2011 som det relateres til
     */
    var beregning2011: Beregning2011? = null
        set(ber11) {
            if (ber11 != null) {
                if (beregning != null) {
                    throw RuntimeException("Kan ikke sette beregning2011 på BeregningRelasjon dersom beregning er satt")
                } else {
                    ber11.beregningsrelasjon = this
                    field = ber11
                }
            }
        }

    /**
     * Angir om beregningen er brukt (helt eller delvis) i beregningen den tilhører.
     */
    var bruk: Boolean = false

    /**
     * Referanse tilbake til beregning hvor beregningsrelasjon inngår i delberegningslisten.
     */
    @JsonIgnore
    var parentBeregning: Beregning? = null

    /**
     * Referanse tilbake til beregning2011 hvor beregningsrelasjon inngår i delberegningslisten.
     */
    @JsonIgnore
    var parentBeregning2011: Beregning2011? = null

    val iBeregning: IBeregning?
        get() = if (beregning != null) {
            beregning
        } else {
            beregning2011
        }

    constructor(beregningRelasjon: BeregningRelasjon) {
        if (beregningRelasjon.beregning != null) {
            beregning = Beregning(beregningRelasjon.beregning)
            beregning!!.beregningsrelasjon = this
        }
        beregning2011 = createBeregning2011UsingCopyConstructor(beregningRelasjon.beregning2011)
        if (beregning2011 != null) {
            beregning2011!!.beregningsrelasjon = this
        }
        bruk = beregningRelasjon.bruk
    }

    constructor(beregning: Beregning?, bruk: Boolean? = null) : super() {
        this.beregning = beregning
        if (this.beregning != null) {
            this.beregning!!.beregningsrelasjon = this
        }
        if (bruk != null) {
            this.bruk = bruk
        }
    }

    constructor(beregning2011: Beregning2011?, bruk: Boolean? = null) : super() {
        this.beregning2011 = beregning2011
        if (this.beregning2011 != null) {
            this.beregning2011!!.beregningsrelasjon = this
        }
        if (bruk != null) {
            this.bruk = bruk
        }
    }

    @JvmOverloads
    constructor(
        beregning: Beregning? = null,
        beregning2011: Beregning2011? = null,
        bruk: Boolean = false,
        parentBeregning: Beregning? = null,
        parentBeregning2011: Beregning2011? = null
    ) : super() {
        this.beregning = beregning
        this.beregning2011 = beregning2011
        this.bruk = bruk
        this.parentBeregning = parentBeregning
        this.parentBeregning2011 = parentBeregning2011
    }

    /**
     * Oppretter ny beregning2011, ved å benytte riktig copyconstructor for den underliggende subklassen.
     * Reflection benyttes for å avgjøre hvilken constructor som skal benyttes.
     *
     * @param beregning2011
     * @return beregning2011
     */
    private fun createBeregning2011UsingCopyConstructor(beregning2011: Beregning2011?): Beregning2011? {
        if (beregning2011 != null) {
            val clazz = beregning2011.javaClass
            try {
                return if (beregning2011 is Uforetrygdberegning) {
                    val constructor = clazz.getConstructor(clazz, Boolean::class.javaPrimitiveType)
                    constructor.newInstance(beregning2011, true)
                } else {
                    val constructor = clazz.getConstructor(clazz)
                    constructor.newInstance(beregning2011)
                }
            } catch (e: NoSuchMethodException) {
                throw RuntimeException(e)
            } catch (e: IllegalArgumentException) {
                throw RuntimeException(e)
            } catch (e: InstantiationException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e)
            }

        }
        return null
    }
}
