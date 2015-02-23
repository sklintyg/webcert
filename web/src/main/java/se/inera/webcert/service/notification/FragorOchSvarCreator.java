package se.inera.webcert.service.notification;

import se.inera.certificate.modules.support.api.notification.FragorOchSvar;

public interface FragorOchSvarCreator {

    public abstract FragorOchSvar createFragorOchSvar(String intygsId);

}
