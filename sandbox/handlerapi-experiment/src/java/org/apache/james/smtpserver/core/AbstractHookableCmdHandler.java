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



package org.apache.james.smtpserver.core;

import java.util.List;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.james.smtpserver.CommandHandler;
import org.apache.james.smtpserver.ExtensibleHandler;
import org.apache.james.smtpserver.SMTPResponse;
import org.apache.james.smtpserver.SMTPSession;
import org.apache.james.smtpserver.hook.EhloHook;
import org.apache.james.smtpserver.hook.HeloHook;
import org.apache.james.smtpserver.hook.HookResult;
import org.apache.james.smtpserver.hook.HookReturnCode;
import org.apache.james.smtpserver.hook.RcptHook;
import org.apache.james.util.mail.SMTPRetCode;
import org.apache.mailet.MailAddress;

/**
  * Abstract class which Handle hooks. 
  * 
  * TODO: Maybe we should take care of relaying etc here ?
  */
public abstract class AbstractHookableCmdHandler extends AbstractLogEnabled implements
        CommandHandler, ExtensibleHandler {
    
    /**
     * Handle command processing
     *
     * @see org.apache.james.smtpserver.CommandHandler#onCommand(org.apache.james.smtpserver.SMTPSession, java.lang.String, java.lang.String) 
    **/
    public SMTPResponse onCommand(SMTPSession session, String command, String parameters) {
        SMTPResponse response = doFilterChecks(session,command,parameters);
    
        if (response == null) {
            
            response = processHooks(session, command, parameters);
            if (response == null) {
                return doCoreCmd(session, command, parameters);
            } else {
                return response;
            }
        } else {
            return response;
        }

    }

    /**
     * Process all hooks for the given command
     * 
     * @param session the SMTPSession object
     * @param command the command
     * @param parameters the paramaters 
     * @return SMTPResponse
     */
    private SMTPResponse processHooks(SMTPSession session,String command,String parameters) {
        List hooks = getHooks();
        
    if(hooks != null) {
            getLogger().debug("executing  hooks");
            int count = hooks.size();
            for(int i =0; i < count; i++) {
                Object rawHook = hooks.get(i);
                HookResult result = null;
                
                if ("HELO".equals(command) && rawHook instanceof HeloHook) {
                    result = ((HeloHook) rawHook).doHelo(session, parameters);    
                }
                
                if ("EHLO".equals(command) && rawHook instanceof EhloHook) {
                    result = ((EhloHook) rawHook).doEhlo(session, parameters);    
                }
                
                if ("RCPT".equals(command) && rawHook instanceof RcptHook) {
                    result = ((RcptHook) rawHook).doRcpt(session, (MailAddress) session.getState().get(SMTPSession.SENDER), (MailAddress) session.getState().get(SMTPSession.CURRENT_RECIPIENT));
                }
                
                //TODO: Add more hooks
                
                if (result != null) {
                    int rCode = result.getResult();
                    String smtpRetCode = result.getSmtpRetCode();
                    String smtpDesc = result.getSmtpDescription();

                    if (rCode == HookReturnCode.DENY) {
                        if (smtpRetCode == null) smtpRetCode = SMTPRetCode.TRANSACTION_FAILED;
                        if (smtpDesc == null) smtpDesc = "Email rejected";
                        
                        return new SMTPResponse(smtpRetCode, smtpDesc);
                    }else if (rCode == HookReturnCode.DENYSOFT) {
                        return new SMTPResponse(SMTPRetCode.LOCAL_ERROR,"Temporary problem. Please try again later");
                    } else if (rCode == HookReturnCode.OK) {
                    return new SMTPResponse(SMTPRetCode.MAIL_OK,"Accepted.");
                    }
                }
            }
        }
        return null;
    }
    

    /**
     * Execute Syntax checks and return a SMTPResponse if a syntax error was detected, otherwise null.
     * 
     * @param session 
     * @param command
     * @param parameters
     * @return
     */
    protected abstract SMTPResponse doFilterChecks(SMTPSession session, String command, String parameters);
    
    /**
     * Execute the core commandHandling.
     * 
     * @param session
     * @param command
     * @param parameters
     * @return
     */
    protected abstract SMTPResponse doCoreCmd(SMTPSession session , String command, String parameters);
    
    /**
     * Return a list which holds all hooks for the cmdHandler
     * 
     * @return
     */
    protected abstract List getHooks(); 
}
