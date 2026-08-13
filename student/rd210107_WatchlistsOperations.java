package student;

import rs.ac.bg.etf.sab.operations.WatchlistsOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class rd210107_WatchlistsOperations implements WatchlistsOperations {

    @Override
    public boolean addMovieToWatchlist(Integer userId, Integer movieId) {
        String sql = "INSERT INTO dbo.Watchlist (UserId, MovieId, AddedAt) "
                   + "VALUES (?, ?, SYSDATETIME())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean removeMovieFromWatchlist(Integer userId, Integer movieId) {
        String sql = "DELETE FROM dbo.Watchlist WHERE UserId = ? AND MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("removeMovieFromWatchlist failed", e);
        }
    }

    @Override
    public boolean isMovieInWatchlist(Integer userId, Integer movieId) {
        String sql = "SELECT 1 FROM dbo.Watchlist WHERE UserId = ? AND MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("isMovieInWatchlist failed", e);
        }
    }

    @Override
    public List<Integer> getMoviesInWatchlist(Integer userId) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT MovieId FROM dbo.Watchlist WHERE UserId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getMoviesInWatchlist failed", e);
        }
        return result;
    }

    @Override
    public List<Integer> getUsersWithMovieInWatchlist(Integer movieId) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT UserId FROM dbo.Watchlist WHERE MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getUsersWithMovieInWatchlist failed", e);
        }
        return result;
    }
}
