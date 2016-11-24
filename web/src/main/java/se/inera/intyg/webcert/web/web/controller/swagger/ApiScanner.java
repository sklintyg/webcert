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

package se.inera.intyg.webcert.web.web.controller.swagger;

import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.config.FilterFactory;
import io.swagger.core.filter.SpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.*;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Swagger;

/**
 * This class intefaces with io.swagger core classes for on-the-fly generation of Swagger .JSON files for use by
 * swagger-ui.
 *
 * Derived from io.swagger.jaxrs.listing.ApiListingResource.
 *
 * Created by eriklupander on 2015-11-09.
 */
@Path("/")
@Api(value = "/services/swagger", description = "REST API för att generera swagger-beskrivning av våra REST API:er", produces = MediaType.APPLICATION_JSON)
public class ApiScanner {

    private static final String SWAGGER_TOKEN = "swagger";
    private static final String BASE_REST_PACKAGE_NAME = "se.inera.intyg.webcert.web.web.controller.";

    @Context
    private ServletContext context;

    /** Keeps track of whether a given endpoint has been initialized and stored in the context. */
    private Map<String, Boolean> apiInitialized = new HashMap<>();

    /** Populated from CXF xml file swagger-cxf-servlet.xml. */
    private Map<String, String> basepathMap = new HashMap<>();

    /**
     * Returns a list of key->value pairs of REST APIs declared in swagger-cxf-servlet.xml. Primarily used by our customized
     * swagger-ui to list available endpoints.
     *
     * @return
     *      List of serialized Map.Entry items.
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/")
    @ApiOperation(value = "List available APIs", hidden = true)
    public Response getApiList() {
        return Response.ok().entity(basepathMap.entrySet()).build();
    }


    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/{api}")
    @ApiOperation(value = "The swagger definition in JSON", hidden = true)
    public Response getListingJson(@PathParam("api") String api, @Context Application app, @Context ServletConfig sc,
                                   @Context HttpHeaders headers, @Context UriInfo uriInfo) {
        Swagger swagger = process(app, sc, headers, uriInfo, api);

        if (swagger != null) {
            return Response.ok().entity(swagger).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
        }
    }


    private synchronized Swagger scan(Application app, ServletConfig sc, String api) {
        Swagger swagger = null;

        ReflectiveJaxrsScanner scanner = new ReflectiveJaxrsScanner();
        scanner.setResourcePackage(BASE_REST_PACKAGE_NAME + api);
        scanner.setPrettyPrint(true);

        SwaggerSerializers.setPrettyPrint(scanner.getPrettyPrint());

        Set<Class<?>> classes = resolveClasses(app, sc, scanner);
        if (classes != null) {
            Reader reader = new Reader(null, ReaderConfigUtils.getReaderConfig(context));
            swagger = reader.read(classes);
            swagger.setBasePath(resolveBasePathForApi(api));
            swagger = scanner.configure(swagger);

            context.setAttribute(SWAGGER_TOKEN + api, swagger);
            apiInitialized.put(SWAGGER_TOKEN + api, Boolean.TRUE);
        }
        return swagger;
    }

    private Set<Class<?>> resolveClasses(Application app, ServletConfig sc, ReflectiveJaxrsScanner scanner) {
        Set<Class<?>> classes;
        if (scanner instanceof JaxrsScanner) {
            JaxrsScanner jaxrsScanner = (JaxrsScanner) scanner;

            classes = jaxrsScanner.classesFromContext(app, sc);
        } else {
            classes = scanner.classes();
        }
        return classes;
    }

    private String resolveBasePathForApi(String api) {
        if (basepathMap.containsKey(api)) {
            return basepathMap.get(api);
        } else {
            throw new IllegalArgumentException("Cannot generate Swagger docs for API '" + api + "'. Its basepath must be mapped in swagger-cxf-servlet.xml");
        }
    }

    private Swagger process(Application app, ServletConfig sc, HttpHeaders headers, UriInfo uriInfo, String api) {
        Swagger swagger = (Swagger) context.getAttribute(SWAGGER_TOKEN + api);
        if (!apiInitialized.containsKey(SWAGGER_TOKEN + api) || !apiInitialized.get(SWAGGER_TOKEN + api)) {
            swagger = scan(app, sc, api);
        }

        if (swagger != null) {
            SwaggerSpecFilter filterImpl = FilterFactory.getFilter();
            if (filterImpl != null) {
                SpecFilter f = new SpecFilter();
                swagger = f.filter(swagger, filterImpl, getQueryParams(uriInfo.getQueryParameters()), getCookies(headers),
                        getHeaders(headers));
            }
        }
        return swagger;
    }


    protected Map<String, List<String>> getQueryParams(MultivaluedMap<String, String> params) {
        Map<String, List<String>> output = new HashMap<>();
        if (params != null) {
            for (String key : params.keySet()) {
                List<String> values = params.get(key);
                output.put(key, values);
            }
        }
        return output;
    }

    protected Map<String, String> getCookies(HttpHeaders headers) {
        Map<String, String> output = new HashMap<>();
        if (headers != null) {
            for (String key : headers.getCookies().keySet()) {
                Cookie cookie = headers.getCookies().get(key);
                output.put(key, cookie.getValue());
            }
        }
        return output;
    }

    protected Map<String, List<String>> getHeaders(HttpHeaders headers) {
        Map<String, List<String>> output = new HashMap<>();
        if (headers != null) {
            for (String key : headers.getRequestHeaders().keySet()) {
                List<String> values = headers.getRequestHeaders().get(key);
                output.put(key, values);
            }
        }
        return output;
    }

    public void setBasepathMap(Map<String, String> basepathMap) {
        this.basepathMap = basepathMap;
    }
}
