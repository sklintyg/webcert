/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade.dto;

import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.webcert.web.service.facade.list.CertificateListItemDTO;

import java.util.List;

public class ListResponseDTO {
    private List<CertificateListItemDTO> list;
    private int totalCount;

    public static ListResponseDTO create(List<CertificateListItemDTO> list, int totalCount) {
        final var responseDTO = new ListResponseDTO();
        responseDTO.setList(list);
        responseDTO.setTotalCount(totalCount);
        return responseDTO;
    }

    public List<CertificateListItemDTO> getList() {
        return list;
    }

    public void setList(List<CertificateListItemDTO> list) {
        this.list = list;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
