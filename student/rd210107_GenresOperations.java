package student;

import rs.ac.bg.etf.sab.operations.GenresOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class rd210107_GenresOperations implements GenresOperations {

    @Override
    public Integer addGenre(String name) {
        String sql = "INSERT INTO dbo.Genres (Name) VALUES (?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer updateGenre(Integer id, String newName) {
        if (id == null || !doesGenreIdExist(id)) {
            return null;
        }
        String sql = "UPDATE dbo.Genres SET Name = ? WHERE GenreId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0 ? id : null;
        } catch (SQLException e) {
            // UQ_Genres_Name (novo ime vec postoji).
            return null;
        }
    }

    @Override
    public Integer removeGenre(Integer id) {
        if (id == null || !doesGenreIdExist(id)) {
            return null;
        }
        // FK na GenreId je ON DELETE NO ACTION, pa se veze sa filmovima
        // moraju eksplicitno obrisati pre brisanja samog zanra.
        String delLinks = "DELETE FROM dbo.MovieGenres WHERE GenreId = ?";
        String delGenre = "DELETE FROM dbo.Genres WHERE GenreId = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(delLinks)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            int rows;
            try (PreparedStatement ps = con.prepareStatement(delGenre)) {
                ps.setInt(1, id);
                rows = ps.executeUpdate();
            }
            con.commit();
            return rows > 0 ? id : null;
        } catch (SQLException e) {
            safeRollback(con);
            throw new RuntimeException("removeGenre failed", e);
        } finally {
            closeQuietly(con);
        }
    }

    private static void safeRollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private static void closeQuietly(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public boolean doesGenreExist(String name) {
        String sql = "SELECT 1 FROM dbo.Genres WHERE Name = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("doesGenreExist failed", e);
        }
    }

    @Override
    public Integer getGenreId(String name) {
        String sql = "SELECT GenreId FROM dbo.Genres WHERE Name = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getGenreId failed", e);
        }
    }

    @Override
    public List<Integer> getAllGenreIds() {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT GenreId FROM dbo.Genres";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("getAllGenreIds failed", e);
        }
        return result;
    }

    private boolean doesGenreIdExist(int id) {
        String sql = "SELECT 1 FROM dbo.Genres WHERE GenreId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("doesGenreIdExist failed", e);
        }
    }
}
