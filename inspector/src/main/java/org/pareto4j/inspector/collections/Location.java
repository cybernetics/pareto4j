/*
 * Copyright 2014 Johan Parent
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.pareto4j.inspector.collections;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 20/08/13
 * Time: 9:46
 * To change this template use File | Settings | File Templates.
 */
public class Location implements Comparable<Location>, Serializable {
    public static final int SZ = 1000;
    public static final Map<Where, Location> cache = new LinkedHashMap<Where, Location>(SZ) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Where, Location> eldest) {
            return size() > SZ;
        }
    };

    public static final Location OTHERS = Location.create(new Where());

    boolean needCompute = true;
    int hash;
    StackTraceElement owner = null;
    /**
     * Used by compareTo
     */
    String ownerString;
    Where where;

    /**
     * Returns existing Location instances if possible
     *
     * @param w
     * @return
     */
    public static Location create(Where w) {
        Location l = cache.get(w);
        if (l == null) {
            l = new Location(w);
            cache.put(w, l);
        }

        return l;
    }

    private Location(Where where) {
        if (Profile.fullStacktrace)
            this.where = where;

        owner = where.getStackTrace()[1];
        ownerString = owner.toString();
    }

    public StackTraceElement getOwner() {
        return owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location that = (Location) o;

        if (Profile.fullStacktrace) {
            if (where == that.where) return true;
            return Arrays.equals(where.getStackTrace(), that.where.getStackTrace());
        }

        return getOwner().equals(that.getOwner());
    }

    public int hashCode() {
        if (needCompute) {
            hash = Profile.fullStacktrace ? Arrays.hashCode(where.getStackTrace()) : getOwner().hashCode();
            needCompute = false;
        }

        return hash;
    }

    public int compareTo(Location o) {
        return ownerString.compareTo(o.ownerString);
    }

    public String toString() {
        if (Profile.fullStacktrace)
            return toString(where);

        return ownerString;
    }

    String toString(Where e) {
        StringWriter sw = new StringWriter();
        PrintWriter s = new PrintWriter(sw);
        e.printStackTrace(s);

        return sw.toString();
    }

}
