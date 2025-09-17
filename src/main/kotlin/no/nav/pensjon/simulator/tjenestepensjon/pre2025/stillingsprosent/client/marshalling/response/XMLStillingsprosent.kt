package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response

import jakarta.xml.bind.annotation.*
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.toLocalDate
import javax.xml.datatype.XMLGregorianCalendar

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = [
    "stillingsprosent",
    "datoFom",
    "datoTom",
    "faktiskHovedlonn",
    "stillingsuavhengigTilleggslonn",
    "aldersgrense"
])
class XMLStillingsprosent {
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    lateinit var datoFom: XMLGregorianCalendar
    @XmlElement(required = false)
    @XmlSchemaType(name = "date")
    var datoTom: XMLGregorianCalendar? = null
    @XmlElement(required = true)
    var stillingsprosent: Double = 0.0
    @XmlElement(required = true)
    var aldersgrense: Int = 0
    @XmlElement(required = false)
    var faktiskHovedlonn: String? = null
    @XmlElement(required = false)
    var stillingsuavhengigTilleggslonn: String? = null

    fun toStillingsprosent() = Stillingsprosent(
        datoFom = datoFom.toLocalDate(),
        datoTom = datoTom?.toLocalDate(),
        stillingsprosent = stillingsprosent,
        aldersgrense = aldersgrense,
        faktiskHovedlonn = faktiskHovedlonn,
        stillingsuavhengigTilleggslonn = stillingsuavhengigTilleggslonn,
        utvidelse = null
    )
}