package se.inera.intyg.webcert.web.service.util;

import se.inera.certificate.modules.support.api.dto.HoSPersonal;
import se.inera.certificate.modules.support.api.dto.Vardenhet;
import se.inera.webcert.hsa.model.AbstractVardenhet;
import se.inera.webcert.hsa.model.SelectableVardenhet;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;

public final class UpdateUserUtil {

    private UpdateUserUtil() {
    }

    /**
     * Create a user object from WebCertUser.
     * @param user {@link WebCertUser}
     */
    public static HoSPersonal createUserObject(WebCertUser user) {
        SelectableVardenhet valdVardgivare = user.getValdVardgivare();
        se.inera.certificate.modules.support.api.dto.Vardgivare vardgivare = new se.inera.certificate.modules.support.api.dto.Vardgivare(
                valdVardgivare.getId(), valdVardgivare.getNamn());

        AbstractVardenhet valdVardenhet = (AbstractVardenhet) user.getValdVardenhet();
        Vardenhet vardenhet = new se.inera.certificate.modules.support.api.dto.Vardenhet(
                valdVardenhet.getId(), valdVardenhet.getNamn(), valdVardenhet.getPostadress(), valdVardenhet.getPostnummer(),
                valdVardenhet.getPostort(), valdVardenhet.getTelefonnummer(), valdVardenhet.getEpost(), valdVardenhet.getArbetsplatskod(), vardgivare);

        HoSPersonal hosPerson = new HoSPersonal(
                user.getHsaId(),
                user.getNamn(), user.getForskrivarkod(), user.getTitel(), user.getSpecialiseringar(), vardenhet);
        return hosPerson;
    }

    public static VardpersonReferens createVardpersonFromWebCertUser(WebCertUser user) {
        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(user.getNamn());
        vardPerson.setHsaId(user.getHsaId());

        return vardPerson;
    }

    public static VardpersonReferens createVardpersonFromHosPerson(HoSPerson hosPerson) {
        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(hosPerson.getNamn());
        vardPerson.setHsaId(hosPerson.getHsaId());

        return vardPerson;
    }

}
