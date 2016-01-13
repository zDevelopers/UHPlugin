/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.UHCReloaded.utils;

import com.google.common.collect.Sets;
import org.apache.commons.lang.WordUtils;

import java.util.Set;


public final class TextUtils
{
    private TextUtils() {}

    private static final Set<String> SMALL_WORDS = Sets.newHashSet(
            // English
            "the", "a", "it", "they", "them", "an", "all", "of", "this", "is", "not", "that",
            // French
            "un", "une", "le", "la", "les", "des", "je", "tu", "il", "elle", "on", "nous", "vous", "ils", "elles", "ça", "ca", "sa", "cela", "lui", "l"
    );

    /**
     * Tries to find a single alphanumeric character best representing this string.
     *
     * @param text The string.
     * @return An alphanumeric character. A space if the initial text is blank.
     */
    public static char getInitialLetter(String text)
    {
        text = toAlphanumeric(text);

        if (text == null || text.isEmpty())
            return ' ';

        // We try to find the main word of the sentence, based on four principles:
        // - the main word is likely to be at the beginning of the string;
        // - the main word is likely to be pretty long;
        // - the main word is unlikely to be a single-letter word;
        // - the main word is unlikely to be an article like “The”.

        String[] words = text.split(" ");

        Integer bestScore = Integer.MIN_VALUE;
        String  bestWord  = " ";

        Integer averageWordLength = 0;
        for (String word : words)
            averageWordLength += word.length();

        averageWordLength /= words.length;

        for (int i = 0, wordsCount = words.length; i < wordsCount; i++)
        {
            String word = words[i].toLowerCase();
            Integer score = 0;

            if (i < 3)
                score += 5;

            if (word.length() == 1)
                score -= 3;
            else if (word.length() >= averageWordLength)
                score += 5;

            if (SMALL_WORDS.contains(word))
                score -= 10;

            if (score > bestScore)
            {
                bestScore = score;
                bestWord = words[i];
            }
        }

        return bestWord.charAt(0);
    }

    /**
     * Removes all non-alphanumeric characters from the string.
     *
     * @param text The text.
     * @return The same text, without non-alphanumeric characters.
     */
    public static String toAlphanumeric(String text)
    {
        if (text == null)
            return null;

        StringBuilder builder = new StringBuilder();
        for (Character character : text.toCharArray())
        {
            // Convert all kind of spaces (unbreakable...) and apostrophes to basic spaces
            if (Character.isSpaceChar(character) || character.equals('\''))
                builder.append(" ");

            if (Character.isTitleCase(character))
                character = Character.toUpperCase(character);

            // Only keeps alphanumeric characters
            else if ((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || (character >= '0' && character <= '9'))
                builder.append(character);
        }

        return builder.toString();
    }

    public static String friendlyEnumName(Enum<?> enumConstant)
    {
        return WordUtils.capitalizeFully(enumConstant.name().replace("_", " "));
    }
}
