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

package org.jboss.as.clustering.infinispan;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.manager.AbstractCacheManager;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class IntegrationCacheManager extends AbstractCacheManager {

    private final GlobalComponentRegistry globalComponentRegistry;
    private final DelegateSet<String> cacheKeys;
    private final DelegateEmbeddedCacheManager cacheManager;

    public IntegrationCacheManager(final GlobalConfiguration globalConfiguration, final Configuration defaultConfiguration, final boolean start) {
        super(globalConfiguration, defaultConfiguration);
        this.cacheManager = new DelegateEmbeddedCacheManager(this);
        this.cacheKeys = new DelegateSet<String>(getCacheKeys());
        this.globalComponentRegistry = new GlobalComponentRegistry(globalConfiguration, this.cacheManager, this.cacheKeys);
        if (start) {
            start();
        }
    }

    public void cleanUp() {
        this.cacheManager.cleanUp();
        this.cacheKeys.cleanUp();
    }

    @Override
    protected GlobalComponentRegistry getGlobalComponentRegistry() {
        return this.globalComponentRegistry;
    }

}
