/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates and others.
 * All rights reserved.
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

package com.sun.ts.tests.el.spec.concatoperator;

import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.el.common.util.ELTestUtil;
import com.sun.ts.tests.el.common.util.TestNum;
import com.sun.ts.tests.el.common.util.Validator;
import jakarta.el.ELProcessor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

public class ELClient {
    /**
     * @testName: elBigDecimalConcatenationTest
     * @assertion_ids: EL:SPEC:38; EL:SPEC:47.1; EL:SPEC:47.1.1; EL:SPEC:47.1.2
     * @test_Strategy: Validate that if one of the operands is BigDecimal that the
     * operator is '+=' that both operands are coerced to type
     * String and concatenated.
     * <p>
     * Equations tested: BigDecimal += BigDecimal BigDecimal +=
     * Double BigDecimal += Float BigDecimal += String
     * containing".", "e", or "E" BigDecimal += BigInteger
     * BigDecimal += Integer BigDecimal += Long BigDecimal +=
     * Short BigDecimal += Byte
     */
    @Test
    public void elBigDecimalConcatenationTest() {

        BigDecimal testValue = BigDecimal.valueOf(10.531);
        /*
         * The expected result is actually computed in the testBigDecimal method for
         * the '+=' operator!
         */
        Validator.testBigDecimal(testValue, null, "+=");

    }

    /**
     * @testName: elBigIntegerConcatenationTest
     * @assertion_ids: EL:SPEC:38; EL:SPEC:47.1; EL:SPEC:47.1.1; EL:SPEC:47.1.2
     * @test_Strategy: Validate that if one of the operands is BigInteger that the
     * operator is '+=' that both operands are coerced to type
     * String and concatenated.
     * <p>
     * Equations tested: BigInteger += BigInteger BigInteger +=
     * Integer BigInteger += Long BigInteger += Short BigInteger
     * += Byte
     */
    @Test
    public void elBigIntegerConcatenationTest() {

        BigInteger testValue = BigInteger.valueOf(10531);
        /*
         * The expected result is actually computed in the testBigInteger method for
         * the '+=' operator!
         */
        Validator.testBigInteger(testValue, null, "+=");

    }

    /**
     * @testName: elFloatConcatenationTest
     * @assertion_ids: EL:SPEC:38; EL:SPEC:47.1; EL:SPEC:47.1.1; EL:SPEC:47.1.2
     * @test_Strategy: Validate when the operator is '+=' that the operands are
     * coerced to Strings and the result is a Concatenation.
     * <p>
     * Equations tested: Float + Double Float + Float Float +
     * String containing ".", "e", or "E" Float + BigInteger Float
     * + Integer Float + Long Float + Short Float + Byte
     */
    @Test
    public void elFloatConcatenationTest() {

        // For each float in this List.
        for (Iterator<?> it = TestNum.getFloatList().iterator(); it.hasNext(); ) {
            Float testValue = (Float) it.next();
            /*
             * The expected result is actually computed in the testFloat method for
             * the '+=' operator!
             */
            Validator.testFloat(testValue, null, "+=");
        }

    }

    /**
     * @testName: elDoubleConcatenationTest
     * @assertion_ids: EL:SPEC:38; EL:SPEC:47.1; EL:SPEC:47.1.1; EL:SPEC:47.1.2
     * @test_Strategy: Validate when the operator is '+=' that the operands are
     * coerced to Strings and the result is a Concatenation.
     * <p>
     * Equations tested: Double += Double Double += String
     * containing ".", "e", or "E" Double += BigInteger Double +=
     * Integer Double += Long Double += Short Double += Byte
     */
    @Test
    public void elDoubleConcatenationTest() {

        Double testValue = Double.valueOf(2.5);
        /*
         * The expected result is actually computed in the testDouble method for the
         * '+=' operator!
         */
        Validator.testDouble(testValue, null, "+=");

    }

    /**
     * @testName: elLongConcatenationTest
     * @assertion_ids: EL:SPEC:38; EL:SPEC:47.1; EL:SPEC:47.1.1; EL:SPEC:47.1.2
     * @test_Strategy: Validate when the operator is '+=' that the operands are
     * coerced to Strings and the result is a Concatenation.
     * <p>
     * Equations tested: Long += Integer Long += Long Long +=
     * Short Long += Byte
     */
    @Test
    public void elLongConcatenationTest() {

        Long testValue = Long.valueOf(25000);
        /*
         * The expected result is actually computed in the testLong method for the
         * '+=' operator!
         */
        Validator.testLong(testValue, null, "+=");

    }

    /**
     * @testName: elIntegerConcatenationTest
     * @assertion_ids: EL:SPEC:38; EL:SPEC:47.1; EL:SPEC:47.1.1; EL:SPEC:47.1.2
     * @test_Strategy: Validate when the operator is '+=' that the operands are
     * coerced to Strings and the result is a Concatenation.
     * <p>
     * Equations tested: Integer += Integer Integer += Short
     * Integer += Byte
     */
    @Test
    public void elIntegerConcatenationTest() {

        Integer testValue = Integer.valueOf(25);
        /*
         * The expected result is actually computed in the testInteger method for
         * the '+=' operator!
         */
        Validator.testInteger(testValue, null, "+=");

    }

    /**
     * @testName: elShortConcatenationTest
     * @assertion_ids: EL:SPEC:38; EL:SPEC:47.1; EL:SPEC:47.1.1; EL:SPEC:47.1.2
     * @test_Strategy: Validate when the operator is '+=' that the operands are
     * coerced to Strings and the result is a Concatenation.
     * <p>
     * Equations tested: Short += Short Short += Byte
     */
    @Test
    public void elShortConcatenationTest() {

        Short testValue = Short.valueOf("2");
        /*
         * The expected result is actually computed in the testShort method for the
         * '+=' operator!
         */
        Validator.testShort(testValue, null, "+=");

    }

    /**
     * @testName: elByteConcatenationTest
     * @assertion_ids: EL:SPEC:38; EL:SPEC:47.1; EL:SPEC:47.1.1; EL:SPEC:47.1.2
     * @test_Strategy: Validate that if the operator is '+=' that both operands
     * are coerced to String and the result is a Concatenation of
     * the operands.
     * <p>
     * Equations tested: Byte += Byte
     */
    @Test
    public void elByteConcatenationTest() {

        Byte testValue = Byte.valueOf("2");
        /*
         * The expected result is actually computed in the testByte method for the
         * '+=' operator!
         */
        Validator.testByte(testValue, null, "+=");

    }

    /**
     * @testName: elBooleanConcatenationTest
     * @assertion_ids: EL:SPEC:38; EL:SPEC:47.1; EL:SPEC:47.1.1; EL:SPEC:47.1.2
     * @test_Strategy: Validate when the operator is '+=' that both operands are
     * coerced to Strings and that they result is a Concatenation
     * of the operands.
     * <p>
     * Equations tested: Boolean += String Boolean += Boolean
     */
    @Test
    public void elBooleanConcatenationTest() {

        /*
         * The expected result is actually computed in the testBoolean method for
         * the '+=' operator!
         */
        Validator.testBoolean(false, "true", null, "+=");
        Validator.testBoolean(false, true, null, "+=");

    }

    // ------------------------------------------------------- private methods
    private void logLine(String s) {
        TestUtil.logTrace(s);
    }

    /**
     * Test a query for the correct value.
     *
     * @param name     The Name of the test
     * @param query    The EL query string
     * @param expected The expected result of the query.
     */
    private void testQuery(String name, String query, String expected) {
        ELProcessor elp = new ELProcessor();

        logLine("=== Testing " + name + " ===");
        logLine(query);

        logLine(" = returns =");
        Object ret = elp.eval(query);

        if (!expected.equals(ret.toString())) {

            throw new RuntimeException(
                    ELTestUtil.FAIL + "  Unexpected Value!" + ELTestUtil.NL + "Expected: "
                            + expected + ELTestUtil.NL + "Received: " + ret);

        }
    }

}
