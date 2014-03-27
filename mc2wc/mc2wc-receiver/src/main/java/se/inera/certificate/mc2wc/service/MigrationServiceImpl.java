package se.inera.certificate.mc2wc.service;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.mc2wc.converter.FragaSvarConverter;
import se.inera.certificate.mc2wc.converter.MedcertIntygConverter;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationResultType;
import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.persistence.legacy.model.MigreratMedcertIntyg;
import se.inera.webcert.persistence.legacy.repository.MigreratMedcertIntygRepository;

import java.util.List;

public class MigrationServiceImpl implements MigrationService {

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    @Autowired
    private MigreratMedcertIntygRepository medcertIntygRepository;

    @Autowired
    private MedcertIntygConverter medcertIntygConverter;

    @Autowired
    private FragaSvarConverter fragaSvarConverter;

    @Override
    public MigrationResultType processMigrationMessage(MigrationMessage message) {

        List<QuestionType> questions = message.getQuestions();

        for (QuestionType q : questions) {
            FragaSvar fs = fragaSvarConverter.toFragaSvar(q);
            fragaSvarRepository.save(fs);
        }

        if (message.getCertificate() != null) {
            MigreratMedcertIntyg mcCert = medcertIntygConverter.toMigreratMedcertIntyg(message.getCertificate());
            medcertIntygRepository.save(mcCert);
        }

        return MigrationResultType.OK;
    }

}
