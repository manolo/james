/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache", "Jakarta", "JAMES" and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */

package org.apache.james.imapserver;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests commands which are valid in AUTHENTICATED and NONAUTHENTICATED by running
 * them in the SELECTED state. Many commands function identically, while others
 * are invalid in this state.
 *
 * @author  Darrell DeBoer <darrell@apache.org>
 *
 * @version $Revision: 1.4 $
 */
public class TestOtherCommandsInSelectedState
        extends TestCommandsInAuthenticatedState
{
    public TestOtherCommandsInSelectedState( String name )
    {
        super( name );
    }

    /**
     * Superclass sets up welcome message and login session in {@link #preElements}.
     * A "SELECT INBOX" session is then added to these elements.
     * @throws Exception
     */
    public void setUp() throws Exception
    {
        super.setUp();
        addTestFile( "SelectInbox.test", preElements );
    }

    protected void addCloseInbox()
    {
        postElements.CL( "a CLOSE");
        postElements.SL( ".*", "TestOtherCommandsInSelectedState.java:96");
    }
    
    /**
     * Provides all tests which should be run in the selected state. Each test name
     * corresponds to a protocol session file.
     */
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        // Not valid in this state
        suite.addTest( new TestOtherCommandsInSelectedState( "ValidNonAuthenticated" ) );

        // Valid in all states
        suite.addTest( new TestOtherCommandsInSelectedState( "Capability" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "Noop" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "Logout" ) );

        // Valid in authenticated state
        suite.addTest( new TestOtherCommandsInSelectedState( "Create" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "ExamineEmpty" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "SelectEmpty" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "ListNamespace" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "ListMailboxes" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "Status" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "StringArgs" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "Subscribe" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "Append" ) );
        suite.addTest( new TestOtherCommandsInSelectedState( "Delete" ) );

        return suite;
    }
}
