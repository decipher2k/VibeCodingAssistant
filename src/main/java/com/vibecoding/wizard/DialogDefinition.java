/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.Serializable;
import java.util.Objects;

public final class DialogDefinition implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private final String name;
    private final String windowTitle;
    private final String description;
    private final boolean modal;
    private final String formLayoutJson;
    private final boolean showInMdiMenu;

    public DialogDefinition(String name, String windowTitle, String description) {
        this(name, windowTitle, description, false);
    }

    public DialogDefinition(String name, String windowTitle, String description, boolean modal) {
        this(name, windowTitle, description, modal, null);
    }

    public DialogDefinition(String name, String windowTitle, String description, boolean modal, String formLayoutJson) {
        this(name, windowTitle, description, modal, formLayoutJson, true);
    }

    public DialogDefinition(String name, String windowTitle, String description, boolean modal, String formLayoutJson, boolean showInMdiMenu) {
        this.name = name;
        this.windowTitle = windowTitle;
        this.description = description;
        this.modal = modal;
        this.formLayoutJson = formLayoutJson;
        this.showInMdiMenu = showInMdiMenu;
    }

    public String getName() {
        return name;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public String getDescription() {
        return description;
    }

    public boolean isModal() {
        return modal;
    }

    public String getFormLayoutJson() {
        return formLayoutJson;
    }

    public boolean isShowInMdiMenu() {
        return showInMdiMenu;
    }

    @Override
    public String toString() {
        return name;
    }

    public DialogDefinition withName(String newName) {
        return new DialogDefinition(newName, windowTitle, description, modal, formLayoutJson, showInMdiMenu);
    }

    public DialogDefinition withWindowTitle(String newWindowTitle) {
        return new DialogDefinition(name, newWindowTitle, description, modal, formLayoutJson, showInMdiMenu);
    }

    public DialogDefinition withDescription(String newDescription) {
        return new DialogDefinition(name, windowTitle, newDescription, modal, formLayoutJson, showInMdiMenu);
    }

    public DialogDefinition withModal(boolean newModal) {
        return new DialogDefinition(name, windowTitle, description, newModal, formLayoutJson, showInMdiMenu);
    }

    public DialogDefinition withFormLayoutJson(String newFormLayoutJson) {
        return new DialogDefinition(name, windowTitle, description, modal, newFormLayoutJson, showInMdiMenu);
    }

    public DialogDefinition withShowInMdiMenu(boolean newShowInMdiMenu) {
        return new DialogDefinition(name, windowTitle, description, modal, formLayoutJson, newShowInMdiMenu);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DialogDefinition)) {
            return false;
        }
        DialogDefinition that = (DialogDefinition) o;
        return Objects.equals(name, that.name)
            && Objects.equals(windowTitle, that.windowTitle)
            && Objects.equals(description, that.description)
            && modal == that.modal
            && Objects.equals(formLayoutJson, that.formLayoutJson)
            && showInMdiMenu == that.showInMdiMenu;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, windowTitle, description, modal, formLayoutJson, showInMdiMenu);
    }
}
