/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ts.tests.el.spec.binaryoperator;

import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.el.common.util.ExprEval;
import com.sun.ts.tests.el.common.util.TestNum;
import com.sun.ts.tests.el.common.util.Validator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

public class ELClient {
    /**
     * @testName: elNullOperandAddTest
     * @assertion_ids: EL:SPEC:17.1
     * @test_Strategy: Validate that if both of the operands in an EL "+"
     * (addition) operation are null, the result is (Long) 0.
     */
    @Test
    public void elNullOperandAddTest() {

        boolean pass = false;

        Long expectedResult = Long.valueOf("0");
        try {
            String expr = ExprEval.buildElExpr(true, "+");
            TestUtil.logTrace("expression to be evaluated is " + expr);

            Object result = ExprEval.evaluateValueExpression(expr, null,
                    Object.class);
            TestUtil.logTrace("result is " + result.toString());
            pass = (ExprEval.compareClass(result, Long.class)
                    && ExprEval.compareValue((Long) result, expectedResult, 0));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!pass)
            throw new RuntimeException("TEST FAILED: pass = false");
    }

    /**
     * @testName: elNullOperandSubtractTest
     * @assertion_ids: EL:SPEC:17.1
     * @test_Strategy: Validate that if both of the operands in an EL "-"
     * (subtraction) operation are null, the result is (Long) 0.
     */
    @Test
    public void elNullOperandSubtractTest() {

        boolean pass = false;

        Long expectedResult = Long.valueOf("0");
        try {
            String expr = ExprEval.buildElExpr(false, "-");
            TestUtil.logTrace("expression to be evaluated is " + expr);

            Object result = ExprEval.evaluateValueExpression(expr, null,
                    Object.class);
            TestUtil.logTrace("result is " + result.toString());
            pass = (ExprEval.compareClass(result, Long.class)
                    && ExprEval.compareValue((Long) result, expectedResult, 0));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!pass)
            throw new RuntimeException("TEST FAILED: pass = false");
    }

    /**
     * @testName: elNullOperandMultiplyTest
     * @assertion_ids: EL:SPEC:17.1
     * @test_Strategy: Validate that if both of the operands in an EL "*"
     * (multiplication) operation are null, the result is (Long)
     * 0.
     */
    @Test
    public void elNullOperandMultiplyTest() {

        boolean pass = false;

        Long expectedResult = Long.valueOf("0");
        try {
            String expr = ExprEval.buildElExpr(true, "*");
            TestUtil.logTrace("expression to be evaluated is " + expr);

            Object result = ExprEval.evaluateValueExpression(expr, null,
                    Object.class);
            TestUtil.logTrace("result is " + result.toString());
            pass = (ExprEval.compareClass(result, Long.class)
                    && ExprEval.compareValue((Long) result, expectedResult, 0));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!pass)
            throw new RuntimeException("TEST FAILED: pass = false");
    }

    /**
     * @testName: elNullOperandDivisionTest
     * @assertion_ids: EL:SPEC:18.1
     * @test_Strategy: Validate that if both of the operands in an EL "/"
     * (division) operation are null, the result is (Long) 0.
     */
    @Test
    public void elNullOperandDivisionTest() {

        boolean pass = false;
        Long expectedResult = Long.valueOf("0");

        try {
            String expr = ExprEval.buildElExpr(true, "/");
            TestUtil.logTrace("expression to be evaluated is " + expr);

            Object result = ExprEval.evaluateValueExpression(expr, null,
                    Object.class);
            TestUtil.logTrace("result is " + result.toString());
            pass = (ExprEval.compareClass(result, Long.class)
                    && ExprEval.compareValue((Long) result, expectedResult, 0));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!pass)
            throw new RuntimeException("TEST FAILED: pass = false");
    }

    /**
     * @testName: elNullOperandModulusTest
     * @assertion_ids: EL:SPEC:19.1
     * @test_Strategy: Validate that if both of the operands in an EL "%" (mod)
     * operation are null, the result is (Long) 0.
     */
    @Test
    public void elNullOperandModulusTest() {

        boolean pass = false;

        Long expectedResult = Long.valueOf("0");
        try {
            String expr = ExprEval.buildElExpr(true, "%");
            TestUtil.logTrace("expression to be evaluated is " + expr);

            Object result = ExprEval.evaluateValueExpression(expr, null,
                    Object.class);
            TestUtil.logTrace("result is " + result.toString());
            pass = (ExprEval.compareClass(result, Long.class)
                    && ExprEval.compareValue((Long) result, expectedResult, 0));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!pass)
            throw new RuntimeException("TEST FAILED: pass = false");
    }

    /**
     * @testName: elBigDecimalAddTest
     * @assertion_ids: EL:SPEC:17.2.1
     * @test_Strategy: Validate that if one of the operands in an EL "+"
     * (addition) operation is a BigDecimal, the result is coerced
     * to BigDecimal and is the sum of the operands.
     * <p>
     * Equations tested: BigDecimal + BigDecimal BigDecimal +
     * Double BigDecimal + Float BigDecimal + String containing
     * ".", "e", or "E" BigDecimal + BigInteger BigDecimal +
     * Integer BigDecimal + Long BigDecimal + Short BigDecimal +
     * Byte
     */
    @Test
    public void elBigDecimalAddTest() {

        BigDecimal testValue = BigDecimal.valueOf(10.531);
        BigDecimal expectedResult = BigDecimal.valueOf(11.531);

        Validator.testBigDecimal(testValue, expectedResult, "+");

    }

    /**
     * @testName: elBigDecimalSubtractTest
     * @assertion_ids: EL:SPEC:17.2.2
     * @test_Strategy: Validate that if one of the operands in an EL "-"
     * (subtraction) operation is a BigDecimal, the result is
     * coerced to BigDecimal and is the difference of the
     * operands.
     * <p>
     * Equations tested: BigDecimal - BigDecimal BigDecimal -
     * Double BigDecimal - Float BigDecimal - String containing
     * ".", "e", or "E" BigDecimal - BigInteger BigDecimal -
     * Integer BigDecimal - Long BigDecimal - Short BigDecimal -
     * Byte
     */
    @Test
    public void elBigDecimalSubtractTest() {

        BigDecimal testValue = BigDecimal.valueOf(10.531);
        BigDecimal expectedResult = BigDecimal.valueOf(9.531);

        Validator.testBigDecimal(testValue, expectedResult, "-");

    }

    /**
     * @testName: elBigDecimalMultiplyTest
     * @assertion_ids: EL:SPEC:17.2.3
     * @test_Strategy: Validate that if one of the operands in an EL "*"
     * (multiplication) operation is a BigDecimal, the result is
     * coerced to BigDecimal and is the product of the operands.
     * <p>
     * Equations tested: BigDecimal * BigDecimal BigDecimal *
     * Double BigDecimal * Float BigDecimal * String containing
     * ".", "e", or "E" BigDecimal * BigInteger BigDecimal *
     * Integer BigDecimal * Long BigDecimal * Short BigDecimal *
     * Byte
     */
    @Test
    public void elBigDecimalMultiplyTest() {

        BigDecimal testValue = BigDecimal.valueOf(1.5);
        BigDecimal expectedResult = BigDecimal.valueOf(1.5);

        Validator.testBigDecimal(testValue, expectedResult, "*");

    }

    /**
     * @testName: elBigDecimalDivisionTest
     * @assertion_ids: EL:SPEC:18.2
     * @test_Strategy: Validate that if one of the operands in an EL "/" (div)
     * operation is a BigDecimal, the result is coerced to
     * BigDecimal and is the quotient of the operands.
     * <p>
     * Equations tested: BigDecimal / BigDecimal BigDecimal /
     * Double BigDecimal / Float BigDecimal / String containing
     * ".", "e", or "E" BigDecimal / BigInteger BigDecimal /
     * Integer BigDecimal / Long BigDecimal / Short BigDecimal /
     * Byte
     */
    @Test
    public void elBigDecimalDivisionTest() {

        BigDecimal testValue = BigDecimal.valueOf(3.0);
        BigDecimal expectedResult = BigDecimal.valueOf(3.0);

        Validator.testBigDecimal(testValue, expectedResult, "/");

    }

    /**
     * @testName: elBigDecimalModulusTest
     * @assertion_ids: EL:SPEC:19.2
     * @test_Strategy: Validate that if one of the operands in an EL "%" (mod)
     * operation is a BigDecimal, the result is coerced to Double
     * and is the remainder of the quotient of the operands.
     * <p>
     * Equations tested: BigDecimal % BigDecimal BigDecimal %
     * Double BigDecimal % Float BigDecimal % String containing
     * ".", "e", or "E" BigDecimal % BigInteger BigDecimal %
     * Integer BigDecimal % Long BigDecimal % Short BigDecimal %
     * Byte
     */
    @Test
    public void elBigDecimalModulusTest() {

        BigDecimal testValue = BigDecimal.valueOf(2.5);
        BigDecimal expectedResult = BigDecimal.valueOf(0.5);

        Validator.testBigDecimal(testValue, expectedResult, "%");

    }

    /**
     * @testName: elBigIntegerAddTest
     * @assertion_ids: EL:SPEC:17.4.1
     * @test_Strategy: Validate that if one of the operands in an EL "+"
     * (addition) operation is a BigInteger, the result is coerced
     * to BigInteger and is the sum of the operands.
     * <p>
     * Equations tested: BigInteger + BigInteger BigInteger +
     * Integer BigInteger + Long BigInteger + Short BigInteger +
     * Byte
     */
    @Test
    public void elBigIntegerAddTest() {

        BigInteger testValue = BigInteger.valueOf(10531);
        BigInteger expectedResult = BigInteger.valueOf(10532);

        Validator.testBigInteger(testValue, expectedResult, "+");

    }

    /**
     * @testName: elBigIntegerSubtractTest
     * @assertion_ids: EL:SPEC:17.4.2
     * @test_Strategy: Validate that if one of the operands in an EL "-"
     * (subtraction) operation is a BigInteger, the result is
     * coerced to BigInteger and is the difference of the
     * operands.
     * <p>
     * Equations tested: BigInteger - BigInteger BigInteger -
     * Integer BigInteger - Long BigInteger - Short BigInteger -
     * Byte
     */
    @Test
    public void elBigIntegerSubtractTest() {

        BigInteger testValue = BigInteger.valueOf(10531);
        BigInteger expectedResult = BigInteger.valueOf(10530);

        Validator.testBigInteger(testValue, expectedResult, "-");

    }

    /**
     * @testName: elBigIntegerMultiplyTest
     * @assertion_ids: EL:SPEC:17.4.3
     * @test_Strategy: Validate that if one of the operands in an EL "*" operation
     * is a BigInteger, the result is coerced to BigInteger and is
     * the product of the operands.
     * <p>
     * BigInteger * BigInteger BigInteger * Integer BigInteger *
     * Long BigInteger * Short BigInteger * Byte
     */
    @Test
    public void elBigIntegerMultiplyTest() {

        BigInteger testValue = BigInteger.valueOf(10531);
        BigInteger expectedResult = BigInteger.valueOf(10531);

        Validator.testBigInteger(testValue, expectedResult, "*");

    }

    /**
     * @testName: elBigIntegerDivisionTest
     * @assertion_ids: EL:SPEC:18.2
     * @test_Strategy: Validate that if one of the operands in an EL "/" (div)
     * operation is a BigInteger, the result is coerced to
     * BigDecimal and is the quotient of the operands.
     * <p>
     * BigInteger / BigInteger BigInteger / Integer BigInteger /
     * Long BigInteger / Short BigInteger / Byte
     */
    @Test
    public void elBigIntegerDivisionTest() {

        BigInteger testValue = BigInteger.valueOf(10531);
        BigInteger expectedResult = BigInteger.valueOf(10531);

        Validator.testBigInteger(testValue, expectedResult, "/");

    }

    /**
     * @testName: elBigIntegerModulusTest
     * @assertion_ids: EL:SPEC:19.3
     * @test_Strategy: Validate that if one of the operands in an EL "%" (mod)
     * operation is a BigInteger, the result is coerced to
     * BigInteger and is the remainder of the quotient of the
     * operands.
     * <p>
     * BigInteger % BigInteger BigInteger % Integer BigInteger %
     * Long BigInteger % Short BigInteger % Byte
     */
    @Test
    public void elBigIntegerModulusTest() {

        BigInteger testValue = BigInteger.valueOf(10531);
        BigInteger expectedResult = BigInteger.valueOf(0);

        Validator.testBigInteger(testValue, expectedResult, "%");

    }

    /**
     * @testName: elFloatAddTest
     * @assertion_ids: EL:SPEC:17.3.1; EL:SPEC:17.3.2
     * @test_Strategy: Validate that if one of the operands in an EL "+"
     * (addition) operation is a Float, the result is coerced to
     * Double and is the sum of the operands.
     * <p>
     * Equations tested: Float + Double Float + Float Float +
     * String containing ".", "e", or "E" Float + BigInteger Float
     * + Integer Float + Long Float + Short Float + Byte
     */
    @Test
    public void elFloatAddTest() {

        Float expectedResult;
        // For each float in validate List.
        for (Iterator<?> it = TestNum.getFloatList().iterator(); it.hasNext(); ) {
            Float testValue = (Float) it.next();
            expectedResult = testValue + Float.valueOf("1.0");
            Validator.testFloat(testValue, expectedResult, "+");
        }

    }

    /**
     * @testName: elFloatSubtractTest
     * @assertion_ids: EL:SPEC:17.3.1; EL:SPEC:17.3.2
     * @test_Strategy: Validate that if one of the operands in an EL "-"
     * (subtraction) operation is a Float, the result is coerced
     * to Double and is the difference of the operands.
     * <p>
     * Equations tested: Float - Double Float - Float Float -
     * String containing ".", "e", or "E" Float - BigInteger Float
     * - Integer Float - Long Float - Short Float - Byte
     */
    @Test
    public void elFloatSubtractTest() {

        Float expectedResult;
        // For each float in validate List.
        for (Iterator<?> it = TestNum.getFloatList().iterator(); it.hasNext(); ) {
            Float testValue = (Float) it.next();
            expectedResult = testValue - Float.valueOf("1.0");
            Validator.testFloat(testValue, expectedResult, "-");
        }

    }

    /**
     * @testName: elFloatMultiplyTest
     * @assertion_ids: EL:SPEC:17.3.1; EL:SPEC:17.3.2
     * @test_Strategy: Validate that if one of the operands in an EL "*"
     * (multiplication) operation is a Float, the result is
     * coerced to Double and is the product of the operands.
     * <p>
     * Equations tested: Float * Double Float * Float Float *
     * String containing ".", "e", or "E" Float * BigInteger Float
     * * Integer Float * Long Float * Short Float * Byte
     */
    @Test
    public void elFloatMultiplyTest() {

        Float expectedResult;
        // For each float in validate List.
        for (Iterator<?> it = TestNum.getFloatList().iterator(); it.hasNext(); ) {
            Float testValue = (Float) it.next();
            expectedResult = testValue * Float.valueOf("1.0");
            Validator.testFloat(testValue, expectedResult, "*");
        }

    }

    /**
     * @testName: elFloatDivisionTest
     * @assertion_ids: EL:SPEC:18.2; EL:SPEC:18.3
     * @test_Strategy: Validate that if one of the operands in an EL "/" (div)
     * operation is a Float, the result is coerced to Double and
     * is the quotient of the operands.
     * <p>
     * Equations tested: Float / Double Float / Float Float /
     * String containing ".", "e", or "E" Float / BigInteger Float
     * / Integer Float / Long Float / Short Float / Byte
     */
    @Test
    public void elFloatDivisionTest() {

        Float expectedResult;
        // For each float in validate List.
        for (Iterator<?> it = TestNum.getFloatList().iterator(); it.hasNext(); ) {
            Float testValue = (Float) it.next();
            expectedResult = testValue / Float.valueOf("1.0");
            Validator.testFloat(testValue, expectedResult, "/");
        }

    }

    /**
     * @testName: elFloatModulusTest
     * @assertion_ids: EL:SPEC:19.2
     * @test_Strategy: Validate that if one of the operands in an EL "%" (mod)
     * operation is a Float, the result is coerced to Double and
     * is the remainder of the quotient of the operands.
     * <p>
     * Equations tested: Float % Double Float % Float Float %
     * String containing ".", "e", or "E" Float % BigInteger Float
     * % Integer Float % Long Float % Short Float % Byte
     */
    @Test
    public void elFloatModulusTest() {

        Float expectedResult;
        // For each float in validate List.
        for (Iterator<?> it = TestNum.getFloatList().iterator(); it.hasNext(); ) {
            Float testValue = (Float) it.next();
            expectedResult = testValue % Float.valueOf("1.0");
            Validator.testFloat(testValue, expectedResult, "%");
        }

    }

    /**
     * @testName: elDoubleAddTest
     * @assertion_ids: EL:SPEC:17.3.1; EL:SPEC:17.3.2
     * @test_Strategy: Validate that if one of the operands in an EL "+"
     * (addition) operation is a Double, the result is coerced to
     * Double and is the sum of the operands.
     * <p>
     * Equations tested: Double + Double Double + String
     * containing ".", "e", or "E" Double + BigInteger Double +
     * Integer Double + Long Double + Short Double + Byte
     */
    @Test
    public void elDoubleAddTest() {

        Double testValue = Double.valueOf(2.5);
        Double expectedResult = Double.valueOf(3.5);

        Validator.testDouble(testValue, expectedResult, "+");

    }

    /**
     * @testName: elDoubleSubtractTest
     * @assertion_ids: EL:SPEC:17.3.1; EL:SPEC:17.3.2
     * @test_Strategy: Validate that if one of the operands in an EL "-"
     * (subtraction) operation is a Double, the result is coerced
     * to Double and is the difference of the operands.
     * <p>
     * Equations tested: Double - Double Double - String
     * containing ".", "e", or "E" Double - BigInteger Double -
     * Integer Double - Long Double - Short Double - Byte
     */
    @Test
    public void elDoubleSubtractTest() {

        Double testValue = Double.valueOf(2.5);
        Double expectedResult = Double.valueOf(1.5);

        Validator.testDouble(testValue, expectedResult, "-");

    }

    /**
     * @testName: elDoubleMultiplyTest
     * @assertion_ids: EL:SPEC:17.3.1; EL:SPEC:17.3.2
     * @test_Strategy: Validate that if one of the operands in an EL "*"
     * (multiplication) operation is a Double, the result is
     * coerced to Double and is the product of the operands.
     * <p>
     * Equations tested: Double * Double Double * String
     * containing ".", "e", or "E" Double * BigInteger Double *
     * Integer Double * Long Double * Short Double * Byte
     */
    @Test
    public void elDoubleMultiplyTest() {

        Double testValue = Double.valueOf(2.5);
        Double expectedResult = Double.valueOf(2.5);

        Validator.testDouble(testValue, expectedResult, "*");

    }

    /**
     * @testName: elDoubleDivisionTest
     * @assertion_ids: EL:SPEC:18.2; EL:SPEC:18.3
     * @test_Strategy: Validate that if one of the operands in an EL "/" (div)
     * operation is a Double, the result is coerced to Double and
     * is the quotient of the operands.
     * <p>
     * Equations tested: Double / Double Double / String
     * containing ".", "e", or "E" Double / BigInteger Double /
     * Integer Double / Long Double / Short Double / Byte
     */
    @Test
    public void elDoubleDivisionTest() {

        Double testValue = Double.valueOf(2.5);
        Double expectedResult = Double.valueOf(2.5);

        Validator.testDouble(testValue, expectedResult, "/");

    }

    /**
     * @testName: elDoubleModulusTest
     * @assertion_ids: EL:SPEC:19.2
     * @test_Strategy: Validate that if one of the operands in an EL "%" (mod)
     * operation is a Double, the result is coerced to Double and
     * is the remainder of the quotient of the operands.
     * <p>
     * Equations tested: Double % Double Double % String
     * containing ".", "e", or "E" Double % BigInteger Double %
     * Integer Double % Long Double % Short Double % Byte
     */
    @Test
    public void elDoubleModulusTest() {

        Double testValue = Double.valueOf(2.5);
        Double expectedResult = Double.valueOf(0.5);

        Validator.testDouble(testValue, expectedResult, "%");
    }

    /**
     * @testName: elNumericStringSubtractTest
     * @assertion_ids: EL:SPEC:17.3.1; EL:SPEC:17.3.2
     * @test_Strategy: Validate that if one of the operands in an EL "-"
     * (subtraction) operation is a numeric string, the result is
     * coerced to Double and is the difference of the operands.
     * <p>
     * Equations tested: Numeric String - String containing ".",
     * "e", or "E" Numeric String - BigInteger Numeric String -
     * Integer Numeric String - Long Numeric String - Short
     * Numeric String - Byte
     */
    @Test
    public void elNumericStringSubtractTest() {

        String testValue = "25e-1";
        Double expectedResult = Double.valueOf(1.5);

        Validator.testNumericString(testValue, expectedResult, "-");

    }

    /**
     * @testName: elNumericStringMultiplyTest
     * @assertion_ids: EL:SPEC:17.3.1; EL:SPEC:17.3.2
     * @test_Strategy: Validate that if one of the operands in an EL "*"
     * (multiplication) operation is a numeric string, the result
     * is coerced to Double and is the product of the operands.
     * <p>
     * Equations tested: Numeric String * String containing ".",
     * "e", or "E" Numeric String * BigInteger Numeric String *
     * Integer Numeric String * Long Numeric String * Short
     * Numeric String * Byte
     */
    @Test
    public void elNumericStringMultiplyTest() {

        String testValue = "25E-1";
        Double expectedResult = Double.valueOf(2.5);

        Validator.testNumericString(testValue, expectedResult, "*");

    }

    /**
     * @testName: elNumericStringDivisionTest
     * @assertion_ids: EL:SPEC:18.2; EL:SPEC:18.3
     * @test_Strategy: Validate that if one of the operands in an EL "/" (div)
     * operation is a numeric string, the result is coerced to
     * Double and is the quotient of the operands.
     * <p>
     * Equations tested: Numeric String / String containing ".",
     * "e", or "E" Numeric String / BigInteger Numeric String /
     * Integer Numeric String / Long Numeric String / Short
     * Numeric String / Byte
     */
    @Test
    public void elNumericStringDivisionTest() {

        String testValue = "2.5";
        Double expectedResult = Double.valueOf(2.5);

        Validator.testNumericString(testValue, expectedResult, "/");

    }

    /**
     * @testName: elNumericStringModulusTest
     * @assertion_ids: EL:SPEC:19.2
     * @test_Strategy: Validate that if one of the operands in an EL "%" (mod)
     * operation is a numeric string, the result is coerced to
     * Double and is the remainder of the quotient of the
     * operands.
     * <p>
     * Equations tested: Numeric String % String containing ".",
     * "e", or "E" Numeric String % BigInteger Numeric String %
     * Integer Numeric String % Long Numeric String % Short
     * Numeric String % Byte
     */
    @Test
    public void elNumericStringModulusTest() {

        String testValue = "2.5e0";
        Double expectedResult = Double.valueOf(0.5);

        Validator.testNumericString(testValue, expectedResult, "%");

    }

    /**
     * @testName: elLongAddTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if one of the operands in an EL "+"
     * (addition) operation is a Long, the result is coerced to
     * Long and is the sum of the operands.
     * <p>
     * Equations tested: Long + Integer Long + Long Long + Short
     * Long + Byte
     */
    @Test
    public void elLongAddTest() {

        Long testValue = Long.valueOf(25000);
        Long expectedResult = Long.valueOf(25001);

        Validator.testLong(testValue, expectedResult, "+");

    }

    /**
     * @testName: elLongSubtractTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if one of the operands in an EL "-"
     * (subtraction) operation is a Long, the result is coerced to
     * Long and is the difference of the operands.
     * <p>
     * Equations tested: Long - Integer Long - Long Long - Short
     * Long - Byte
     */
    @Test
    public void elLongSubtractTest() {

        Long testValue = Long.valueOf(25000);
        Long expectedResult = Long.valueOf(24999);

        Validator.testLong(testValue, expectedResult, "-");

    }

    /**
     * @testName: elLongMultiplyTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if one of the operands in an EL "*"
     * (multiplication) operation is a Long, the result is coerced
     * to Long and is the product of the operands.
     * <p>
     * Equations tested: Long * Integer Long * Long Long * Short
     * Long * Byte
     */
    @Test
    public void elLongMultiplyTest() {

        Long testValue = Long.valueOf(25000);
        Long expectedResult = Long.valueOf(25000);

        Validator.testLong(testValue, expectedResult, "*");

    }

    /**
     * @testName: elLongDivisionTest
     * @assertion_ids: EL:SPEC:18.3
     * @test_Strategy: Validate that if one of the operands in an EL "/" (div)
     * operation is a Long, the result is coerced to Double and is
     * the quotient of the operands.
     * <p>
     * Equations tested: Long / Integer Long / Long Long / Short
     * Long / Byte
     */
    @Test
    public void elLongDivisionTest() {

        Long testValue = Long.valueOf(25000);
        Long expectedResult = Long.valueOf(25000);

        Validator.testLong(testValue, expectedResult, "/");

    }

    /**
     * @testName: elLongModulusTest
     * @assertion_ids: EL:SPEC:19.4
     * @test_Strategy: Validate that if one of the operands in an EL "%" (mod)
     * operation is a Long, the result is coerced to Long and is
     * the remainder of the quotient of the operands.
     * <p>
     * Equations tested: Long % Integer Long % Long Long % Short
     * Long % Byte
     */
    @Test
    public void elLongModulusTest() {

        Long testValue = Long.valueOf(25000);
        Long expectedResult = Long.valueOf(0);

        Validator.testLong(testValue, expectedResult, "%");

    }

    /**
     * @testName: elIntegerAddTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if one of the operands in an EL "+"
     * (addition) operation is a Integer, the result is coerced to
     * Long and is the sum of the operands.
     * <p>
     * Equations tested: Integer + Integer Integer + Short Integer
     * + Byte
     */
    @Test
    public void elIntegerAddTest() {

        Integer testValue = Integer.valueOf(25);
        Integer expectedResult = Integer.valueOf(26);

        Validator.testInteger(testValue, expectedResult, "+");

    }

    /**
     * @testName: elIntegerSubtractTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if one of the operands in an EL "-"
     * (subtraction) operation is a Integer, the result is coerced
     * to Long and is the difference of the operands.
     * <p>
     * Equations tested: Long - Integer Long - Short Long - Byte
     */
    @Test
    public void elIntegerSubtractTest() {

        Integer testValue = Integer.valueOf(25);
        Integer expectedResult = Integer.valueOf(24);

        Validator.testInteger(testValue, expectedResult, "-");

    }

    /**
     * @testName: elIntegerMultiplyTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if one of the operands in an EL "*"
     * (multiplication) operation is a Integer, the result is
     * coerced to Long and is the product of the operands.
     * <p>
     * Equations tested: Integer * Integer Integer * Short Integer
     * * Byte
     */
    @Test
    public void elIntegerMultiplyTest() {

        Integer testValue = Integer.valueOf(25);
        Integer expectedResult = Integer.valueOf(25);

        Validator.testInteger(testValue, expectedResult, "*");

    }

    /**
     * @testName: elIntegerDivisionTest
     * @assertion_ids: EL:SPEC:18.3
     * @test_Strategy: Validate that if one of the operands in an EL "/" (div)
     * operation is a Integer, the result is coerced to Double and
     * is the quotient of the operands.
     * <p>
     * Equations tested: Integer / Integer Integer / Short Integer
     * / Byte
     */
    @Test
    public void elIntegerDivisionTest() {

        Integer testValue = Integer.valueOf(25);
        Integer expectedResult = Integer.valueOf(25);

        Validator.testInteger(testValue, expectedResult, "/");

    }

    /**
     * @testName: elIntegerModulusTest
     * @assertion_ids: EL:SPEC:19.4
     * @test_Strategy: Validate that if one of the operands in an EL "%" (mod)
     * operation is a Integer, the result is coerced to Long and
     * is the remainder of the quotient of the operands.
     * <p>
     * Equations tested: Integer % Integer Integer % Short Integer
     * % Byte
     */
    @Test
    public void elIntegerModulusTest() {

        Integer testValue = Integer.valueOf(25);
        Integer expectedResult = Integer.valueOf(0);

        Validator.testInteger(testValue, expectedResult, "%");

    }

    /**
     * @testName: elShortAddTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if one of the operands in an EL "+"
     * (addition) operation is a Short, the result is coerced to
     * Long and is the sum of the operands.
     * <p>
     * Equations tested: Short + Short Short + Byte
     */
    @Test
    public void elShortAddTest() {

        Short testValue = Short.valueOf("2");
        Short expectedResult = Short.valueOf("3");

        Validator.testShort(testValue, expectedResult, "+");

    }

    /**
     * @testName: elShortSubtractTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if one of the operands in an EL "-"
     * (subtraction) operation is a Short, the result is coerced
     * to Long and is the difference of the operands.
     * <p>
     * Equations tested: Short - Short Short - Byte
     */
    @Test
    public void elShortSubtractTest() {

        Short testValue = Short.valueOf("2");
        Short expectedResult = Short.valueOf("1");

        Validator.testShort(testValue, expectedResult, "-");

    }

    /**
     * @testName: elShortMultiplyTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if one of the operands in an EL "*"
     * (multiplication) operation is a Short, the result is
     * coerced to Long and is the product of the operands.
     * <p>
     * Equations tested: Short * Short Short * Byte
     */
    @Test
    public void elShortMultiplyTest() {

        Short testValue = Short.valueOf("2");
        Short expectedResult = Short.valueOf("2");

        Validator.testShort(testValue, expectedResult, "*");

    }

    /**
     * @testName: elShortDivisionTest
     * @assertion_ids: EL:SPEC:18.3
     * @test_Strategy: Validate that if one of the operands in an EL "/" (div)
     * operation is a Short, the result is coerced to Double and
     * is the quotient of the operands.
     * <p>
     * Equations tested: Short / Short Short / Byte
     */
    @Test
    public void elShortDivisionTest() {

        Short testValue = Short.valueOf("2");
        Short expectedResult = Short.valueOf("2");

        Validator.testShort(testValue, expectedResult, "/");

    }

    /**
     * @testName: elShortModulusTest
     * @assertion_ids: EL:SPEC:19.4
     * @test_Strategy: Validate that if one of the operands in an EL "%" (mod)
     * operation is a Short, the result is coerced to Long and is
     * the remainder of the quotient of the operands.
     * <p>
     * Equations tested: Short % Short Short % Byte
     */
    @Test
    public void elShortModulusTest() {

        Short testValue = Short.valueOf("2");
        Short expectedResult = Short.valueOf("0");

        Validator.testShort(testValue, expectedResult, "%");

    }

    /**
     * @testName: elByteAddTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if both operands in an EL "+" (addition)
     * operation are Bytes, the result is coerced to Long and is
     * the sum of the operands.
     * <p>
     * Equations tested: Byte + Byte
     */
    @Test
    public void elByteAddTest() {

        Byte testValue = Byte.valueOf("2");
        Byte expectedResult = Byte.valueOf("3");

        Validator.testByte(testValue, expectedResult, "+");

    }

    /**
     * @testName: elByteSubtractTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if both operands in an EL "-" (subtraction)
     * operation are Bytes, the result is coerced to Long and is
     * the difference of the operands.
     * <p>
     * Equations tested: Byte - Byte
     */
    @Test
    public void elByteSubtractTest() {

        Byte testValue = Byte.valueOf("2");
        Byte expectedResult = Byte.valueOf("1");

        Validator.testByte(testValue, expectedResult, "-");

    }

    /**
     * @testName: elByteMultiplyTest
     * @assertion_ids: EL:SPEC:17.5
     * @test_Strategy: Validate that if both operands in an EL "*"
     * (multiplication) operation are Bytes, the result is coerced
     * to Long and is the product of the operands.
     * <p>
     * Equations tested: Byte * Byte
     */
    @Test
    public void elByteMultiplyTest() {

        Byte testValue = Byte.valueOf("2");
        Byte expectedResult = Byte.valueOf("2");

        Validator.testByte(testValue, expectedResult, "*");

    }

    /**
     * @testName: elByteDivisionTest
     * @assertion_ids: EL:SPEC:18.3
     * @test_Strategy: Validate that if both operands in an EL "/" (div) operation
     * are Bytes, the result is coerced to Double and is the
     * quotient of the operands.
     * <p>
     * Equations tested: Byte / Byte
     */
    @Test
    public void elByteDivisionTest() {

        Byte testValue = Byte.valueOf("2");
        Byte expectedResult = Byte.valueOf("2");

        Validator.testByte(testValue, expectedResult, "/");

    }

    /**
     * @testName: elByteModulusTest
     * @assertion_ids: EL:SPEC:19.4
     * @test_Strategy: Validate that if both operands in an EL "%" (mod) operation
     * are Bytes, the result is coerced to Long and is the
     * remainder of the quotient of the operands.
     * <p>
     * Equations tested: Byte % Byte
     */
    @Test
    public void elByteModulusTest() {

        Byte testValue = Byte.valueOf("2");
        Byte expectedResult = Byte.valueOf("0");

        Validator.testByte(testValue, expectedResult, "%");

    }

    /**
     * @testName: elBooleanAndTest
     * @assertion_ids: EL:SPEC:23.1; EL:SPEC:24.2.1
     * @test_Strategy: Validate that if one of the operands in an EL "&&", "and"
     * operation is a Boolean, the result is coerced to Boolean.
     * <p>
     * Equations tested: Boolean && String Boolean && Boolean
     * Boolean and String Boolean and Boolean
     */
    @Test
    public void elBooleanAndTest() {

        Validator.testBoolean(true, "true", true, "&&");
        Validator.testBoolean(true, true, true, "&&");

        Validator.testBoolean(true, "false", false, "and");
        Validator.testBoolean(true, false, false, "and");

    }

    /**
     * @testName: elBooleanOrTest
     * @assertion_ids: EL:SPEC:23.1; EL:SPEC:24.2.1
     * @test_Strategy: Validate that if one of the operands in an EL "||", "or"
     * operation is a Boolean, the result is coerced to Boolean.
     * <p>
     * Equations tested: Boolean || String Boolean || Boolean
     * Boolean or String Boolean or Boolean
     */
    @Test
    public void elBooleanOrTest() {

        Validator.testBoolean(false, "false", false, "||");
        Validator.testBoolean(true, "false", true, "or");

        Validator.testBoolean(true, false, true, "||");
        Validator.testBoolean(true, true, true, "or");

    }

}
