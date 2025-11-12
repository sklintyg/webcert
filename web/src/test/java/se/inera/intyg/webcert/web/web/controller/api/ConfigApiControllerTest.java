package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.postnummer.model.Omrade;
import se.inera.intyg.infra.integration.postnummer.service.PostnummerService;

@ExtendWith(MockitoExtension.class)
class ConfigApiControllerTest {

  private static final String ZIP_CODE = "12345";

  @Mock
  PostnummerService postnummerService;

  @InjectMocks
  ConfigApiController configApiController;

  @Test
  void shouldGetAreaByZipCode() {

    when(postnummerService.getOmradeByPostnummer(ZIP_CODE)).thenReturn(List.of(
        new Omrade(ZIP_CODE, "City", "Municipality", "County")
    ));

    final var response = configApiController.getAreaByZipCode(ZIP_CODE);

    assertAll(
        () -> assertEquals(ZIP_CODE, response.getFirst().zipCode()),
        () -> assertEquals("City", response.getFirst().city()),
        () -> assertEquals("Municipality", response.getFirst().municipality()),
        () -> assertEquals("County", response.getFirst().county())
        );
  }

}