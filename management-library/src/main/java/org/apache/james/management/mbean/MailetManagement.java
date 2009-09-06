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

package org.apache.james.management.mbean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.mailet.MailetConfig;

public class MailetManagement implements MailetManagementMBean {
    private MailetConfig mailetConfig;

    public MailetManagement(MailetConfig mailetConfig) {
        this.mailetConfig = mailetConfig;
    }

    public String getMailetName() {
        return mailetConfig.getMailetName();
    }
    
    public String[] getMailetParameters() {
        return getMailetParameters(mailetConfig);
    }

    public static String[] getMailetParameters(MailetConfig mailetConfig) {
        List parameterList = new ArrayList();
        Iterator iterator = mailetConfig.getInitParameterNames();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            String value = mailetConfig.getInitParameter(name);
            parameterList.add(name + "=" + value);
        }
        String[] result = (String[]) parameterList.toArray(new String[] {});
        return result;
    }
}