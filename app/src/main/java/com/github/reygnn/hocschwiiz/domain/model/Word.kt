package com.github.reygnn.hocschwiiz.domain.model

/**
 * Domain model for a Swiss German word with translations and phonetic notation.
 *
 * ## Dialect Variants
 *
 * The Aargau canton has no unified dialect – it's a transition zone between
 * Bernese (west) and Zurich/Central Swiss (east) influences. This model captures
 * that reality:
 *
 * - [swiss] + [phonetic]: The authoritative form (Westaargau/Berner influence)
 * - [freiamt] + [freiamtPhonetic]: What's spoken in the Freiamt region (Zurich/Central Swiss influence)
 * - [ruleHint]: The phonetic rule that explains the difference (e.g., "il→öu")
 *
 * Example: "Milch" → swiss="Möuch", freiamt="Milch", ruleHint="il→öu"
 *
 * ## Phonetic Notation System
 *
 * The [phonetic] field uses a custom notation designed for Vietnamese speakers,
 * leveraging their familiarity with diacritical marks to indicate pronunciation.
 *
 * **Important:** The accent marks indicate MOUTH POSITION, not tone pitch!
 * - Acute (´) = lips rounded, tense
 * - Grave (`) = mouth open, relaxed
 * - Dot below (̣) = abrupt stop (like Vietnamese "nặng")
 *
 * ### Vowel Quality (open vs. closed)
 * - ó/ò = closed/open O (Rose vs. Ross)
 * - é/è = closed/open E (élève vs. Bett)
 * - ö́/ö̀ = closed/open Ö (peu vs. Götter)
 * - ú/ù = closed/open U (Mut vs. book)
 * - í/ì = closed/open I (Miete vs. Mitte)
 * - ä = always open in Aargau dialect (no marking needed)
 *
 * ### Special Markers
 * - ẹ/ặ/ị/ụ = glottal stop / abrupt ending (Vietnamese nặng-inspired)
 * - ĭ = explicitly short vowel (breve)
 * - Double vowels (aa, ee, oo) = long vowels
 *
 * ### Consonants
 * - gh = soft/aspirated g (like word-final g in "Zmittag")
 * - ggh = double soft g (like in "Schoggi")
 * - ng = velar nasal [ŋ] (same as Vietnamese "ng" – a natural advantage!)
 * - ch = always the "ach"-sound in Swiss German
 *
 * ### L-Vocalization (Möuch-Regel)
 * - Final -l becomes -ụ (e.g., "Milch" → "Mö́ụch")
 * - In Westaargau: il → öu (e.g., "viel" → "vöu", "wild" → "wöud")
 * - In Freiamt: often stays as "il" or "iel"
 *
 * ### Diphthongs
 * - "e" after a vowel (ie, ue, üe) = schwa, not é or è
 * - Example: "Rüebli" – the "e" glides to schwa
 *
 * @property swiss The primary Swiss German spelling (authoritative for this dialect)
 * @property phonetic Phonetic notation for [swiss] using the Vietnamese-friendly system
 * @property freiamt Alternative spelling as spoken in Freiamt region (nullable)
 * @property freiamtPhonetic Phonetic notation for [freiamt] (rarely needed)
 * @property ruleHint Short description of the phonetic rule, e.g., "il→öu"
 *
 * @see PHONETIC_NOTATION.md for complete documentation
 */
data class Word(
    val id: String,
    val german: String,
    val swiss: String,
    val phonetic: String? = null,
    val freiamt: String? = null,
    val freiamtPhonetic: String? = null,
    val ruleHint: String? = null,
    val vietnamese: String,
    val category: Category,
    val dialect: Dialect,
    val gender: Gender? = null,
    val altSpellings: List<String> = emptyList(),
    val notes: String? = null,
    val examples: List<String> = emptyList()
)