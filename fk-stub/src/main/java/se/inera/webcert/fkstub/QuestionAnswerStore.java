package se.inera.webcert.fkstub;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.QuestionToFkType;

/**
 * @author andreaskaltenbach
 */
@Component
public class QuestionAnswerStore {

    private ConcurrentHashMap<String, QuestionToFkType> questions = new ConcurrentHashMap<String, QuestionToFkType>();
    private ConcurrentHashMap<String, AnswerToFkType> answers = new ConcurrentHashMap<String, AnswerToFkType>();

    public ConcurrentHashMap<String, QuestionToFkType> getQuestions() {
        return questions;
    }

    public ConcurrentHashMap<String, AnswerToFkType> getAnswers() {
        return answers;
    }

    public void addQuestion(QuestionToFkType question) {
        questions.put(question.getVardReferensId(), question);
    }

    public void addAnswer(AnswerToFkType answer) {
        answers.put(answer.getVardReferensId(), answer);
    }
}
