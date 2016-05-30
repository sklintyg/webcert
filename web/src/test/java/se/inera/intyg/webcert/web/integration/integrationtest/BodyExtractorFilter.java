/**
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of rehabstod (https://github.com/sklintyg/rehabstod).
 *
 * rehabstod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * rehabstod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.integration.integrationtest;

import java.util.Map;

import com.jayway.restassured.builder.ResponseBuilder;
import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;

/*
 * Detta är en kopia av motsvarande klass i IT. Efter införande av gradle som byggsystem
 * skall dessa testutilklasser flyttas till en test-jar i common som alla applikationer SOAP tjänster kan använda.
 *
 * Se INTYG-2391, INTYG-2536
 */
public class BodyExtractorFilter implements Filter {

    private final String extractXPath;
    private final Map<String, String> namespaceMap;

    public BodyExtractorFilter(Map<String, String> namespaceMap, String extractXPath) {
        this.namespaceMap = namespaceMap;
        this.extractXPath = extractXPath;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);
        XPathExtractor extractor = new XPathExtractor(response.print(), namespaceMap);
        String newBody = extractor.getFragmentFromXPath(extractXPath);
        return new ResponseBuilder().clone(response).setBody(newBody).build();
    }

}
