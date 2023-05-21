package springdb.jdbc_study.connection;

/*
 상수를 쓰기 위해 모아둔 것이므로, 객체 생성을 방지하기 위해 ABSTRACT
 */
public abstract class ConnectionConst {

    // 평소에 yml에 적던 것들
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";

}
