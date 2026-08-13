package student;

import rs.ac.bg.etf.sab.operations.RatingsOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class rd210107_RatingsOperations implements RatingsOperations {

    @Override
    public boolean addRating(Integer userId, Integer movieId, Integer score) {
        String sql = "INSERT INTO dbo.Ratings (UserId, MovieId, Score, RatedAt) "
                   + "VALUES (?, ?, ?, SYSDATETIME())";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(true);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, movieId);
                ps.setInt(3, score);
                ps.executeUpdate();
            }

            try (CallableStatement cs = con.prepareCall("{call dbo.SP_REWARD_USER_Evaluate(?, ?)}")) {
                cs.setInt(1, userId);
                cs.setInt(2, movieId);
                cs.execute();
            }
            return true;
        } catch (SQLException e) {

            return false;
        } finally {
            closeQuietly(con);
        }
    }

    @Override
    public boolean updateRating(Integer userId, Integer movieId, Integer newScore) {
        String sql = "UPDATE dbo.Ratings SET Score = ?, RatedAt = SYSDATETIME() "
                   + "WHERE UserId = ? AND MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newScore);
            ps.setInt(2, userId);
            ps.setInt(3, movieId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean removeRating(Integer userId, Integer movieId) {
        String sql = "DELETE FROM dbo.Ratings WHERE UserId = ? AND MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("removeRating failed", e);
        }
    }

    @Override
    public Integer getRating(Integer userId, Integer movieId) {
        String sql = "SELECT Score FROM dbo.Ratings WHERE UserId = ? AND MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getRating failed", e);
        }
    }

    @Override
    public List<Integer> getRatedMoviesByUser(Integer userId) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT MovieId FROM dbo.Ratings WHERE UserId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getRatedMoviesByUser failed", e);
        }
        return result;
    }

    @Override
    public List<Integer> getUsersWhoRatedMovie(Integer movieId) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT UserId FROM dbo.Ratings WHERE MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getUsersWhoRatedMovie failed", e);
        }
        return result;
    }

    private static void closeQuietly(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
