package ai_generated

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import no.nav.pensjon.Evaluator.readResourceAsText
import no.nav.pensjon.generated.DiffFinder

class DiffFinderTest : StringSpec({

    "no differences" {
        val expected = "identical string"
        val actual = "identical string"
        DiffFinder.findDifferences(expected, actual) shouldBe "<no differences>"
    }

    "single-word replacement" {
        val expected = "foo bar baz"
        val actual = "foo qux baz"
        DiffFinder.findDifferences(expected, actual) shouldBe "foo qux baz"
    }

    "trailing insertion" {
        val expected = "hello world"
        val actual = "hello world john"
        DiffFinder.findDifferences(expected, actual) shouldBe "hello world john"
    }

    "multiple differences" {
        val expected = "a b c d e"
        val actual = "x b y d z"
        val result = DiffFinder.findDifferences(expected, actual)
        // Expect three hunks: "x b", "y d", "z"
        result.split("; ").let { parts ->
            parts.size shouldBe 3
            parts[0] shouldBe "x b"
            parts[1] shouldBe "y d"
            parts[2] shouldBe "z"
        }
    }

    "honors maxChunks" {
        val expected = "a b c d e f g"
        val actual   = "A b C d E f G"
        val result = DiffFinder.findDifferences(expected, actual, maxChunks = 2)
        // Only 2 hunks should be returned
        result.split("; ").size shouldBe 2
    }

    "resultat inneholder nok kontekst for aa bli funnet i json" {
        val expected = """
            {
                "simuleringSuccess": true,
                "alderspensjon": 25000
            }
        """.trimIndent()
        val actual = """
            {
                "simuleringSuccess": false,
                "alderspensjon": 0.0
            }
        """.trimIndent()
        val resultat = DiffFinder.findDifferences(expected, actual)
        resultat shouldContain """simuleringSuccess": false"""
        resultat shouldContain """"alderspensjon": 0.0"""
    }

    "resultat inneholder alle endringer" {
        val expected = readResourceAsText("afp-etterfulgt-av-alder-response-expected-in-test.json")
        val actual = readResourceAsText("afp-etterfulgt-av-alder-response-actual-in-test.json")
        val resultat = DiffFinder.findDifferences(expected, actual)
        resultat shouldContain """simuleringSuksess": false"""
        resultat shouldContain """BeregnetTidligereInntekt"""
        resultat shouldContain """maanedligUtbetaling": 8585"""
        resultat shouldContain """delingstall": 16.02"""
        resultat shouldContain """sumManedligUtbetaling"""
        resultat shouldContain """alderspensjonKapittel21"""
    }

})
