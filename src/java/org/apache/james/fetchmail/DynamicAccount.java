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

package org.apache.james.fetchmail;

import org.apache.avalon.framework.configuration.ConfigurationException;

public class DynamicAccount extends Account
{

    /**
     * Constructor for DynamicAccount.
     * @param sequenceNumber
     * @param parsedConfiguration
     * @param user
     * @param password
     * @param recipient
     * @param ignoreRecipientHeader
     * @throws ConfigurationException
     */
    private DynamicAccount(
        int sequenceNumber,
        ParsedConfiguration parsedConfiguration,        
        String user,
        String password,
        String recipient,
        boolean ignoreRecipientHeader)
        throws ConfigurationException
    {
        super(sequenceNumber, parsedConfiguration, user, password, recipient, ignoreRecipientHeader);
    }

    /**
     * Constructor for DynamicAccount.
     * @param sequenceNumber
     * @param parsedConfiguration
     * @param userName
     * @param userPrefix 
     * @param userSuffix
     * @param password
     * @param recipientPrefix 
     * @param recipientSuffix  
     * @param ignoreRecipientHeader
     * @throws ConfigurationException
     */
    public DynamicAccount(
        int sequenceNumber,
        ParsedConfiguration parsedConfiguration,         
        String userName,
        String userPrefix,
        String userSuffix,
        String password,        
        String recipientPrefix,
        String recipientSuffix,
        boolean ignoreRecipientHeader)
        throws ConfigurationException
    {
        this(sequenceNumber, parsedConfiguration, null, password, null, ignoreRecipientHeader);

        StringBuffer userBuffer = new StringBuffer(userPrefix);
        userBuffer.append(userName);
        userBuffer.append(userSuffix);
        setUser(userBuffer.toString());

        StringBuffer recipientBuffer = new StringBuffer(recipientPrefix);
        recipientBuffer.append(userName);
        recipientBuffer.append(recipientSuffix);
        setRecipient(recipientBuffer.toString());
    }
}
