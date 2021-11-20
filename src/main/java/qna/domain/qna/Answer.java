package qna.domain.qna;

import javax.persistence.Embedded;
import qna.common.exception.CannotDeleteException;
import qna.common.exception.NotFoundException;
import qna.common.exception.UnAuthorizedException;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import qna.domain.BaseEntity;
import qna.domain.deletehistory.ContentType;
import qna.domain.user.User;
import qna.domain.deletehistory.DeleteHistory;

@Entity
@Table(name = "answer")
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", foreignKey = @ForeignKey(name = "fk_answer_writer"))
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", foreignKey = @ForeignKey(name = "fk_answer_to_question"))
    private Question question;

    @Embedded
    private Contents contents;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = Boolean.FALSE;

    protected Answer() {
    }

    public Answer(User writer, Question question, Contents contents) {
        validCanWritten(writer, question);
        question.addAnswer(this);

        this.writer = writer;
        this.question = question;
        this.contents = contents;
    }

    public DeleteHistory delete(User loginUser) {
        if (!isOwner(loginUser)) {
            throw new CannotDeleteException(ContentType.ANSWER);
        }

        this.deleted = true;
        return DeleteHistory.OfAnswer(this, loginUser);
    }

    public boolean isOwner(User writer) {
        return this.writer.isMe(writer);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Long getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public User getWriter() {
        return writer;
    }

    private void validCanWritten(User writer, Question question) {
        if (Objects.isNull(writer) || writer.isGuestUser()) {
            throw new UnAuthorizedException();
        }

        if (Objects.isNull(question)) {
            throw new NotFoundException();
        }
    }

    @Override
    public String toString() {
        return "Answer{" +
            "id=" + id +
            ", writerId=" + writer.getId() +
            ", questionId=" + question.getId() +
            ", contents='" + contents.getContents() + '\'' +
            ", deleted=" + deleted +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Answer answer = (Answer) o;
        return deleted == answer.deleted
            && Objects.equals(id, answer.id)
            && Objects.equals(writer, answer.writer)
            && Objects.equals(question, answer.question)
            && Objects.equals(contents, answer.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, writer, question, contents, deleted);
    }
}