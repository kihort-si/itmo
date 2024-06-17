package ru.itmo.client.gui;

import javax.swing.*;

public class LanguageItem {
    private String text;
    private Icon icon;

    public LanguageItem(String text, Icon icon) {
        this.text = text;
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return text;
    }
}