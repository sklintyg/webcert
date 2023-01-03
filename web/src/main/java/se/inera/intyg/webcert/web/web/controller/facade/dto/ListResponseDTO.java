/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;

import java.util.List;

public class ListResponseDTO {
    private List<CertificateListItem> list;
    private int totalCount;

    public static ListResponseDTO create(List<CertificateListItem> list, int totalCount) {
        final var responseDTO = new ListResponseDTO();
        responseDTO.setList(list);
        responseDTO.setTotalCount(totalCount);
        return responseDTO;
    }

    public List<CertificateListItem> getList() {
        return list;
    }

    public void setList(List<CertificateListItem> list) {
        this.list = list;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
