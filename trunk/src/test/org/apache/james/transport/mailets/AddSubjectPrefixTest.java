/***********************************************************************
 * Copyright (c) 2006 The Apache Software Foundation.                  *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/

package org.apache.james.transport.mailets;

import junit.framework.TestCase;
import org.apache.james.test.mock.mailet.MockMailContext;
import org.apache.james.test.mock.mailet.MockMailetConfig;
import org.apache.james.test.util.Util;
import org.apache.mailet.Mail;
import org.apache.mailet.Mailet;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;
import java.io.UnsupportedEncodingException;

public class AddSubjectPrefixTest extends TestCase {

    private MimeMessage mockedMimeMessage;

    private Mail mockedMail;

    private Mailet mailet;

    private final String SUBJECT_PREFIX = "JUNIT";

    private String subject = null;

    public AddSubjectPrefixTest(String arg0)
            throws UnsupportedEncodingException {
        super(arg0);
    }

    private void setSubject(String subject) {
        this.subject = subject;
    }

    private void setupMockedMimeMessage() throws MessagingException {
        mockedMimeMessage = Util.createMimeMessageWithSubject(subject);
    }

    private void setupMockedMail(MimeMessage m) throws ParseException {
        mockedMail = Util.createMockMail2Recipients(m);
    }

    private void setupMailet() throws MessagingException {
        setupMockedMimeMessage();
        mailet = new AddSubjectPrefix();
        MockMailetConfig mci = new MockMailetConfig("Test", new MockMailContext());
        mci.setProperty("subjectPrefix", SUBJECT_PREFIX);

        mailet.init(mci);
    }

    private void setupInvalidMailet() throws MessagingException {
        setupMockedMimeMessage();
        mailet = new AddSubjectPrefix();
        MockMailetConfig mci = new MockMailetConfig("Test", new MockMailContext());
        mci.setProperty("subjectPrefix", "");

        mailet.init(mci);
    }

    // test if prefix was added
    public void testSubjectPrefixWasAdded() throws MessagingException {
        setSubject("test");
        setupMockedMimeMessage();
        setupMockedMail(mockedMimeMessage);
        setupMailet();

        mailet.service(mockedMail);

        assertEquals(SUBJECT_PREFIX + " " + subject, mockedMail.getMessage().getSubject());

    }

    // test if prefix was added to message without subject
    public void testSubjectPrefixWasAddedWithoutSubject()
            throws MessagingException {
        setupMockedMimeMessage();
        setupMockedMail(mockedMimeMessage);
        setupMailet();

        mailet.service(mockedMail);

        assertEquals(SUBJECT_PREFIX, mockedMail.getMessage().getSubject());

    }

    // test if exception was thrown cause missing configure value
    public void testThrowException() throws MessagingException {
        boolean exceptionThrown = false;
        setupMockedMimeMessage();
        setupMockedMail(mockedMimeMessage);

        try {
            setupInvalidMailet();
        } catch (MessagingException m) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

    }
}
