/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

public final class ThemeManager {
    private ThemeManager() {
    }

    public static void apply() {
        // Use FlatLaf Light theme as base
        FlatLightLaf.setup();
        
        // Customize FlatLaf with our professional color palette
        Color background = new Color(0xF0F0F0);          // Slightly darker grey for better contrast
        Color cardBackground = new Color(0xFFFFFF);      // Pure white cards
        Color buttonGray = new Color(0xE1E1E1);          // Light grey for buttons
        Color buttonHoverGray = new Color(0xD0D0D0);     // Slightly darker grey for hover
        Color accent = new Color(0x5B7FD7);              // Softer, less vibrant blue
        Color accentHover = new Color(0x4A6BB8);         // Slightly darker for hover
        Color text = new Color(0x323130);                // Warm dark gray for text
        Color textSecondary = new Color(0x605E5C);       // Medium gray for secondary text
        Color border = new Color(0xEDEBE9);              // Very subtle warm border
        Color darkerBorder = new Color(0xA19F9D);        // Darker border for inputs
        Color scrollbarGray = new Color(0xC8C6C4);       // Neutral gray for scrollbars
        Color success = new Color(0x107C10);             // Classic green
        Color warning = new Color(0xD83B01);             // Classic orange
        
        // Background colors
        UIManager.put("Panel.background", background);
        UIManager.put("OptionPane.background", background);
        UIManager.put("ScrollPane.background", background);
        
        // Button styling - light grey background with dark text
        UIManager.put("Button.background", buttonGray);
        UIManager.put("Button.foreground", text);
        UIManager.put("Button.hoverBackground", buttonHoverGray);
        UIManager.put("Button.pressedBackground", buttonHoverGray);
        UIManager.put("Button.arc", 4);  // Slightly rounded corners
        
        // Scrollbar styling - flat, square, gray
        UIManager.put("ScrollBar.track", background);
        UIManager.put("ScrollBar.thumb", scrollbarGray);
        UIManager.put("ScrollBar.hoverTrackColor", background);
        UIManager.put("ScrollBar.thumbArc", 0);  // Square corners - no rounding!
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(0, 0, 0, 0));
        UIManager.put("ScrollBar.width", 14);
        UIManager.put("ScrollBar.showButtons", false);  // Hide arrow buttons
        
        // TextField and TextArea - darker borders
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(darkerBorder, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.border", BorderFactory.createLineBorder(darkerBorder, 1));
        
        // ComboBox - darker borders
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.borderColor", darkerBorder);
        
        // List - darker borders
        UIManager.put("List.background", Color.WHITE);
        UIManager.put("List.selectionBackground", accent);
        UIManager.put("List.selectionForeground", Color.WHITE);
        
        // Modern fonts
        Font baseFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font headingFont = new Font("Segoe UI", Font.BOLD, 16);
        
        UIManager.getDefaults().keys().asIterator().forEachRemaining(key -> {
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, baseFont);
            }
        });

        UIManager.put("Button.font", baseFont.deriveFont(Font.BOLD));
        UIManager.put("Label.font", baseFont);
        UIManager.put("TextArea.font", baseFont);
        UIManager.put("TextField.font", baseFont);
        UIManager.put("List.font", baseFont);
        UIManager.put("TitledBorder.font", headingFont);
    }
    
    /**
     * Returns the professional accent color for use in custom components
     */
    public static Color getAccentColor() {
        return new Color(0x5B7FD7);
    }
    
    /**
     * Returns the background color for use in custom components
     */
    public static Color getBackgroundColor() {
        return new Color(0xF0F0F0);
    }
    
    /**
     * Returns the border color for use in custom components
     */
    public static Color getBorderColor() {
        return new Color(0xEDEBE9);
    }
    
    /**
     * Returns the text color for use in custom components
     */
    public static Color getTextColor() {
        return new Color(0x323130);
    }
    
    /**
     * Returns the secondary text color for use in custom components
     */
    public static Color getSecondaryTextColor() {
        return new Color(0x605E5C);
    }
}
