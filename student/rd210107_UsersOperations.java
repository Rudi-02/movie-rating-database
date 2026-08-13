package student;

import rs.ac.bg.etf.sab.operations.UsersOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class rd210107_UsersOperations implements UsersOperations {

    @Override
    public Integer addUser(String username) {
        String sql = "INSERT INTO dbo.Users (Username) VALUES (?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
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
    public Integer updateUser(Integer id, String newUsername) {
        if (id == null || !doesUserIdExist(id)) {
            return null;
        }
        String sql = "UPDATE dbo.Users SET Username = ? WHERE UserId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0 ? id : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer removeUser(Integer id) {
        if (id == null || !doesUserIdExist(id)) {
            return null;
        }
        // FK-ovi na UserId su ON DELETE NO ACTION, pa se zavisni redovi
        // moraju eksplicitno obrisati pre brisanja samog korisnika.
        String delRatings = "DELETE FROM dbo.Ratings WHERE UserId = ?";
        String delWatchlist = "DELETE FROM dbo.Watchlist WHERE UserId = ?";
        String delUser = "DELETE FROM dbo.Users WHERE UserId = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            for (String sql : new String[]{delRatings, delWatchlist}) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }
            int rows;
            try (PreparedStatement ps = con.prepareStatement(delUser)) {
                ps.setInt(1, id);
                rows = ps.executeUpdate();
            }
            con.commit();
            return rows > 0 ? id : null;
        } catch (SQLException e) {
            safeRollback(con);
            throw new RuntimeException("removeUser failed", e);
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
    public boolean doesUserExist(String username) {
        String sql = "SELECT 1 FROM dbo.Users WHERE Username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("doesUserExist failed", e);
        }
    }

    @Override
    public Integer getUserId(String username) {
        String sql = "SELECT UserId FROM dbo.Users WHERE Username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getUserId failed", e);
        }
    }

    @Override
    public List<Integer> getAllUserIds() {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT UserId FROM dbo.Users";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("getAllUserIds failed", e);
        }
        return result;
    }

    @Override
    public List<Integer> getRecommendedMoviesFromFavoriteGenres(Integer userId) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT MovieId FROM dbo.fn_RecommendedMovies(?) "
                   + "ORDER BY AvgScore DESC, MovieId ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getRecommendedMoviesFromFavoriteGenres failed", e);
        }
        return result;
    }

    @Override
    public Integer getRewards(Integer userId) {
        String sql = "SELECT RewardsCount FROM dbo.Users WHERE UserId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getRewards failed", e);
        }
    }

    @Override
    public List<String> getThematicSpecializations(Integer userId) {
        List<String> result = new ArrayList<>();
        String sql = "SELECT Tag FROM dbo.fn_ThematicSpecializations(?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getThematicSpecializations failed", e);
        }
        return result;
    }

    @Override
    public String getUserDescription(Integer userId) {
        String sql = "SELECT dbo.fn_UserDescription(?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getUserDescription failed", e);
        }
    }

    private boolean doesUserIdExist(int id) {
        String sql = "SELECT 1 FROM dbo.Users WHERE UserId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("doesUserIdExist failed", e);
        }
    }
}
