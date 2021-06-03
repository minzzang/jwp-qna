package qna.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    UserRepository repository;

    private User user;

    private User actual;

    @BeforeEach
    void setup() {
        user = User.of("jin", "1234", "김석진", "7271kim@naver.com");
        actual = repository.save(user);
    }

    @Test
    @DisplayName("정상적으로 전 후 데이터가 들어가 있는지 확인한다.")
    void save() {
        assertAll(
            () -> assertThat(actual.getId()).isNotNull(),
            () -> assertThat(actual).isSameAs(user));
    }

    @Test
    @DisplayName("update 확인")
    void updata() {
        user.updateEmail("change Email");
        repository.saveAndFlush(user);
        User finedUser = repository.findById(user.getId()).get();
        assertThat(finedUser).isSameAs(user);
    }
}