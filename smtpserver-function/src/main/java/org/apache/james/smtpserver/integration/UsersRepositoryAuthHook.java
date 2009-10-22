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
package org.apache.james.smtpserver.integration;

import javax.annotation.Resource;

import org.apache.james.api.user.UsersRepository;
import org.apache.james.smtpserver.protocol.SMTPSession;
import org.apache.james.smtpserver.protocol.hook.AuthHook;
import org.apache.james.smtpserver.protocol.hook.HookResult;
import org.apache.james.smtpserver.protocol.hook.HookReturnCode;

/**
 * This Auth hook can be used to authenticate against the james user repository
 */
public class UsersRepositoryAuthHook implements AuthHook {
    
    private UsersRepository users;
    
    /**
     * Gets the users repository.
     * @return the users
     */
    public final UsersRepository getUsers() {
        return users;
    }

    /**
     * Sets the users repository.
     * @param users the users to set
     */
    @Resource(name="org.apache.james.api.user.UsersRepository")
    public final void setUsers(UsersRepository users) {
        this.users = users;
    }


    /**
     * @see org.apache.james.smtpserver.protocol.hook.AuthHook#doAuth(org.apache.james.smtpserver.protocol.SMTPSession, java.lang.String, java.lang.String)
     */
    public HookResult doAuth(SMTPSession session, String username, String password) {
        if (users.test(username, password)) {
            session.setUser(username);
            session.setRelayingAllowed(true);
            return new HookResult(HookReturnCode.OK, "Authentication Successful");
        }
        return new HookResult(HookReturnCode.DECLINED);
    }
}
