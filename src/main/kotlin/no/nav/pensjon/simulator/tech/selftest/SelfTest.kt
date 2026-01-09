package no.nav.pensjon.simulator.tech.selftest

import no.nav.pensjon.simulator.common.client.pen.PenPingClient
import org.springframework.stereotype.Component

@Component
class SelfTest(penClient: PenPingClient) {

    private val services: List<Pingable> = listOf(penClient)

    fun perform(): Map<String, PingResult> =
        services
            .map { it.ping() }
            .associateBy { it.service.name }

    /**
     * Returns result of self-test in HTML format.
     */
    fun performAndReportAsHtml(): String =
        htmlPage(tableRows = htmlStatusRows(resultsByService = perform()))

    companion object {
        const val APPLICATION_NAME = "pensjonssimulator"

        private fun htmlPage(tableRows: String) = """
            <!DOCTYPE html>
            <html>
            <head>
            <title>$APPLICATION_NAME selvtest</title>
            <style type="text/css">
            table {border-collapse: collapse; font-family: Tahoma, Geneva, sans-serif;}
            table td {padding: 15px;}
            table thead th {padding: 15px; background-color: #54585d; color: #ffffff; font-weight: bold; font-size: 13px; border: 1px solid #54585d;}
            table tbody td {border: 1px solid #dddfe1;}
            table tbody tr {background-color: #f9fafb;}
            table tbody tr:nth-child(odd) {background-color: #ffffff;}
            </style>
            </head>
            <body>
            <div>
            <table>
            <thead>
            <tr>
            <th>Tjeneste</th><th>Status</th><th>Informasjon</th><th>Endepunkt</th><th>Beskrivelse</th>
            </tr>
            </thead>
            <tbody>$tableRows</tbody>
            </table>
            </div>
            </body>
            </html>
            """.trimIndent()

        private fun htmlStatusRows(resultsByService: Map<String, PingResult>) =
            resultsByService.entries.joinToString(separator = "", transform = ::htmlRow)

        private fun htmlRow(entry: Map.Entry<String, PingResult>) =
            htmlRow(entry.value)

        private fun htmlRow(result: PingResult) =
            "<tr>${htmlCell(result.service.description)}${htmlStatusCell(result.status)}${htmlCell(result.message)}" +
                    "${htmlCell(result.endpoint)}${htmlCell(result.service.purpose)}</tr>"

        private fun htmlCell(content: String) =
            "<td>$content</td>"

        private fun htmlStatusCell(status: ServiceStatus) =
            """<td style="background-color:${status.color};text-align:center;">${status.name}</td>"""
    }
}
