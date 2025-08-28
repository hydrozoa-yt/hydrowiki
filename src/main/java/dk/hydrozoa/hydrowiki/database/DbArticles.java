package dk.hydrozoa.hydrowiki.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbArticles {

    public record RArticle(int id, String title, String content){}

    public record RArticleEdit(int id, int articleId, int version, int userId, String diff, int charLenDiff, Timestamp created) {}

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
        int charLenDiff = rs.getInt("character_len_diff");
        Timestamp created = rs.getTimestamp("created_at");
        return new RArticleEdit(id, articleId, version, userId, diff, charLenDiff, created);
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
                and version=?
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

    public static List<RArticle> searchArticles(String terms, Connection con, Counter counter) {
        counter.increment();
        String query = """
            select 
                * 
            from 
                articles
            where 
                MATCH(title, content) AGAINST (?)
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, terms);

            try (ResultSet rs = pstmt.executeQuery();) {
                List<RArticle> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(articleFromResultSet(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all the edits between the newest version and the specified version, ordered by the newest version first.
     */
    public static List<RArticleEdit> getArticleEditsSince(int articleId, int version, Connection con, Counter counter) {
        counter.increment();
        String query = """
            SELECT
                 *
             FROM
                 article_edits
             WHERE
                 article_id = ?  -- Replace with the actual article ID
                 AND version >= ? -- Replace with the starting version you're interested in
                 AND version <= (SELECT MAX(version) FROM article_edits WHERE article_id = ?)
             ORDER BY
                 version DESC;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, articleId);
            pstmt.setInt(2, version);
            pstmt.setInt(3, articleId);

            try (ResultSet rs = pstmt.executeQuery();) {
                List<RArticleEdit> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(articleEditFromResultSet(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return List.of();
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
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(articleFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return List.of();
    }



    public static List<RArticleEditWithExtra> getAllArticleEdits(int articleId, Connection con, Counter counter) {
        counter.increment();
        String query = """
            SELECT
                article_edits.*,
                articles.title,
                users.username
            FROM
                article_edits
            LEFT JOIN
                articles ON articles.id=article_edits.article_id
            LEFT JOIN
                users ON users.id=article_edits.user_id
            WHERE
                article_id = ?
            ORDER BY
                version DESC
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, articleId);

            List<RArticleEditWithExtra> result = new ArrayList<>();
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                RArticleEdit editData = articleEditFromResultSet(rs);
                String title = rs.getString("title");
                String username = rs.getString("username");
                result.add(new RArticleEditWithExtra(editData, title, username));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public record RArticleEditWithExtra(RArticleEdit edit, String title, String username){}

    public static List<RArticleEditWithExtra> getRecentArticleEdits(Connection con, Counter counter) {
        counter.increment();
        String query = """
            SELECT
                article_edits.*,
                articles.title,
                users.username
            FROM
                article_edits
            LEFT JOIN
                articles ON articles.id=article_edits.article_id
            LEFT JOIN
                users ON users.id=article_edits.user_id
            ORDER BY
                article_edits.created_at DESC
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            List<RArticleEditWithExtra> result = new ArrayList<>();
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                RArticleEdit edit = articleEditFromResultSet(rs);
                String title = rs.getString("title");
                String username = rs.getString("username") == null ? "NaN" : rs.getString("username");
                result.add(new RArticleEditWithExtra(edit, title, username));
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

    public static int insertArticleEdit(int userId, int articleId, String diff, int characterLenDiff, Connection con, Counter counter) {
        counter.increment();
        String query = """
            INSERT INTO article_edits (article_id, version, user_id, unified_diff_to_prev, character_len_diff)
            SELECT
                ? AS article_id,
                COALESCE(MAX(ae.version), 0) + 1 AS version,
                ? AS user_id,
                ? AS unified_diff_to_prev,
                ? AS character_len_diff
            FROM
                article_edits AS ae
            WHERE
                ae.article_id = ?;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, articleId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, diff);
            pstmt.setInt(4, characterLenDiff);
            pstmt.setInt(5, articleId);
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

    /**
     * Deletes an entry from the 'articles' table by its id, and all associated entries in table "article_edits".
     */
    public static int deleteArticle(int articleID, Connection con, Counter counter) {
        counter.increment();
        String queryArticles = """
            DELETE FROM
                articles
            WHERE
                id = ?;
            """;

        int rowsAffected = 0;

        try (PreparedStatement pstmt = con.prepareStatement(queryArticles)) {
            pstmt.setInt(1, articleID);
            rowsAffected += pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting article entry: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }

        counter.increment();
        String queryEdits = """
            DELETE FROM
                article_edits
            WHERE
                article_id = ?;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(queryEdits)) {
            pstmt.setInt(1, articleID);
            rowsAffected += pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting article edits entry: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }

        return rowsAffected;
    }
}
