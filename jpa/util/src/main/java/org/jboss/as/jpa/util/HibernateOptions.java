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

package org.jboss.as.jpa.util;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public final class HibernateOptions {

    public final class Property {
        public static final String CACHE_USE_SECOND_LEVEL_CACHE = "hibernate.cache.use_second_level_cache";
        public static final String CACHE_REGION_PREFIX = "hibernate.cache.region_prefix";
        public static final String CACHE_REGION_FACTORY_CLASS = "hibernate.cache.region.factory_class";
        public static final String CACHE_INFINISPAN_CONTAINER = "hibernate.cache.infinispan.container";
        public static final String CACHE_ENTITY_CFG = "hibernate.cache.infinispan.entity.cfg";
        public static final String CACHE_COLLECTION_CFG = "hibernate.cache.infinispan.collection.cfg";
        public static final String CACHE_QUERY_CFG = "hibernate.cache.infinispan.query.cfg";
        public static final String CACHE_TIMESTAMPS_CFG = "hibernate.cache.infinispan.timestamps.cfg";
        public static final String TRANSACTION_MANAGER_LOOKUP_CLASS = "hibernate.transaction.manager_lookup_class";
    }

    public final class Default {
        public static final String CACHE_INFINISPAN_CONTAINER = "hibernate";
        public static final String CACHE_ENTITY_CFG = "entity";
        public static final String CACHE_COLLECTION_CFG = "entity";
        public static final String CACHE_QUERY_CFG = "local-query";
        public static final String CACHE_TIMESTAMPS_CFG = "timestamps";

        public static final String HIBERNATE_3_CACHE_REGION_FACTORY_CLASS = "org.jboss.as.jpa.hibernate3.cache.infinispan.InfinispanRegionFactory";
        public static final String HIBERNATE_3_SCANNER = "org.jboss.as.jpa.hibernate3.HibernateAnnotationScanner";
        public static final String HIBERNATE_3_TRANSACTION_MANAGER_LOOKUP_CLASS = "org.jboss.as.jpa.hibernate3.JBossAppServerJtaPlatform";

        public static final String HIBERNATE_4_CACHE_REGION_FACTORY_CLASS = "org.jboss.as.jpa.hibernate4.cache.infinispan.InfinispanRegionFactory";
        public static final String HIBERNATE_4_SCANNER = "org.jboss.as.jpa.hibernate4.HibernateAnnotationScanner";
    }

}
