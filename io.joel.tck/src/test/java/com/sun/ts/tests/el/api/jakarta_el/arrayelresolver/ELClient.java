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

package com.sun.ts.tests.el.api.jakarta_el.arrayelresolver;


import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.common.el.api.resolver.ResolverTest;
import com.sun.ts.tests.el.common.elcontext.BareBonesELContext;
import com.sun.ts.tests.el.common.util.ELTestUtil;
import jakarta.el.ArrayELResolver;
import jakarta.el.ELContext;

import org.junit.jupiter.api.Test;

public class ELClient {

    private static final String[] names = {"doug", "nick", "roger", "ryan",
            "ed"};

    /**
     * @testName: arrayELResolverTest
     * @assertion_ids: EL:JAVADOC:1; EL:JAVADOC:3; EL:JAVADOC:4; EL:JAVADOC:5;
     * EL:JAVADOC:6; EL:JAVADOC:7; EL:JAVADOC:8
     * @test_Strategy: Verify that API calls work as expected: arrayELResolver()
     * getValue() getType() setValue() isReadOnly()
     * getCommonPropertyType() getFeatureDescriptors()
     */
    @Test
    public void arrayELResolverTest() {

        boolean pass;
        StringBuffer buf = new StringBuffer();
        String[] colors = {"red", "blue", "green"};

        try {
            ArrayELResolver arrayResolver = new ArrayELResolver();
            BareBonesELContext barebonesContext = new BareBonesELContext();
            ELContext context = barebonesContext.getELContext();

            pass = ResolverTest.testELResolver(context, arrayResolver, colors,
                    Integer.valueOf(1), "yellow", buf, false);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass) {
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        }
        TestUtil.logTrace(buf.toString());
    }

    /**
     * @testName: arrayELResolverNPETest
     * @assertion_ids: EL:JAVADOC:1; EL:JAVADOC:5; EL:JAVADOC:6; EL:JAVADOC:7;
     * EL:JAVADOC:8; EL:JAVADOC:122; EL:JAVADOC:125;
     * EL:JAVADOC:128; EL:JAVADOC:131
     * @test_Strategy: Verify that the following methods throw a
     * NullPointerException, if context is null:
     * <p>
     * getValue() getType() setValue() isReadOnly()
     */
    @Test
    public void arrayELResolverNPETest() {

        boolean pass;
        StringBuffer buf = new StringBuffer();

        ArrayELResolver resolver = new ArrayELResolver();

        try {
            pass = ResolverTest.testELResolverNPE(resolver, names, Integer.valueOf(1),
                    "billy", buf);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass) {
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        }
        TestUtil.logTrace(buf.toString());
    }

    /**
     * @testName: arrayELResolverPNFETest
     * @assertion_ids: EL:JAVADOC:1; EL:JAVADOC:5; EL:JAVADOC:7; EL:JAVADOC:8;
     * EL:JAVADOC:121; EL:JAVADOC:127; EL:JAVADOC:134
     * @test_Strategy: Verify that the following methods throw a
     * PropertyNotFoundException, if the given index is out of
     * bounds for this array :
     * <p>
     * getType() isReadOnly() setValue()
     */
    @Test
    public void arrayELResolverPNFETest() {

        boolean pass;
        StringBuffer buf = new StringBuffer();

        ArrayELResolver resolver = new ArrayELResolver();
        BareBonesELContext barebonesContext = new BareBonesELContext();
        ELContext context = barebonesContext.getELContext();

        try {
            pass = ResolverTest.testELResolverPNFE(context, resolver, names,
                    Integer.valueOf(10), buf);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass) {
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        }
        TestUtil.logTrace(buf.toString());
    }

    /**
     * @testName: arrayELResolverIAETest
     * @assertion_ids: EL:JAVADOC:1; EL:JAVADOC:6; EL:JAVADOC:8; EL:JAVADOC:124;
     * EL:JAVADOC:132
     * @test_Strategy: Verify that the following methods throw an
     * IllegalArgumentException, if the property could not be
     * coerced into an integer:
     * <p>
     * getValue() setValue()
     */
    @Test
    public void arrayELResolverIAETest() {

        boolean pass;
        StringBuffer buf = new StringBuffer();

        ArrayELResolver resolver = new ArrayELResolver();
        BareBonesELContext barebonesContext = new BareBonesELContext();
        ELContext context = barebonesContext.getELContext();

        try {
            pass = ResolverTest.testELResolverIAE(context, resolver, names, "GARBAGE",
                    "billy", buf);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass) {
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        }
        TestUtil.logTrace(buf.toString());
    }

    /**
     * @testName: arrayELResolverPNWETest
     * @assertion_ids: EL:JAVADOC:2; EL:JAVADOC:8; EL:JAVADOC:127; EL:JAVADOC:133
     * @test_Strategy: Verify that the single-parameter constructor for
     * arrayELResolver can be used to construct a read-only
     * resolver, and that the setValue() method throws a
     * PropertyNotWritableException, if the resolver was
     * constructed in read-only mode.
     * <p>
     * ArrayELResolver(boolean) setValue()
     */
    @Test
    public void arrayELResolverPNWETest() {

        boolean pass;
        StringBuffer buf = new StringBuffer();

        ArrayELResolver resolver = new ArrayELResolver(true);
        BareBonesELContext barebonesContext = new BareBonesELContext();
        ELContext context = barebonesContext.getELContext();

        try {
            pass = ResolverTest.testELResolverPNWE(context, resolver, names,
                    Integer.valueOf(1), "billy", buf);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass) {
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        }
        TestUtil.logTrace(buf.toString());
    }

    /*
     * @testName: arrayELResolverOBETest
     *
     * @assertion_ids: EL:JAVADOC:1; EL:JAVADOC:6
     *
     * @test_Strategy: Verify that if the index is out of bounds, null is
     * returned.
     *
     * getValue()
     */
    @Test
    public void arrayELResolverOBETest() {

        boolean pass = true;

        StringBuffer buf = new StringBuffer();

        ArrayELResolver resolver = new ArrayELResolver();
        BareBonesELContext barebonesContext = new BareBonesELContext();
        ELContext context = barebonesContext.getELContext();

        try {
            Object value = resolver.getValue(context, names, 5);

            if (value != null) {
                pass = false;
                buf.append("Expected Value: 'null'").append(TestUtil.NEW_LINE)
                        .append("Received Value: ").append(value);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass) {
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        }
    }

    /*
     * @testName: arrayELResolverCCETest
     *
     * @assertion_ids: EL:JAVADOC:1; EL:JAVADOC:8; EL:JAVADOC:130
     *
     * @test_Strategy: Verify that if the class of a specified object prevents it
     * from being added to the array, a ClassCastException is thrown.
     *
     * setValue()
     */
    @Test
    public void arrayELResolverCCETest() {

        boolean pass = false;

        ArrayELResolver resolver = new ArrayELResolver();
        BareBonesELContext barebonesContext = new BareBonesELContext();
        ELContext context = barebonesContext.getELContext();

        try {
            resolver.setValue(context, names, Integer.valueOf(1), Boolean.TRUE);

        } catch (ClassCastException cce) {
            pass = true;

        } catch (Exception e) {
            TestUtil.logErr(
                    "Failed: Exception thrown but was not a " + "ClassCastException");
            throw new RuntimeException(e);
        }

        if (!pass) {
            throw new RuntimeException("Failed: No exception thrown.");
        }
    }
}
