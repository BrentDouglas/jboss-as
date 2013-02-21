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

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.Transport;

import java.util.List;
import java.util.Set;

/**
 * Delegating {@link EmbeddedCacheManager}
 *
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class DelegateEmbeddedCacheManager implements EmbeddedCacheManager {

    private volatile EmbeddedCacheManager delegate;

    public DelegateEmbeddedCacheManager(final EmbeddedCacheManager delegate) {
        this.delegate = delegate;
    }

    public void cleanUp() {
        this.delegate = null;
    }

    @Override
    public org.infinispan.config.Configuration defineConfiguration(final String cacheName, final org.infinispan.config.Configuration configurationOverride) {
        return this.delegate.defineConfiguration(cacheName, configurationOverride);
    }

    @Override
    public Configuration defineConfiguration(final String cacheName, final Configuration configurationOverride) {
        return this.delegate.defineConfiguration(cacheName, configurationOverride);
    }

    @Override
    public org.infinispan.config.Configuration defineConfiguration(final String cacheName, final String templateCacheName, final org.infinispan.config.Configuration configurationOverride) {
        return this.delegate.defineConfiguration(cacheName, templateCacheName, configurationOverride);
    }

    @Override
    public String getClusterName() {
        return this.delegate.getClusterName();
    }

    @Override
    public List<Address> getMembers() {
        return this.delegate.getMembers();
    }

    @Override
    public Address getAddress() {
        return this.delegate.getAddress();
    }

    @Override
    public Address getCoordinator() {
        return this.delegate.getCoordinator();
    }

    @Override
    public boolean isCoordinator() {
        return this.delegate.isCoordinator();
    }

    @Override
    public ComponentStatus getStatus() {
        return this.delegate.getStatus();
    }

    @Override
    public org.infinispan.config.GlobalConfiguration getGlobalConfiguration() {
        return this.delegate.getGlobalConfiguration();
    }

    @Override
    public GlobalConfiguration getCacheManagerConfiguration() {
        return this.delegate.getCacheManagerConfiguration();
    }

    @Override
    public Configuration getCacheConfiguration(final String name) {
        return this.delegate.getCacheConfiguration(name);
    }

    @Override
    public org.infinispan.config.Configuration getDefaultConfiguration() {
        return this.delegate.getDefaultConfiguration();
    }

    @Override
    public Configuration getDefaultCacheConfiguration() {
        return this.delegate.getDefaultCacheConfiguration();
    }

    @Override
    public Set<String> getCacheNames() {
        return this.delegate.getCacheNames();
    }

    @Override
    public boolean isRunning(final String cacheName) {
        return this.delegate.isRunning(cacheName);
    }

    @Override
    public boolean isDefaultRunning() {
        return this.delegate.isDefaultRunning();
    }

    @Override
    public boolean cacheExists(final String cacheName) {
        return this.delegate.cacheExists(cacheName);
    }

    @Override
    public <K, V> Cache<K, V> getCache(final String cacheName, final boolean createIfAbsent) {
        return this.delegate.getCache(cacheName, createIfAbsent);
    }

    @Override
    public EmbeddedCacheManager startCaches(final String... cacheNames) {
        return this.delegate.startCaches(cacheNames);
    }

    @Override
    public void removeCache(final String cacheName) {
        this.delegate.removeCache(cacheName);
    }

    @Override
    public Transport getTransport() {
        return this.delegate.getTransport();
    }

    @Override
    public <K, V> Cache<K, V> getCache() {
        return this.delegate.getCache();
    }

    @Override
    public <K, V> Cache<K, V> getCache(final String cacheName) {
        return this.delegate.getCache(cacheName);
    }

    @Override
    public void start() {
        this.delegate.start();
    }

    @Override
    public void stop() {
        this.delegate.stop();
    }

    @Override
    public void addListener(final Object listener) {
        this.delegate.addListener(listener);
    }

    @Override
    public void removeListener(final Object listener) {
        this.delegate.removeListener(listener);
    }

    @Override
    public Set<Object> getListeners() {
        return this.delegate.getListeners();
    }

    @Override
    public boolean equals(final Object object) {
        return (object == this) || (object == this.delegate);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }
}
