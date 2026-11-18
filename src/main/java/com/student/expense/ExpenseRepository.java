package com.student.expense;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class ExpenseRepository {
    public void add(Expense item) throws SQLException {
        String sql="INSERT INTO transactions(title,category,amount,transaction_date,type) VALUES(?,?,?,?,?)";
        try(Connection c=ExpenseDatabase.connect(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,item.getTitle()); p.setString(2,item.getCategory());
            p.setDouble(3,item.getAmount()); p.setString(4,item.getDate().toString());
            p.setString(5,item.getType()); p.executeUpdate();
        }
    }
    public List<Expense> findAll() throws SQLException {
        List<Expense> items=new ArrayList<>();
        try(Connection c=ExpenseDatabase.connect();
            Statement s=c.createStatement();
            ResultSet r=s.executeQuery("SELECT * FROM transactions ORDER BY transaction_date DESC,id DESC")) {
            while(r.next()) items.add(new Expense(r.getInt("id"),r.getString("title"),
                    r.getString("category"),r.getDouble("amount"),
                    LocalDate.parse(r.getString("transaction_date")),r.getString("type")));
        }
        return items;
    }
    public void delete(int id) throws SQLException {
        try(Connection c=ExpenseDatabase.connect();
            PreparedStatement p=c.prepareStatement("DELETE FROM transactions WHERE id=?")) {
            p.setInt(1,id); p.executeUpdate();
        }
    }
}
