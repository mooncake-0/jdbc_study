package springdb.jdbc_study.connection;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class DBConnectionUtilTest {

    @Test
    @DisplayName("Driver 를 통해 Connection 을 가져오는데 성공")
    void connection() {
        // Connection 은 추상객체이다
        // 따라서 getconnection 을 통해서 구현체를 가져오게 되어 있다
        Connection connection = DBConnectionUtil.getConnection();
        assertThat(connection).isNotNull();
    }

}
