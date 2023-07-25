package springdb.jdbc_study.service;

import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepositoryV1;

/*
 이체하는 비즈니스 로직에서
 트랜젝션을 자바 앱에서 사용해볼 수 있도록  Service 계층을 만든다
 */
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepositoryV1;

    public MemberServiceV1(MemberRepositoryV1 memberRepositoryV1) {
        this.memberRepositoryV1 = memberRepositoryV1;
    }

    // MEMO :: EXCEPTION 은 발생한 곳에서 해결이 원칙. 이렇게 줄줄이 소세지로 올라오지 않는다
    public void accountTransfer(String fromId, String toId, int money) throws Exception {

        Member fromMember = memberRepositoryV1.findById(fromId);
        Member toMember = memberRepositoryV1.findById(toId);

        // 이 한 묶음이 트랜젝션화 되어야 한다
        memberRepositoryV1.update(fromId, fromMember.getMoney() - money);

        // 어김없이 강제로 에러 상황을 만들어 주기 위해, validation 을 건다
        validation(toMember);
        memberRepositoryV1.update(toId, toMember.getMoney() + money);
    }


    private void validation(Member member) {

        if (member.getMemberId().equals("FOR_ERROR")) {
            throw new IllegalStateException("이체중 예외 발생 : 그냥 상황 종료시켜버리기");
        }

    }
}
