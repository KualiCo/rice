/**
 * Copyright 2005-2019 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.routemodule.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.rice.core.api.reflect.ObjectDefinition;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.api.WorkflowRuntimeException;
import org.kuali.rice.kew.api.exception.ResourceUnavailableException;
import org.kuali.rice.kew.engine.node.RouteNode;
import org.kuali.rice.kew.role.RoleRouteModule;
import org.kuali.rice.kew.routemodule.FlexRMAdapter;
import org.kuali.rice.kew.routemodule.RouteModule;
import org.kuali.rice.kew.routemodule.service.RouteModuleService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class RouteModuleServiceImpl implements RouteModuleService, BeanFactoryAware {

    private static final Logger LOG = LogManager.getLogger(RouteModuleServiceImpl.class);

    private BeanFactory beanFactory;

    public RouteModule findRouteModule(RouteNode node) throws ResourceUnavailableException {
        String routeMethodName = node.getRouteMethodName();
        LOG.debug("Finding route module for routeMethodName="+routeMethodName+" at route level "+node.getRouteNodeName());
        RouteModule routeModule = null;
        if (routeMethodName == null || node.isFlexRM()) {
            routeModule = getFlexRMRouteModule(routeMethodName);
        } else {
            routeModule = getRouteModule(routeMethodName);
        }
        return routeModule;
    }

    public RouteModule findRouteModule(ActionRequestValue actionRequest) throws ResourceUnavailableException {
    	if (!actionRequest.getResolveResponsibility()) {
    		return new RoleRouteModule();
    	}
        if (actionRequest.getNodeInstance() == null) {
            return null;
        }
        return findRouteModule(actionRequest.getNodeInstance().getRouteNode());
    }

    private RouteModule getRouteModule(String routeMethodName) throws ResourceUnavailableException {
        if (routeMethodName == null) {
            return null;
        } else if ("".equals(routeMethodName.trim()) || KewApiConstants.ROUTE_LEVEL_NO_ROUTE_MODULE.equals(routeMethodName)) {
                return null;
        }
        Object routeModule = GlobalResourceLoader.getObject(new ObjectDefinition(routeMethodName));//SpringServiceLocator.getExtensionService().getRouteModule(routeMethodName);
        if (routeModule instanceof RouteModule) {
            return (RouteModule)routeModule;
        }
        throw new WorkflowRuntimeException("Could not locate the Route Module with the given name: " + routeMethodName);
    }

    private RouteModule getFlexRMRouteModule(String ruleTemplateName) {
        return new FlexRMAdapter();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
