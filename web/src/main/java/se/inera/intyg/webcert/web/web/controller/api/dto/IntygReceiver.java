/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api.dto;

public class IntygReceiver {

    private String id;
    private String name;
    private String receiverType;
    private boolean selected;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public static final class IntygReceiverBuilder {
        private String id;
        private String name;
        private String receiverType;
        private boolean selected;

        private IntygReceiverBuilder() {
        }

        public static IntygReceiverBuilder anIntygReceiver() {
            return new IntygReceiverBuilder();
        }

        public IntygReceiverBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public IntygReceiverBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public IntygReceiverBuilder withReceiverType(String receiverType) {
            this.receiverType = receiverType;
            return this;
        }

        public IntygReceiverBuilder withSelected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public IntygReceiver build() {
            IntygReceiver intygReceiver = new IntygReceiver();
            intygReceiver.setId(id);
            intygReceiver.setName(name);
            intygReceiver.setReceiverType(receiverType);
            intygReceiver.setSelected(selected);
            return intygReceiver;
        }
    }
}
