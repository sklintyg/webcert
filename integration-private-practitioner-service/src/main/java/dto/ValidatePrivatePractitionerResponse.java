package dto;

import lombok.Data;

@Data
public class ValidatePrivatePractitionerResponse {

  private ValidatePrivatePractitionerResultCode resultCode;
  private String resultText;

}
