package qna.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import qna.annotation.QnaDataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static qna.domain.QuestionTest.Q1;
import static qna.domain.QuestionTest.Q2;

@QnaDataJpaTest
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void id로_조회() {
        // given
        Question question = new Question("title", "contents");
        Question saved = questionRepository.save(question);
        // when
        Optional<Question> result = questionRepository.findById(saved.getId());
        // then
        assertAll(
                () -> assertThat(result)
                        .map(Question::getTitle)
                        .hasValue("title"),
                () -> assertThat(result)
                        .map(Question::getContents)
                        .hasValue("contents")
        );
    }

    @Test
    void 저장() {
        // given
        Question question = new Question("title", "contents");
        // when
        Question result = questionRepository.save(question);
        // then
        assertThat(result).isNotNull();
    }

    @Test
    void 삭제() {
        // given
        Question question = new Question("title", "contents");
        Question saved = questionRepository.save(question);
        // when
        questionRepository.deleteById(saved.getId());
        Optional<Question> result = questionRepository.findById(saved.getId());
        // then
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    void 수정() {
        // given
        Question question = new Question("title", "contents");
        Question saved = questionRepository.save(question);

        saved.writeContents("update contents");
        // when
        Optional<Question> result = questionRepository.findById(saved.getId());
        // then
        assertThat(result)
                .map(Question::getContents)
                .hasValue("update contents");
    }

    @ParameterizedTest
    @MethodSource(value = "question과_기댓값을_리턴한다")
    void id로_삭제되지_않은_질문_찾기(Question question, boolean expected) {
        // given
        Question saved = questionRepository.save(question);
        // when
        Optional<Question> result = questionRepository.findByIdAndDeletedFalse(saved.getId());
        // then
        assertThat(result.isPresent()).isEqualTo(expected);
    }

    static Stream<Arguments> question과_기댓값을_리턴한다() {
        Q1.delete();

        return Stream.of(
                Arguments.of(
                        Q1,
                        false
                ),
                Arguments.of(
                        Q2,
                        true
                )
        );
    }

    @Test
    void 삭제되지_않은_질문들을_조회한다() {
        // given
        questionRepository.saveAll(Arrays.asList(Q1, Q2));
        // when
        List<Question> result = questionRepository.findByDeletedFalse();
        // then
        assertThat(result).hasSize(2);
    }
}