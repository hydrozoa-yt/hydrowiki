package dk.hydrozoa.hydrowiki.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbMedia {

    public record RMedia(int id, String filename, int userId, Timestamp created){}

    private static RMedia mediaFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String filename = rs.getString("filename");
        int userId = rs.getInt("user_id");
        Timestamp created = rs.getTimestamp("created_at");
        return new RMedia(id, filename, userId, created);
    }

    /**
     * Retrieves media by filename.
     */
    public static RMedia getMedia(String filename, Connection con, Counter counter) {
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

    /**
     * Retrieves all media sorted by newest first.
     */
    public static List<RMedia> getAllMedia(Connection con, Counter counter) {
        counter.increment();
        String query = """
            select 
                * 
            from 
                uploaded_media
            ORDER BY
                created_at DESC
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            try (ResultSet rs = pstmt.executeQuery();) {
                List result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mediaFromResultSet(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int insertMedia(String filename, int userId, Connection con, Counter counter) {
        counter.increment();
        String query = """
            INSERT INTO 
                uploaded_media(
                    filename, 
                    user_id
                    ) 
                VALUES (?,?);
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, filename);
            pstmt.setInt(2, userId);
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

    /**
     * Deletes an entry from the 'uploaded_media' table by its filename.
     *
     * @param filename The name of the file to delete.
     * @param con The database connection.
     * @param counter A counter object to track operations.
     * @return The number of rows deleted, or -1 if an error occurred.
     */
    public static int deleteMedia(String filename, Connection con, Counter counter) {
        counter.increment(); // Increment the counter for this operation
        String query = """
            DELETE FROM
                uploaded_media
            WHERE
                filename = ?;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, filename);
            // Use executeUpdate() for DELETE, INSERT, UPDATE statements.
            // It returns the row count of affected rows.
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            System.err.println("Error deleting media entry: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Return -1 to indicate an error
    }
}
