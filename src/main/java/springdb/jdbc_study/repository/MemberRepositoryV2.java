package springdb.jdbc_study.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;
import springdb.jdbc_study.domain.Member;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/*
 JDBC 의 Connection 을 PARAMETER 로 전달하면서 같은 Connection 보장해줄 수 있도록 만들어보자
 MemberServiceV1 에서 findById, Update 함수를 쓰고 있으므로, 해당 함수들에 Connection 을 줄 수 있도록 PARAM 을 넣자
 */
@Slf4j
public class MemberRepositoryV2 {


    /*
    V2 - 전달받은 Connection 을 사용해야 한다
       - Transaction 보장을 위해 Repository 단에서 Connection 을 형성하면 안된다
       - Connection 형성을 모두 주석 처리
       - 단, Update 랑 findById 만 사용하므로, 나머지는 귀찮으므로 하지 않는다
       - 컴파일 에러 방지를 위해 DataSource 는 여전히 넣어준다 ㅋ
     */
    private final DataSource dataSource;

    // 의존성 주입을 위함
    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
//        return DBConnectionUtil.getConnection(); // V0 였던 것
        Connection connFromDs = dataSource.getConnection();
        log.info("getting connection from DataSource : connection = {}, class = {}", connFromDs, connFromDs.getClass());
        return connFromDs;
    }

    /*
     JDBC 를 사용해서 DB를 등록한다
     MEMO :: connection 을 통해 PreparedStatment 를 준비
            > PreparedStatement 는 ? 를 통해서 SQL 을 준비하고, Parameter 들을 추후에 넣어서,
            > Logging 되는 실 데이터 값들을 보호해주기 위함이다
     */
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

            // MEMO :: UPDATE 하는게 아니고, 영향 받은 row 수를 반환하는 것
            //         return 해줄게 없으니, ResultSet 를 사용할 필요가 없음
            int effectedRow = pstmt.executeUpdate();

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


    /*
     새로 추가된 result set 은
     DB 콘솔에서 결과가 나온 row * col 표를 그대로 가지고 온거라고 생각하면 된다
     cursor 을 통해서 한 row 를 기준으로 읽어나간다
     */
    public Member findById(Connection con, String memberId) throws SQLException {

        String sql = "select * from member where member_id = ? ";

//        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

//            con = getConnection(); // 새로 연결하면 절대 안됨
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId); // 첫번째 ? 에 string 값을 지정한다

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
            // con 도 닫아버리면 그 Connection 이 끝나버린다 (전달해줘야 하기 때문에 닫으면 안됨)
            // 기존 close 함수를 이용할 수 없이, 직접 닫아야함
            JdbcUtils.closeStatement(pstmt);
            JdbcUtils.closeResultSet(rs);
//            close(con, pstmt, rs);
        }
    }


    /*
     수정과 삭제에 대해서도 추가해줄 수 있다.
     */
    public void update(Connection con, String memberId, int updateMoney) throws SQLException {

        String sql = "update member set money = ? where member_id = ?";

        // JDBC 사용법
//        Connection con = null;
        PreparedStatement pstmt = null;

        try {

//            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, updateMoney);
            pstmt.setString(2, memberId);
            int effectedRow = pstmt.executeUpdate(); // 영향 받은 row 수를 반환 : 1이면 성공이라 볼 수 있음

        } catch (SQLException e) {
            log.info("DB ERROR :: ", e);
            throw e;
        } finally {
            JdbcUtils.closeStatement(pstmt);
//            close(con, pstmt, null); // 닫으면 안됨!
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

    private void close(Connection con , Statement stmt, ResultSet rs) {
        JdbcUtils.closeConnection(con);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeResultSet(rs);
    }

}