package springdb.jdbc_study.repository;

import lombok.extern.slf4j.Slf4j;
import springdb.jdbc_study.connection.DBConnectionUtil;
import springdb.jdbc_study.domain.Member;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/*
 한번쯤은 JDBC 를 직접 사용해보는 것도 좋은 경험이다
 ㅈㄴ 복잡.. 자원 할당 관리 이런것도 치명적으로 귀찮
 JDBC는 표준 인터페이스를 사용하더라도 처럼 모두 일관된 형식을 유지한다, SQL 과 PSTMT 에 PARAMS 를 설정하는 부분 빼고는 모두 동일

 */
@Slf4j
public class MemberRepositoryV1 {


    /*
     V0 - 매번 사용 DB Driver Manager 를 활용해서 직접 Connection 을 가져온다
     V1 - DataSource 를 통해 가져오면 Interface 화 되므로, 다른 DB 기술을 쓰더라도 바꿀 필요가 없다
     */
    private final DataSource dataSource;

    // 의존성 주입을 위함
    public MemberRepositoryV1(DataSource dataSource) {
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
    public Member findById(String memberId) throws SQLException {

        String sql = "select * from member where member_id = ? ";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId); // 첫번째 ? 에 string 값을 지정한다

            rs = pstmt.executeQuery();

            if (rs.next()) { // 한번 호출해야 데이터 쪽으로 감 ( 맨 위에 항목 값들에 있음 )
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }

        } catch (SQLException e) {
            log.error("db ERROR:: ", e);
            throw e;
        }finally {
            close(con, pstmt, rs);
        }
    }


    /*
     수정과 삭제에 대해서도 추가해줄 수 있다.
     */
    public void update(String memberId, int updateMoney) throws SQLException {

        String sql = "update member set money = ? where member_id = ?";

        // JDBC 사용법
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
        }finally {
            close(con, pstmt, null);
        }
    }


    // 삭제
    public void delete(String memberId) throws SQLException {

        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{

            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);
            int effectedRow = pstmt.executeUpdate(); // 역시 1을 반환

        } catch (SQLException e) {
            log.info("DB ERROR :: ", e);
            throw e;
        }finally {
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
