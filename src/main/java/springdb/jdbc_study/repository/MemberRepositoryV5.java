package springdb.jdbc_study.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import springdb.jdbc_study.domain.Member;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/*
 JDBCTemplate 찍먹
 - JDBC 를 사용하면서 발생한 반복을 다 해결해준다
 - 변하는 부분만 알아서 제어하면 된다
 - 트랜젝션 커넥션 동기화 / 스프링 예외 변환기까지 다~~~
 - 지금까지 한 이유는 JDBCTemplate 이란 기술만해도 얼마나 많은 일을 해주는지 알 수 있게끔 하기 위해서이다
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository {

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) { // SQLException 은 직접 구현체로 넣어줍니다 // 그냥 SQL 측면에서만 일단 살펴보는거다
        this.template = new JdbcTemplate(dataSource);
    }


    @Override
    public Member save(Member member) {

        String sql = "insert into member(member_id, money) values (?, ?)";

        int update = template.update(sql, member.getMemberId(), member.getMoney()); // update 된 숫자

        /*
         원래 있었던 거를 다해줌. Connection , PSTMT, 심지어 예외변환까지 다해줌
         - Resource Close
         - Connection 동기화
         - 다해준다!!! 모두 Template 에 있는 일치하는 작업임
         */

        return member; // 저장한 member 를 반환한다

    }

    public Member findById(String memberId) {

        String sql = "select * from member where member_id = ? ";
        return template.queryForObject(sql, memberRowMapper(), memberId);// 한 건 조회하는건 queryForObject()
    }


    /* Entity Mapping 을 직접 한다 - ORM 이 얼마나 대단한지 알 수 있는 부분 */
    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member(); // 결과를 반환한 PSTMT 를 Member 로 묶어준다.
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }


    public void update(String memberId, int updateMoney) {

        String sql = "update member set money = ? where member_id = ?";

        int update = template.update(sql, updateMoney, memberId); // 순서대로!

    }


    // 삭제
    public void delete(String memberId) {

        String sql = "delete from member where member_id = ?";

        int update = template.update(sql, memberId);
    }
}