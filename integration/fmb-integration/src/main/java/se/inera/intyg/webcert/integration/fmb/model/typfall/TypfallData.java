/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.fmb.model.typfall;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import se.inera.intyg.webcert.integration.fmb.model.Links;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "attributes",
    "id",
    "links",
    "relationships",
    "type"
})
public class TypfallData {

    @JsonProperty("attributes")
    private Attributes attributes;
    @JsonProperty("id")
    private String id;
    @JsonProperty("links")
    private Links links;
    @JsonProperty("relationships")
    private Relationships relationships;
    @JsonProperty("type")
    private String type;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("attributes")
    public Attributes getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("links")
    public Links getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(Links links) {
        this.links = links;
    }

    @JsonProperty("relationships")
    public Relationships getRelationships() {
        return relationships;
    }

    @JsonProperty("relationships")
    public void setRelationships(Relationships relationships) {
        this.relationships = relationships;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
