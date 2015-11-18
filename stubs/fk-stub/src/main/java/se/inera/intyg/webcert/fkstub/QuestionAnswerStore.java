package se.inera.intyg.webcert.fkstub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;

/**
 * @author andreaskaltenbach
 */
@Component
public class QuestionAnswerStore {

    private final Map<String, QuestionToFkType> questions = new ConcurrentHashMap<>();
    private final Map<String, AnswerToFkType> answers = new ConcurrentHashMap<>();

    public Map<String, QuestionToFkType> getQuestions() {
        return questions;
    }

    public Map<String, AnswerToFkType> getAnswers() {
        return answers;
    }

    public void addQuestion(QuestionToFkType question) {
        questions.put(question.getVardReferensId(), question);
    }

    public void addAnswer(AnswerToFkType answer) {
        answers.put(answer.getVardReferensId(), answer);
    }
}
