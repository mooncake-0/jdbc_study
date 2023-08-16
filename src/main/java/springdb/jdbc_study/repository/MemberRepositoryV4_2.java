package springdb.jdbc_study.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.exception.MyDbException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/*
 SQL ExceptionTranslator 추가로 원하는 Exception 을 제어할 수 있다
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    private final DataSource dataSource;
    private final SQLExceptionTranslator exceptionTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) { // SQLException 은 직접 구현체로 넣어줍니다 // 그냥 SQL 측면에서만 일단 살펴보는거다
        this.dataSource = dataSource;
        this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    private Connection getConnection() throws SQLException {
        // 주의 ! 트랜젝션 동기화를 사용하려면 DataSourceUtils 를 사용해야 한다
        // DataSource 에서 직접 getConnection 을 하는 것이 아니라, 관리하고 있는 곳에서 꺼내는 느낌으로 보면 될듯
        Connection conn = DataSourceUtils.getConnection(dataSource);
        log.info("Connection = {}, Class ={} ", conn, conn.getClass());
        return conn;
    }

    // CLOSE 할 때도 DataSourceUtils 를 통해 해제해줘야한다
    private void close(Connection con, Statement stmt, ResultSet rs) {

        // Connection 빼고는 원래 닫던 방식 유지
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        // 주의 ! 트랜젝션 동기화를 사용하려면 DataSourceUtils 를 사용해야 한다
        // 트랜젝션 진행중일 경우 - SyncManager 에 반환
        // Tx 아닐 경우 - 1) HCP 사용일 경우 반환한다 2) DriverManager 사용시 그냥 Connection 해제한다
        DataSourceUtils.releaseConnection(con, dataSource);
    }


    @Override
    public Member save(Member member) {

        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            int effectedRow = pstmt.executeUpdate();

            return member;

        } catch (SQLException e) {
            throw exceptionTranslator.translate("SAVE EXCEPTION OCCUR", sql, e); // 이렇게 하면 익셉션이 뭔지 판단하고 던져준다

        } finally {
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) {

        String sql = "select * from member where member_id = ? ";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            // MEMO :: 다시 getConnection 을 해도 됨
            //         왜냐하면 SyncManager 에서 가져오기 때문임!
            con = getConnection(); // getConnection 방식이 V1과는 바뀌었음!!
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();

            if (rs.next()) { // 한번 호출해야 데이터 쪽으로 감 ( 맨 위에 항목 값들에 있음 )
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }

        } catch (SQLException e) {
            throw exceptionTranslator.translate("FINDBYID EXCEPTION OCCUR", sql, e); // 이렇게 하면 익셉션이 뭔지 판단하고 던져준다
        } finally {
            // CLOSE 도 원래대로 되돌리면 됨. Close 방식도 바뀜
            close(con, pstmt, rs);
        }
    }


    public void update(String memberId, int updateMoney) {

        String sql = "update member set money = ? where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, updateMoney);
            pstmt.setString(2, memberId);
            int effectedRow = pstmt.executeUpdate(); // 영향 받은 row 수를 반환 : 1이면 성공이라 볼 수 있음

        } catch (SQLException e) {
            throw exceptionTranslator.translate("UPDATE EXCEPTION OCCUR", sql, e); // 이렇게 하면 익셉션이 뭔지 판단하고 던져준다
        } finally {
            close(con, pstmt, null);
        }
    }


    // 삭제
    public void delete(String memberId) {

        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);
            int effectedRow = pstmt.executeUpdate(); // 역시 1을 반환

        } catch (SQLException e) {
            throw exceptionTranslator.translate("DELETE EXCEPTION OCCUR", sql, e); // 이렇게 하면 익셉션이 뭔지 판단하고 던져준다
        } finally {
            close(con, pstmt, null);
        }
    }
}