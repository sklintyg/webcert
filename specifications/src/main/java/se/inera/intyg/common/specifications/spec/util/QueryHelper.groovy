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
