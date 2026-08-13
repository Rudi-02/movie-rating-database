package student;

import rs.ac.bg.etf.sab.operations.MoviesOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class rd210107_MoviesOperations implements MoviesOperations {

    @Override
    public Integer addMovie(String title, Integer genreId, String director) {
        String insMovie = "INSERT INTO dbo.Movies (Title, Director) VALUES (?, ?)";
        String insGenre = "INSERT INTO dbo.MovieGenres (MovieId, GenreId) VALUES (?, ?)";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            Integer movieId = null;
            try (PreparedStatement ps = con.prepareStatement(insMovie, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, title);
                ps.setString(2, director);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        movieId = keys.getInt(1);
                    }
                }
            }
            if (movieId == null) {
                con.rollback();
                return null;
            }
            try (PreparedStatement ps = con.prepareStatement(insGenre)) {
                ps.setInt(1, movieId);
                ps.setInt(2, genreId);
                ps.executeUpdate();
            }
            con.commit();
            return movieId;
        } catch (SQLException e) {
            safeRollback(con);
            return null;
        } finally {
            closeQuietly(con);
        }
    }

    @Override
    public Integer updateMovieTitle(Integer id, String newTitle) {
        if (id == null || !doesMovieExist(id)) {
            return null;
        }
        String sql = "UPDATE dbo.Movies SET Title = ? WHERE MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newTitle);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0 ? id : null;
        } catch (SQLException e) {
            throw new RuntimeException("updateMovieTitle failed", e);
        }
    }

    @Override
    public Integer addGenreToMovie(Integer movieId, Integer genreId) {
        String sql = "INSERT INTO dbo.MovieGenres (MovieId, GenreId) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setInt(2, genreId);
            ps.executeUpdate();
            return movieId;
        } catch (SQLException e) {
            // Film ili zanr ne postoji (FK), ili je veza vec upisana (PK).
            return null;
        }
    }

    @Override
    public Integer removeGenreFromMovie(Integer movieId, Integer genreId) {
        String sql = "DELETE FROM dbo.MovieGenres WHERE MovieId = ? AND GenreId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setInt(2, genreId);
            int rows = ps.executeUpdate();
            return rows > 0 ? movieId : null;
        } catch (SQLException e) {
            throw new RuntimeException("removeGenreFromMovie failed", e);
        }
    }

    @Override
    public Integer updateMovieDirector(Integer id, String newDirector) {
        if (id == null || !doesMovieExist(id)) {
            return null;
        }
        String sql = "UPDATE dbo.Movies SET Director = ? WHERE MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newDirector);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0 ? id : null;
        } catch (SQLException e) {
            throw new RuntimeException("updateMovieDirector failed", e);
        }
    }

    @Override
    public Integer removeMovie(Integer id) {
        if (id == null || !doesMovieExist(id)) {
            return null;
        }
        // FK-ovi na MovieId su ON DELETE NO ACTION, pa se zavisni redovi
        // moraju eksplicitno obrisati pre brisanja samog filma.
        String delTags = "DELETE FROM dbo.MovieTags WHERE MovieId = ?";
        String delGenres = "DELETE FROM dbo.MovieGenres WHERE MovieId = ?";
        String delRatings = "DELETE FROM dbo.Ratings WHERE MovieId = ?";
        String delWatchlist = "DELETE FROM dbo.Watchlist WHERE MovieId = ?";
        String delMovie = "DELETE FROM dbo.Movies WHERE MovieId = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            for (String sql : new String[]{delTags, delGenres, delRatings, delWatchlist}) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }
            int rows;
            try (PreparedStatement ps = con.prepareStatement(delMovie)) {
                ps.setInt(1, id);
                rows = ps.executeUpdate();
            }
            con.commit();
            return rows > 0 ? id : null;
        } catch (SQLException e) {
            safeRollback(con);
            throw new RuntimeException("removeMovie failed", e);
        } finally {
            closeQuietly(con);
        }
    }

    @Override
    public List<Integer> getMovieIds(String title, String director) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT MovieId FROM dbo.Movies WHERE Title = ? AND Director = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, director);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getMovieIds failed", e);
        }
        return result;
    }

    @Override
    public List<Integer> getAllMovieIds() {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT MovieId FROM dbo.Movies";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("getAllMovieIds failed", e);
        }
        return result;
    }

    @Override
    public List<Integer> getMovieIdsByGenre(Integer genreId) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT MovieId FROM dbo.MovieGenres WHERE GenreId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, genreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getMovieIdsByGenre failed", e);
        }
        return result;
    }

    @Override
    public List<Integer> getGenreIdsForMovie(Integer movieId) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT GenreId FROM dbo.MovieGenres WHERE MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getGenreIdsForMovie failed", e);
        }
        return result;
    }

    @Override
    public List<Integer> getMovieIdsByDirector(String director) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT MovieId FROM dbo.Movies WHERE Director = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, director);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getMovieIdsByDirector failed", e);
        }
        return result;
    }

    @Override
    public String getMovieTrend(Integer movieId) {
        String sql = "SELECT TrendStatus FROM dbo.Movies WHERE MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null; // film ne postoji
                }
                return rs.getString(1); // moze biti null ako nijedno pravilo nije zadovoljeno
            }
        } catch (SQLException e) {
            throw new RuntimeException("getMovieTrend failed", e);
        }
    }

    private boolean doesMovieExist(int id) {
        String sql = "SELECT 1 FROM dbo.Movies WHERE MovieId = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("doesMovieExist failed", e);
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
}
