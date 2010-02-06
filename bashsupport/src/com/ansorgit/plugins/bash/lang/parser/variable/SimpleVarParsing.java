/*
 * Copyright 2009 Joachim Ansorg, mail@ansorg-it.com
 * File: SimpleVarParsing.java, Class: SimpleVarParsing
 * Last modified: 2009-12-04
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

package com.ansorgit.plugins.bash.lang.parser.variable;

import com.ansorgit.plugins.bash.lang.parser.BashPsiBuilder;
import com.ansorgit.plugins.bash.lang.parser.DefaultParsingFunction;
import com.ansorgit.plugins.bash.lang.parser.util.ParserUtil;
import com.intellij.psi.tree.IElementType;

/**
 * Date: 02.05.2009
 * Time: 21:34:56
 *
 * @author Joachim Ansorg
 */
public class SimpleVarParsing extends DefaultParsingFunction {
    //static final Logger log = Logger.getInstance("#bash.varParsingFunction");

    public boolean isValid(IElementType tokenType) {
        return tokenType == VARIABLE;
    }

    public boolean parse(BashPsiBuilder builder) {
        ParserUtil.markTokenAndAdvance(builder, VAR_ELEMENT);
        return true;
    }
}