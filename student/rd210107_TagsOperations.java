package student;

import rs.ac.bg.etf.sab.operations.TagsOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class rd210107_TagsOperations implements TagsOperations {

    @Override
    public Integer addTag(Integer movieId, String tag) {
        String sql = "INSERT INTO dbo.MovieTags (MovieId, Tag) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setString(2, tag);
            ps.executeUpdate();
            return movieId;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer removeTag(Integer movieId, String tag) {
        String sql = "DELETE FROM dbo.MovieTags WHERE MovieId = ? AND Tag = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setString(2, tag);
            int rows = ps.executeUpdate();
            return rows > 0 ? movieId : null;
        } catch (SQLException e) {
            throw new RuntimeException("removeTag failed", e);
        }
    }

    @Override
    public int removeAllTagsForMovie(Integer movieId) {
        String sql = "DELETE FROM dbo.MovieTags WHERE MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("removeAllTagsForMovie failed", e);
        }
    }

    @Override
    public boolean hasTag(Integer movieId, String tag) {
        String sql = "SELECT 1 FROM dbo.MovieTags WHERE MovieId = ? AND Tag = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setString(2, tag);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("hasTag failed", e);
        }
    }

    @Override
    public List<String> getTagsForMovie(Integer movieId) {
        List<String> result = new ArrayList<>();
        String sql = "SELECT Tag FROM dbo.MovieTags WHERE MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getTagsForMovie failed", e);
        }
        return result;
    }

    @Override
    public List<Integer> getMovieIdsByTag(String tag) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT MovieId FROM dbo.MovieTags WHERE Tag = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tag);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getMovieIdsByTag failed", e);
        }
        return result;
    }

    @Override
    public List<String> getAllTags() {
        List<String> result = new ArrayList<>();
        String sql = "SELECT DISTINCT Tag FROM dbo.MovieTags";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("getAllTags failed", e);
        }
        return result;
    }
}
