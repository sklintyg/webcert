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

package se.inera.intyg.webcert.web.auth.authorities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Created by Magnus Ekstrand on 19/11/15.
 */
public class RequestOrigin {

    @JsonProperty
    private String name;

    @JsonProperty
    private List<String> intygstyper;


    // ~ Getter and setter
    // =======================================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIntygstyper() {
        return intygstyper;
    }

    public void setIntygstyper(List<String> intygstyper) {
        if (intygstyper == null) {
            this.intygstyper = Collections.emptyList();
        } else {
            this.intygstyper = intygstyper;
        }
    }


    // ~ API
    // =======================================================================

    @Override
    public String toString() {
        return "\nRequestOrigin {"
                + " name='" + name + '\''
                + ", intygstyper= " + intygstyper
                + "}";
    }

}
