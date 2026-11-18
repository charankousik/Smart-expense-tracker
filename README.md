# Smart Expense Tracker

A modern, dark-themed personal finance desktop app built with Java 17, Swing, SQLite and Maven. It uses a lime accent, readable Segoe UI typography, rounded cards and curved-edge buttons. There is no Folia branding.

## Features
- Dashboard with live income, expense and available-balance totals.
- Monthly income-versus-expense chart and category spending breakdown.
- Add income and expense transactions with input validation.
- Persistent local SQLite storage; existing transaction records are retained.
- Search and delete transactions, plus export all transactions to CSV.
- Monthly category budgets with automatic current-month spending and remaining-limit calculations.
- Savings goals with target amounts, optional due dates, contributions and progress tracking.
- Analytics page with charts that refresh after data changes.
- Rounded, colour-coded buttons and dark-themed forms/dialogs.

## Requirements
- JDK 17 or newer
- IntelliJ IDEA (or another Java IDE)
- Maven support in the IDE; SQLite JDBC is resolved from the included `pom.xml` on first import.

## Run in IntelliJ IDEA
1. Extract this ZIP to a normal folder.
2. In IntelliJ, choose **File → Open** and select the `SmartExpenseTracker_Humanized` folder (the folder containing `pom.xml`).
3. Trust the project if IntelliJ asks, then wait for Maven dependency import to finish.
4. Open `src/main/java/com/student/expense/Main.java`.
5. Click the green run triangle beside `main`, or right-click and choose **Run 'Main.main()'**.

The app creates its tables automatically. Keep `expenses.db` in the project root to retain your local data. Existing transactions are not deleted by the upgrade; the new budgets and savings-goals tables are added alongside them.

## Project structure
- `Expense.java` — transaction model
- `ExpenseDatabase.java` — SQLite connection and schema creation
- `ExpenseRepository.java` — transaction database operations
- `ExpenseTrackerWindow.java` — dashboard, transactions, analytics, budgets, savings goals and UI actions
- `Main.java` — application entry point and theme setup
