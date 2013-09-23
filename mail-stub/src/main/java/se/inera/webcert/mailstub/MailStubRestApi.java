package se.inera.webcert.mailstub;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author andreaskaltenbach
 */
public class MailStubRestApi {

    @Autowired
    MailStore mailStore;

    @GET
    @Path("/mails")
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<OutgoingMail> mails() {
        return mailStore.getMails();
    }
}
