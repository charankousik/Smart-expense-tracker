package com.student.expense;
import java.time.LocalDate;
public class Expense {
    private final int id;
    private final String title, category, type;
    private final double amount;
    private final LocalDate date;
    public Expense(int id, String title, String category, double amount, LocalDate date, String type) {
        this.id=id; this.title=title; this.category=category; this.amount=amount; this.date=date; this.type=type;
    }
    public int getId(){return id;} public String getTitle(){return title;}
    public String getCategory(){return category;} public double getAmount(){return amount;}
    public LocalDate getDate(){return date;} public String getType(){return type;}
}
