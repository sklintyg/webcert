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

package se.inera.intyg.webcert.web.service.dto;

import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.ArrayList;
import java.util.List;

public class HoSPerson {

    private String hsaId;

    private String namn;

    private String forskrivarkod;

    private String befattning;

    private List<String> specialiseringar;

    public HoSPerson() {

    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getForskrivarkod() {
        return forskrivarkod;
    }

    public void setForskrivarkod(String forskrivarkod) {
        this.forskrivarkod = forskrivarkod;
    }

    public String getBefattning() {
        return befattning;
    }

    public void setBefattning(String befattning) {
        this.befattning = befattning;
    }

    public List<String> getSpecialiseringar() {
        if (specialiseringar == null) {
            specialiseringar = new ArrayList<>();
        }
        return specialiseringar;
    }

    public static HoSPerson create(WebCertUser user) {
        HoSPerson person = new HoSPerson();
        person.setHsaId(user.getHsaId());
        person.setNamn(user.getNamn());
        person.setForskrivarkod(user.getForskrivarkod());
        person.getSpecialiseringar().addAll(user.getSpecialiseringar());
        person.setBefattning(user.getBefattningar() != null && user.getBefattningar().size() > 0 ? user.getBefattningar().get(0) : null);
        // TODO Sätt befattning
        return person;
    }
}
