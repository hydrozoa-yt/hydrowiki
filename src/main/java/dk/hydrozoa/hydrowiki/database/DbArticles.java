package dk.hydrozoa.hydrowiki.database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbArticles {

    public record RArticle(int id, String title, String content){}

    private static RArticle articleFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String content = rs.getString("content");;
        return new RArticle(id, title, content);
    }

    /**
     * Retrieves an article by its title.
     */
    public static RArticle get(String title, Connection con, Counter counter) {
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

    /**
     * Retrieves an article by its title.
     */
    public static List<RArticle> getAll(Connection con, Counter counter) {
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

    public static int insert(String title, String content, Connection con, Counter counter) {
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

    public static boolean updateContent(int articleID, String newContent, Connection con) {
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
