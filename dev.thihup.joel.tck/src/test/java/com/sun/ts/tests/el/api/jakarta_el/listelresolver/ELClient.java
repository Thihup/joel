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

package com.sun.ts.tests.el.api.jakarta_el.listelresolver;

import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.common.el.api.resolver.ResolverTest;
import com.sun.ts.tests.el.common.elcontext.BareBonesELContext;
import com.sun.ts.tests.el.common.util.ELTestUtil;
import jakarta.el.ELContext;
import jakarta.el.ListELResolver;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ELClient {

    private final List<String> names;

    public ELClient() {
        names = new ArrayList<String>();
        names.add("doug");
        names.add("nick");
        names.add("ryan");
    }

    /**
     * @testName: listELResolverTest
     * @assertion_ids: EL:JAVADOC:68; EL:JAVADOC:69; EL:JAVADOC:70; EL:JAVADOC:71;
     * EL:JAVADOC:72; EL:JAVADOC:73; EL:JAVADOC:75
     * @test_Strategy: Verify that API calls work as expected: ListELResolver()
     * getValue() getType() setValue() isReadOnly()
     * getCommonPropertyType() getFeatureDescriptors()
     */

    @Test
    public void listELResolverTest() {

        boolean pass = false;
        StringBuffer buf = new StringBuffer();

        try {
            ListELResolver listResolver = new ListELResolver();
            BareBonesELContext barebonesContext = new BareBonesELContext();
            ELContext context = barebonesContext.getELContext();

            pass = ResolverTest.testELResolver(context, listResolver, names,
                    Integer.valueOf(1), "ed", buf, false);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass)
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        TestUtil.logTrace(buf.toString());
    }

    /**
     * @testName: listELResolverNPETest
     * @assertion_ids: EL:JAVADOC:70; EL:JAVADOC:71; EL:JAVADOC:72; EL:JAVADOC:73;
     * EL:JAVADOC:75; EL:JAVADOC:276; EL:JAVADOC:279;
     * EL:JAVADOC:282; EL:JAVADOC:285
     * @test_Strategy: Verify that the following methods throw a
     * NullPointerException, if context is null:
     * <p>
     * getType() getValue() isReadOnly() setValue()
     */

    @Test
    public void listELResolverNPETest() {

        boolean pass = false;
        StringBuffer buf = new StringBuffer();
        ListELResolver resolver = new ListELResolver();

        try {
            pass = ResolverTest.testELResolverNPE(resolver, names, Integer.valueOf(1),
                    "billy", buf);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass)
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        TestUtil.logTrace(buf.toString());
    }

    /**
     * @testName: listELResolverPNFETest
     * @assertion_ids: EL:JAVADOC:70; EL:JAVADOC:72; EL:JAVADOC:73; EL:JAVADOC:75;
     * EL:JAVADOC:275; EL:JAVADOC:281; EL:JAVADOC:288
     * @test_Strategy: Verify that the following methods throw a
     * PropertyNotFoundException, if the given index is out of
     * bounds for this list:
     * <p>
     * getType() isReadOnly() setValue()
     */
    @Test
    public void listELResolverPNFETest() {

        boolean pass = false;
        StringBuffer buf = new StringBuffer();

        ListELResolver resolver = new ListELResolver();
        BareBonesELContext barebonesComtext = new BareBonesELContext();
        ELContext context = barebonesComtext.getELContext();

        try {
            pass = ResolverTest.testELResolverPNFE(context, resolver, names,
                    Integer.valueOf(10), buf);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass)
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        TestUtil.logTrace(buf.toString());
    }

    /**
     * @testName: listELResolverIAETest
     * @assertion_ids: EL:JAVADOC:71; EL:JAVADOC:73; EL:JAVADOC:75;
     * EL:JAVADOC:278; EL:JAVADOC:286
     * @test_Strategy: Verify that the following methods throw an
     * IllegalArgumentException, if the property could not be
     * coerced into an integer:
     * <p>
     * getValue() setValue()
     */
    @Test
    public void listELResolverIAETest() {

        boolean pass = false;
        StringBuffer buf = new StringBuffer();

        ListELResolver resolver = new ListELResolver();
        BareBonesELContext barebonesComtext = new BareBonesELContext();
        ELContext context = barebonesComtext.getELContext();

        try {
            pass = ResolverTest.testELResolverIAE(context, resolver, names, "GARBAGE",
                    "billy", buf);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass)
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        TestUtil.logTrace(buf.toString());
    }

    /**
     * @testName: listELResolverPNWETest
     * @assertion_ids: EL:JAVADOC:74; EL:JAVADOC:75; EL:JAVADOC:287
     * @test_Strategy: Verify that the single-parameter constructor for
     * listELResolver can be used to construct a read-only
     * resolver, and that the setValue() method throws a
     * PropertyNotWritableException, if the resolver was
     * constructed in read-only mode.
     * <p>
     * ListELResolver(boolean) setValue()
     */
    @Test
    public void listELResolverPNWETest() {

        boolean pass = false;
        StringBuffer buf = new StringBuffer();

        ListELResolver resolver = new ListELResolver(true);
        BareBonesELContext barebonesComtext = new BareBonesELContext();
        ELContext context = barebonesComtext.getELContext();

        try {
            pass = ResolverTest.testELResolverPNWE(context, resolver, names,
                    Integer.valueOf(1), "billy", buf);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!pass)
            throw new RuntimeException(ELTestUtil.FAIL + buf);
        TestUtil.logTrace(buf.toString());
    }

}
