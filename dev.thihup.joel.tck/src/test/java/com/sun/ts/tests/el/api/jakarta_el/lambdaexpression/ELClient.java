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

package com.sun.ts.tests.el.api.jakarta_el.lambdaexpression;

import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.el.common.util.ELTestUtil;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.LambdaExpression;
import jakarta.el.StandardELContext;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ELClient {

    @Test
    public void invokeNPETest() {
        List<String> params = new ArrayList<String>();
        params.add("one");
        LambdaExpression le = new LambdaExpression(params, null);

        ELTestUtil.checkForNPE(le, "invoke",
                new Class<?>[]{ELContext.class, Object[].class},
                new Object[]{null, new Object[]{"one"}});
    }// End invokeNPETest

    /**
     * @testName: invokeELETest
     * @assertion_ids: EL:JAVADOC:408
     * @test_Strategy: Throws a ELException if not enough arguments are provided.
     */

    @Test
    public void invokeELETest() {
        StringBuffer buff = new StringBuffer();
        boolean pass = false;
        List<String> params = new ArrayList<String>();
        params.add("one");

        LambdaExpression le = new LambdaExpression(params, null);
        try {
            le.invoke(new StandardELContext(ExpressionFactory.newInstance())
            );
            buff.append(ELTestUtil.FAIL + " No Exception thrown!" + ELTestUtil.NL
                    + "Expected an ELException to be thrown!");
        } catch (ELException ele) {
            TestUtil.logMsg(ELTestUtil.PASS);
            pass = true;
        } catch (Exception e) {
            TestUtil.logMsg(ELTestUtil.FAIL + ELTestUtil.NL + "Expected: ELException"
                    + ELTestUtil.NL + "Received: " + e.getClass().getSimpleName());
        }

        if (!pass)
            throw new RuntimeException(ELTestUtil.FAIL);
    }// End invokeELETest

}
