/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Alexey Loubyansky
 */
public class CollectionUtils {

    public static <K,V> Map<K,V> unmodifiable(Map<K,V> m) {
        return m.size() > 1 ? Collections.unmodifiableMap(m) : m;
    }

    public static <K,V> Map<K,V> clone(Map<K,V> m) {
        return m.size() > 1 ? new HashMap<>(m) : m;
    }

    public static <K,V> Map<K,V> put(Map<K,V> m, K k, V v) {
        if(m.isEmpty()) {
            return Collections.singletonMap(k, v);
        }
        if(m.size() == 1) {
            if(m.containsKey(k)) {
                return Collections.singletonMap(k, v);
            }
            final Map.Entry<K, V> first = m.entrySet().iterator().next();
            m = new HashMap<>(2);
            m.put(first.getKey(), first.getValue());
        }
        m.put(k, v);
        return m;
    }

    public static <K,V> Map<K,V> putAll(Map<K,V> m, Map<K, V> values) {
        if(values.isEmpty()) {
            return m;
        }
        if(m.isEmpty()) {
            final int size = values.size();
            if(size == 1) {
                return values;
            }
            m = new HashMap<>(size);
            m.putAll(values);
            return m;
        }
        if(m.size() == 1) {
            final Map.Entry<K, V> first = m.entrySet().iterator().next();
            m = new HashMap<>(values.size());
            m.put(first.getKey(), first.getValue());
        }
        m.putAll(values);
        return m;
    }

    public static <K,V> Map<K,V> putLinked(Map<K,V> m, K k, V v) {
        if(m.isEmpty()) {
            return Collections.singletonMap(k, v);
        }
        if(m.size() == 1) {
            if(m.containsKey(k)) {
                return Collections.singletonMap(k, v);
            }
            final Map.Entry<K, V> first = m.entrySet().iterator().next();
            m = new LinkedHashMap<>(2);
            m.put(first.getKey(), first.getValue());
        }
        m.put(k, v);
        return m;
    }

    public static <K,V> Map<K,V> remove(Map<K,V> m, K k) {
        if(!m.containsKey(k)) {
            return m;
        }
        switch(m.size()) {
            case 1:
                return Collections.emptyMap();
            case 2:
                for(Map.Entry<K, V> e : m.entrySet()) {
                    if(e.getKey().equals(k)) {
                        continue;
                    }
                    return Collections.singletonMap(e.getKey(), e.getValue());
                }
            default:
                m.remove(k);
        }
        return m;
    }

    public static <T> List<T> unmodifiable(List<T> l) {
        return l.size() > 1 ? Collections.unmodifiableList(l) : l;
    }

    public static <T> List<T> add(List<T> s, T t) {
        if(s.isEmpty()) {
            return Collections.singletonList(t);
        }
        if(s.size() == 1) {
            final T first = s.get(0);
            s = new ArrayList<>(2);
            s.add(first);
        }
        s.add(t);
        return s;
    }

    public static <T> List<T> add(List<T> s, int index, T t) {
        if (s.isEmpty()) {
            return Collections.singletonList(t);
        }
        if (s.size() == 1) {
            final T first = s.get(0);
            s = new ArrayList<>(2);
            s.add(first);
        }
        if (index >= s.size()) {
            s.add(t);
        } else {
            s.add(index, t);
        }
        return s;
    }

    public static <T> List<T> remove(List<T> s, int index) {
        if (s.isEmpty() || s.size() == 1) {
            return Collections.emptyList();
        }
        s.remove(index);
        if (s.size() == 1) {
            return Collections.singletonList(s.get(0));
        }
        return s;
    }

    public static <T> List<T> addAll(List<T> dest, List<T> src) {
        if(dest.isEmpty()) {
            return src;
        }
        if(src.isEmpty()) {
            return dest;
        }
        if(dest.size() == 1) {
            final T first = dest.get(0);
            dest = new ArrayList<>(src.size() + 1);
            dest.add(first);
        }
        dest.addAll(src);
        return dest;
    }

    public static <T> List<T> clone(List<T> s) {
        return s.size() > 1 ? new ArrayList<>(s) : s;
    }

    public static <T> Set<T> unmodifiable(Set<T> s) {
        return s.size() > 1 ? Collections.unmodifiableSet(s) : s;
    }

    public static <T> Set<T> add(Set<T> s, T t) {
        if(s.isEmpty()) {
            return Collections.singleton(t);
        }
        if(s.size() == 1) {
            if (s.contains(t)) {
                return s;
            }
            final T first = s.iterator().next();
            s = new HashSet<>(2);
            s.add(first);
        }
        s.add(t);
        return s;
    }

    public static <T> Set<T> remove(Set<T> s, T t) {
        if(!s.contains(t)) {
            return s;
        }
        switch(s.size()) {
            case 1:
                return Collections.emptySet();
            case 2:
                for(T i : s) {
                    if(i.equals(t)) {
                        continue;
                    }
                    return Collections.singleton(i);
                }
            default:
                s.remove(t);
        }
        return s;
    }

    public static <T> Set<T> addLinked(Set<T> s, T t) {
        if(s.isEmpty()) {
            return Collections.singleton(t);
        }
        if(s.size() == 1) {
            if (s.contains(t)) {
                return s;
            }
            final T first = s.iterator().next();
            s = new LinkedHashSet<>(2);
            s.add(first);
        }
        s.add(t);
        return s;
    }

    public static <T> Set<T> addAllLinked(Set<T> dest, Set<T> src) {
        if(dest.isEmpty()) {
            return src;
        }
        if(src.isEmpty()) {
            return dest;
        }
        if(dest.size() == 1) {
            final T first = dest.iterator().next();
            dest = new LinkedHashSet<>(src.size() + 1);
            dest.add(first);
        }
        dest.addAll(src);
        return dest;
    }

    public static <T> Set<T> clone(Set<T> s) {
        return s.size() > 1 ? new HashSet<>(s) : s;
    }
}
