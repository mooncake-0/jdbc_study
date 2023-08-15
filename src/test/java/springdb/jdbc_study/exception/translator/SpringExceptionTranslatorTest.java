package springdb.jdbc_study.exception.translator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import springdb.jdbc_study.connection.DBConnectionUtil;


import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static springdb.jdbc_study.connection.ConnectionConst.*;

public class SpringExceptionTranslatorTest {

    private DataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrCode() {

        String bad_sql = "select bad grammar";

        try {

            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(bad_sql);
            pstmt.executeQuery();

        } catch (SQLException e) {
            // 필요한 상황을 모두 ErrorCode 로 처리해주는건 말도 안됨
            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122); // 안외워도 된다
            if (e.getErrorCode() == 42122) { // 이런식으로 일일이 변환해줘야한다
                throw new BadSqlGrammarException("", "", e);
            }
        }
    }

    @Test
    void exceptionTranslator() {

        String bad_sql = "select bad grammar";

        try {

            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(bad_sql);
            pstmt.executeQuery();

        } catch (SQLException e) {

            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122); // 같은 상황이다.

            // 스프링 형님 등장
            SQLErrorCodeSQLExceptionTranslator translator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            DataAccessException resultException = translator.translate("읽을 수 있는 설명 넣으면 됨", bad_sql, e); // 최종 상위 계층으로 반환한다 (translator 가 무슨 Exception 인지 확인한다)
            Assertions.assertThat(resultException.getClass()).isEqualTo(BadSqlGrammarException.class);
        }
    }
}
