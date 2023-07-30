package springdb.jdbc_study.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import springdb.jdbc_study.domain.Member;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/*
 1) 트랜젝션 - 트랜젝션 매니저 사용
 2) 트랜젝션 동기화 Lib 사용하기 위해
 > DatSourceUtils 에 들어가보면, TransactionSynchronizationManager 에서 Connection 을 조회하는 모습을 확인할 수 있음
 >DataSourceUtils.getConnection() 을 사용할 것
 >DataSourceUtils.releaseConnection() 을 사용할 것
 */
@Slf4j
public class MemberRepositoryV3 {

    private final DataSource dataSource;

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
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


    public Member save(Member member) throws SQLException {

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
            log.error("DB SQL ERROR: ", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {

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
            log.error("db ERROR:: ", e);
            throw e;
        } finally {
            // CLOSE 도 원래대로 되돌리면 됨. Close 방식도 바뀜
            close(con, pstmt, rs);
        }
    }


    public void update(String memberId, int updateMoney) throws SQLException {

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
            log.info("DB ERROR :: ", e);
            throw e;

        } finally {
            close(con, pstmt, null);
        }
    }


    // 삭제
    public void delete(String memberId) throws SQLException {

        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);
            int effectedRow = pstmt.executeUpdate(); // 역시 1을 반환

        } catch (SQLException e) {
            log.info("DB ERROR :: ", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }
}