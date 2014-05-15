/*
 * Inera Medcert - Sjukintygsapplikation
 *
 * Copyright (C) 2010-2011 Inera AB (http://www.inera.se)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package se.inera.certificate.mc2wc.medcert.jpa.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Pär Wenåker
 */
@Embeddable
public class AddressCare {

    @Column(name = "CARE_GIVER_ID")
    protected String careGiverId;
    @Column(name = "CARE_GIVER_NAME")
    protected String careGiverName;
    @Column(name = "CARE_UNIT_ID")
    protected String careUnitId;
    @Column(name = "CARE_UNIT_NAME")
    protected String careUnitName;
    @Column(name = "CARE_UNIT_WORKPLACE_CODE")
    protected String careUnitWorkplaceCode;
    @Column(name = "CARE_PERSON_ID")
    protected String carePersonId;
    @Column(name = "CARE_PERSON_NAME")
    protected String carePersonName;
    @Column(name = "CARE_PERSON_CODE")
    protected String carePersonCode;
    @Column(name = "CARE_POSTAL_ADDRESS")
    protected String postalAddress;
    @Column(name = "CARE_POSTAL_NUMBER")
    protected String postalNumber;
    @Column(name = "CARE_POSTAL_CITY")
    protected String postalCity;
    @Column(name = "CARE_PHONE_NUMBER")
    protected String phoneNumber;
    @Column(name = "CARE_EMAIL_ADDRESS")
    protected String emailAddress;

    public String getCareGiverId() {
        return careGiverId;
    }

    public void setCareGiverId(String careGiverId) {
        this.careGiverId = careGiverId;
    }

    public String getCareGiverName() {
        return careGiverName;
    }

    public void setCareGiverName(String careGiverName) {
        this.careGiverName = careGiverName;
    }

    public String getCareUnitId() {
        return careUnitId;
    }

    public void setCareUnitId(String careUnitId) {
        this.careUnitId = careUnitId;
    }

    public String getCareUnitName() {
        return careUnitName;
    }

    public void setCareUnitName(String careUnitName) {
        this.careUnitName = careUnitName;
    }

    public String getCareUnitWorkplaceCode() {
        return careUnitWorkplaceCode;
    }

    public void setCareUnitWorkplaceCode(String careUnitWorkplaceCode) {
        this.careUnitWorkplaceCode = careUnitWorkplaceCode;
    }

    public String getCarePersonId() {
        return carePersonId;
    }

    public void setCarePersonId(String carePersonId) {
        this.carePersonId = carePersonId;
    }

    public String getCarePersonName() {
        return carePersonName;
    }

    public void setCarePersonName(String carePersonName) {
        this.carePersonName = carePersonName;
    }

    public String getCarePersonCode() {
        return carePersonCode;
    }

    public void setCarePersonCode(String carePersonCode) {
        this.carePersonCode = carePersonCode;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(String postalAddress) {
        this.postalAddress = postalAddress;
    }

    public String getPostalNumber() {
        return postalNumber;
    }

    public void setPostalNumber(String postalNumber) {
        this.postalNumber = postalNumber;
    }

    public String getPostalCity() {
        return postalCity;
    }

    public void setPostalCity(String postalCity) {
        this.postalCity = postalCity;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
