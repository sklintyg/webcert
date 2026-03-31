/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.servicenow.stub.settings.api;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.integration.servicenow.stub.settings.service.ServiceNowStubSettingsApiService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stubs/servicenowstub/settings")
public class ServiceNowStubSettingsApi {

  private final ServiceNowStubSettingsApiService stubSettingsService;

  @GetMapping("/set/{returnValue}")
  public ResponseEntity<String> setReturnValue(@PathVariable("returnValue") String returnValue) {
    if ("true".equals(returnValue) || "false".equals(returnValue)) {
      stubSettingsService.setSubscriptionReturnValue("true".equals(returnValue));
      return ResponseEntity.ok(
          "Set stub return value to '" + returnValue + "' and cleared all active subscriptions.");
    }
    return ResponseEntity.badRequest().body("Accepted parameter values are 'true' or 'false'.");
  }

  @GetMapping("/get")
  public Boolean getReturnValue() {
    return stubSettingsService.getSubscriptionReturnValue();
  }

  @GetMapping("/setactive/{orgNumber}/{serviceCode}")
  public String setActiveSubscription(
      @PathVariable String orgNumber, @PathVariable String serviceCode) {
    stubSettingsService.setActiveSubscription(orgNumber, serviceCode);
    return "Set subscription active for organization '"
        + orgNumber
        + "' and serviceCode '"
        + serviceCode
        + "'.";
  }

  @GetMapping("/removeactive/{orgNumber}")
  public String removeActiveSubscriptions(@PathVariable String orgNumber) {
    stubSettingsService.removeActiveSubscriptions(orgNumber);
    return "Removed active subscriptions for organization '" + orgNumber + "'.";
  }

  @GetMapping("/clearactive")
  public String clearActiveSubscriptions() {
    stubSettingsService.clearActiveSubscriptions();
    return "Cleared active subscription for all organizations.";
  }

  @GetMapping("/getactive")
  public Map<String, List<String>> getActiveSubscriptions() {
    return stubSettingsService.getActiveSubscriptions();
  }

  @GetMapping("/seterror/{errorCode}")
  public String setServiceError(@PathVariable int errorCode) {
    stubSettingsService.setHttpError(errorCode);
    return "Set stub to return Http error with code " + errorCode + " (if it exists, else 500).";
  }

  @GetMapping("/clearerror")
  public String clearServiceError() {
    stubSettingsService.clearHttpError();
    return "Cleared ServiceNow stub Http error code.";
  }
}
