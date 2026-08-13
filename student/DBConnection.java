package student;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=MovieDB;"
                    + "encrypt=false;trustServerCertificate=true;";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "sa123";

    private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Microsoft JDBC Driver za SQL Server nije pronadjen na classpath-u. "
              + "Dodajte mssql-jdbc-*.jar u zavisnosti projekta.", e);
        }
    }

    private DBConnection() {
    }

    private static String resolve(String systemPropertyKey, String defaultValue) {
        String value = System.getProperty(systemPropertyKey);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        String url = resolve("db.url", DEFAULT_URL);
        String user = resolve("db.user", DEFAULT_USER);
        String password = resolve("db.password", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }
}
