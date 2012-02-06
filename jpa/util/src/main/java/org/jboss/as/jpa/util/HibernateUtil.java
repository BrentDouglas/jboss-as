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

import org.jboss.as.jpa.util.HibernateOptions.Default;
import org.jboss.as.jpa.util.HibernateOptions.Property;

import java.util.Properties;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public final class HibernateUtil {

    public static String setDefaultIfAbsentAndGet(final Properties properties, final String property, final String defaultValue) {
        final String value = properties.getProperty(property);
        if (value == null) {
            properties.setProperty(property, defaultValue);
            return defaultValue;
        }
        return value;
    }

    public static void addHibernate3Config(final Properties properties, final String cacheRegionPrefix) {
        if (Boolean.parseBoolean(properties.getProperty(Property.CACHE_USE_SECOND_LEVEL_CACHE))) {
            setDefaultIfAbsentAndGet(properties, Property.CACHE_REGION_PREFIX, cacheRegionPrefix);
            final String regionFactory = setDefaultIfAbsentAndGet(properties, Property.CACHE_REGION_FACTORY_CLASS, Default.HIBERNATE_3_CACHE_REGION_FACTORY_CLASS);
            if (regionFactory.equals(Default.HIBERNATE_3_CACHE_REGION_FACTORY_CLASS)) {
                setDefaultIfAbsentAndGet(properties, Property.CACHE_INFINISPAN_CONTAINER, Default.CACHE_INFINISPAN_CONTAINER);
            }
        }
    }
}
