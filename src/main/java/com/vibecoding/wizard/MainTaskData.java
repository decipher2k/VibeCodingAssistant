/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MainTaskData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String projectOverview;
    private String expectedBehavior;
    private String actualBehavior;
    private String errorDetails;
    private List<WorkflowItem> workflowItems = new ArrayList<>();
    private List<DialogDefinition> dialogs = new ArrayList<>();
    private String algorithmDescription;
    private String changeDescription;
    private String involvedFiles;
    private String themeDescription;
    private String mainWindowName; // The name of the main window to show on module load

    public String getProjectOverview() {
        return projectOverview;
    }

    public void setProjectOverview(String projectOverview) {
        this.projectOverview = projectOverview;
    }

    public String getExpectedBehavior() {
        return expectedBehavior;
    }

    public void setExpectedBehavior(String expectedBehavior) {
        this.expectedBehavior = expectedBehavior;
    }

    public String getActualBehavior() {
        return actualBehavior;
    }

    public void setActualBehavior(String actualBehavior) {
        this.actualBehavior = actualBehavior;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public List<WorkflowItem> getWorkflowItems() {
        return Collections.unmodifiableList(workflowItems);
    }

    public void setWorkflowItems(List<WorkflowItem> workflowItems) {
        this.workflowItems = new ArrayList<>(workflowItems);
    }

    public List<DialogDefinition> getDialogs() {
        return Collections.unmodifiableList(dialogs);
    }

    public void setDialogs(List<DialogDefinition> dialogs) {
        this.dialogs = new ArrayList<>(dialogs);
    }

    public String getAlgorithmDescription() {
        return algorithmDescription;
    }

    public void setAlgorithmDescription(String algorithmDescription) {
        this.algorithmDescription = algorithmDescription;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    public String getInvolvedFiles() {
        return involvedFiles;
    }

    public void setInvolvedFiles(String involvedFiles) {
        this.involvedFiles = involvedFiles;
    }

    public String getThemeDescription() {
        return themeDescription;
    }

    public void setThemeDescription(String themeDescription) {
        this.themeDescription = themeDescription;
    }

    public String getMainWindowName() {
        return mainWindowName;
    }

    public void setMainWindowName(String mainWindowName) {
        this.mainWindowName = mainWindowName;
    }

    public void copyFrom(MainTaskData other) {
        if (other == null) {
            return;
        }
        setProjectOverview(other.getProjectOverview());
        setExpectedBehavior(other.getExpectedBehavior());
        setActualBehavior(other.getActualBehavior());
        setErrorDetails(other.getErrorDetails());
        setWorkflowItems(other.getWorkflowItems());
        setDialogs(other.getDialogs());
        setAlgorithmDescription(other.getAlgorithmDescription());
        setChangeDescription(other.getChangeDescription());
        setInvolvedFiles(other.getInvolvedFiles());
        setThemeDescription(other.getThemeDescription());
        setMainWindowName(other.getMainWindowName());
    }
    
    /**
     * Clears all data fields.
     */
    public void clear() {
        projectOverview = null;
        expectedBehavior = null;
        actualBehavior = null;
        errorDetails = null;
        workflowItems = new ArrayList<>();
        dialogs = new ArrayList<>();
        algorithmDescription = null;
        changeDescription = null;
        involvedFiles = null;
        themeDescription = null;
        mainWindowName = null;
    }
    
    /**
     * Custom deserialization to ensure backward compatibility.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Ensure lists are initialized if they were null
        if (workflowItems == null) {
            workflowItems = new ArrayList<>();
        }
        if (dialogs == null) {
            dialogs = new ArrayList<>();
        }
    }
}
