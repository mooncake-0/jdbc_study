package springdb.jdbc_study.repository;

import springdb.jdbc_study.domain.Member;

public interface MemberRepository {

    Member save(Member member);

    Member findById(String memberId);

    void update(String memberId, int updateMoney);

    void delete(String memberId);
}
