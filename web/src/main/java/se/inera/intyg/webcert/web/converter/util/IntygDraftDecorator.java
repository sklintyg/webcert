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
package se.inera.intyg.webcert.web.converter.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@Component
public class IntygDraftDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(IntygDraftDecorator.class);

    private IntygModuleRegistry intygModuleRegistry;

    @Autowired
    public IntygDraftDecorator(IntygModuleRegistry intygModuleRegistry) {
        this.intygModuleRegistry = intygModuleRegistry;
    }

    /**
     * Decorates the {@link ListIntygEntry} with the display name of the certificate type.
     *
     * @param listIntygEntries {@link List} of {@link ListIntygEntry} to decorate.
     */
    public void decorateWithCertificateTypeName(List<ListIntygEntry> listIntygEntries) {
        listIntygEntries.stream().forEach(listIntygEntry -> addCertificateTypeName(listIntygEntry));
    }

    private void addCertificateTypeName(ListIntygEntry listIntygEntry) {
        try {
            final ModuleEntryPoint moduleEntryPoint = intygModuleRegistry.getModuleEntryPoint(listIntygEntry.getIntygType());
            listIntygEntry.setIntygTypeName(moduleEntryPoint.getModuleName());
        } catch (ModuleNotFoundException e) {
            listIntygEntry.setIntygTypeName(listIntygEntry.getIntygType());
            LOG.error("Could not find ModuleEntryPoint for certificate type: " + listIntygEntry.getIntygType());
        }
    }

    /**
     * Decorates the {@link ListIntygEntry} with the display name of the certificate status.
     *
     * @param listIntygEntries {@link List} of {@link ListIntygEntry} to decorate.
     */
    public void decorateWithCertificateStatusName(List<ListIntygEntry> listIntygEntries) {
        listIntygEntries.stream().forEach(listIntygEntry -> addCertificateStatusName(listIntygEntry));
    }

    private void addCertificateStatusName(ListIntygEntry listIntygEntry) {
        UtkastStatus status = UtkastStatus.fromValue(listIntygEntry.getStatus());
        listIntygEntry.setStatusName(status.getKlartext());
    }
}
