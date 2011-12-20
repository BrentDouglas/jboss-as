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

package org.jboss.as.jpa.hibernate3;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.ejb.AvailableSettings;
import org.jboss.as.jpa.util.HibernateOptions.Default;
import org.jboss.as.jpa.util.HibernateOptions.Property;
import org.jboss.as.jpa.spi.JtaManager;
import org.jboss.as.jpa.spi.ManagementAdaptor;
import org.jboss.as.jpa.spi.PersistenceProviderAdaptor;
import org.jboss.as.jpa.spi.PersistenceUnitMetadata;
import org.jboss.as.jpa.util.HibernateUtil;
import org.jboss.msc.service.ServiceName;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Implements the PersistenceProviderAdaptor for Hibernate 3.3.x or higher 3.x
 *
 * @author Scott Marlow
 */
public class HibernatePersistenceProviderAdaptor implements PersistenceProviderAdaptor {

    @Override
    public void injectJtaManager(final JtaManager jtaManager) {
        JBossAppServerJtaPlatform.initJBossAppServerJtaPlatform(jtaManager);
    }

    @Override
    public void addProviderProperties(final Map properties, final PersistenceUnitMetadata pu) {
        putPropertyIfAbsent(pu, properties, Property.TRANSACTION_MANAGER_LOOKUP_CLASS, Default.HIBERNATE_3_TRANSACTION_MANAGER_LOOKUP_CLASS);
        putPropertyIfAbsent(pu, properties, Configuration.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        addAnnotationScanner(pu);
    }

    /**
     * Use reflection to see if we are using Hibernate 3.3.x or older (which doesn't have the
     * org.hibernate.ejb.packaging.Scanner class)
     *
     * @param pu
     */
    private void addAnnotationScanner(final PersistenceUnitMetadata pu) {
        try {
            Configuration.class.getClassLoader().loadClass(Default.HIBERNATE_3_SCANNER);
            pu.getProperties().put(AvailableSettings.SCANNER, Default.HIBERNATE_3_SCANNER);
        } catch (Throwable ignore) {

        }
    }

    @Override
    public Iterable<ServiceName> getProviderDependencies(final PersistenceUnitMetadata pu) {
        final Properties properties = pu.getProperties();
        if (Boolean.parseBoolean(properties.getProperty(Environment.USE_SECOND_LEVEL_CACHE))) {

            // Cache entries for this PU will be identified by scoped pu name + Entity class name
            HibernateUtil.setDefaultIfAbsentAndGet(properties, Environment.CACHE_REGION_PREFIX, pu.getScopedPersistenceUnitName());
            final String regionFactory = HibernateUtil.setDefaultIfAbsentAndGet(properties, Environment.CACHE_REGION_FACTORY, Default.HIBERNATE_3_CACHE_REGION_FACTORY_CLASS);
            final String container = HibernateUtil.setDefaultIfAbsentAndGet(properties, Property.CACHE_INFINISPAN_CONTAINER, Default.CACHE_INFINISPAN_CONTAINER);

            // Set infinispan defaults
            final String entity = properties.getProperty(Property.CACHE_ENTITY_CFG, Default.CACHE_ENTITY_CFG);
            final String collection = properties.getProperty(Property.CACHE_COLLECTION_CFG, Default.CACHE_COLLECTION_CFG);
            final String query = properties.getProperty(Property.CACHE_QUERY_CFG, Default.CACHE_QUERY_CFG);
            final String timestamps = properties.getProperty(Property.CACHE_TIMESTAMPS_CFG, Default.CACHE_TIMESTAMPS_CFG);

            if (regionFactory.equals(Default.HIBERNATE_3_CACHE_REGION_FACTORY_CLASS)) {
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
        if (pu.getProperties().containsKey(AvailableSettings.SCANNER)) {
            try {
                final Class<?> scanner = Configuration.class.getClassLoader().loadClass(Default.HIBERNATE_3_SCANNER);
                // get method for public static void setThreadLocalPersistenceUnitMetadata(final PersistenceUnitMetadata pu) {
                final Method setThreadLocalPersistenceUnitMetadata = scanner.getMethod("setThreadLocalPersistenceUnitMetadata", PersistenceUnitMetadata.class);
                setThreadLocalPersistenceUnitMetadata.invoke(null, pu);
            } catch (Throwable ignore) {

            }
        }
    }

    @Override
    public void afterCreateContainerEntityManagerFactory(PersistenceUnitMetadata pu) {
        if (pu.getProperties().containsKey(AvailableSettings.SCANNER)) {
            // clear backdoor annotation scanner access to pu
            try {
                Class<?> scanner = Configuration.class.getClassLoader().loadClass(Default.HIBERNATE_3_SCANNER);
                // get method for public static void clearThreadLocalPersistenceUnitMetadata() {
                Method clearThreadLocalPersistenceUnitMetadata = scanner.getMethod("clearThreadLocalPersistenceUnitMetadata");
                clearThreadLocalPersistenceUnitMetadata.invoke(null);
            } catch (Throwable ignore) {
            }
        }
    }

    @Override
    public ManagementAdaptor getManagementAdaptor() {
        return null;
    }

    @Override
    public void cleanup(PersistenceUnitMetadata pu) {
        HibernateAnnotationScanner.cleanup(pu);
    }
}

