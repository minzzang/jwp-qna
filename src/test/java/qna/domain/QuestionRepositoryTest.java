package qna.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import qna.annotation.QnaDataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@QnaDataJpaTest
class QuestionRepositoryTest {
    private Question question1;
    private Question question2;

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void before() {
        User user1 = new User("user1", "password", "name", "user1@com");
        User user2 = new User("user2", "password", "name", "user2@com");

        question1 = new Question("title1", "contents1").writeBy(user1);
        question2 = new Question("title2", "contents2").writeBy(user2);
    }

    @Test
    void id로_Many_To_One_관계_조회() {
        // given
        userRepository.save(question1.getWriter());
        Question saved = questionRepository.save(question1);
        em.clear();
        // when
        Optional<Question> result = questionRepository.findById(saved.getId());
        // then
        assertAll(
                () -> assertThat(result)
                        .map(Question::getTitle)
                        .hasValue("title1"),
                () -> assertThat(result)
                        .map(Question::getWriter)
                        .isNotNull()
        );
    }

    @Test
    void 저장() {
        // given
        userRepository.save(question1.getWriter());
        // when
        Question result = questionRepository.save(question1);
        questionRepository.flush();
        // then
        assertThat(result).isNotNull();
    }

    @Test
    void 삭제() {
        // given
        userRepository.save(question1.getWriter());
        Question saved = questionRepository.save(question1);
        // when
        questionRepository.deleteById(saved.getId());
        Optional<Question> result = questionRepository.findById(saved.getId());
        // then
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    void 수정() {
        // given
        userRepository.save(question1.getWriter());
        User reWriteUser = userRepository.save(question2.getWriter());
        Question saved = questionRepository.save(question1);

        saved.writeContents("update contents");
        saved.writeBy(reWriteUser);
        em.flush();
        // when
        Optional<Question> result = questionRepository.findById(saved.getId());
        // then
        assertAll(
                () -> assertThat(result)
                        .map(Question::getContents)
                        .hasValue("update contents"),
                () -> assertThat(result)
                        .map(Question::getWriter)
                        .hasValue(reWriteUser)
        );
    }

    @ParameterizedTest
    @MethodSource(value = "question을_리턴한다")
    void id로_삭제되지_않은_질문_찾기(Question question) {
        // given
        question.delete();
        Question saved = questionRepository.save(question);
        // when
        Optional<Question> result = questionRepository.findByIdAndDeletedFalse(saved.getId());
        // then
        assertThat(result.isPresent()).isFalse();
    }

    static Stream<Arguments> question을_리턴한다() {
        return Stream.of(
                Arguments.of(
                        new Question("title1", "contents1"),
                        false
                ),
                Arguments.of(
                        new Question("title2", "contents2"),
                        false
                )
        );
    }

    @Test
    void 삭제되지_않은_질문들을_조회한다() {
        // given
        userRepository.saveAll(Arrays.asList(question1.getWriter(), question2.getWriter()));
        questionRepository.saveAll(Arrays.asList(question1, question2));
        // when
        List<Question> result = questionRepository.findByDeletedFalse();
        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 질문에_달린_답변들을_조회한다() {
        // given
        saveAnswer();
        // when
        Optional<Question> result = questionRepository.findQuestionById(question1.getId());
        // then
        assertThat(result)
                .map(question -> question.getAnswers().size())
                .hasValue(2);
    }

    private void saveAnswer() {
        userRepository.save(question1.getWriter());
        Question saved = questionRepository.save(question1);

        User minje = new User("minje", "password", "minje", "minje@com");
        User suzy = new User("suzy", "password", "suzy", "suzy@com");
        userRepository.saveAll(Arrays.asList(minje, suzy));

        Answer answerOfMinje = new Answer(minje, saved, "good!");
        Answer answerOfSuzy = new Answer(suzy, saved, "bad!");
        answerRepository.saveAll(Arrays.asList(answerOfMinje, answerOfSuzy));
    }
}