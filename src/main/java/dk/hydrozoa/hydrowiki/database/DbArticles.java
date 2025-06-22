package dk.hydrozoa.hydrowiki.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbArticles {

    public record RArticle(int id, String title, String content){}

    private static RArticle articleFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String content = rs.getString("content");;
        return new RArticle(id, title, content);
    }

    public static RArticle getArticle(String name, Connection con, Counter counter) {
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
            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    return articleFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
