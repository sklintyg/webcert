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
public class Patient {

    @Column(name = "PATIENT_NAME")
    protected String name;

    @Column(name = "PATIENT_SSN")
    protected String ssn;

    public String getName() {
        return name;
    }

    public String getSsn() {
        return ssn;
    }
}
