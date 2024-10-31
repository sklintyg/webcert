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
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(
    ignoreUnknown = true
)
public class CredentialInformation {

    public static final String VARD_OCH_BEHANDLING = "Vård och behandling";
    public static final String STATISTIK = "Statistik";
    private String hsaId;
    private String givenName;
    private List<Commission> commissionList = new ArrayList();

    public CredentialInformation() {
    }

    public CredentialInformation(String hsaId, List<Commission> commissionList) {
        this.hsaId = hsaId;
        this.commissionList = commissionList;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof CredentialInformation)) {
            return false;
        } else {
            CredentialInformation other = (CredentialInformation)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label47: {
                    Object this$hsaId = this.getHsaId();
                    Object other$hsaId = other.getHsaId();
                    if (this$hsaId == null) {
                        if (other$hsaId == null) {
                            break label47;
                        }
                    } else if (this$hsaId.equals(other$hsaId)) {
                        break label47;
                    }

                    return false;
                }

                Object this$givenName = this.getGivenName();
                Object other$givenName = other.getGivenName();
                if (this$givenName == null) {
                    if (other$givenName != null) {
                        return false;
                    }
                } else if (!this$givenName.equals(other$givenName)) {
                    return false;
                }

                Object this$commissionList = this.getCommissionList();
                Object other$commissionList = other.getCommissionList();
                if (this$commissionList == null) {
                    if (other$commissionList != null) {
                        return false;
                    }
                } else if (!this$commissionList.equals(other$commissionList)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof CredentialInformation;
    }

    public int hashCode() {
        int result = 1;
        Object $hsaId = this.getHsaId();
        result = result * 59 + ($hsaId == null ? 43 : $hsaId.hashCode());
        Object $givenName = this.getGivenName();
        result = result * 59 + ($givenName == null ? 43 : $givenName.hashCode());
        Object $commissionList = this.getCommissionList();
        result = result * 59 + ($commissionList == null ? 43 : $commissionList.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getHsaId();
        return "CredentialInformation(hsaId=" + var10000 + ", givenName=" + this.getGivenName() + ", commissionList=" + this.getCommissionList() + ")";
    }

    public static class Commission {
        private String healthCareProviderHsaId;
        private String healthCareUnitHsaId;
        private List<String> commissionPurpose;

        public Commission() {
            this.healthCareUnitHsaId = "";
            this.commissionPurpose = Arrays.asList("Vård och behandling");
        }

        public Commission(String healthCareUnitHsaId, String commissionPurpose) {
            this.healthCareUnitHsaId = healthCareUnitHsaId;
            this.commissionPurpose = Arrays.asList(commissionPurpose);
        }

        public String getHealthCareProviderHsaId() {
            return this.healthCareProviderHsaId;
        }

        public String getHealthCareUnitHsaId() {
            return this.healthCareUnitHsaId;
        }

        public List<String> getCommissionPurpose() {
            return this.commissionPurpose;
        }

        public void setHealthCareProviderHsaId(String healthCareProviderHsaId) {
            this.healthCareProviderHsaId = healthCareProviderHsaId;
        }

        public void setHealthCareUnitHsaId(String healthCareUnitHsaId) {
            this.healthCareUnitHsaId = healthCareUnitHsaId;
        }

        public void setCommissionPurpose(List<String> commissionPurpose) {
            this.commissionPurpose = commissionPurpose;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Commission)) {
                return false;
            } else {
                Commission other = (Commission)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    label47: {
                        Object this$healthCareProviderHsaId = this.getHealthCareProviderHsaId();
                        Object other$healthCareProviderHsaId = other.getHealthCareProviderHsaId();
                        if (this$healthCareProviderHsaId == null) {
                            if (other$healthCareProviderHsaId == null) {
                                break label47;
                            }
                        } else if (this$healthCareProviderHsaId.equals(other$healthCareProviderHsaId)) {
                            break label47;
                        }

                        return false;
                    }

                    Object this$healthCareUnitHsaId = this.getHealthCareUnitHsaId();
                    Object other$healthCareUnitHsaId = other.getHealthCareUnitHsaId();
                    if (this$healthCareUnitHsaId == null) {
                        if (other$healthCareUnitHsaId != null) {
                            return false;
                        }
                    } else if (!this$healthCareUnitHsaId.equals(other$healthCareUnitHsaId)) {
                        return false;
                    }

                    Object this$commissionPurpose = this.getCommissionPurpose();
                    Object other$commissionPurpose = other.getCommissionPurpose();
                    if (this$commissionPurpose == null) {
                        if (other$commissionPurpose != null) {
                            return false;
                        }
                    } else if (!this$commissionPurpose.equals(other$commissionPurpose)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof Commission;
        }

        public int hashCode() {
            int result = 1;
            Object $healthCareProviderHsaId = this.getHealthCareProviderHsaId();
            result = result * 59 + ($healthCareProviderHsaId == null ? 43 : $healthCareProviderHsaId.hashCode());
            Object $healthCareUnitHsaId = this.getHealthCareUnitHsaId();
            result = result * 59 + ($healthCareUnitHsaId == null ? 43 : $healthCareUnitHsaId.hashCode());
            Object $commissionPurpose = this.getCommissionPurpose();
            result = result * 59 + ($commissionPurpose == null ? 43 : $commissionPurpose.hashCode());
            return result;
        }

        public String toString() {
            String var10000 = this.getHealthCareProviderHsaId();
            return "CredentialInformation.Commission(healthCareProviderHsaId=" + var10000 + ", healthCareUnitHsaId=" + this.getHealthCareUnitHsaId() + ", commissionPurpose=" + this.getCommissionPurpose() + ")";
        }
    }
}
