package springdb.jdbc_study.exception.translator;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import springdb.jdbc_study.connection.ConnectionConst;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.exception.MyDbDuplicateKeyException;
import springdb.jdbc_study.repository.exception.MyDbException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static springdb.jdbc_study.connection.ConnectionConst.*;

@Slf4j
public class ExceptionTranslatorV1Test {

    private Repository repository;
    private Service service;


    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);

    }

    @Test
    @DisplayName("같은 ID 저장 시도")
    void duplicateKeySave() {
        service.create("myId");
        service.create("myId"); // 두번째 진행 로그를 확인할 수 있음
    }


    @RequiredArgsConstructor
    static class Service{

        private final Repository repository;

        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }

        public void create(String memberId) {
            try {
                repository.save(new Member(memberId, 1000));
                log.info("SAVED ID = {}", memberId); // 이게 안되는 경우가 문제인거임.
            } catch (MyDbDuplicateKeyException exception) {

                log.info("키 에러가 발생하였다, Service 단에서 직접 복구한다");
                String retryId = generateNewId(memberId);

                log.info("SAVED ID = {}", retryId);
                repository.save(new Member(retryId, 1000));
            } catch (MyDbException exception) {
                log.info("데이터 접근 계층 예외", exception);
                throw exception;
            }
        }
    }

    @RequiredArgsConstructor
    static class Repository {

        private final DataSource dataSource;

        public Member save(Member member) {
            String sql = "insert into member(member_id, money) values(?, ?)";

            Connection con = null;
            PreparedStatement pstmt = null;

            try {

                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();

                return member;

            } catch (SQLException e) {

                // h2 DB 라는 가정하에
                if (e.getErrorCode() == 23505) { // 이 경우에만 이렇게 한다!
                    throw new MyDbDuplicateKeyException();
                }

                throw new MyDbException(e);

            }finally {

                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(con);
            }
        }
    }

}
