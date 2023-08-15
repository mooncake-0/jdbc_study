package springdb.jdbc_study.repository.exception;

public class MyDbException extends RuntimeException{

    public MyDbException() {
    }

    public MyDbException(String message) {
        super(message);
    }

    /*
     원인을 지속 연결시켜 줄 수 있는 Exception Constructor 를 주렁주렁 달아주어야 한다
     */
    public MyDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDbException(Throwable cause) {
        super(cause);
    }
}
