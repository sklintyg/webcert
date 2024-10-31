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
import java.util.List;

@JsonIgnoreProperties(
    ignoreUnknown = true
)
public class HsaPerson {
    private String hsaId;
    private String personalIdentityNumber;
    private String givenName;
    private String middleAndSurname;
    private boolean protectedPerson;
    private List<Speciality> specialities = new ArrayList();
    private List<String> unitIds = new ArrayList();
    private String title;
    private List<String> healthCareProfessionalLicence = new ArrayList();
    private List<PaTitle> paTitle;
    private String personalPrescriptionCode;
    private List<String> systemRoles;
    private List<String> educationCodes;
    private List<Restrictions> restrictions;
    private FakeProperties fakeProperties;
    private String gender;
    private String age;
    private List<HealthCareProfessionalLicenceType> healthCareProfessionalLicenceType = new ArrayList();

    public HsaPerson() {
    }

    public HsaPerson(String hsaId, String givenName, String middleAndSurname) {
        this.hsaId = hsaId;
        this.givenName = givenName;
        this.middleAndSurname = middleAndSurname;
    }

    public HsaPerson(String hsaId, String givenName, String middleAndSurname, String title) {
        this.hsaId = hsaId;
        this.givenName = givenName;
        this.middleAndSurname = middleAndSurname;
        this.title = title;
    }

    public String getHsaId() {
        return this.hsaId;
    }

    public String getPersonalIdentityNumber() {
        return this.personalIdentityNumber;
    }

    public String getGivenName() {
        return this.givenName;
    }

    public String getMiddleAndSurname() {
        return this.middleAndSurname;
    }

    public boolean isProtectedPerson() {
        return this.protectedPerson;
    }

    public List<Speciality> getSpecialities() {
        return this.specialities;
    }

    public List<String> getUnitIds() {
        return this.unitIds;
    }

    public String getTitle() {
        return this.title;
    }

    public List<String> getHealthCareProfessionalLicence() {
        return this.healthCareProfessionalLicence;
    }

    public List<PaTitle> getPaTitle() {
        return this.paTitle;
    }

    public String getPersonalPrescriptionCode() {
        return this.personalPrescriptionCode;
    }

    public List<String> getSystemRoles() {
        return this.systemRoles;
    }

    public List<String> getEducationCodes() {
        return this.educationCodes;
    }

    public List<Restrictions> getRestrictions() {
        return this.restrictions;
    }

    public FakeProperties getFakeProperties() {
        return this.fakeProperties;
    }

    public String getGender() {
        return this.gender;
    }

    public String getAge() {
        return this.age;
    }

    public List<HealthCareProfessionalLicenceType> getHealthCareProfessionalLicenceType() {
        return this.healthCareProfessionalLicenceType;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public void setPersonalIdentityNumber(String personalIdentityNumber) {
        this.personalIdentityNumber = personalIdentityNumber;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setMiddleAndSurname(String middleAndSurname) {
        this.middleAndSurname = middleAndSurname;
    }

    public void setProtectedPerson(boolean protectedPerson) {
        this.protectedPerson = protectedPerson;
    }

    public void setSpecialities(List<Speciality> specialities) {
        this.specialities = specialities;
    }

    public void setUnitIds(List<String> unitIds) {
        this.unitIds = unitIds;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setHealthCareProfessionalLicence(List<String> healthCareProfessionalLicence) {
        this.healthCareProfessionalLicence = healthCareProfessionalLicence;
    }

    public void setPaTitle(List<PaTitle> paTitle) {
        this.paTitle = paTitle;
    }

    public void setPersonalPrescriptionCode(String personalPrescriptionCode) {
        this.personalPrescriptionCode = personalPrescriptionCode;
    }

    public void setSystemRoles(List<String> systemRoles) {
        this.systemRoles = systemRoles;
    }

    public void setEducationCodes(List<String> educationCodes) {
        this.educationCodes = educationCodes;
    }

    public void setRestrictions(List<Restrictions> restrictions) {
        this.restrictions = restrictions;
    }

    public void setFakeProperties(FakeProperties fakeProperties) {
        this.fakeProperties = fakeProperties;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setHealthCareProfessionalLicenceType(List<HealthCareProfessionalLicenceType> healthCareProfessionalLicenceType) {
        this.healthCareProfessionalLicenceType = healthCareProfessionalLicenceType;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof HsaPerson)) {
            return false;
        } else {
            HsaPerson other = (HsaPerson)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.isProtectedPerson() != other.isProtectedPerson()) {
                return false;
            } else {
                label217: {
                    Object this$hsaId = this.getHsaId();
                    Object other$hsaId = other.getHsaId();
                    if (this$hsaId == null) {
                        if (other$hsaId == null) {
                            break label217;
                        }
                    } else if (this$hsaId.equals(other$hsaId)) {
                        break label217;
                    }

                    return false;
                }

                Object this$personalIdentityNumber = this.getPersonalIdentityNumber();
                Object other$personalIdentityNumber = other.getPersonalIdentityNumber();
                if (this$personalIdentityNumber == null) {
                    if (other$personalIdentityNumber != null) {
                        return false;
                    }
                } else if (!this$personalIdentityNumber.equals(other$personalIdentityNumber)) {
                    return false;
                }

                label203: {
                    Object this$givenName = this.getGivenName();
                    Object other$givenName = other.getGivenName();
                    if (this$givenName == null) {
                        if (other$givenName == null) {
                            break label203;
                        }
                    } else if (this$givenName.equals(other$givenName)) {
                        break label203;
                    }

                    return false;
                }

                Object this$middleAndSurname = this.getMiddleAndSurname();
                Object other$middleAndSurname = other.getMiddleAndSurname();
                if (this$middleAndSurname == null) {
                    if (other$middleAndSurname != null) {
                        return false;
                    }
                } else if (!this$middleAndSurname.equals(other$middleAndSurname)) {
                    return false;
                }

                Object this$specialities = this.getSpecialities();
                Object other$specialities = other.getSpecialities();
                if (this$specialities == null) {
                    if (other$specialities != null) {
                        return false;
                    }
                } else if (!this$specialities.equals(other$specialities)) {
                    return false;
                }

                label182: {
                    Object this$unitIds = this.getUnitIds();
                    Object other$unitIds = other.getUnitIds();
                    if (this$unitIds == null) {
                        if (other$unitIds == null) {
                            break label182;
                        }
                    } else if (this$unitIds.equals(other$unitIds)) {
                        break label182;
                    }

                    return false;
                }

                label175: {
                    Object this$title = this.getTitle();
                    Object other$title = other.getTitle();
                    if (this$title == null) {
                        if (other$title == null) {
                            break label175;
                        }
                    } else if (this$title.equals(other$title)) {
                        break label175;
                    }

                    return false;
                }

                Object this$healthCareProfessionalLicence = this.getHealthCareProfessionalLicence();
                Object other$healthCareProfessionalLicence = other.getHealthCareProfessionalLicence();
                if (this$healthCareProfessionalLicence == null) {
                    if (other$healthCareProfessionalLicence != null) {
                        return false;
                    }
                } else if (!this$healthCareProfessionalLicence.equals(other$healthCareProfessionalLicence)) {
                    return false;
                }

                Object this$paTitle = this.getPaTitle();
                Object other$paTitle = other.getPaTitle();
                if (this$paTitle == null) {
                    if (other$paTitle != null) {
                        return false;
                    }
                } else if (!this$paTitle.equals(other$paTitle)) {
                    return false;
                }

                label154: {
                    Object this$personalPrescriptionCode = this.getPersonalPrescriptionCode();
                    Object other$personalPrescriptionCode = other.getPersonalPrescriptionCode();
                    if (this$personalPrescriptionCode == null) {
                        if (other$personalPrescriptionCode == null) {
                            break label154;
                        }
                    } else if (this$personalPrescriptionCode.equals(other$personalPrescriptionCode)) {
                        break label154;
                    }

                    return false;
                }

                label147: {
                    Object this$systemRoles = this.getSystemRoles();
                    Object other$systemRoles = other.getSystemRoles();
                    if (this$systemRoles == null) {
                        if (other$systemRoles == null) {
                            break label147;
                        }
                    } else if (this$systemRoles.equals(other$systemRoles)) {
                        break label147;
                    }

                    return false;
                }

                Object this$educationCodes = this.getEducationCodes();
                Object other$educationCodes = other.getEducationCodes();
                if (this$educationCodes == null) {
                    if (other$educationCodes != null) {
                        return false;
                    }
                } else if (!this$educationCodes.equals(other$educationCodes)) {
                    return false;
                }

                Object this$restrictions = this.getRestrictions();
                Object other$restrictions = other.getRestrictions();
                if (this$restrictions == null) {
                    if (other$restrictions != null) {
                        return false;
                    }
                } else if (!this$restrictions.equals(other$restrictions)) {
                    return false;
                }

                label126: {
                    Object this$fakeProperties = this.getFakeProperties();
                    Object other$fakeProperties = other.getFakeProperties();
                    if (this$fakeProperties == null) {
                        if (other$fakeProperties == null) {
                            break label126;
                        }
                    } else if (this$fakeProperties.equals(other$fakeProperties)) {
                        break label126;
                    }

                    return false;
                }

                label119: {
                    Object this$gender = this.getGender();
                    Object other$gender = other.getGender();
                    if (this$gender == null) {
                        if (other$gender == null) {
                            break label119;
                        }
                    } else if (this$gender.equals(other$gender)) {
                        break label119;
                    }

                    return false;
                }

                Object this$age = this.getAge();
                Object other$age = other.getAge();
                if (this$age == null) {
                    if (other$age != null) {
                        return false;
                    }
                } else if (!this$age.equals(other$age)) {
                    return false;
                }

                Object this$healthCareProfessionalLicenceType = this.getHealthCareProfessionalLicenceType();
                Object other$healthCareProfessionalLicenceType = other.getHealthCareProfessionalLicenceType();
                if (this$healthCareProfessionalLicenceType == null) {
                    if (other$healthCareProfessionalLicenceType != null) {
                        return false;
                    }
                } else if (!this$healthCareProfessionalLicenceType.equals(other$healthCareProfessionalLicenceType)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof HsaPerson;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.isProtectedPerson() ? 79 : 97);
        Object $hsaId = this.getHsaId();
        result = result * 59 + ($hsaId == null ? 43 : $hsaId.hashCode());
        Object $personalIdentityNumber = this.getPersonalIdentityNumber();
        result = result * 59 + ($personalIdentityNumber == null ? 43 : $personalIdentityNumber.hashCode());
        Object $givenName = this.getGivenName();
        result = result * 59 + ($givenName == null ? 43 : $givenName.hashCode());
        Object $middleAndSurname = this.getMiddleAndSurname();
        result = result * 59 + ($middleAndSurname == null ? 43 : $middleAndSurname.hashCode());
        Object $specialities = this.getSpecialities();
        result = result * 59 + ($specialities == null ? 43 : $specialities.hashCode());
        Object $unitIds = this.getUnitIds();
        result = result * 59 + ($unitIds == null ? 43 : $unitIds.hashCode());
        Object $title = this.getTitle();
        result = result * 59 + ($title == null ? 43 : $title.hashCode());
        Object $healthCareProfessionalLicence = this.getHealthCareProfessionalLicence();
        result = result * 59 + ($healthCareProfessionalLicence == null ? 43 : $healthCareProfessionalLicence.hashCode());
        Object $paTitle = this.getPaTitle();
        result = result * 59 + ($paTitle == null ? 43 : $paTitle.hashCode());
        Object $personalPrescriptionCode = this.getPersonalPrescriptionCode();
        result = result * 59 + ($personalPrescriptionCode == null ? 43 : $personalPrescriptionCode.hashCode());
        Object $systemRoles = this.getSystemRoles();
        result = result * 59 + ($systemRoles == null ? 43 : $systemRoles.hashCode());
        Object $educationCodes = this.getEducationCodes();
        result = result * 59 + ($educationCodes == null ? 43 : $educationCodes.hashCode());
        Object $restrictions = this.getRestrictions();
        result = result * 59 + ($restrictions == null ? 43 : $restrictions.hashCode());
        Object $fakeProperties = this.getFakeProperties();
        result = result * 59 + ($fakeProperties == null ? 43 : $fakeProperties.hashCode());
        Object $gender = this.getGender();
        result = result * 59 + ($gender == null ? 43 : $gender.hashCode());
        Object $age = this.getAge();
        result = result * 59 + ($age == null ? 43 : $age.hashCode());
        Object $healthCareProfessionalLicenceType = this.getHealthCareProfessionalLicenceType();
        result = result * 59 + ($healthCareProfessionalLicenceType == null ? 43 : $healthCareProfessionalLicenceType.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getHsaId();
        return "HsaPerson(hsaId=" + var10000 + ", personalIdentityNumber=" + this.getPersonalIdentityNumber() + ", givenName=" + this.getGivenName() + ", middleAndSurname=" + this.getMiddleAndSurname() + ", protectedPerson=" + this.isProtectedPerson() + ", specialities=" + this.getSpecialities() + ", unitIds=" + this.getUnitIds() + ", title=" + this.getTitle() + ", healthCareProfessionalLicence=" + this.getHealthCareProfessionalLicence() + ", paTitle=" + this.getPaTitle() + ", personalPrescriptionCode=" + this.getPersonalPrescriptionCode() + ", systemRoles=" + this.getSystemRoles() + ", educationCodes=" + this.getEducationCodes() + ", restrictions=" + this.getRestrictions() + ", fakeProperties=" + this.getFakeProperties() + ", gender=" + this.getGender() + ", age=" + this.getAge() + ", healthCareProfessionalLicenceType=" + this.getHealthCareProfessionalLicenceType() + ")";
    }

    public static class HealthCareProfessionalLicenceType {
        private String healthCareProfessionalLicenceCode;
        private String healthCareProfessionalLicenceName;

        public HealthCareProfessionalLicenceType() {
        }

        public String getHealthCareProfessionalLicenceCode() {
            return this.healthCareProfessionalLicenceCode;
        }

        public String getHealthCareProfessionalLicenceName() {
            return this.healthCareProfessionalLicenceName;
        }

        public void setHealthCareProfessionalLicenceCode(String healthCareProfessionalLicenceCode) {
            this.healthCareProfessionalLicenceCode = healthCareProfessionalLicenceCode;
        }

        public void setHealthCareProfessionalLicenceName(String healthCareProfessionalLicenceName) {
            this.healthCareProfessionalLicenceName = healthCareProfessionalLicenceName;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof HealthCareProfessionalLicenceType)) {
                return false;
            } else {
                HealthCareProfessionalLicenceType other = (HealthCareProfessionalLicenceType)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$healthCareProfessionalLicenceCode = this.getHealthCareProfessionalLicenceCode();
                    Object other$healthCareProfessionalLicenceCode = other.getHealthCareProfessionalLicenceCode();
                    if (this$healthCareProfessionalLicenceCode == null) {
                        if (other$healthCareProfessionalLicenceCode != null) {
                            return false;
                        }
                    } else if (!this$healthCareProfessionalLicenceCode.equals(other$healthCareProfessionalLicenceCode)) {
                        return false;
                    }

                    Object this$healthCareProfessionalLicenceName = this.getHealthCareProfessionalLicenceName();
                    Object other$healthCareProfessionalLicenceName = other.getHealthCareProfessionalLicenceName();
                    if (this$healthCareProfessionalLicenceName == null) {
                        if (other$healthCareProfessionalLicenceName != null) {
                            return false;
                        }
                    } else if (!this$healthCareProfessionalLicenceName.equals(other$healthCareProfessionalLicenceName)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof HealthCareProfessionalLicenceType;
        }

        public int hashCode() {
            int result = 1;
            Object $healthCareProfessionalLicenceCode = this.getHealthCareProfessionalLicenceCode();
            result = result * 59 + ($healthCareProfessionalLicenceCode == null ? 43 : $healthCareProfessionalLicenceCode.hashCode());
            Object $healthCareProfessionalLicenceName = this.getHealthCareProfessionalLicenceName();
            result = result * 59 + ($healthCareProfessionalLicenceName == null ? 43 : $healthCareProfessionalLicenceName.hashCode());
            return result;
        }

        public String toString() {
            String var10000 = this.getHealthCareProfessionalLicenceCode();
            return "HsaPerson.HealthCareProfessionalLicenceType(healthCareProfessionalLicenceCode=" + var10000 + ", healthCareProfessionalLicenceName=" + this.getHealthCareProfessionalLicenceName() + ")";
        }
    }

    public static class Speciality {
        private String specialityName;
        private String specialityCode;

        public Speciality() {
        }

        public String getSpecialityName() {
            return this.specialityName;
        }

        public String getSpecialityCode() {
            return this.specialityCode;
        }

        public void setSpecialityName(String specialityName) {
            this.specialityName = specialityName;
        }

        public void setSpecialityCode(String specialityCode) {
            this.specialityCode = specialityCode;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Speciality)) {
                return false;
            } else {
                Speciality other = (Speciality)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$specialityName = this.getSpecialityName();
                    Object other$specialityName = other.getSpecialityName();
                    if (this$specialityName == null) {
                        if (other$specialityName != null) {
                            return false;
                        }
                    } else if (!this$specialityName.equals(other$specialityName)) {
                        return false;
                    }

                    Object this$specialityCode = this.getSpecialityCode();
                    Object other$specialityCode = other.getSpecialityCode();
                    if (this$specialityCode == null) {
                        if (other$specialityCode != null) {
                            return false;
                        }
                    } else if (!this$specialityCode.equals(other$specialityCode)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof Speciality;
        }

        public int hashCode() {
            int result = 1;
            Object $specialityName = this.getSpecialityName();
            result = result * 59 + ($specialityName == null ? 43 : $specialityName.hashCode());
            Object $specialityCode = this.getSpecialityCode();
            result = result * 59 + ($specialityCode == null ? 43 : $specialityCode.hashCode());
            return result;
        }

        public String toString() {
            String var10000 = this.getSpecialityName();
            return "HsaPerson.Speciality(specialityName=" + var10000 + ", specialityCode=" + this.getSpecialityCode() + ")";
        }
    }

    public static class Restrictions {
        private String restrictionCode;
        private String restrictionName;

        public Restrictions() {
        }

        public String getRestrictionCode() {
            return this.restrictionCode;
        }

        public String getRestrictionName() {
            return this.restrictionName;
        }

        public void setRestrictionCode(String restrictionCode) {
            this.restrictionCode = restrictionCode;
        }

        public void setRestrictionName(String restrictionName) {
            this.restrictionName = restrictionName;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Restrictions)) {
                return false;
            } else {
                Restrictions other = (Restrictions)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$restrictionCode = this.getRestrictionCode();
                    Object other$restrictionCode = other.getRestrictionCode();
                    if (this$restrictionCode == null) {
                        if (other$restrictionCode != null) {
                            return false;
                        }
                    } else if (!this$restrictionCode.equals(other$restrictionCode)) {
                        return false;
                    }

                    Object this$restrictionName = this.getRestrictionName();
                    Object other$restrictionName = other.getRestrictionName();
                    if (this$restrictionName == null) {
                        if (other$restrictionName != null) {
                            return false;
                        }
                    } else if (!this$restrictionName.equals(other$restrictionName)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof Restrictions;
        }

        public int hashCode() {
            int result = 1;
            Object $restrictionCode = this.getRestrictionCode();
            result = result * 59 + ($restrictionCode == null ? 43 : $restrictionCode.hashCode());
            Object $restrictionName = this.getRestrictionName();
            result = result * 59 + ($restrictionName == null ? 43 : $restrictionName.hashCode());
            return result;
        }

        public String toString() {
            String var10000 = this.getRestrictionCode();
            return "HsaPerson.Restrictions(restrictionCode=" + var10000 + ", restrictionName=" + this.getRestrictionName() + ")";
        }
    }

    public static class PaTitle {
        private String titleCode;
        private String titleName;

        public PaTitle() {
        }

        public String getTitleCode() {
            return this.titleCode;
        }

        public String getTitleName() {
            return this.titleName;
        }

        public void setTitleCode(String titleCode) {
            this.titleCode = titleCode;
        }

        public void setTitleName(String titleName) {
            this.titleName = titleName;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof PaTitle)) {
                return false;
            } else {
                PaTitle other = (PaTitle)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$titleCode = this.getTitleCode();
                    Object other$titleCode = other.getTitleCode();
                    if (this$titleCode == null) {
                        if (other$titleCode != null) {
                            return false;
                        }
                    } else if (!this$titleCode.equals(other$titleCode)) {
                        return false;
                    }

                    Object this$titleName = this.getTitleName();
                    Object other$titleName = other.getTitleName();
                    if (this$titleName == null) {
                        if (other$titleName != null) {
                            return false;
                        }
                    } else if (!this$titleName.equals(other$titleName)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof PaTitle;
        }

        public int hashCode() {
            int result = 1;
            Object $titleCode = this.getTitleCode();
            result = result * 59 + ($titleCode == null ? 43 : $titleCode.hashCode());
            Object $titleName = this.getTitleName();
            result = result * 59 + ($titleName == null ? 43 : $titleName.hashCode());
            return result;
        }

        public String toString() {
            String var10000 = this.getTitleCode();
            return "HsaPerson.PaTitle(titleCode=" + var10000 + ", titleName=" + this.getTitleName() + ")";
        }
    }
}
