/*
 * Copyright 2009 Joachim Ansorg, mail@ansorg-it.com
 * File: HereDocParsing.java, Class: HereDocParsing
 * Last modified: 2010-01-30
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ansorgit.plugins.bash.lang.parser.misc;

import com.ansorgit.plugins.bash.lang.parser.BashPsiBuilder;
import com.ansorgit.plugins.bash.lang.parser.BashSmartMarker;
import com.ansorgit.plugins.bash.lang.parser.HereDocData;
import com.ansorgit.plugins.bash.lang.parser.ParsingTool;
import com.ansorgit.plugins.bash.lang.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * Here-docs are started in a command with a redirect.
 * <p/>
 * Date: 11.04.2009
 * Time: 20:57:24
 *
 * @author Joachim Ansorg
 */
public class HereDocParsing implements ParsingTool {
    //private static final Logger log = Logger.getInstance("#bash.HereDocParsing");

    public boolean parseOptionalHereDocs(BashPsiBuilder builder) {
        if (!builder.getHereDocData().expectsHereDoc()) {
            return false;
        }

        builder.eatOptionalNewlines(1);

        while (!builder.eof() && builder.getHereDocData().expectsHereDoc()) {
            final String expectedEnd = builder.getHereDocData().expectedDocEnd();
            //read lines until we found the end of the current here-doc

            final BashSmartMarker hereDocMarker = new BashSmartMarker(builder.mark());

            boolean foundPrefixedEnd = false;
            boolean foundExactEnd = false;
            Pair<String, BashSmartMarker> line = readLine(builder);

            int readLines = 1;

            while (line != null) {
                foundExactEnd = expectedEnd.equals(line.first);
                foundPrefixedEnd = !foundExactEnd && expectedEnd.equals(line.first.trim());

                if (expectedEnd.equals(line.first.trim())) {
                    //we've found our end marker, the heredoc marker should end before this end marker, though
                    //so we need to rollback to the start of the line, finish the document marker and then
                    //read in the end marker line again

                    line.second.rollbackTo();
                    if (readLines > 1) {
                        hereDocMarker.done(HEREDOC_ELEMENT);
                    } else {
                        //we don't want to mark a heredoc which is only start and end marker
                        hereDocMarker.drop();
                    }

                    line = readLine(builder);
                    //noinspection ConstantConditions
                    line.second.done(HEREDOC_END_MARKER_ELEMENT);

                    builder.eatOptionalNewlines(1);

                    break;
                }

                line.second.drop();

                if (builder.getTokenType(true) == LINE_FEED) {
                    builder.eatOptionalNewlines(-1, true);
                }

                line = readLine(builder);
                readLines++;
            }

            if (foundPrefixedEnd) {
                hereDocMarker.error("An heredoc end marker must be at the beginning of a line.");
                builder.getHereDocData().reset();//fixme check this

                return false;
            } else if (!foundExactEnd) {
                ParserUtil.error(hereDocMarker, "parser.heredoc.expectedEnd");
                builder.getHereDocData().reset();

                return false;
            }

            //this could happen if the users enters text at the end of a document
            //in this case the while loop is never entered and the marker is never closed
            if (hereDocMarker.isOpen()) {
                hereDocMarker.drop();
            }

            builder.getHereDocData().removeExpectedEnd();
        }

        return true;
    }

    /**
     * Returns the read line and a yet UNCLOSED marker
     *
     * @param builder The builder to read from
     * @return A pair of line text and UNCLOSED marker. Null if the builder has no more tokens.
     */
    @Nullable
    private Pair<String, BashSmartMarker> readLine(BashPsiBuilder builder) {
        if (builder.eof()) {
            return null;
        }

        //fixme buggy: Doesn't work with different whitespace
        StringBuilder string = new StringBuilder();

        PsiBuilder.Marker lineMarker = builder.mark();
        while (!builder.eof() && builder.getTokenType(true) != LINE_FEED) {
            if (string.length() > 0) { //isEmpty is JDK6
                string.append(" ");
            }

            string.append(builder.getTokenText(true));
            builder.advanceLexer(true);
        }

        return Pair.create(string.toString(), new BashSmartMarker(lineMarker));
    }

    /**
     * Helper method to read a heredoc start marker from the psi document.
     *
     * @param builder WHere to read from
     * @return The text string of the marker. Does NOT include the optional string start/end markers
     */
    public static String readHeredocMarker(BashPsiBuilder builder) {
        String markerText;
        HereDocData.MarkerType evalMode;

        if (builder.getTokenType() == STRING_BEGIN) {
            ParserUtil.getTokenAndAdvance(builder); //string start
            markerText = builder.getTokenText();
            ParserUtil.markTokenAndAdvance(builder, HEREDOC_START_MARKER_ELEMENT);
            ParserUtil.getTokenAndAdvance(builder); //string end

            evalMode = HereDocData.MarkerType.NoEval;
        } else {
            markerText = builder.getTokenText();
            ParserUtil.markTokenAndAdvance(builder, HEREDOC_START_MARKER_ELEMENT);
            evalMode = HereDocData.MarkerType.Eval;
        }

        builder.getHereDocData().addExpectedDoc(markerText, evalMode, false);

        return builder.getTokenText();
    }
}
