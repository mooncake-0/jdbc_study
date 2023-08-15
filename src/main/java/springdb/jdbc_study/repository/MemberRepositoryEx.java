package springdb.jdbc_study.repository;

import springdb.jdbc_study.domain.Member;

import java.sql.SQLException;

/*
 해결하려고 만든 Interface 까지 Exception 이 누수되는 안좋은 예시
 */
public interface MemberRepositoryEx {

    Member save(Member member) throws SQLException;

    Member findById(String memberId) throws SQLException;

    void update(String memberId, int updateMoney) throws SQLException;

    void delete(String memberId) throws SQLException;
}
