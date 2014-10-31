package se.inera.webcert.mailstub;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author andreaskaltenbach
 */
@Path("/mails")
public class MailStubRestApi {

    @Autowired
    private MailStore mailStore;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<OutgoingMail> mails() {
        return mailStore.getMails();
    }

    @DELETE
    public Response deleteMailbox() {
        mailStore.getMails().clear();
        return Response.ok().build();
    }

}
