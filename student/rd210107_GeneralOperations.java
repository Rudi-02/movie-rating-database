package student;

import rs.ac.bg.etf.sab.operations.GeneralOperations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class rd210107_GeneralOperations implements GeneralOperations {

    @Override
    public void eraseAll() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute("EXEC dbo.SP_ERASE_ALL");
        } catch (SQLException e) {
            throw new RuntimeException("eraseAll failed", e);
        }
    }
}
