package dk.hydrozoa.hydrowiki.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbArticles {

    public record RArticle(int id, String title, String content){}

    public record RArticleEdit(int id, int articleId, int version, int userId, String diff) {}

    private static RArticle articleFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String content = rs.getString("content");;
        return new RArticle(id, title, content);
    }

    private static RArticleEdit articleEditFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int articleId = rs.getInt("article_id");
        int version = rs.getInt("version");
        int userId = rs.getInt("user_id");
        String diff = rs.getString("unified_diff_to_prev");
        return new RArticleEdit(id, articleId, version, userId, diff);
    }

    /**
     * Retrieves an article by its title.
     */
    public static RArticle getArticle(String title, Connection con, Counter counter) {
        counter.increment();
        String query = """
            select 
                * 
            from 
                articles
            where 
                title=?
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, title);

            try (ResultSet rs = pstmt.executeQuery();) {
                while (rs.next()) {
                    return articleFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RArticleEdit getArticleEdit(int articleId, int version, Connection con, Counter counter) {
        counter.increment();
        String query = """
            select 
                * 
            from 
                article_edits
            where 
                article_id=?
                && version=?
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, articleId);
            pstmt.setInt(2, version);

            try (ResultSet rs = pstmt.executeQuery();) {
                while (rs.next()) {
                    return articleEditFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<RArticle> getAllArticles(Connection con, Counter counter) {
        counter.increment();
        String query = """
            select 
                * 
            from 
                articles
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            List<RArticle> result = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery();) {
                while (rs.next()) {
                    result.add(articleFromResultSet(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public static List<RArticleEdit> getAllArticleEdits(Connection con, Counter counter) {
        counter.increment();
        String query = """
            select 
                * 
            from 
                article_edits
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            List<RArticleEdit> result = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery();) {
                while (rs.next()) {
                    result.add(articleEditFromResultSet(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public static int insertArticle(String title, String content, Connection con, Counter counter) {
        counter.increment();
        String query = """
            INSERT INTO 
                articles(
                    title, 
                    content) 
                VALUES (?,?);
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.executeQuery();

            try (ResultSet rs = con.prepareStatement("SELECT LAST_INSERT_ID()").executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int insertArticleEdit(int articleId, String diff, Connection con, Counter counter) {
        counter.increment();
        String query = """
            INSERT INTO article_edits (article_id, version, user_id, unified_diff_to_prev)
            SELECT
                ? AS article_id,
                COALESCE(MAX(ae.version), 0) + 1 AS version,
                -1 AS user_id,
                ? AS unified_diff_to_prev
            FROM
                article_edits AS ae
            WHERE
                ae.article_id = ?;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, articleId);
            pstmt.setString(2, diff);
            pstmt.setInt(3, articleId);
            pstmt.executeQuery();

            try (ResultSet rs = con.prepareStatement("SELECT LAST_INSERT_ID()").executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean updateArticleContent(int articleID, String newContent, Connection con, Counter counter) {
        counter.increment();
        String query = "update articles set content=? where id=?;";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, newContent);
            pstmt.setInt(2, articleID);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
