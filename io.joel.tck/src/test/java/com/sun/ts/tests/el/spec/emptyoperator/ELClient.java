/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * $Id$
 */

package com.sun.ts.tests.el.spec.emptyoperator;

import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.el.common.util.ExprEval;
import com.sun.ts.tests.el.common.util.NameValuePair;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class ELClient {

    private final boolean[] deferred = {true, false};


    /*
     * @testName: elEmptyNullTest
     *
     * @assertion_ids: EL:SPEC:25.1
     *
     * @test_Strategy: Validate that if "null" is passed with the Empty operator,
     * the correct Boolean result is returned.
     */
    @Test
    public void elEmptyNullTest() {

        boolean pass = false;

        String[] symbols = {"$", "#"};
        boolean expectedResult = true;

        try {
            for (String prefix : symbols) {
                String expr = prefix + "{empty null}";
                Object result = ExprEval.evaluateValueExpression(expr, null,
                        Object.class);

                if (result == null) {
                    TestUtil.logTrace("result is null");
                } else {
                    TestUtil.logTrace("result is " + result);
                }

                pass = (ExprEval.compareClass(result, Boolean.class)
                        && ExprEval.compareValue((Boolean) result, expectedResult));

                if (!pass)
                    throw new RuntimeException("TEST FAILED: pass = false");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /*
     * @testName: elEmptyStringTest
     *
     * @assertion_ids: EL:SPEC:25.2; EL:SPEC:25.6
     *
     * @test_Strategy: Validate that if a String is passed with the Empty
     * operator, the correct Boolean result is returned.
     */
    @Test
    public void elEmptyStringTest() {

        this.testEmptyOperator("", true);

        this.testEmptyOperator("something", false);

    }

    /*
     * @testName: elEmptyArrayTest
     *
     * @assertion_ids: EL:SPEC:25.3; EL:SPEC:25.6
     *
     * @test_Strategy: Validate that if a Array is passed with the Empty operator,
     * the correct Boolean result is returned.
     */
    @Test
    public void elEmptyArrayTest() {

        this.testEmptyOperator(new String[0], true);

        String[] testArray = {"Apple"};
        this.testEmptyOperator(testArray, false);

    }

    /*
     * @testName: elEmptyMapTest
     *
     * @assertion_ids: EL:SPEC:25.4; EL:SPEC:25.6
     *
     * @test_Strategy: Validate that if a Map is passed with the Empty operator,
     * the correct Boolean result is returned.
     */
    @Test
    public void elEmptyMapTest() {

        HashMap testMap = new HashMap();

        this.testEmptyOperator(testMap, true);

        testMap.put("Fruit", "Apple");
        this.testEmptyOperator(testMap, false);

        // Clean out the Map.
        testMap.clear();
    }

    /*
     * @testName: elEmptyCollectionTest
     *
     * @assertion_ids: EL:SPEC:25.5; EL:SPEC:25.6
     *
     * @test_Strategy: Validate that if a Collection is passed with the Empty
     * operator, the correct Boolean result is returned.
     */
    @Test
    public void elEmptyCollectionTest() {

        ArrayList testCollection = new ArrayList();

        this.testEmptyOperator(testCollection, true);

        testCollection.add("Apple");
        this.testEmptyOperator(testCollection, false);

        // Clean out the Collection.
        testCollection.clear();
    }

    // ---------------------------------------------------------- private methods

    // Test Empty operator.
    private void testEmptyOperator(Object testVal, boolean expectedResult) {

        boolean pass = false;

        NameValuePair[] value = NameValuePair.buildUnaryNameValue(testVal);

        try {
            for (boolean tf : deferred) {
                String expr = ExprEval.buildElExpr(tf, "empty");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());

                pass = (ExprEval.compareClass(result, Boolean.class)
                        && ExprEval.compareValue((Boolean) result, expectedResult));

                if (!pass)
                    throw new RuntimeException("TEST FAILED: pass = false");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
