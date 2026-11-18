package com.student.expense;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class ExpenseDatabase {
    private static final String URL = "jdbc:sqlite:expenses.db";
    public static Connection connect() throws SQLException { return DriverManager.getConnection(URL); }
    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, " +
                "category TEXT NOT NULL, amount REAL NOT NULL, " +
                "transaction_date TEXT NOT NULL, type TEXT NOT NULL)";
        try (Connection c=connect(); Statement s=c.createStatement()) { s.execute(sql);
            s.execute("CREATE TABLE IF NOT EXISTS budgets (id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT NOT NULL UNIQUE, monthly_limit REAL NOT NULL CHECK(monthly_limit > 0))");
            s.execute("CREATE TABLE IF NOT EXISTS savings_goals (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, target REAL NOT NULL CHECK(target > 0), saved REAL NOT NULL DEFAULT 0 CHECK(saved >= 0), due_date TEXT)");
        }
        catch (SQLException e) { throw new RuntimeException("Could not create the database.", e); }
    }
}
