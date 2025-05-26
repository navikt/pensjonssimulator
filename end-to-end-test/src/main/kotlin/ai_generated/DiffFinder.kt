package no.nav.pensjon.generated

/*
*
* KI-generert kode for å finne forskjeller i to strenger.
* Forskjellene returneres med ett foregående ord for å forenkle feilsøking.
* Algoritmen er baser på å finne like strenger og returnere områder med ulike strenger for enklere feilsøking
*
* */

object DiffFinder {

    /**
     * returns semicolon-separated (;) string with strings which are different from expected.
     */
    fun findDifferences(
        expected: String,
        actual: String,
        maxChunks: Int = 15,
    ): String {
        val m = expected.length
        val n = actual.length

        // 1) Build LCS table
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in m - 1 downTo 0) {
            for (j in n - 1 downTo 0) {
                dp[i][j] = if (expected[i] == actual[j]) {
                    dp[i + 1][j + 1] + 1
                } else {
                    maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        // 2) Walk through both strings, collect mismatch‐runs in actual
        data class Hunk(val start: Int, val end: Int)
        val hunks = mutableListOf<Hunk>()
        var i = 0
        var j = 0
        while (i < m && j < n && hunks.size < maxChunks) {
            if (expected[i] == actual[j]) {
                i++; j++
            } else {
                val hunkStart = j
                while (i < m && j < n && expected[i] != actual[j]) {
                    // on tie, prefer advancing j so that insertions/end‐mismatches get captured
                    if (dp[i + 1][j] > dp[i][j + 1]) {
                        i++
                    } else {
                        j++
                    }
                }
                val hunkEnd = j
                hunks += Hunk(hunkStart, hunkEnd)
            }
        }
        // capture any trailing insertion beyond j
        if (j < n && hunks.size < maxChunks) {
            hunks += Hunk(j, n)
        }

        // 3) Format output
        if (hunks.isEmpty()) return "<no differences>"
        // a single mismatch‐run: show entire actual
        if (hunks.size == 1) return actual

        // Check if the string looks like JSON by finding key-value patterns
        val isJson = actual.contains("\":") || expected.contains("\":")

        // multiple runs: show each mismatch with appropriate context
        return hunks
            .take(maxChunks)
            .map { (start, end) ->
                if (isJson) {
                    // For JSON, extract just the precise key-value pair
                    extractExactJsonKeyValue(actual, start, end)
                } else {
                    // Standard handling for non-JSON
                    // find beginning of the mismatch word
                    var s = start.coerceIn(0, actual.length)
                    while (s > 0 && !actual[s - 1].isWhitespace()) s--
                    // scan past the mismatch to next token
                    var e = end.coerceIn(0, actual.length)
                    while (e < actual.length && actual[e].isWhitespace()) e++
                    // consume that next word for context
                    while (e < actual.length && !actual[e].isWhitespace()) e++
                    actual.substring(s, e)
                }
            }
            .filterNot { it.isBlank() }
            .joinToString("; ")
    }

    private fun extractExactJsonKeyValue(json: String, start: Int, end: Int): String {
        // Handle single-character differences that may not be part of a complete key-value pair
        if (end - start <= 2) {
            // Look for a complete key-value pattern around the difference
            val patternStart = maxOf(0, start - 20)
            val patternEnd = minOf(json.length, end + 20)
            val context = json.substring(patternStart, patternEnd)

            // Try to extract a complete property
            val keyValuePattern = "\"\\w+\"\\s*:\\s*[^,{}\\[\\]]*".toRegex()
            val match = keyValuePattern.find(context)

            if (match != null) {
                return match.value.trim()
            }
        }

        // Find the position of the containing property
        val keyIndex = findPropertyKeyIndex(json, start)
        if (keyIndex >= 0) {
            val keyEndIndex = json.indexOf('"', keyIndex + 1)
            if (keyEndIndex > keyIndex) {
                val keyName = json.substring(keyIndex + 1, keyEndIndex)

                // Find the value part
                val colonIndex = json.indexOf(':', keyEndIndex)
                if (colonIndex > 0) {
                    var valueEndIndex = findValueEndIndex(json, colonIndex + 1)

                    // Extract just the key-value pair
                    return json.substring(keyIndex, valueEndIndex).trim()
                }
            }
        }

        // Fallback - extract a small fragment around the difference
        val contextStart = maxOf(0, start - 5)
        val contextEnd = minOf(json.length, end + 5)
        return json.substring(contextStart, contextEnd).trim()
    }

    private fun findPropertyKeyIndex(json: String, position: Int): Int {
        // Search backward for the nearest property key
        var i = position
        while (i >= 0) {
            // Look for the pattern: "key":
            if (i > 0 && json[i] == '"' && (i == 0 || json[i-1] != '\\')) {
                // Check if this looks like the start of a key
                var j = i - 1
                // Skip whitespace before the quote
                while (j >= 0 && json[j].isWhitespace()) j--
                // If we hit a comma or opening brace/bracket, this is likely a key
                if (j < 0 || json[j] == '{' || json[j] == '[' || json[j] == ',') {
                    return i
                }
            }
            i--
        }
        return -1
    }

    private fun findValueEndIndex(json: String, startPos: Int): Int {
        var i = startPos
        // Skip initial whitespace
        while (i < json.length && json[i].isWhitespace()) i++

        if (i >= json.length) return json.length

        // Handle different value types
        when (json[i]) {
            '{', '[' -> {
                // Object or array - find matching closing brace/bracket
                val openChar = json[i]
                val closeChar = if (openChar == '{') '}' else ']'
                var depth = 1
                i++

                while (i < json.length && depth > 0) {
                    when (json[i]) {
                        openChar -> depth++
                        closeChar -> depth--
                        '"' -> {
                            // Skip string content
                            i++
                            while (i < json.length && (json[i] != '"' || json[i-1] == '\\')) {
                                i++
                            }
                        }
                    }
                    i++
                }
                return i
            }
            '"' -> {
                // String value - find closing quote
                i++
                while (i < json.length && (json[i] != '"' || json[i-1] == '\\')) {
                    i++
                }
                return i + 1 // Include the closing quote
            }
            else -> {
                // Number, boolean, null - find end of token
                while (i < json.length && json[i] !in ",]}") {
                    i++
                }
                return i
            }
        }
    }
}
