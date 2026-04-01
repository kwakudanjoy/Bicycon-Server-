package Database;

import org.example.bycicon.SHA256;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Create_Admin_Account {

    public static void create_Admin_Account() {

        try {
            if (!find_Admin()) {

                String adminPassword = SHA256.hash("admin");

                String query = """
                        INSERT INTO Account_Table 
                        (UserName, Email, Password, Role)
                        VALUES 
                        ('admin', 'admin@gmail.com', '%s', 'ADMIN')
                        """.formatted(adminPassword);

                Connection conn = DatabaseManager.getConnection();
                Statement stm = conn.createStatement();
                stm.executeUpdate(query);

                System.out.println("✅ Admin account created");

            } else {
                System.out.println("✅ Admin already exists");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* Check if admin exists */
    private  static boolean find_Admin() throws SQLException {

        String query = """
                SELECT COUNT(*) 
                FROM Account_Table 
                WHERE Role = 'ADMIN'
                """;

        Connection conn = DatabaseManager.getConnection();
        Statement stm = conn.createStatement();
        ResultSet rs = stm.executeQuery(query);

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }

        return false;
    }
}
