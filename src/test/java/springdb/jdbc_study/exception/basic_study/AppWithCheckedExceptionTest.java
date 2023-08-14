package springdb.jdbc_study.exception.basic_study;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

/*
 1. 어차피 내가 해결할 수도 없는데도 계속 누적되고, 코드 가독성을 낮춘다
 2. 어차피 내가 해결할 수도 없는데도 의존성이 걸려야 한다 -> 유지 보수 측면에서 안좋음
 3. 내가 반드시 처리를 해주기 위해 지정해주는 Exception 만 Checked Exception 으로 활용하는게 맞다 --> 대안이 Runtime Exception
 */
public class AppWithCheckedExceptionTest {

    @Test
    void checked() {
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    static class Controller{
        Service s = new Service();

        public void request() throws SQLException, ConnectException { // 또 누적된다
            s.logic(); // 이것도 내가 처리 못함 // ControllerAdvice 까지 가야함
        }
    }

    static class Service {

        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        // 처리 안하면 두 로직 다 빨간줄 뜬다 // 이렇게 Exception 들이 해당 Class 가 처리할 수 없는 로직임에도 쌓여나간다
        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();
        }

    }

    static class NetworkClient { // 외부 서버와 통신하는 곳 (내가 Client 가 됨)
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패"); // Checked 기 때문에 던지든가 해야함
        }
    }

    static class Repository {
        public void call() throws SQLException {
            throw new SQLException("SQL 예외 발생"); // Checked 기 때문에 던지든가 해야함
        }
    }
}
