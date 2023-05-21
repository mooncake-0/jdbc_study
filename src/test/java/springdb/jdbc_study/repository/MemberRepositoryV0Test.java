package springdb.jdbc_study.repository;

import org.junit.jupiter.api.Test;
import springdb.jdbc_study.domain.Member;

import java.sql.SQLException;

public class MemberRepositoryV0Test {

    MemberRepositoryV0 memberRepository = new MemberRepositoryV0();

    // 이건 찐으로 넣은거임. Transaction, Rollback 같은거 없음
    @Test
    void crud() throws SQLException {
        Member memberV0 = new Member("memberV0", 10000);
        memberRepository.save(memberV0);
    }
}
