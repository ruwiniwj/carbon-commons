/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.reporting.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.reporting.core.utils.CommonUtil;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

@Component(
        name = "reporting.services",
        immediate = true)
public class ReportingComponent {

    private static Log log = LogFactory.getLog(ReportingComponent.class);

    private static RegistryService registryServiceInstance;

    private static DataSourceService dataSourceService1;

    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            ReportingAxis2ConfigurationContextObserver observer = new ReportingAxis2ConfigurationContextObserver();
            componentContext.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                    observer, null);
            CommonUtil.addJrxmlConfigs(registryServiceInstance.getSystemRegistry());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        log.debug("report definition  deactivated");
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        registryServiceInstance = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {

        registryServiceInstance = null;
    }

    public static RegistryService getRegistryService() throws RegistryException {

        if (registryServiceInstance == null) {
            throw new RegistryException("Registry Service instance null");
        }
        return registryServiceInstance;
    }

    @Reference(
            name = "org.wso2.carbon.ndatasource.core.DataSourceService",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonDataSourceService")
    protected void setCarbonDataSourceService(DataSourceService dataSourceService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Carbon Data Sources Service");
        }
        ReportingComponent.dataSourceService1 = dataSourceService;
    }

    protected void unsetCarbonDataSourceService(DataSourceService dataSourceService) {

        dataSourceService1 = dataSourceService;
    }

    public static DataSourceService getCarbonDataSourceService() {

        return dataSourceService1;
    }
}
