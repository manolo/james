/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.transport.camel;

import java.util.Locale;

import javax.mail.MessagingException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.james.transport.MailetConfigImpl;
import org.apache.james.transport.ProcessorUtil;
import org.apache.mailet.Mail;
import org.apache.mailet.Mailet;
import org.apache.mailet.MailetConfig;

/**
 * Mailet wrapper which execute a Mailet in a Processor
 *
 */
public class MailetProcessor implements Processor{

    private Mailet mailet;
    private Log logger;
   
    /**
     * Mailet to call on process
     * 
     * @param mailet
     */
    public MailetProcessor(Mailet mailet, Log logger) {
        this.mailet = mailet;
        this.logger = logger;
    }
    
    /**
     * Call the wrapped mailet for the exchange
     */
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) throws Exception {
        Mail mail = (Mail) exchange.getIn().getBody();
        try {
            mailet.service(mail);
        } catch (MessagingException me) {
            String onMailetException = null;
            
            MailetConfig mailetConfig = mailet.getMailetConfig();
            if (mailetConfig instanceof MailetConfigImpl) {
                onMailetException = ((MailetConfigImpl) mailetConfig).getInitAttribute("onMailetException");
            }
            if (onMailetException == null) {
                onMailetException = Mail.ERROR;
            } else {
                onMailetException = onMailetException.trim().toLowerCase(Locale.US);
            }
            if (onMailetException.compareTo("ignore") == 0) {
                // ignore the exception and continue
                // this option should not be used if the mail object can be
                // changed by the mailet
                ProcessorUtil.verifyMailAddresses(mail.getRecipients());
            } else {
                ProcessorUtil.handleException(me, mail, mailet.getMailetConfig().getMailetName(), onMailetException, logger);
            }
        }
    }

}