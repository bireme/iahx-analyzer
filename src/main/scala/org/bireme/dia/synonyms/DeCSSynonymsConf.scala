package org.bireme.dia.synonyms

/**
 * @param indexPath path to the DeCS Lucene index
 * @param getDescriptor if true, it will not return the term but only the qualifier. Require the 'qualifier' flag to be true
 * @param getQualifiers if true, it  (also) returns qualifiers
 * @param getJoinDescrQual if true it will return the descriptor, plus / and the qualifier, as 'matadouro/sangue'
 * @param getSynonyms if true, it will return the synonym field of the term and/or qualifier
 * @param getCategory if true, it will return the category field of the term and/or qualifier
 * @param getAbbreviation if true, it will return the abbreviation field of the qualifier. Require the 'qualifier' flag to be true
 * @param acceptOnlyPrecoded if true, it will process the input string only if it is in the '^d1234' format (precoded terms/qualifiers)
 * @param splitWords if true, it will split the term and/or qualifier string in its spaces and add each part to the result
 * @param splitHifen if true, it will split the term and/or qualifier string in its hifen characters and add each part to the result
 * @param toLowerCase if true, it will convert all the result characters into lower case
 * @param fixPrefixSuffix if true, it will remove from prefix and suffix characters that are not letters or digits. It requires the flag splitWords and/or splitHifen to be true
 */
case class DeCSSynonymsConf(
                             indexPath: String,
                             getDescriptor: Boolean = false,
                             getQualifiers: Boolean = false,
                             getJoinDescrQual: Boolean = false,
                             getSynonyms: Boolean = false,
                             getCategory: Boolean = false,
                             getAbbreviation: Boolean = false,
                             acceptOnlyPrecoded: Boolean = false,
                             splitWords: Boolean = false,
                             splitHifen: Boolean = false,
                             toLowerCase: Boolean = false,
                             fixPrefixSuffix: Boolean = false
                           )
