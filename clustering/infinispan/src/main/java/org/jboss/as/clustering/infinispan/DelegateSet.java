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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
* @author Brent Douglas <brent.n.douglas@gmail.com>
*/
public final class DelegateSet<T> implements Set<T> {

    private Set<T> delegate;

    public DelegateSet(final Set<T> delegate) {
        this.delegate = delegate;
    }

    public void cleanUp() {
        this.delegate = null;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(final Object that) {
        return delegate.contains(that);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <X> X[] toArray(final X[] that) {
        return delegate.toArray(that);
    }

    @Override
    public boolean add(final T that) {
        return delegate.add(that);
    }

    @Override
    public boolean remove(final Object that) {
        return delegate.remove(that);
    }

    @Override
    public boolean containsAll(final Collection<?> that) {
        return delegate.containsAll(that);
    }

    @Override
    public boolean addAll(final Collection<? extends T> that) {
        return delegate.addAll(that);
    }

    @Override
    public boolean retainAll(final Collection<?> that) {
        return delegate.retainAll(that);
    }

    @Override
    public boolean removeAll(final Collection<?> that) {
        return delegate.removeAll(that);
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
