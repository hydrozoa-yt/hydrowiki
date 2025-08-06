package dk.hydrozoa.hydrowiki.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbUsers {

    public record RUser(int id, String username, String email, String password, int rights){}

    private static RUser userFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        int rights = rs.getInt("rights");
        return new RUser(id, username, email, password, rights);
    }

    /**
     * Retrieves a user by username.
     */
    public static RUser getUser(String username, Connection con, Counter counter) {
        counter.increment();
        String query = """
            select 
                * 
            from 
                users
            where 
                username=?
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery();) {
                while (rs.next()) {
                    return userFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a user by id.
     */
    public static RUser getUser(int id, Connection con, Counter counter) {
        counter.increment();
        String query = """
            select 
                * 
            from 
                users
            where 
                id=?
            ;
            """;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery();) {
                while (rs.next()) {
                    return userFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
