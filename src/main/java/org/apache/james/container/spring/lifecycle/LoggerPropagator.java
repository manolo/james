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
package org.apache.james.container.spring.lifecycle;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.james.container.spring.adaptor.LoggingBridge;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * propagates Loggers for all avalon components
 */
public class LoggerPropagator extends AbstractPropagator implements BeanPostProcessor, Ordered {

    private LoggingBridge loggingBridge;

    protected Class getLifecycleInterface() {
        return LogEnabled.class;
    }

    protected void invokeLifecycleWorker(String beanName, Object bean, BeanDefinition beanDefinition) {
        ContainerUtil.enableLogging(bean, loggingBridge);
    }

    public int getOrder() {
        return 0;
    }

    public void setLoggingBridge(LoggingBridge loggingBridge) {
        this.loggingBridge = loggingBridge;
    }
}
