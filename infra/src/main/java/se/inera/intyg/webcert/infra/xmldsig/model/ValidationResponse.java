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
package se.inera.intyg.webcert.infra.xmldsig.model;

import se.inera.intyg.infra.xmldsig.model.ValidationResult;

public class ValidationResponse {

  private ValidationResult signatureValid = ValidationResult.NOT_CHCEKED;
  private ValidationResult referencesValid = ValidationResult.NOT_CHCEKED;

  public ValidationResult getSignatureValid() {
    return signatureValid;
  }

  public void setSignatureValid(ValidationResult signatureValid) {
    this.signatureValid = signatureValid;
  }

  public ValidationResult getReferencesValid() {
    return referencesValid;
  }

  public void setReferencesValid(ValidationResult referencesValid) {
    this.referencesValid = referencesValid;
  }

  public boolean isValid() {
    return this.signatureValid == ValidationResult.OK
        && (this.referencesValid == ValidationResult.OK
            || this.referencesValid == ValidationResult.NOT_CHCEKED);
  }

  public static final class ValidationResponseBuilder {

    private ValidationResult signatureValid = ValidationResult.NOT_CHCEKED;
    private ValidationResult referencesValid = ValidationResult.NOT_CHCEKED;

    private ValidationResponseBuilder() {}

    public static ValidationResponseBuilder aValidationResponse() {
      return new ValidationResponseBuilder();
    }

    public ValidationResponseBuilder withSignatureValid(ValidationResult signatureValid) {
      this.signatureValid = signatureValid;
      return this;
    }

    public ValidationResponseBuilder withReferencesValid(ValidationResult referencesValid) {
      this.referencesValid = referencesValid;
      return this;
    }

    public ValidationResponse build() {
      ValidationResponse validationResponse = new ValidationResponse();
      validationResponse.setSignatureValid(signatureValid);
      validationResponse.setReferencesValid(referencesValid);
      return validationResponse;
    }
  }
}
