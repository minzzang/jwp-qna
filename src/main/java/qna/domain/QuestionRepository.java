package qna.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByDeletedFalse();

    @Query("select q from Question q join fetch q.answers where q.id = :id and q.deleted = false")
    Optional<Question> findByIdAndDeletedFalse(@Param(value = "id") Long id);
}
