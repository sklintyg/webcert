/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.specifications.spec.notification_sender
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils

class SkickadeNotifieringar {

    def notifieringar

    String id 
    String kod
    int antal = 0
    def matching = new HashMap<String, Collection>()

    public void execute() {
        notifieringar = WebcertRestUtils.getNotifications(antal)
        notifieringar.each {
            if (it?.utlatande?.utlatandeId?.extension.equalsIgnoreCase(id) &&
                it?.utlatande?.handelse?.handelsekod?.code.equalsIgnoreCase(kod)) {
                matching.put(it?.utlatande?.handelse?.handelsekod?.code, it)
            }
        }
    }

    public boolean checkHandelseKod() {
        return matching.isEmpty() == true ? false : true
    }

    public def antalFragor() {
        matching.get(kod)?.utlatande?.fragorOchSvar?.antalFragor
    }

    public def antalHanteradeFragor() {
        matching.get(kod)?.utlatande?.fragorOchSvar?.antalHanteradeFragor
    }

    public def antalSvar() {
        matching.get(kod)?.utlatande?.fragorOchSvar?.antalSvar
    }

    public def antalHanteradeSvar() {
        matching.get(kod)?.utlatande?.fragorOchSvar?.antalHanteradeSvar
    }

}
