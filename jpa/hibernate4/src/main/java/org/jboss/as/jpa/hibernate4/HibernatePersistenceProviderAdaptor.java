/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.jpa.hibernate4;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.jboss.as.jpa.util.HibernateOptions.Default;
import org.jboss.as.jpa.util.HibernateOptions.Property;
import org.jboss.as.jpa.hibernate4.management.HibernateManagementAdaptor;
import org.jboss.as.jpa.spi.JtaManager;
import org.jboss.as.jpa.spi.ManagementAdaptor;
import org.jboss.as.jpa.spi.PersistenceProviderAdaptor;
import org.jboss.as.jpa.spi.PersistenceUnitMetadata;
import org.jboss.as.jpa.util.HibernateUtil;
import org.jboss.msc.service.ServiceName;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Implements the PersistenceProviderAdaptor for Hibernate
 *
 * @author Scott Marlow
 */
public class HibernatePersistenceProviderAdaptor implements PersistenceProviderAdaptor {

    private volatile JBossAppServerJtaPlatform appServerJtaPlatform;

    @Override
    public void injectJtaManager(final JtaManager jtaManager) {
        appServerJtaPlatform = new JBossAppServerJtaPlatform(jtaManager);
    }

    @Override
    public void addProviderProperties(final Map properties, final PersistenceUnitMetadata pu) {
        putPropertyIfAbsent(pu, properties, Configuration.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        putPropertyIfAbsent(pu, properties, org.hibernate.ejb.AvailableSettings.SCANNER, Default.HIBERNATE_4_SCANNER);
        properties.put(AvailableSettings.APP_CLASSLOADER, pu.getClassLoader());
        putPropertyIfAbsent(pu, properties, AvailableSettings.JTA_PLATFORM, appServerJtaPlatform);
        properties.remove(AvailableSettings.TRANSACTION_MANAGER_STRATEGY);  // remove legacy way of specifying TX manager (conflicts with JTA_PLATFORM)
        putPropertyIfAbsent(pu,properties, org.hibernate.ejb.AvailableSettings.ENTITY_MANAGER_FACTORY_NAME, pu.getScopedPersistenceUnitName());
        putPropertyIfAbsent(pu, properties, AvailableSettings.SESSION_FACTORY_NAME, pu.getScopedPersistenceUnitName());
        if (!pu.getProperties().containsKey(AvailableSettings.SESSION_FACTORY_NAME)) {
            putPropertyIfAbsent(pu, properties, AvailableSettings.SESSION_FACTORY_NAME_IS_JNDI, Boolean.FALSE);
        }
    }

    @Override
    public Iterable<ServiceName> getProviderDependencies(final PersistenceUnitMetadata pu) {
        final Properties properties = pu.getProperties();
        if (Boolean.parseBoolean(properties.getProperty(Property.CACHE_USE_SECOND_LEVEL_CACHE))) {

            // Cache entries for this PU will be identified by scoped pu name + Entity class name
            HibernateUtil.setDefaultIfAbsentAndGet(properties, Property.CACHE_REGION_PREFIX, pu.getScopedPersistenceUnitName());
            final String regionFactory = HibernateUtil.setDefaultIfAbsentAndGet(properties, Property.CACHE_REGION_FACTORY_CLASS, Default.HIBERNATE_4_CACHE_REGION_FACTORY_CLASS);
            final String container = HibernateUtil.setDefaultIfAbsentAndGet(properties, Property.CACHE_INFINISPAN_CONTAINER, Default.CACHE_INFINISPAN_CONTAINER);

            // Set infinispan defaults
            final String entity = properties.getProperty(Property.CACHE_ENTITY_CFG, Default.CACHE_ENTITY_CFG);
            final String collection = properties.getProperty(Property.CACHE_COLLECTION_CFG, Default.CACHE_COLLECTION_CFG);
            final String query = properties.getProperty(Property.CACHE_QUERY_CFG, Default.CACHE_QUERY_CFG);
            final String timestamps = properties.getProperty(Property.CACHE_TIMESTAMPS_CFG, Default.CACHE_TIMESTAMPS_CFG);

            if (regionFactory.equals(Default.HIBERNATE_4_CACHE_REGION_FACTORY_CLASS)) {
                final Set<ServiceName> result = new HashSet<ServiceName>();
                result.add(this.getCacheConfigServiceName(container, entity));
                result.add(this.getCacheConfigServiceName(container, collection));
                result.add(this.getCacheConfigServiceName(container, timestamps));
                result.add(this.getCacheConfigServiceName(container, query));
                return result;
            }
        }
        return null;
    }

    private ServiceName getCacheConfigServiceName(final String container, final String cache) {
        return ServiceName.JBOSS.append("infinispan", container, cache, "config");
    }

    private void putPropertyIfAbsent(final PersistenceUnitMetadata pu, final Map properties, final String property, final Object value) {
        if (!pu.getProperties().containsKey(property)) {
            properties.put(property, value);
        }
    }

    @Override
    public void beforeCreateContainerEntityManagerFactory(final PersistenceUnitMetadata pu) {
        // set backdoor annotation scanner access to pu
        HibernateAnnotationScanner.setThreadLocalPersistenceUnitMetadata(pu);
    }

    @Override
    public void afterCreateContainerEntityManagerFactory(final PersistenceUnitMetadata pu) {
        // clear backdoor annotation scanner access to pu
        HibernateAnnotationScanner.clearThreadLocalPersistenceUnitMetadata();
    }

    @Override
    public ManagementAdaptor getManagementAdaptor() {
        return HibernateManagementAdaptor.getInstance();
    }

    public void cleanup(PersistenceUnitMetadata pu) {
        HibernateAnnotationScanner.cleanup(pu);
    }
}

