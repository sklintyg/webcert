/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.common.specifications.spec.util;

import groovy.transform.PackageScope

/**
 * Helper class which wraps a domain object or list of objects as a List of List
 * of List of property, property-value pairs, suitable as return type in a
 * FitNesse-SLIM Query table. 
 */
public class QueryHelper {

    public static List<Object> asList(List<Object> domainObjects) {
        domainObjects.collect {
            wrap(it)
        }
    }

    public static List<Object> asList(Object domainObject) {
       [wrap(domainObject)]
    }
    
    @PackageScope
    static def wrap(Map domainObject) {
        def result = []
        domainObject.each { key, value ->
            result << [key, value.toString()]
        }
        result
    }

    @PackageScope
    static def wrap(Object domainObject) {
        domainObject.properties.findAll {key, value -> key != "class"}.collect { key, value ->
            [key, value.toString()]
        }
    }
}
