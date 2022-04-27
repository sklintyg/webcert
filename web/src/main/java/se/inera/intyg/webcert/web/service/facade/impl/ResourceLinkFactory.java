package se.inera.intyg.webcert.web.service.facade.impl;

import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

import java.util.Objects;

public class ResourceLinkFactory {

    private static final String RENEW_NAME = "Förnya";
    private static final String RENEW_DESCRIPTION = "Skapar en redigerbar kopia av intyget på den enhet som du är inloggad på.";

    private static final String FORWARD_NAME = "Vidarebefodra utkast";
    private static final String FORWARD_DESCRIPTION = "Skapar ett e-postmeddelande i din e-postklient med en direktlänk till utkastet.";

    public static final String EVENTUAL_COMPLEMENTARY_REQUEST_WONT_BE_MARKED_READY = "Eventuell kompletteringsbegäran kommer inte "
            + "att klarmarkeras.";
    public static final String EVENTUAL_COMPLEMENTARY_WILL_BE_MARKED_READY = "Eventuell kompletteringsbegäran kommer att klarmarkeras.";

    public static ResourceLinkDTO forward() {
        return ResourceLinkDTO.create(
                ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
                FORWARD_NAME,
                FORWARD_DESCRIPTION,
                true
        );
    }

    public static ResourceLinkDTO forwardGeneric() {
        return ResourceLinkDTO.create(
                ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
                "Vidarebefordra",
                FORWARD_DESCRIPTION,
                true
        );
    }

    public static ResourceLinkDTO renew(String loggedInUnitId, String savedUnitId, String certificateType) {
        return ResourceLinkDTO.create(
                ResourceLinkTypeDTO.RENEW_CERTIFICATE,
                RENEW_NAME,
                RENEW_DESCRIPTION,
                getRenewBody(loggedInUnitId, savedUnitId, certificateType),
                true
        );
    }

    public static ResourceLinkDTO read() {
        return ResourceLinkDTO.create(
                ResourceLinkTypeDTO.READ_CERTIFICATE,
                "Öppna",
                "",
                true
        );
    }

    private static String getRenewBody(String loggedInUnitId, String savedUnitId, String certificateType) {
        if (isLisjpCertificate(certificateType)) {
            final var complementaryText =
                    isUserAndCertificateFromSameCareUnit(loggedInUnitId, savedUnitId)
                            ? EVENTUAL_COMPLEMENTARY_WILL_BE_MARKED_READY
                            : EVENTUAL_COMPLEMENTARY_REQUEST_WONT_BE_MARKED_READY;

            return String.format(
                    "Förnya intyg kan användas vid förlängning av en sjukskrivning. När ett intyg förnyas skapas ett nytt intygsutkast"
                            + " med viss information från det ursprungliga intyget.<br><br>\n"
                            + "Uppgifterna i det nya intygsutkastet går att ändra innan det signeras.<br><br>\n"
                            + "De uppgifter som inte kommer med till det nya utkastet är:<br><br>\n"
                            + "<ul>\n"
                            + "<li>Sjukskrivningsperiod och grad.</li>\n"
                            + "<li>Valet om man vill ha kontakt med Försäkringskassan.</li>\n"
                            + "<li>Referenser som intyget baseras på.</li>\n"
                            + "</ul>\n"
                            + "<br>%s<br><br>\n"
                            + "Det nya utkastet skapas på den enhet du är inloggad på.", complementaryText);
        } else {
            return
                    "Förnya intyg kan användas vid förlängning av en sjukskrivning. När ett intyg förnyas skapas ett nytt intygsutkast"
                            + " med viss information från det ursprungliga intyget.<br><br>"
                            + "Uppgifterna i det nya intygsutkastet går att ändra innan det signeras.<br><br>"
                            + "De uppgifter som inte kommer med till det nya utkastet är:<br><br>"
                            + "<ul>"
                            + "<li>Valet om diagnos ska förmedlas till arbetsgivaren</li>"
                            + "<li>Valet om funktionsnedsättning ska förmedlas till arbetsgivaren</li>"
                            + "<li>Sjukskrivningsperiod och grad</li>"
                            + "<li>Valet om man vill ha kontakt med arbetsgivaren</li>"
                            + "<li>Referenser som intyget baseras på</li>"
                            + "</ul>"
                            + "<br>Det nya utkastet skapas på den enhet du är inloggad på.";
        }
    }

    private static Boolean isLisjpCertificate(String certificateType) {
        return Objects.equals(certificateType, LisjpEntryPoint.MODULE_ID);
    }

    private static Boolean isUserAndCertificateFromSameCareUnit(String loggedInCareUnitId, String savedUnitId) {
        return Objects.equals(loggedInCareUnitId, savedUnitId);
    }
}
