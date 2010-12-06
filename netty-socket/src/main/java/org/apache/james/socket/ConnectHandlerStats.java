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
package org.apache.james.socket;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.james.lifecycle.Disposable;

public class ConnectHandlerStats extends StandardMBean implements HandlerStatsMBean, Disposable{

    private String name;
    private String handlerName;
    private MBeanServer mbeanserver;
    private AtomicLong disconnect = new AtomicLong(0);
    private AtomicLong all = new AtomicLong(0);

    public ConnectHandlerStats(String jmxName, String handlerName) throws NotCompliantMBeanException, MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException {
        super(HandlerStatsMBean.class);
        this.handlerName = handlerName;
        
        this.name = "org.apache.james:type=server,name=" + jmxName + ",handler=connecthandler,connecthandler=" + handlerName;
        mbeanserver = ManagementFactory.getPlatformMBeanServer();
        ObjectName baseObjectName = new ObjectName(name);
        mbeanserver.registerMBean(this, baseObjectName);
    }
    
    public void increment(boolean result) {
        if (result) {
            disconnect .incrementAndGet();
        }
        all.incrementAndGet();
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.james.socket.HandlerStatsMBean#getAll()
     */
    public long getAll() {
        return all.get();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.james.socket.HandlerStatsMBean#getName()
     */
    public String getName() {
        return handlerName;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.james.socket.HandlerStatsMBean#getDisconnect()
     */
    public long getDisconnect() {
        return disconnect.get();
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.james.lifecycle.Disposable#dispose()
     */
    public void dispose() {
        try {
            mbeanserver.unregisterMBean(new ObjectName(name));
        } catch (Exception e) {
            // ignore here;
        }
    }

}