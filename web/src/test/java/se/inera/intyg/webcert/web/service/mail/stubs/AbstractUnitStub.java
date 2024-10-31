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

import java.io.Serializable;

public abstract class AbstractUnitStub implements Serializable {
    private String id;
    private String name;
    private String mail;
    private String postalAddress;
    private String postalCode;
    private String postalTown;
    private String telephoneNumber;
    private String prescriptionCode;
    private String healthCareProviderOrgno;
    private String careProviderHsaId;
    private String countyCode;
    private String municipalityCode;

    public AbstractUnitStub() {
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getMail() {
        return this.mail;
    }

    public String getPostalAddress() {
        return this.postalAddress;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public String getPostalTown() {
        return this.postalTown;
    }

    public String getTelephoneNumber() {
        return this.telephoneNumber;
    }

    public String getPrescriptionCode() {
        return this.prescriptionCode;
    }

    public String getHealthCareProviderOrgno() {
        return this.healthCareProviderOrgno;
    }

    public String getCareProviderHsaId() {
        return this.careProviderHsaId;
    }

    public String getCountyCode() {
        return this.countyCode;
    }

    public String getMunicipalityCode() {
        return this.municipalityCode;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPostalAddress(String postalAddress) {
        this.postalAddress = postalAddress;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setPostalTown(String postalTown) {
        this.postalTown = postalTown;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public void setPrescriptionCode(String prescriptionCode) {
        this.prescriptionCode = prescriptionCode;
    }

    public void setHealthCareProviderOrgno(String healthCareProviderOrgno) {
        this.healthCareProviderOrgno = healthCareProviderOrgno;
    }

    public void setCareProviderHsaId(String careProviderHsaId) {
        this.careProviderHsaId = careProviderHsaId;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }
}

