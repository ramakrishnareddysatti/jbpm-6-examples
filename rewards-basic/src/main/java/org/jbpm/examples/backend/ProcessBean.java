/**
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.examples.backend;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.cdi.qualifier.PerProcessInstance;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ProcessBean {

    @Inject
    @PerProcessInstance
    private RuntimeManager ppiManager;

    @PostConstruct
    public void configure() {
        // use toString to make sure CDI initializes the bean
        // this makes sure that RuntimeManager is started asap,
        // otherwise after server restart complete task won't move process forward 
        ppiManager.toString();
    }

    public long startProcess(String recipient) throws Exception {

        RuntimeEngine runtime = ppiManager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();

        long processInstanceId = -1;

        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();

        try {
            // start a new process instance
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("recipient", recipient);
            ProcessInstance processInstance = ksession.startProcess(
                    "com.sample.rewards-basic", params);

            processInstanceId = processInstance.getId();

            System.out.println("Process started ... : processInstanceId = "
                    + processInstanceId);

            ut.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (ut.getStatus() == Status.STATUS_ACTIVE) {
                ut.rollback();
            }
            throw e;
        }

        return processInstanceId;
    }

}