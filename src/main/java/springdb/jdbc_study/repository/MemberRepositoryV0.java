package springdb.jdbc_study.repository;

import lombok.extern.slf4j.Slf4j;
import springdb.jdbc_study.connection.DBConnectionUtil;
import springdb.jdbc_study.domain.Member;

import java.sql.*;

/*
 한번쯤은 JDBC 를 직접 사용해보는 것도 좋은 경험이다
 ㅈㄴ 복잡.. 자원 할당 관리 이런것도 치명적으로 귀찮
 */
@Slf4j
public class MemberRepositoryV0 {

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            con = getConnection();
            pstmt = con.prepareStatement(sql);

            // 위에 ?, ? 에 들어갈 값들을 지정한다
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            int effectedRow = pstmt.executeUpdate(); // 실행한다 (update 붙으면 반환값이 붙는데, 몇 개의 row 가 영향을 받았는지 반환해준다)

            return member;

        } catch (SQLException e) {
            log.error("DB SQL ERROR: ", e);
            throw e;
        } finally {

            // 항상 호출되는 것을 보장해줘야 한다
            // MEMO :: 커넥션이 끊어지지 않고 유지되는 문제를 [리소스 누수 (Leak)] 라고 부른다
            //         커넥션 부족 장애가 발생할 수 있음
            // 외부 Resource 를 쓰는 것. 꼭 닫아줘야
            close(con, pstmt, null);
        }
    }


    // 각각 닫히면서도 Exception 발생할 수 있기 때문에, 각각을 try/catch 해줘야 한다
    private void close(Connection conn, Statement stmt, ResultSet rs) {

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error: ", e);
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.info("error: ", e);
            }
        }

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error: ", e);
            }
        }
    }

}
