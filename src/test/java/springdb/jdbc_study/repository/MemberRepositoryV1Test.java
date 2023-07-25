package springdb.jdbc_study.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import springdb.jdbc_study.domain.Member;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class MemberRepositoryV1Test {

    MemberRepositoryV0 memberRepository = new MemberRepositoryV0();

    // 이건 찐으로 넣은거임. Transaction, Rollback 같은거 없음
    @Test
    void crud() throws SQLException {

        // 등록 : SAVE
        Member memberV0 = new Member("memberV0", 10000);
        memberRepository.save(memberV0);

        // 조회 : FIND BY ID
        Member findMember = memberRepository.findById(memberV0.getMemberId());

        // MEMO : 동등성 비교
        log.info("findMember = {}", findMember);
        log.info("is member == findMember {}", memberV0 == findMember); // 얘는 FALSE 반환
        log.info("is member equals findMember {}", memberV0.equals(findMember)); // 얘는 EQUALS 반환
        assertThat(findMember).isEqualTo(memberV0); // 이거는 통과한다 (세번째 EQUALS 를 통과하기 때문)


        // 업데이트 : UPDATE
        memberRepository.update(memberV0.getMemberId(), 20000);
        Member findUpdatedMember = memberRepository.findById(memberV0.getMemberId());
        assertThat(findUpdatedMember.getMoney()).isEqualTo(20000);

        // 삭제 :: DELETE
        memberRepository.delete(memberV0.getMemberId());
        assertThatThrownBy(() -> memberRepository.findById(memberV0.getMemberId())).isInstanceOf(NoSuchElementException.class);

    }

    /*
     참고로, 원래 .equals() 를 통과하려면 같은 객체여야한다
     하지만 동등성 비교로 변경해주기 위해 Member 객체에서 @Override 하여 equals() and HashCode() 를 만들어 주면
     동등성 비교로 바뀌어서, 속성 값들만 동일하면 동등하다고 판단해준다
     @Data 는 이 Equals and hashCode 를 자동으로 형성해놔준다
     */
}
