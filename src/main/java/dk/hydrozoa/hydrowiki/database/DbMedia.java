package dk.hydrozoa.hydrowiki.database;

import java.sql.*;

public class DbMedia {

    public record RMedia(int id, String filename, int userId, Timestamp created){}

    private static RMedia mediaFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String filename = rs.getString("username");
        int userId = rs.getInt("user_id");
        Timestamp created = rs.getTimestamp("created_at");
        return new RMedia(id, filename, userId, created);
    }

    /**
     * Retrieves media by filename.
     */
    public static RMedia getUser(String filename, Connection con, Counter counter) {
        counter.increment();
        String query = """
            select 
                * 
            from 
                uploaded_media
            where 
                filename=?
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, filename);

            try (ResultSet rs = pstmt.executeQuery();) {
                while (rs.next()) {
                    return mediaFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
