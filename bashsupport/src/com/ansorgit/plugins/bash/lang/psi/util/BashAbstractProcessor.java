/*
 * Copyright 2009 Joachim Ansorg, mail@ansorg-it.com
 * File: BashAbstractProcessor.java, Class: BashAbstractProcessor
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

package com.ansorgit.plugins.bash.lang.psi.util;

import com.ansorgit.plugins.bash.lang.psi.api.ResolveProcessor;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.scope.PsiScopeProcessor;

/**
 * The base class for psi processors.
 * <p/>
 * Date: 14.04.2009
 * Time: 17:30:27
 *
 * @author Joachim Ansorg
 */
public abstract class BashAbstractProcessor implements PsiScopeProcessor, ResolveProcessor {
    private final Multimap<Integer, PsiElement> results = LinkedListMultimap.create();

    public void handleEvent(Event event, Object o) {
    }

    public final PsiElement getBestResult(boolean firstResult, PsiElement referenceElement) {
        return findBestResult(results, firstResult, referenceElement);
    }

    public Iterable<PsiElement> getResults() {
        return results.values();
    }

    public boolean hasResults() {
        return !results.isEmpty();
    }

    protected final void storeResult(PsiElement element, Integer rating) {
        results.put(rating, element);
    }

    /**
     * Returns the best results. It takes all the elements which have been rated the best
     * and returns the first / last, depending on the parameter.
     *
     * @param results          The results to check
     * @param firstResult      If the first element of the best element list should be returned.
     * @param referenceElement
     * @return The result
     */
    private PsiElement findBestResult(Multimap<Integer, PsiElement> results, boolean firstResult, PsiElement referenceElement) {
        if (results.isEmpty()) {
            return null;
        }

        if (firstResult) {
            return Iterators.get(results.values().iterator(), 0);
        }

        //if the first should not be used return the best element
        int referenceLevel = referenceElement != null ? BashPsiUtils.blockNestingLevel(referenceElement) : 0;

        //find the best suitable result rating
        // The best one is as close as possible to the given referenceElement
        int bestRating = Integer.MAX_VALUE;
        int bestDelta = Integer.MAX_VALUE;
        for (int rating : results.keySet()) {
            final int delta = Math.abs(referenceLevel - rating);
            if (delta < bestDelta) {
                bestDelta = delta;
                bestRating = rating;
            }
        }

        //now get the correct result
        return Iterators.getLast(results.get(bestRating).iterator());
    }

    public void reset() {
        results.clear();
    }
}
