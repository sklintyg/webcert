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
package se.inera.intyg.webcert.infra.pu.integration.api.model;

import java.io.Serializable;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class PersonSvar implements Serializable {

  private static final long serialVersionUID = 2L;

  public static PersonSvar found(final Person person) {
    return new PersonSvar(person, Status.FOUND);
  }

  public static PersonSvar notFound() {
    return new PersonSvar(null, Status.NOT_FOUND);
  }

  public static PersonSvar error() {
    return new PersonSvar(null, Status.ERROR);
  }

  private final Person person;
  private final Status status;

  protected PersonSvar(Person person, Status status) {
    this.person = person;
    this.status = status;
  }

  public Person getPerson() {
    return person;
  }

  public Status getStatus() {
    return status;
  }

  public enum Status {
    FOUND,
    NOT_FOUND,
    ERROR
  }
}
