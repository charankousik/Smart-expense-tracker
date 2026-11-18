# Smart Expense Tracker — Project Overview

## Objective
Build a simple desktop application that helps a user record income and expenses and understand their current balance.

## Technology
- Java 17
- Java Swing for the desktop interface
- SQLite for persistent local storage
- JDBC for database access
- Maven for dependency management

## Implemented functionality
1. Dashboard showing total income, total expenses and available balance.
2. Add income and expense transactions with a description, positive amount and category.
3. Store transactions in a local SQLite database so records remain available after restarting the application.
4. Search transactions from the dashboard and the full transactions page.
5. Delete a selected transaction after confirmation.
6. Monthly income/expense chart and category-based expense breakdown.
7. Refresh the interface from the saved database records.

## Design
The interface uses a dark, high-contrast palette, a lime accent, readable Segoe UI typography, rounded cards and rounded action buttons. Navigation is provided for Dashboard, Transactions and Analytics. The application uses its own product name, Smart Expense Tracker, and contains no Folia branding.

## Data model
Each transaction stores an automatically generated ID, description/title, category, amount, transaction date and transaction type (Income or Expense).

## How to run
Open the project folder containing `pom.xml` in IntelliJ IDEA, allow Maven to resolve the SQLite JDBC dependency, then run `com.student.expense.Main`.
