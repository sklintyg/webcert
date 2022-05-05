/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.facade.dto;

import java.util.Arrays;
import java.util.List;

public class CareProviderForUserResponseDTO {

    private List<CareProviderDTO> careProviders;

    public static CareProviderForUserResponseDTO create() {
        final var careProvider = new CareProviderDTO(
            "TSTNMT2321000156-ALFA",
            "Alfa Regionen",
            "Storgatan 1",
            "12345",
            "Småmåla",
            "0101234567890",
            "AlfaHC@webcert.invalid.se",
            Arrays.asList(
                new CareUnitDTO(
                    "TSTNMT2321000156-ALHC",
                    "Alfa Hjärtcentrum",
                    "Storgatan 1",
                    "12345",
                    "Småmåla",
                    "0101234567890",
                    "AlfaHC@webcert.invalid.se",
                    Arrays.asList(
                        new UnitDTO(
                            "TSTNMT2321000156-ALFM",
                            "Alfa Fysiologiskamottagningen",
                            "Storgatan 1",
                            "12345",
                            "Småmåla",
                            "0101234890",
                            "AlfaHC@webcert.invalid.se"
                        )
                    )
                ),
                new CareUnitDTO(
                    "TSTNMT2321000156-ALMC",
                    "Alfa Medicincentrum",
                    "Storgatan 1",
                    "12345",
                    "Småmåla",
                    "0101234567890",
                    "AlfaHC@webcert.invalid.se",
                    Arrays.asList(
                        new UnitDTO(
                            "TSTNMT2321000156-ALAM",
                            "Alfa Allergimottagningen",
                            "Storgatan 1",
                            "12345",
                            "Småmåla",
                            "0101234890",
                            "AlfaHC@webcert.invalid.se"
                        ),
                        new UnitDTO(
                            "TSTNMT2321000156-ALHM",
                            "Alfa Hudmottagningen",
                            "Storgatan 1",
                            "12345",
                            "Småmåla",
                            "0101234890",
                            "AlfaHC@webcert.invalid.se"
                        )
                    )
                )
            )
        );

        final var careProviderForUserResponseDTO = new CareProviderForUserResponseDTO();
        careProviderForUserResponseDTO.setCareProviders(Arrays.asList(careProvider));
        return careProviderForUserResponseDTO;
    }

    public List<CareProviderDTO> getCareProviders() {
        return careProviders;
    }

    public void setCareProviders(List<CareProviderDTO> careProviders) {
        this.careProviders = careProviders;
    }
}

class CareProviderDTO {

    private String unitId;
    private String unitName;
    private String address;
    private String zipCode;
    private String city;
    private String phoneNumber;
    private String email;
    private List<CareUnitDTO> careUnits;

    CareProviderDTO(String unitId, String unitName, String address,
        String zipCode, String city, String phoneNumber, String email,
        List<CareUnitDTO> careUnits) {
        this.unitId = unitId;
        this.unitName = unitName;
        this.address = address;
        this.zipCode = zipCode;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.careUnits = careUnits;
    }

    CareProviderDTO() {
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<CareUnitDTO> getCareUnits() {
        return careUnits;
    }

    public void setCareUnits(List<CareUnitDTO> careUnits) {
        this.careUnits = careUnits;
    }
}

class CareUnitDTO {

    private String unitId;
    private String unitName;
    private String address;
    private String zipCode;
    private String city;
    private String phoneNumber;
    private String email;
    private List<UnitDTO> units;

    CareUnitDTO(String unitId, String unitName, String address, String zipCode,
        String city, String phoneNumber, String email, List<UnitDTO> units) {
        this.unitId = unitId;
        this.unitName = unitName;
        this.address = address;
        this.zipCode = zipCode;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.units = units;
    }

    CareUnitDTO() {
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<UnitDTO> getUnits() {
        return units;
    }

    public void setUnits(List<UnitDTO> units) {
        this.units = units;
    }
}

class UnitDTO {

    private String unitId;
    private String unitName;
    private String address;
    private String zipCode;
    private String city;
    private String phoneNumber;
    private String email;

    UnitDTO(String unitId, String unitName, String address, String zipCode,
        String city, String phoneNumber, String email) {
        this.unitId = unitId;
        this.unitName = unitName;
        this.address = address;
        this.zipCode = zipCode;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    UnitDTO() {
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}