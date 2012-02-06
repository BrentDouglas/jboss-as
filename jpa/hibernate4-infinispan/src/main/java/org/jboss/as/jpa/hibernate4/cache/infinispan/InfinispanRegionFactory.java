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

package org.jboss.as.jpa.hibernate4.cache.infinispan;

import org.hibernate.cache.CacheException;
import org.infinispan.AdvancedCache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.as.clustering.infinispan.subsystem.EmbeddedCacheManagerService;
import org.jboss.as.jpa.util.HibernateOptions.Default;
import org.jboss.as.jpa.util.HibernateOptions.Property;
import org.jboss.as.jpa.util.HibernateUtil;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceName;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * @author Paul Ferraro
 */
public class InfinispanRegionFactory extends org.hibernate.cache.infinispan.InfinispanRegionFactory {
    private static final long serialVersionUID = -3277051412715973863L;

    public InfinispanRegionFactory() {
        super();
    }

    public InfinispanRegionFactory(final Properties props) {
        super(props);
    }

    @Override
    protected EmbeddedCacheManager createCacheManager(final Properties properties) throws CacheException {
        final String container = HibernateUtil.setDefaultIfAbsentAndGet(properties, Property.CACHE_INFINISPAN_CONTAINER, Default.CACHE_INFINISPAN_CONTAINER);
        final ServiceName serviceName = EmbeddedCacheManagerService.getServiceName(container);
        return (EmbeddedCacheManager) CurrentServiceContainer.getServiceContainer().getRequiredService(serviceName).getValue();
    }

    @Override
    public void stop() {
        // Do not attempt to stop our cache manager because it wasn't created by this region factory.
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected AdvancedCache createCacheWrapper(final AdvancedCache cache) {
        final PrivilegedAction<ClassLoader> action = new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        };
        return cache.with(AccessController.doPrivileged(action));
    }
}
