/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates and others.
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

package com.sun.ts.tests.el.api.jakarta_el.functionmapper;

import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.el.common.elcontext.FuncMapperELContext;
import jakarta.el.FunctionMapper;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class ELClient {
    /**
     * @testName: functionMapperTest
     * @assertion_ids: EL:JAVADOC:35; EL:JAVADOC:67
     * @test_Strategy: Validate the behavior of ELContext.getFunctionMapper()
     * FunctionMapper.resolveFunction()
     */

    @Test
    public void functionMapperTest() {

        String expected = "public static java.lang.Integer "
                + "java.lang.Integer.valueOf"
                + "(java.lang.String) throws java.lang.NumberFormatException";

        FuncMapperELContext context = new FuncMapperELContext();
        FunctionMapper funcMapper = context.getFunctionMapper();
        TestUtil.logTrace("FunctionMapper is " + funcMapper.toString());

        if (funcMapper.resolveFunction("foo", "bar") != null) {
            TestUtil.logErr("Expected call to resolveFunction() to unassigned "
                    + "function to return null" + TestUtil.NEW_LINE
                    + "Instead call returned: "
                    + funcMapper.resolveFunction("foo", "bar").getName()
                    + TestUtil.NEW_LINE);

            throw new RuntimeException("Resolved unassigned function");
        }

        Method method = funcMapper.resolveFunction("Int", "val");
        if (method == null) {
            TestUtil.logErr("Expected call to resolveFunction() to resolvable "
                    + "function to return a non-null value" + TestUtil.NEW_LINE);

            throw new RuntimeException("Incorrect resolution: null method");
        } else {
            String methodSignature = method.toString();
            if (!methodSignature.equals(expected)) {
                TestUtil.logErr("Method Signature of resolved function is " + "invalid"
                        + TestUtil.NEW_LINE + "Expected value:" + expected
                        + TestUtil.NEW_LINE);

                throw new RuntimeException("Incorrect resolution: wrong method Signature");
            }
        }
    }
}
