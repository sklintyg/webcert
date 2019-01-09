/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.util;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.AbstractVardenhet;
import se.inera.intyg.infra.integration.hsa.model.Mottagning;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.common.model.IntygUser;

import java.util.Optional;

/**
 * Helper for finding various HSA organization entities based on hsaId from a User's Vardgivare -> Vardenhet ->
 * Mottagning tree.
 *
 * Created by eriklupander on 2017-09-27.
 */
public final class HoSPersonHelper {

    private HoSPersonHelper() {
    }

    public static Optional<AbstractVardenhet> findVardenhetEllerMottagning(IntygUser user, String enhetsId) {
        for (se.inera.intyg.infra.integration.hsa.model.Vardgivare vg : user.getVardgivare()) {
            for (se.inera.intyg.infra.integration.hsa.model.Vardenhet ve : vg.getVardenheter()) {
                if (enhetsId.equalsIgnoreCase(ve.getId())) {
                    return Optional.of(ve);
                }
                for (se.inera.intyg.infra.integration.hsa.model.Mottagning m : ve.getMottagningar()) {
                    if (enhetsId.equalsIgnoreCase(m.getId())) {
                        return Optional.of(m);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<Vardgivare> findVardgivare(IntygUser user, String vardgivareHsaId) {
        for (Vardgivare vg : user.getVardgivare()) {
            if (vg.getId().equalsIgnoreCase(vardgivareHsaId)) {
                return Optional.of(vg);
            }
        }
        return Optional.empty();
    }

    public static Optional<Vardgivare> findVardgivareForMottagning(IntygUser user, String mottagningsId) {
        for (se.inera.intyg.infra.integration.hsa.model.Vardgivare vg : user.getVardgivare()) {
            for (se.inera.intyg.infra.integration.hsa.model.Vardenhet ve : vg.getVardenheter()) {
                for (se.inera.intyg.infra.integration.hsa.model.Mottagning m : ve.getMottagningar()) {
                    if (mottagningsId.equalsIgnoreCase(m.getId())) {
                        return Optional.of(vg);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static Vardenhet createVardenhetFromIntygUser(String enhetId, IntygUser user) {

        AbstractVardenhet enhet = HoSPersonHelper.findVardenhetEllerMottagning(user, enhetId)
                .orElseThrow(() -> new IllegalStateException("User '" + user.getHsaId() + "' has no MIU for care unit '" + enhetId + "'"));

        if (enhet instanceof se.inera.intyg.infra.integration.hsa.model.Vardenhet) {
            se.inera.intyg.infra.integration.hsa.model.Vardenhet hsaVardenhet =
                    (se.inera.intyg.infra.integration.hsa.model.Vardenhet) enhet;
            se.inera.intyg.infra.integration.hsa.model.Vardgivare hsaVardgivare = HoSPersonHelper
                    .findVardgivare(user, hsaVardenhet.getVardgivareHsaId())
                    .orElseThrow(() -> new IllegalStateException("Unable to find parent vårdgivare for vardenhet '" + enhetId + "'"));

            Vardenhet vardenhet = new Vardenhet();
            vardenhet.setEnhetsnamn(hsaVardenhet.getNamn());
            vardenhet.setEnhetsid(hsaVardenhet.getId());
            vardenhet.setArbetsplatsKod(hsaVardenhet.getArbetsplatskod());
            vardenhet.setPostadress(hsaVardenhet.getPostadress());
            vardenhet.setPostnummer(hsaVardenhet.getPostnummer());
            vardenhet.setPostort(hsaVardenhet.getPostort());
            vardenhet.setTelefonnummer(hsaVardenhet.getTelefonnummer());

            se.inera.intyg.common.support.model.common.internal.Vardgivare vardgivare =
                    new se.inera.intyg.common.support.model.common.internal.Vardgivare();

            vardgivare.setVardgivarid(hsaVardgivare.getId());
            vardgivare.setVardgivarnamn(hsaVardgivare.getNamn());
            vardenhet.setVardgivare(vardgivare);
            return vardenhet;
        }
        if (enhet instanceof Mottagning) {
            Mottagning m = (Mottagning) enhet;
            se.inera.intyg.infra.integration.hsa.model.Vardgivare hsaVardgivare = HoSPersonHelper
                    .findVardgivareForMottagning(user, m.getId())
                    .orElseThrow(() -> new IllegalStateException("Unable to find parent vårdgivare for mottagning '" + enhetId + "'"));

            Vardenhet vardenhet = new Vardenhet();
            vardenhet.setEnhetsnamn(m.getNamn());
            vardenhet.setEnhetsid(m.getId());
            vardenhet.setArbetsplatsKod(m.getArbetsplatskod());
            vardenhet.setPostadress(m.getPostadress());
            vardenhet.setPostnummer(m.getPostnummer());
            vardenhet.setPostort(m.getPostort());
            vardenhet.setTelefonnummer(m.getTelefonnummer());

            se.inera.intyg.common.support.model.common.internal.Vardgivare vardgivare =
                    new se.inera.intyg.common.support.model.common.internal.Vardgivare();

            vardgivare.setVardgivarid(hsaVardgivare.getId());
            vardgivare.setVardgivarnamn(hsaVardgivare.getNamn());
            vardenhet.setVardgivare(vardgivare);
            return vardenhet;
        }
        throw new IllegalArgumentException(
                "AbstractVardenhet instance passed to createVardenhetFromIntygUser was of unsupported type: " + enhet.getClass().getName());
    }

    public static void enrichHoSPerson(HoSPersonal hosPerson, IntygUser user) {
        // set titel, medarbetaruppdrag, befattningar and specialiteter from user object
        hosPerson.setTitel(user.getTitel());
        hosPerson.setMedarbetarUppdrag(user.getSelectedMedarbetarUppdragNamn());
        hosPerson.getBefattningar().addAll(user.getBefattningar());
        hosPerson.getSpecialiteter().addAll(user.getSpecialiseringar());
    }

}
