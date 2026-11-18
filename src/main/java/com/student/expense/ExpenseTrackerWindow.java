package com.student.expense;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ExpenseTrackerWindow extends JFrame {
    private static final Color BG = new Color(13, 16, 15);
    private static final Color SIDEBAR = new Color(18, 22, 20);
    private static final Color PANEL = new Color(26, 31, 28);
    private static final Color PANEL_ALT = new Color(34, 40, 36);
    private static final Color BORDER = new Color(48, 57, 50);
    private static final Color TEXT = new Color(239, 243, 238);
    private static final Color MUTED = new Color(151, 161, 152);
    private static final Color LIME = new Color(190, 255, 86);
    private static final Color RED = new Color(255, 116, 116);
    private static final Color TEAL = new Color(77, 218, 183);
    private static final Color YELLOW = new Color(255, 205, 92);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private final ExpenseRepository repository = new ExpenseRepository();
    private final List<Expense> transactions = new ArrayList<>();
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pages = new JPanel(pageLayout);
    private final JLabel incomeValue = new JLabel("₹0.00");
    private final JLabel expenseValue = new JLabel("₹0.00");
    private final JLabel balanceValue = new JLabel("₹0.00");
    private final JLabel dashboardCount = new JLabel("0 transactions");
    private final JLabel transactionsCount = new JLabel("0 transactions");
    private final JTextField dashboardSearch = new JTextField(16);
    private final JTextField transactionSearch = new JTextField(20);
    private final DefaultTableModel dashboardModel = tableModel();
    private final DefaultTableModel transactionModel = tableModel();
    private final JTable dashboardTable = new JTable(dashboardModel);
    private final JTable transactionTable = new JTable(transactionModel);
    private final MonthlyChart monthlyChart = new MonthlyChart();
    private final CategoryChart categoryChart = new CategoryChart();
    private final JLabel pageHeading = new JLabel("Dashboard");
    private final Map<String, RoundButton> navButtons = new LinkedHashMap<>();
    private final DefaultTableModel budgetModel = new DefaultTableModel(new String[]{"ID", "Category", "Monthly limit", "Spent this month", "Remaining"}, 0) { public boolean isCellEditable(int r,int c){return false;} };
    private final DefaultTableModel goalModel = new DefaultTableModel(new String[]{"ID", "Goal", "Target", "Saved", "Remaining", "Due date", "Progress"}, 0) { public boolean isCellEditable(int r,int c){return false;} };
    private final JTable budgetTable = new JTable(budgetModel);
    private final JTable goalTable = new JTable(goalModel);

    public ExpenseTrackerWindow() {
        setTitle("Smart Expense Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        buildScreen();
        refreshData();
    }

    private DefaultTableModel tableModel() {
        return new DefaultTableModel(new String[]{"ID", "Description", "Date", "Category", "Type", "Amount"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private void buildScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        content.add(buildHeader(), BorderLayout.NORTH);

        pages.setOpaque(false);
        pages.add(buildDashboardPage(), "Dashboard");
        pages.add(buildTransactionsPage(), "Transactions");
        pages.add(buildAnalyticsPage(), "Analytics");
        pages.add(buildBudgetsPage(), "Budgets");
        pages.add(buildGoalsPage(), "Savings Goals");
        content.add(pages, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
        showPage("Dashboard");
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(224, 0));
        side.setBackground(SIDEBAR);
        side.setBorder(new EmptyBorder(25, 15, 20, 15));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("◈  SMART WALLET");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brand.setForeground(LIME);
        brand.setBorder(new EmptyBorder(4, 8, 38, 4));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(brand);
        side.add(sectionLabel("WORKSPACE"));
        addNav(side, "Dashboard", "▦");
        addNav(side, "Transactions", "↕");
        addNav(side, "Analytics", "▥");
        addNav(side, "Budgets", "◫");
        addNav(side, "Savings Goals", "◎");
        side.add(Box.createVerticalGlue());

        JPanel wallet = new RoundedPanel(PANEL, BORDER, 18);
        wallet.setLayout(new BorderLayout(10, 0));
        wallet.setBorder(new EmptyBorder(12, 11, 12, 11));
        JLabel avatar = new JLabel("₹");
        avatar.setOpaque(true); avatar.setBackground(LIME); avatar.setForeground(BG);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 17)); avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(34, 34));
        JLabel walletText = new JLabel("<html><b>My Wallet</b><br><font color='#9aa49b'>Personal finances</font></html>");
        walletText.setForeground(TEXT); walletText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        wallet.add(avatar, BorderLayout.WEST); wallet.add(walletText, BorderLayout.CENTER);
        wallet.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        wallet.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(wallet);
        return side;
    }

    private void addNav(JPanel side, String name, String icon) {
        RoundButton b = new RoundButton(icon + "   " + name, PANEL, TEXT, 12);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(12, 14, 12, 12));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> showPage(name));
        navButtons.put(name, b);
        side.add(b);
        side.add(Box.createVerticalStrut(6));
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10)); l.setForeground(new Color(112, 124, 113));
        l.setBorder(new EmptyBorder(8, 9, 10, 0)); l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel titles = new JPanel(); titles.setOpaque(false); titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel crumb = new JLabel("MY FINANCES   /   OVERVIEW"); crumb.setFont(new Font("Segoe UI", Font.BOLD, 10)); crumb.setForeground(MUTED);
        pageHeading.setFont(new Font("Segoe UI", Font.BOLD, 24)); pageHeading.setForeground(TEXT);
        JLabel subtitle = new JLabel("A clear view of where your money goes."); subtitle.setFont(BODY); subtitle.setForeground(MUTED);
        titles.add(crumb); titles.add(Box.createVerticalStrut(5)); titles.add(pageHeading); titles.add(Box.createVerticalStrut(3)); titles.add(subtitle);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5)); actions.setOpaque(false);
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        date.setFont(BOLD); date.setForeground(MUTED);
        RoundButton add = new RoundButton("＋  Add transaction", LIME, BG, 14);
        add.addActionListener(e -> chooseTransactionType());
        actions.add(date); actions.add(add);
        header.add(titles, BorderLayout.WEST); header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16)); page.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0)); cards.setOpaque(false);
        cards.add(summaryCard("NET INCOME", incomeValue, "↑  Money received", LIME));
        cards.add(summaryCard("TOTAL EXPENSES", expenseValue, "↓  Money spent", RED));
        cards.add(summaryCard("AVAILABLE BALANCE", balanceValue, "●  Current balance", TEAL));

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 0)); charts.setOpaque(false);
        charts.add(chartPanel("Income & expenses", "Monthly activity", monthlyChart));
        charts.add(chartPanel("Spending breakdown", "Expenses by category", categoryChart));
        JPanel recent = tablePanel("Recent transactions", dashboardTable, dashboardSearch, dashboardCount, true);
        JPanel center = new JPanel(new BorderLayout(0, 16)); center.setOpaque(false);
        center.add(charts, BorderLayout.NORTH); center.add(recent, BorderLayout.CENTER);
        page.add(cards, BorderLayout.NORTH); page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildTransactionsPage() {
        JPanel page = new JPanel(new BorderLayout()); page.setOpaque(false);
        JPanel box = tablePanel("All transactions", transactionTable, transactionSearch, transactionsCount, false);
        page.add(box, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildAnalyticsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16)); page.setOpaque(false);
        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 0)); charts.setOpaque(false);
        charts.add(chartPanel("Income & expenses", "Monthly activity for this year", monthlyChart));
        charts.add(chartPanel("Spending breakdown", "Where your expenses go", categoryChart));
        RoundedPanel insight = new RoundedPanel(PANEL, BORDER, 18);
        insight.setLayout(new BorderLayout()); insight.setBorder(new EmptyBorder(18, 20, 18, 20));
        JLabel text = new JLabel("<html><b style='font-size:14px'>Your money snapshot</b><br><br><font color='#9aa49b'>These charts update automatically whenever you add or delete a transaction. Use the Transactions page to search and manage your records.</font></html>");
        text.setForeground(TEXT); text.setFont(BODY); insight.add(text, BorderLayout.CENTER);
        page.add(charts, BorderLayout.CENTER); page.add(insight, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildBudgetsPage() {
        RoundedPanel box = new RoundedPanel(PANEL, BORDER, 18);
        box.setLayout(new BorderLayout(0, 14)); box.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel title = new JLabel("Monthly budgets"); title.setFont(new Font("Segoe UI", Font.BOLD, 17)); title.setForeground(TEXT);
        JLabel note = new JLabel("Set a spending limit for each category and keep an eye on what remains."); note.setFont(BODY); note.setForeground(MUTED);
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS)); text.add(title); text.add(Box.createVerticalStrut(5)); text.add(note);
        RoundButton add = new RoundButton("＋  Add budget", LIME, BG, 14); add.addActionListener(e -> addBudget());
        top.add(text, BorderLayout.WEST); top.add(add, BorderLayout.EAST);
        styleTable(budgetTable); JScrollPane scroll = new JScrollPane(budgetTable); scroll.setBorder(BorderFactory.createLineBorder(BORDER)); scroll.getViewport().setBackground(PANEL);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); bottom.setOpaque(false);
        RoundButton delete = new RoundButton("Delete selected", PANEL_ALT, TEXT, 12); delete.addActionListener(e -> deleteSimpleRow(budgetTable, "budgets"));
        RoundButton refresh = new RoundButton("Refresh", PANEL_ALT, TEXT, 12); refresh.addActionListener(e -> refreshData()); bottom.add(delete); bottom.add(refresh);
        box.add(top, BorderLayout.NORTH); box.add(scroll, BorderLayout.CENTER); box.add(bottom, BorderLayout.SOUTH);
        JPanel page = new JPanel(new BorderLayout()); page.setOpaque(false); page.add(box); return page;
    }

    private JPanel buildGoalsPage() {
        RoundedPanel box = new RoundedPanel(PANEL, BORDER, 18);
        box.setLayout(new BorderLayout(0, 14)); box.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel title = new JLabel("Savings goals"); title.setFont(new Font("Segoe UI", Font.BOLD, 17)); title.setForeground(TEXT);
        JLabel note = new JLabel("Create a goal, add savings whenever you like, and track your progress."); note.setFont(BODY); note.setForeground(MUTED);
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS)); text.add(title); text.add(Box.createVerticalStrut(5)); text.add(note);
        RoundButton add = new RoundButton("＋  New goal", LIME, BG, 14); add.addActionListener(e -> addGoal());
        top.add(text, BorderLayout.WEST); top.add(add, BorderLayout.EAST);
        styleTable(goalTable); JScrollPane scroll = new JScrollPane(goalTable); scroll.setBorder(BorderFactory.createLineBorder(BORDER)); scroll.getViewport().setBackground(PANEL);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); bottom.setOpaque(false);
        RoundButton contribute = new RoundButton("＋  Add savings", LIME, BG, 12); contribute.addActionListener(e -> contributeGoal());
        RoundButton delete = new RoundButton("Delete selected", PANEL_ALT, TEXT, 12); delete.addActionListener(e -> deleteSimpleRow(goalTable, "savings_goals"));
        bottom.add(contribute); bottom.add(delete);
        box.add(top, BorderLayout.NORTH); box.add(scroll, BorderLayout.CENTER); box.add(bottom, BorderLayout.SOUTH);
        JPanel page = new JPanel(new BorderLayout()); page.setOpaque(false); page.add(box); return page;
    }

    private void addBudget() {
        JComboBox<String> category = new JComboBox<>(new String[]{"Food", "Travel", "Shopping", "Bills", "Education", "Health", "Entertainment", "Other"});
        JTextField limit = new JTextField();
        JPanel form = darkForm(); form.add(darkLabel("Category")); form.add(category); form.add(darkLabel("Monthly limit (₹)")); form.add(limit);
        if (JOptionPane.showConfirmDialog(this, form, "Add monthly budget", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try { double value = parsePositive(limit.getText()); try(Connection c=ExpenseDatabase.connect(); PreparedStatement p=c.prepareStatement("INSERT INTO budgets(category,monthly_limit) VALUES(?,?) ON CONFLICT(category) DO UPDATE SET monthly_limit=excluded.monthly_limit")){p.setString(1,(String)category.getSelectedItem());p.setDouble(2,value);p.executeUpdate();} refreshData(); }
        catch(Exception ex){ message("Could not save budget. Enter a valid amount. " + ex.getMessage()); }
    }

    private void addGoal() {
        JTextField name = new JTextField(); JTextField target = new JTextField(); JTextField due = new JTextField(); due.setToolTipText("Optional, format YYYY-MM-DD");
        JPanel form = darkForm(); form.add(darkLabel("Goal name")); form.add(name); form.add(darkLabel("Target amount (₹)")); form.add(target); form.add(darkLabel("Due date (optional, YYYY-MM-DD)")); form.add(due);
        if (JOptionPane.showConfirmDialog(this, form, "Create savings goal", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try { if(name.getText().trim().isEmpty()) throw new IllegalArgumentException("Enter a goal name."); double value=parsePositive(target.getText()); String date=due.getText().trim(); if(!date.isEmpty()) LocalDate.parse(date); try(Connection c=ExpenseDatabase.connect();PreparedStatement p=c.prepareStatement("INSERT INTO savings_goals(name,target,saved,due_date) VALUES(?,?,0,?)")){p.setString(1,name.getText().trim());p.setDouble(2,value);p.setString(3,date.isEmpty()?null:date);p.executeUpdate();} refreshData(); }
        catch(Exception ex){message("Could not create goal. Check the name, amount and date (YYYY-MM-DD). " + ex.getMessage());}
    }

    private void contributeGoal() {
        int row=goalTable.getSelectedRow(); if(row<0){message("Select a savings goal first.");return;} int modelRow=goalTable.convertRowIndexToModel(row); int id=(int)goalModel.getValueAt(modelRow,0);
        String raw=JOptionPane.showInputDialog(this,"How much would you like to add? (₹)","Add savings",JOptionPane.PLAIN_MESSAGE); if(raw==null)return;
        try{double amount=parsePositive(raw);try(Connection c=ExpenseDatabase.connect();PreparedStatement p=c.prepareStatement("UPDATE savings_goals SET saved=saved+? WHERE id=?")){p.setDouble(1,amount);p.setInt(2,id);p.executeUpdate();}refreshData();}
        catch(Exception ex){message("Enter a valid amount greater than zero.");}
    }

    private void deleteSimpleRow(JTable table, String tableName) {
        int row=table.getSelectedRow(); if(row<0){message("Select a row first.");return;} int mr=table.convertRowIndexToModel(row); int id=(int)((DefaultTableModel)table.getModel()).getValueAt(mr,0);
        if(JOptionPane.showConfirmDialog(this,"Permanently delete this item?","Confirm deletion",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try(Connection c=ExpenseDatabase.connect();PreparedStatement p=c.prepareStatement("DELETE FROM "+tableName+" WHERE id=?")){p.setInt(1,id);p.executeUpdate();refreshData();}catch(SQLException ex){message("Could not delete item: "+ex.getMessage());}
    }

    private double parsePositive(String raw) { double v=Double.parseDouble(raw.trim().replace(",","")); if(!Double.isFinite(v)||v<=0)throw new IllegalArgumentException("Amount must be greater than zero."); return v; }
    private JPanel darkForm(){JPanel p=new JPanel(new GridLayout(0,2,12,12));p.setBackground(PANEL);p.setBorder(new EmptyBorder(14,14,14,14));return p;}
    private JLabel darkLabel(String t){JLabel l=new JLabel(t);l.setForeground(TEXT);l.setFont(BODY);return l;}

    private void exportCsv() {
        JFileChooser chooser=new JFileChooser(); chooser.setSelectedFile(new File("transactions.csv"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        File file=chooser.getSelectedFile();
        try(PrintWriter out=new PrintWriter(new FileWriter(file))){out.println("ID,Description,Date,Category,Type,Amount");for(Expense e:transactions)out.println(e.getId()+",\""+e.getTitle().replace("\"","\"\"")+"\","+e.getDate()+",\""+e.getCategory()+"\","+e.getType()+","+e.getAmount());message("Transactions exported to "+file.getAbsolutePath());}
        catch(IOException ex){message("Could not export CSV: "+ex.getMessage());}
    }

    private JPanel summaryCard(String title, JLabel value, String hint, Color accent) {
        RoundedPanel card = new RoundedPanel(PANEL, BORDER, 18);
        card.setLayout(new BorderLayout(0, 13)); card.setBorder(new EmptyBorder(17, 19, 17, 19));
        JLabel label = new JLabel(title); label.setFont(new Font("Segoe UI", Font.BOLD, 10)); label.setForeground(MUTED);
        value.setFont(new Font("Segoe UI", Font.BOLD, 25)); value.setForeground(TEXT);
        JLabel note = new JLabel(hint); note.setFont(new Font("Segoe UI", Font.BOLD, 11)); note.setForeground(accent);
        card.add(label, BorderLayout.NORTH); card.add(value, BorderLayout.CENTER); card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private JPanel chartPanel(String title, String subtitle, JComponent chart) {
        RoundedPanel box = new RoundedPanel(PANEL, BORDER, 18);
        box.setLayout(new BorderLayout(0, 8)); box.setBorder(new EmptyBorder(15, 17, 12, 17));
        JPanel labels = new JPanel(); labels.setOpaque(false); labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(title); name.setFont(new Font("Segoe UI", Font.BOLD, 15)); name.setForeground(TEXT);
        JLabel sub = new JLabel(subtitle); sub.setFont(new Font("Segoe UI", Font.PLAIN, 11)); sub.setForeground(MUTED);
        labels.add(name); labels.add(Box.createVerticalStrut(3)); labels.add(sub);
        box.add(labels, BorderLayout.NORTH); box.add(chart, BorderLayout.CENTER);
        return box;
    }

    private JPanel tablePanel(String title, JTable targetTable, JTextField search, JLabel count, boolean dashboard) {
        RoundedPanel box = new RoundedPanel(PANEL, BORDER, 18);
        box.setLayout(new BorderLayout(0, 12)); box.setBorder(new EmptyBorder(15, 17, 13, 17));
        JPanel heading = new JPanel(new BorderLayout(12, 0)); heading.setOpaque(false);
        JLabel label = new JLabel(title); label.setFont(new Font("Segoe UI", Font.BOLD, 15)); label.setForeground(TEXT);
        styleSearch(search);
        heading.add(label, BorderLayout.WEST); heading.add(search, BorderLayout.EAST);
        styleTable(targetTable);
        JScrollPane scroll = new JScrollPane(targetTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER)); scroll.getViewport().setBackground(PANEL);
        scroll.getVerticalScrollBar().setBackground(PANEL);
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false);
        count.setFont(new Font("Segoe UI", Font.PLAIN, 11)); count.setForeground(MUTED);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); buttons.setOpaque(false);
        RoundButton income = new RoundButton("＋ Income", LIME, BG, 12);
        RoundButton expense = new RoundButton("＋ Expense", new Color(191, 75, 79), Color.WHITE, 12);
        RoundButton delete = new RoundButton("Delete selected", PANEL_ALT, TEXT, 12);
        RoundButton refresh = new RoundButton("Refresh", PANEL_ALT, TEXT, 12);
        income.addActionListener(e -> addTransaction("Income"));
        expense.addActionListener(e -> addTransaction("Expense"));
        delete.addActionListener(e -> deleteTransaction(targetTable));
        refresh.addActionListener(e -> refreshData());
        if (dashboard) { buttons.add(income); buttons.add(expense); } else { RoundButton export = new RoundButton("Export CSV", PANEL_ALT, TEXT, 12); export.addActionListener(e -> exportCsv()); buttons.add(export); }
        buttons.add(delete); buttons.add(refresh);
        footer.add(count, BorderLayout.WEST); footer.add(buttons, BorderLayout.EAST);
        box.add(heading, BorderLayout.NORTH); box.add(scroll, BorderLayout.CENTER); box.add(footer, BorderLayout.SOUTH);
        return box;
    }

    private void styleSearch(JTextField field) {
        field.setToolTipText("Search description, category, date or type");
        field.setFont(BODY); field.setForeground(TEXT); field.setCaretColor(LIME); field.setBackground(BG);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 11, 8, 11)));
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterBothTables(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterBothTables(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterBothTables(); }
        });
    }

    private void styleTable(JTable t) {
        t.setRowHeight(37); t.setFont(BODY); t.setForeground(TEXT); t.setBackground(PANEL);
        t.setGridColor(BORDER); t.setSelectionBackground(new Color(58, 76, 58)); t.setSelectionForeground(Color.WHITE);
        t.setShowVerticalLines(false); t.setFillsViewportHeight(true); t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        JTableHeader h = t.getTableHeader(); h.setPreferredSize(new Dimension(0, 36)); h.setBackground(PANEL_ALT);
        h.setForeground(MUTED); h.setFont(new Font("Segoe UI", Font.BOLD, 11)); h.setReorderingAllowed(false);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, col);
                c.setBackground(selected ? new Color(58, 76, 58) : (row % 2 == 0 ? PANEL : PANEL_ALT));
                c.setForeground(TEXT); setBorder(new EmptyBorder(4, 10, 4, 10)); return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(renderer);
        t.getColumnModel().getColumn(0).setMinWidth(0); t.getColumnModel().getColumn(0).setMaxWidth(0); t.getColumnModel().getColumn(0).setPreferredWidth(0);
    }

    private void showPage(String name) {
        pageLayout.show(pages, name); pageHeading.setText(name);
        for (Map.Entry<String, RoundButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(name);
            entry.getValue().setPalette(active ? LIME : PANEL, active ? BG : TEXT);
        }
    }

    private void chooseTransactionType() {
        Object[] options = {"＋  Add income", "－  Add expense", "Cancel"};
        int selected = JOptionPane.showOptionDialog(this, "Choose the transaction you want to record.", "New transaction",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selected == 0) addTransaction("Income");
        else if (selected == 1) addTransaction("Expense");
    }

    private void addTransaction(String type) {
        JTextField description = new JTextField(18);
        JTextField amount = new JTextField(14);
        JComboBox<String> category = new JComboBox<>(new String[]{"Food", "Travel", "Shopping", "Bills", "Education", "Health", "Entertainment", "Salary", "Other"});
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 12));
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Description")); form.add(description);
        form.add(new JLabel("Amount (₹)")); form.add(amount);
        form.add(new JLabel("Category")); form.add(category);
        form.add(new JLabel("Transaction type")); form.add(new JLabel(type));
        if (JOptionPane.showConfirmDialog(this, form, "Add " + type, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        String title = description.getText().trim();
        if (title.isEmpty()) { message("Please enter a description."); return; }
        try {
            double value = Double.parseDouble(amount.getText().trim().replace(",", ""));
            if (!Double.isFinite(value) || value <= 0) { message("Enter an amount greater than zero."); return; }
            repository.add(new Expense(0, title, (String) category.getSelectedItem(), value, LocalDate.now(), type));
            refreshData();
        } catch (NumberFormatException ex) { message("Please enter a valid amount, for example 250 or 1250.50."); }
        catch (SQLException ex) { message("Could not save transaction: " + ex.getMessage()); }
    }

    private void deleteTransaction(JTable source) {
        int viewRow = source.getSelectedRow();
        if (viewRow < 0) { message("Select a transaction from the table first."); return; }
        int row = source.convertRowIndexToModel(viewRow);
        int id = (int) ((DefaultTableModel) source.getModel()).getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Permanently delete this transaction?", "Confirm deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { repository.delete(id); refreshData(); }
        catch (SQLException ex) { message("Could not delete transaction: " + ex.getMessage()); }
    }

    private void refreshData() {
        try {
            transactions.clear(); transactions.addAll(repository.findAll());
            fillModel(dashboardModel); fillModel(transactionModel);
            double income = 0, expenses = 0;
            for (Expense item : transactions) {
                if ("Income".equalsIgnoreCase(item.getType())) income += item.getAmount(); else expenses += item.getAmount();
            }
            incomeValue.setText(String.format("₹%,.2f", income));
            expenseValue.setText(String.format("₹%,.2f", expenses));
            balanceValue.setText(String.format("₹%,.2f", income - expenses));
            dashboardCount.setText(transactions.size() + " transactions");
            transactionsCount.setText(transactions.size() + " transactions");
            monthlyChart.repaint(); categoryChart.repaint(); loadBudgetRows(); loadGoalRows(); filterBothTables();
        } catch (SQLException ex) { message("Could not load transactions: " + ex.getMessage()); }
    }

    private void loadBudgetRows() {
        budgetModel.setRowCount(0); LocalDate now=LocalDate.now();
        String sql="SELECT b.id,b.category,b.monthly_limit,COALESCE(SUM(t.amount),0) AS spent FROM budgets b LEFT JOIN transactions t ON t.category=b.category AND t.type='Expense' AND substr(t.transaction_date,1,7)=? GROUP BY b.id,b.category,b.monthly_limit ORDER BY b.category";
        try(Connection c=ExpenseDatabase.connect();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,now.toString().substring(0,7));try(ResultSet r=p.executeQuery()){while(r.next()){double lim=r.getDouble("monthly_limit"),spent=r.getDouble("spent");budgetModel.addRow(new Object[]{r.getInt("id"),r.getString("category"),String.format("₹%,.2f",lim),String.format("₹%,.2f",spent),String.format("₹%,.2f",lim-spent)});}}}catch(SQLException ex){message("Could not load budgets: "+ex.getMessage());}
    }
    private void loadGoalRows() {
        goalModel.setRowCount(0); String sql="SELECT * FROM savings_goals ORDER BY id DESC";
        try(Connection c=ExpenseDatabase.connect();Statement st=c.createStatement();ResultSet r=st.executeQuery(sql)){while(r.next()){double target=r.getDouble("target"),saved=r.getDouble("saved");int pct=(int)Math.min(100,Math.round(saved/target*100));goalModel.addRow(new Object[]{r.getInt("id"),r.getString("name"),String.format("₹%,.2f",target),String.format("₹%,.2f",saved),String.format("₹%,.2f",Math.max(0,target-saved)),r.getString("due_date")==null?"—":r.getString("due_date"),pct+"%"});}}catch(SQLException ex){message("Could not load savings goals: "+ex.getMessage());}
    }

    private void fillModel(DefaultTableModel m) {
        m.setRowCount(0);
        int limit = m == dashboardModel ? Math.min(8, transactions.size()) : transactions.size();
        for (int i = 0; i < limit; i++) {
            Expense item = transactions.get(i);
            m.addRow(new Object[]{item.getId(), item.getTitle(), item.getDate(), item.getCategory(), item.getType(), String.format("₹%,.2f", item.getAmount())});
        }
    }

    private void filterBothTables() {
        applyFilter(dashboardTable, dashboardModel, dashboardSearch.getText());
        applyFilter(transactionTable, transactionModel, transactionSearch.getText());
    }

    private void applyFilter(JTable table, DefaultTableModel model, String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        String q = query == null ? "" : query.trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
    }

    private void message(String text) { JOptionPane.showMessageDialog(this, text, "Smart Expense Tracker", JOptionPane.INFORMATION_MESSAGE); }

    private class MonthlyChart extends JPanel {
        private final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        MonthlyChart() { setOpaque(false); setPreferredSize(new Dimension(420, 205)); }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics); Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double[] in = new double[12], out = new double[12]; int year = LocalDate.now().getYear();
            for (Expense item : transactions) if (item.getDate() != null && item.getDate().getYear() == year) {
                int m = item.getDate().getMonthValue() - 1;
                if ("Income".equalsIgnoreCase(item.getType())) in[m] += item.getAmount(); else out[m] += item.getAmount();
            }
            double max = 1; for (int i = 0; i < 12; i++) max = Math.max(max, Math.max(in[i], out[i]));
            int base = getHeight() - 27, top = 25, chartH = Math.max(25, base - top);
            int group = Math.max(22, (getWidth() - 25) / 12); int barW = Math.max(5, Math.min(11, group / 3));
            for (int i = 0; i < 12; i++) {
                int x = 8 + i * group; int h1 = (int)(chartH * in[i] / max), h2 = (int)(chartH * out[i] / max);
                g.setColor(PANEL_ALT); g.fillRoundRect(x + 2, base - chartH, barW, chartH, 7, 7); g.fillRoundRect(x + barW + 5, base - chartH, barW, chartH, 7, 7);
                g.setColor(LIME); if (h1 > 0) g.fillRoundRect(x + 2, base - h1, barW, h1, 7, 7);
                g.setColor(RED); if (h2 > 0) g.fillRoundRect(x + barW + 5, base - h2, barW, h2, 7, 7);
                g.setColor(MUTED); g.setFont(new Font("Segoe UI", Font.PLAIN, 9)); g.drawString(months[i], x, base + 14);
            }
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10)); g.setColor(LIME); g.fillRoundRect(8, 5, 8, 8, 3, 3); g.setColor(MUTED); g.drawString("Income", 21, 13);
            g.setColor(RED); g.fillRoundRect(78, 5, 8, 8, 3, 3); g.setColor(MUTED); g.drawString("Expenses", 91, 13); g.dispose();
        }
    }

    private class CategoryChart extends JPanel {
        private final Color[] palette = {LIME, TEAL, new Color(117, 160, 255), YELLOW, new Color(208, 135, 255), RED};
        CategoryChart() { setOpaque(false); setPreferredSize(new Dimension(420, 205)); }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics); Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Map<String, Double> totals = new LinkedHashMap<>();
            for (Expense item : transactions) if (!"Income".equalsIgnoreCase(item.getType())) totals.merge(item.getCategory() == null ? "Other" : item.getCategory(), item.getAmount(), Double::sum);
            double total = totals.values().stream().mapToDouble(Double::doubleValue).sum();
            int d = Math.min(142, Math.max(95, getHeight() - 35)), x = 12, y = Math.max(12, (getHeight() - d) / 2), start = 90;
            if (total <= 0) { g.setColor(PANEL_ALT); g.fillOval(x, y, d, d); g.setColor(MUTED); g.setFont(BODY); g.drawString("No expenses yet", x + 20, y + d / 2); }
            else {
                int index = 0;
                for (Map.Entry<String, Double> e : totals.entrySet()) { int angle = (int)Math.round(360 * e.getValue() / total); g.setColor(palette[index++ % palette.length]); g.fill(new Arc2D.Double(x, y, d, d, start, -angle, Arc2D.PIE)); start -= angle; }
                int hole = d / 2; g.setColor(PANEL); g.fillOval(x + (d-hole)/2, y + (d-hole)/2, hole, hole);
                g.setColor(TEXT); g.setFont(new Font("Segoe UI", Font.BOLD, 13)); String amount = String.format("₹%.0f", total); FontMetrics fm = g.getFontMetrics(); g.drawString(amount, x + (d - fm.stringWidth(amount))/2, y + d/2 + 5);
            }
            int lx = x + d + 18, ly = Math.max(20, y + 12), i = 0; g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (Map.Entry<String, Double> e : totals.entrySet()) { if (i >= 6) break; g.setColor(palette[i % palette.length]); g.fillRoundRect(lx, ly + i*22, 8, 8, 3, 3); g.setColor(TEXT); String name = e.getKey(); if (name.length() > 12) name = name.substring(0, 11) + "…"; g.drawString(name, lx + 14, ly + 8 + i*22); g.setColor(MUTED); g.drawString(String.format("%.0f%%", e.getValue()/Math.max(total,1)*100), lx + 105, ly + 8 + i*22); i++; }
            g.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color fill, stroke; private final int radius;
        RoundedPanel(Color fill, Color stroke, int radius) { this.fill = fill; this.stroke = stroke; this.radius = radius; setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill); g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g.setColor(stroke); g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius); g.dispose(); super.paintComponent(graphics);
        }
    }

    private static class RoundButton extends JButton {
        private Color fill, ink; private final int radius;
        RoundButton(String text, Color fill, Color ink, int radius) { super(text); this.fill = fill; this.ink = ink; this.radius = radius; setFont(BOLD); setForeground(ink); setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); setBorder(new EmptyBorder(10, 15, 10, 15)); }
        void setPalette(Color background, Color foreground) { fill = background; ink = foreground; setForeground(foreground); repaint(); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = getModel().isPressed() ? fill.darker() : (getModel().isRollover() ? fill.brighter() : fill);
            g.setColor(c); g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius); g.dispose(); super.paintComponent(graphics);
        }
    }
}
