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



package org.apache.james.smtpserver.core.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.james.dsn.DSNStatus;
import org.apache.james.smtpserver.CommandHandler;
import org.apache.james.smtpserver.SMTPSession;

/**
  * Handles EHLO command
  */
public class EhloFilterCmdHandler extends AbstractLogEnabled implements CommandHandler {

    /**
     * The name of the command handled by the command handler
     */
    private final static String COMMAND_NAME = "EHLO";

    /**
     * processes EHLO command
     *
     * @see org.apache.james.smtpserver.CommandHandler#onCommand(SMTPSession)
    **/
    public void onCommand(SMTPSession session) {
        doEHLO(session, session.getCommandArgument());
    }

    /**
     * @param session SMTP session object
     * @param argument the argument passed in with the command by the SMTP client
     */
    private void doEHLO(SMTPSession session, String argument) {
        String responseString = null;        
     
        session.resetState();
        
        if (argument == null) {
            responseString = "501 "+DSNStatus.getStatus(DSNStatus.PERMANENT,DSNStatus.DELIVERY_INVALID_ARG)+" Domain address required: " + COMMAND_NAME;
            session.writeResponse(responseString);
            
            // After this filter match we should not call any other handler!
            session.setStopHandlerProcessing(true);
        } else {
            // store provided name
            session.getState().put(SMTPSession.CURRENT_HELO_NAME,argument);
        }
    }
    
    /**
     * @see org.apache.james.smtpserver.CommandHandler#getImplCommands()
     */
    public Collection getImplCommands() {
        Collection implCommands = new ArrayList();
        implCommands.add("EHLO");
        
        return implCommands;
    }

}