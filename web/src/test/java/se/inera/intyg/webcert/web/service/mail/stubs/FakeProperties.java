/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.mail.stubs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(
    ignoreUnknown = true
)
public class FakeProperties {
    private String displayOrder;
    private String env;
    private boolean readOnly = false;
    private List<String> allowedInApplications = new ArrayList();
    private List<FakeLogins> logins = new ArrayList();
    private Map<String, String> extraContextProperties = new HashMap();

    public FakeProperties() {
    }

    public String getDisplayOrder() {
        return this.displayOrder;
    }

    public String getEnv() {
        return this.env;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public List<String> getAllowedInApplications() {
        return this.allowedInApplications;
    }

    public List<FakeLogins> getLogins() {
        return this.logins;
    }

    public Map<String, String> getExtraContextProperties() {
        return this.extraContextProperties;
    }

    public void setDisplayOrder(String displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setAllowedInApplications(List<String> allowedInApplications) {
        this.allowedInApplications = allowedInApplications;
    }

    public void setLogins(List<FakeLogins> logins) {
        this.logins = logins;
    }

    public void setExtraContextProperties(Map<String, String> extraContextProperties) {
        this.extraContextProperties = extraContextProperties;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FakeProperties)) {
            return false;
        } else {
            FakeProperties other = (FakeProperties)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.isReadOnly() != other.isReadOnly()) {
                return false;
            } else {
                label73: {
                    Object this$displayOrder = this.getDisplayOrder();
                    Object other$displayOrder = other.getDisplayOrder();
                    if (this$displayOrder == null) {
                        if (other$displayOrder == null) {
                            break label73;
                        }
                    } else if (this$displayOrder.equals(other$displayOrder)) {
                        break label73;
                    }

                    return false;
                }

                Object this$env = this.getEnv();
                Object other$env = other.getEnv();
                if (this$env == null) {
                    if (other$env != null) {
                        return false;
                    }
                } else if (!this$env.equals(other$env)) {
                    return false;
                }

                label59: {
                    Object this$allowedInApplications = this.getAllowedInApplications();
                    Object other$allowedInApplications = other.getAllowedInApplications();
                    if (this$allowedInApplications == null) {
                        if (other$allowedInApplications == null) {
                            break label59;
                        }
                    } else if (this$allowedInApplications.equals(other$allowedInApplications)) {
                        break label59;
                    }

                    return false;
                }

                Object this$logins = this.getLogins();
                Object other$logins = other.getLogins();
                if (this$logins == null) {
                    if (other$logins != null) {
                        return false;
                    }
                } else if (!this$logins.equals(other$logins)) {
                    return false;
                }

                Object this$extraContextProperties = this.getExtraContextProperties();
                Object other$extraContextProperties = other.getExtraContextProperties();
                if (this$extraContextProperties == null) {
                    if (other$extraContextProperties != null) {
                        return false;
                    }
                } else if (!this$extraContextProperties.equals(other$extraContextProperties)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof FakeProperties;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.isReadOnly() ? 79 : 97);
        Object $displayOrder = this.getDisplayOrder();
        result = result * 59 + ($displayOrder == null ? 43 : $displayOrder.hashCode());
        Object $env = this.getEnv();
        result = result * 59 + ($env == null ? 43 : $env.hashCode());
        Object $allowedInApplications = this.getAllowedInApplications();
        result = result * 59 + ($allowedInApplications == null ? 43 : $allowedInApplications.hashCode());
        Object $logins = this.getLogins();
        result = result * 59 + ($logins == null ? 43 : $logins.hashCode());
        Object $extraContextProperties = this.getExtraContextProperties();
        result = result * 59 + ($extraContextProperties == null ? 43 : $extraContextProperties.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getDisplayOrder();
        return "FakeProperties(displayOrder=" + var10000 + ", env=" + this.getEnv() + ", readOnly=" + this.isReadOnly() + ", allowedInApplications=" + this.getAllowedInApplications() + ", logins=" + this.getLogins() + ", extraContextProperties=" + this.getExtraContextProperties() + ")";
    }

}
