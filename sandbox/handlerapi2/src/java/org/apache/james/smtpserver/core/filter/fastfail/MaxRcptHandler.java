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



package org.apache.james.smtpserver.core.filter.fastfail;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.james.smtpserver.CommandHandler;
import org.apache.james.smtpserver.SMTPSession;
import org.apache.james.util.mail.dsn.DSNStatus;

public class MaxRcptHandler extends AbstractLogEnabled implements
        CommandHandler, Configurable {

    private int maxRcpt = 0;

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration handlerConfiguration)
            throws ConfigurationException {
        Configuration configuration = handlerConfiguration.getChild("maxRcpt",
                false);
        if (configuration != null) {
            setMaxRcpt(configuration.getValueAsInteger(0));
        } else {
            throw new ConfigurationException(
                    "Please set the maxRcpt configuration value");
        }
    }

    /**
     * Set the max rcpt for wich should be accepted
     * 
     * @param maxRcpt
     *            The max rcpt count
     */
    public void setMaxRcpt(int maxRcpt) {
        this.maxRcpt = maxRcpt;
    }

    /**
     * @see org.apache.james.smtpserver.CommandHandler#onCommand(SMTPSession)
     */
    public void onCommand(SMTPSession session) {
    String response = doRCPT(session);

    if (response == null) {
        // call the next handler in chain
        session.doChain();

    } else {
        // store the response
        session.getSMTPResponse().setRawSMTPResponse(response);
    }
    }

    private String doRCPT(SMTPSession session) {
    String responseString = null;
    int rcptCount = 0;

    rcptCount = session.getRcptCount() + 1;

    // check if the max recipients has reached
    if (rcptCount > maxRcpt) {
        responseString = "452 "
            + DSNStatus.getStatus(DSNStatus.NETWORK,
                DSNStatus.DELIVERY_TOO_MANY_REC)
            + " Requested action not taken: max recipients reached";

        getLogger().error(responseString);
    }

    return responseString;
    }

    /**
     * @see org.apache.james.smtpserver.CommandHandler#getImplCommands()
     */
    public Collection getImplCommands() {
    Collection implCommands = new ArrayList();
    implCommands.add("RCPT");

    return implCommands;
    }

}
