package com.student.expense;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Color panel = new Color(26, 31, 28);
                Color text = new Color(239, 243, 238);
                Color accent = new Color(190, 255, 86);
                UIManager.put("Panel.background", panel);
                UIManager.put("OptionPane.background", panel);
                UIManager.put("OptionPane.messageForeground", text);
                UIManager.put("Label.foreground", text);
                UIManager.put("TextField.background", new Color(13, 16, 15));
                UIManager.put("TextField.foreground", text);
                UIManager.put("TextField.caretForeground", accent);
                UIManager.put("TextField.selectionBackground", new Color(58, 76, 58));
                UIManager.put("ComboBox.background", new Color(34, 40, 36));
                UIManager.put("ComboBox.foreground", text);
                UIManager.put("Button.background", new Color(190, 255, 86));
                UIManager.put("Button.foreground", new Color(13, 16, 15));
                UIManager.put("Button.focus", new Color(190, 255, 86));
            } catch (Exception ignored) { }
            ExpenseDatabase.createTable();
            new ExpenseTrackerWindow().setVisible(true);
        });
    }
}
