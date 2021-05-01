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

package com.sun.ts.tests.el.spec.unaryoperator;

import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.el.common.util.ExprEval;
import com.sun.ts.tests.el.common.util.NameValuePair;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

public class ELClient {
    private final boolean[] deferred = {true, false};

    /*
     * @testName: elNullUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.1
     *
     * @test_Strategy: Validate that if null is passed with the unary minus
     * operator, the result is (Long) 0. Test both ${-null} & #{-null}
     */
    @Test
    public void elNullUnaryTest() {

        boolean pass = false;
        String[] symbols = {"$", "#"};

        Long expectedResult = Long.valueOf("0");

        try {
            for (String prefix : symbols) {
                String expr = prefix + "{-null}";
                TestUtil.logTrace("expression to be evaluated is " + expr);

                Object result = ExprEval.evaluateValueExpression(expr, null,
                        Object.class);
                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, Long.class)
                        && ExprEval.compareValue((Long) result, expectedResult, 0));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elBigDecimalUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.2
     *
     * @test_Strategy: Validate that if a BigDecimal A is passed with the unary
     * minus operator (-), the result is A.negate(). Test both ${- value} & #{-
     * value}
     */
    @Test
    public void elBigDecimalUnaryTest() {

        boolean pass = false;

        BigDecimal bd = BigDecimal.valueOf(10.0);
        BigDecimal expectedResult = BigDecimal.valueOf(-10.0);

        NameValuePair[] value = NameValuePair.buildUnaryNameValue(bd);

        try {
            for (boolean tf : deferred) {

                String expr = ExprEval.buildElExpr(tf, "unary_minus");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, BigDecimal.class)
                        && ExprEval.compareValue((BigDecimal) result, expectedResult, 0));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elBigIntegerUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.2
     *
     * @test_Strategy: Validate that if a BigInteger A is passed with the unary
     * minus operator (-), the result is A.negate(). Test both ${- value} & #{-
     * value}
     */
    @Test
    public void elBigIntegerUnaryTest() {

        boolean pass = false;

        BigInteger bi = BigInteger.valueOf(100);
        BigInteger expectedResult = BigInteger.valueOf(-100);

        NameValuePair[] value = NameValuePair.buildUnaryNameValue(bi);

        try {
            for (boolean tf : deferred) {

                String expr = ExprEval.buildElExpr(tf, "unary_minus");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, BigInteger.class)
                        && ExprEval.compareValue((BigInteger) result, expectedResult, 0));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elDoubleStringUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.3.1
     *
     * @test_Strategy: Validate that if a String is passed with the unary minus
     * operator (-), the result is as follows: - If the String contains ".", "e",
     * or "E" coerce to Double, apply operator.
     *
     * Test both ${- value} & #{- value}
     */
    @Test
    public void elDoubleStringUnaryTest() {

        boolean pass = false;

        String[] values = {"10.0", "10E0", "10e0"};

        Double expectedResult = Double.valueOf("-10");

        try {
            for (boolean tf : deferred) {
                for (String val : values) {
                    NameValuePair[] value = NameValuePair.buildUnaryNameValue(val);

                    String expr = ExprEval.buildElExpr(tf, "unary_minus");
                    Object result = ExprEval.evaluateValueExpression(expr, value,
                            Object.class);

                    TestUtil.logTrace("result is " + result.toString());
                    pass = (ExprEval.compareClass(result, Double.class)
                            && ExprEval.compareValue(result, expectedResult));
                }
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elLongStringUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.3.2
     *
     * @test_Strategy: Validate that if a String is passed with the unary minus
     * operator (-), the result is as follows: - If the String does not contain
     * ".", "e", or "E" coerce to Long, apply operator.
     *
     * Test both ${- value} & #{- value}
     */
    @Test
    public void elLongStringUnaryTest() {

        boolean pass = false;

        String val = "100";

        Long expectedResult = Long.valueOf("-100");

        try {
            for (boolean tf : deferred) {
                NameValuePair[] value = NameValuePair.buildUnaryNameValue(val);

                String expr = ExprEval.buildElExpr(tf, "unary_minus");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, Long.class)
                        && ExprEval.compareValue(result, expectedResult));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elByteUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.4.1
     *
     * @test_Strategy: Validate that if a Byte is passed with the unary minus
     * operator (-), the type is retained and the operator is applied.
     *
     * Test both ${- value} & #{- value}
     */
    @Test
    public void elByteUnaryTest() {

        boolean pass = false;

        byte num = 1;
        byte expectedResult = -1;

        NameValuePair[] value = NameValuePair.buildUnaryNameValue(num);

        try {
            for (boolean tf : deferred) {

                String expr = ExprEval.buildElExpr(tf, "unary_minus");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, Byte.class)
                        && ExprEval.compareValue(result, expectedResult));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elShortUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.4.1
     *
     * @test_Strategy: Validate that if a Short is passed with the unary minus
     * operator (-), the type is retained and the operator is applied.
     *
     * Test both ${- value} & #{- value}
     */
    @Test
    public void elShortUnaryTest() {

        boolean pass = false;

        short num = 1;
        short expectedResult = -1;

        NameValuePair[] value = NameValuePair.buildUnaryNameValue(num);

        try {
            for (boolean tf : deferred) {

                String expr = ExprEval.buildElExpr(tf, "unary_minus");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, Short.class)
                        && ExprEval.compareValue(result, expectedResult));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elIntegerUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.4.1
     *
     * @test_Strategy: Validate that if an Integer is passed with the unary minus
     * operator (-), the type is retained and the operator is applied.
     *
     * Test both ${- value} & #{- value}
     */
    @Test
    public void elIntegerUnaryTest() {

        boolean pass = false;

        int num = 1;
        int expectedResult = -1;

        NameValuePair[] value = NameValuePair.buildUnaryNameValue(num);

        try {
            for (boolean tf : deferred) {

                String expr = ExprEval.buildElExpr(tf, "unary_minus");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, Integer.class)
                        && ExprEval.compareValue(result, expectedResult));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elLongUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.4.1
     *
     * @test_Strategy: Validate that if a Long is passed with the unary minus
     * operator (-), the type is retained and the operator is applied.
     *
     * Test both ${- value} & #{- value}
     */
    @Test
    public void elLongUnaryTest() {

        boolean pass = false;

        long num = 10000;
        long expectedResult = -10000;

        NameValuePair[] value = NameValuePair.buildUnaryNameValue(num);

        try {
            for (boolean tf : deferred) {

                String expr = ExprEval.buildElExpr(tf, "unary_minus");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, Long.class)
                        && ExprEval.compareValue(result, expectedResult));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elFloatUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.4.1
     *
     * @test_Strategy: Validate that if a Float is passed with the unary minus
     * operator (-), the type is retained and the operator is applied.
     *
     * Test both ${- value} & #{- value}
     */
    @Test
    public void elFloatUnaryTest() {

        boolean pass = false;

        float num = 10000f;
        float expectedResult = -10000f;

        NameValuePair[] value = NameValuePair.buildUnaryNameValue(num);

        try {
            for (boolean tf : deferred) {

                String expr = ExprEval.buildElExpr(tf, "unary_minus");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, Float.class)
                        && ExprEval.compareValue((Float) result, expectedResult, 0));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elDoubleUnaryTest
     *
     * @assertion_ids: EL:SPEC:20.4.1
     *
     * @test_Strategy: Validate that if a Double is passed with the unary minus
     * operator (-), the type is retained and the operator is applied.
     *
     * Test both ${- value} & #{- value}
     */
    @Test
    public void elDoubleUnaryTest() {

        boolean pass = false;

        double num = 10000.0;
        double expectedResult = -10000.0;

        NameValuePair[] value = NameValuePair.buildUnaryNameValue(num);

        try {
            for (boolean tf : deferred) {

                String expr = ExprEval.buildElExpr(tf, "unary_minus");
                Object result = ExprEval.evaluateValueExpression(expr, value,
                        Object.class);

                TestUtil.logTrace("result is " + result.toString());
                pass = (ExprEval.compareClass(result, Double.class)
                        && ExprEval.compareValue(result, expectedResult));
            }

            if (!pass)
                throw new RuntimeException("TEST FAILED: pass = false");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * @testName: elBooleanUnaryFalseTest
     *
     * @assertion_ids: EL:SPEC:24.1.1
     *
     * @test_Strategy: Validate that if a String("false") is passed with the unary
     * "not" operator, the type is coerced to Boolean and the operator is applied.
     *
     * Equations Tested: ${not value} & #{not value} ${! value} & #{! value}
     */
    @Test
    public void elBooleanUnaryFalseTest() {

        this.testUnary("false", true);

    }

    /*
     * @testName: elBooleanUnaryTrueTest
     *
     * @assertion_ids: EL:SPEC:24.1.1
     *
     * @test_Strategy: Validate that if a String("true") is passed with the unary
     * "not" operator, the type is coerced to Boolean and the operator is applied.
     *
     * Equations Tested: ${not value} & #{not value} ${! value} & #{! value}
     */
    @Test
    public void elBooleanUnaryTrueTest() {

        this.testUnary("true", false);

    }

    // ---------------------------------------------------------- private methods

    private void testUnary(String testVal, boolean expectedResult) {

        boolean pass = false;

        String[] operator = {"unary_not", "unary_bang"};
        NameValuePair[] value = NameValuePair.buildUnaryNameValue(testVal);

        try {
            for (boolean tf : deferred) {
                for (String op : operator) {
                    String expr = ExprEval.buildElExpr(tf, op);
                    Object result = ExprEval.evaluateValueExpression(expr, value,
                            Object.class);

                    TestUtil.logTrace("result is " + result.toString());
                    pass = (ExprEval.compareClass(result, Boolean.class)
                            && ExprEval.compareValue((Boolean) result, expectedResult));

                    if (!pass)
                        throw new RuntimeException("TEST FAILED: pass = false");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
