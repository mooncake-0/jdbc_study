package springdb.jdbc_study.exception.basic_study;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedExceptionTest {

    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void check_throw() {
        Service service = new Service();
//        service.callThrow(); // 바로 빨간불. 잡거나 던지거나 해야한다
        Assertions.assertThatThrownBy(() -> service.callThrow()).isInstanceOf(MyCheckedException.class); // 그냥  Test 때문에 이렇게 해봄

    }


    /**
     * Exception 을 상속 받은 예외 - 체크 예외가 된다
     */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }
    }

    /*
     가정 사항들
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         * - 잡거나 던지거나!
         */
//        public void callCatch() {
//            /*
//             얘도 빨간줄 그임. 왜냐면 repository 가 던졌거든
//             Service 도 잡거나 던지거나를 하지 않으면 컴파일러가 체크해서 잡아줌 - 체크 예외기 때문
//             */
//            repository.call();
//        }
        public void callCatch() {
            try {
                repository.call();
            } catch (MyCheckedException e) {
                log.info("예외 처리, message = {}", e.getMessage(), e); // 마지막까지 넣으면 에러 스택트레이스
            }

            // 예외를 잡았으면 정상 흐름 궤도로 돌아온다
            System.out.println("정상 흐름으로 돌아옵니다");
        }

        // 이번에는 잡지 않고 던져보겠다.
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        /*
        - 예외는 잡거나 던져야 함
        - MyCheckedException 은 체크 예외
        - 이 에러는 현재 잡지 않았다 - 그럼 던져야 함
        - 체크 예외는 던지는걸 무조건 선언해줘야함 - 선언 안해주면 컴파일러가 체크해준다 (니가 이거 안다루고 있다고 컴파일러가 ㅈㄹ 하는거임)

        public void call() {
            throw new MyCheckedException("예외발생");
        }
        */

        // 컴파일이 될 수 있도록, 밖으로 예외를 던진다 (체크 예외)
        public void call() throws MyCheckedException {
            throw new MyCheckedException("예외발생");
        }
    }
}
