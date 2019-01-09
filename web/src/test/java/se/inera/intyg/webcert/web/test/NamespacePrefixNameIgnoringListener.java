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
package se.inera.intyg.webcert.web.test;

import static org.custommonkey.xmlunit.DifferenceConstants.NAMESPACE_PREFIX_ID;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

/**
 * @author andreaskaltenbach
 */
public class NamespacePrefixNameIgnoringListener implements DifferenceListener {

    @Override
    public int differenceFound(Difference difference) {
        if (NAMESPACE_PREFIX_ID == difference.getId()) {
            // differences in namespace prefix IDs are ok (eg. 'ns1' vs 'ns2'), as long as the namespace URI is the same
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        } else {
            return RETURN_ACCEPT_DIFFERENCE;
        }
    }

    @Override
    public void skippedComparison(Node control, Node test) {
    }
}
