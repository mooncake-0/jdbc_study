package springdb.jdbc_study.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static springdb.jdbc_study.connection.ConnectionConst.*;

@Slf4j
public class ConnectionIntfTest {


    /*
     Driver Manager 을 통해 일일이 Connection 을 직접 획득한다
     */
    @Test
    void driverManagerTest() throws SQLException {
        Connection conn1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection conn2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        // 로깅을 통해 정확히 확인해볼 수 있다
        log.info("Connection = {}, Class = {}", conn1, conn1.getClass());
        log.info("Connection = {}, Class = {}", conn2, conn2.getClass());
    }

    /*
     DriverManager.getConnection() 을 사용하지 않고 어떤 DB 접근 기술을 사용하든
     일관된 수행을 할 수 있도록 .getConnection 을 제공하는 DataSource 를 활용해보자
     DriverManagerDataSource 를 통해 DriverManager 방식의 DataSource 를 사용할 수 있다
     */
    @Test
    void dataSourceDriverManagerTest() throws SQLException {
        // DATASource 생성을 위한 설정단 지정
        DataSource ds = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // 실제 DriverManager 처럼 일일이 Connection 을 가져오는 모습 확인 가능, 사용이 분리됨
        useAndLogDataSource(ds);
    }


    // 그렇다면 이번엔 DataSource 를 사용하되, HCP 로 바꿔보고 싶음.
    // 어떻게 바꿔질까?
    @Test
    void dataSourceConnectionPoolTest() throws SQLException, InterruptedException {

        // 나만의 새로운 HIKARI CONNECTION POOL 을 만들어 본다
        HikariDataSource dataSource = new HikariDataSource();

        // 설정부
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // HikariDataSource 설정부
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MY CUSTOM POOL");

        // 사용부
        useAndLogDataSource(dataSource);
        Thread.sleep(1000); // CUSTOM 에 채워지는 모습 확인 가능

    }


    private void useAndLogDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection(); // interface 의 함수
        Connection con2 = dataSource.getConnection();

        log.info("Connection = {}, Class = {}", con1, con1.getClass());
        log.info("Connection = {}, Class = {}", con2, con2.getClass());
    }
}
