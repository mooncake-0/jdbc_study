package springdb.jdbc_study.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static springdb.jdbc_study.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    /*
     Driver Manager 에서 직접 Connection 을 연결하여 맺어오는 행위 (DataSource API 미적용)
     */
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
