package springdb.jdbc_study.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static springdb.jdbc_study.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    // 이게 바로 JDBC 표준 인터페이스 Connection 을 반환
    // 우리는 H2 DB 를 사용하고 있으므로, H2 에서 사용하는 Driver 를 통해서 Connection 을 찾아오게 된다
    // org.h2.jdbc.JdbcConnection 을 찾아서 반환해주게 된다
    // DB 를 바꾸게 된다면 다른 Driver 가 해당 DB 에 대한 Connection 을 반환해주게된다.
    // 이 코드를 건드릴 필요 없음
    public static Connection getConnection() {

        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection info = {}, class = {}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

    }
}
