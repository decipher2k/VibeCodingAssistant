/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WorkflowItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String windowAffected;
    private String trigger;
    private List<WorkflowStep> steps;

    public WorkflowItem() {
        this.name = "";
        this.windowAffected = "";
        this.trigger = "";
        this.steps = new ArrayList<>();
    }

    public WorkflowItem(String name, String windowAffected, String trigger, List<WorkflowStep> steps) {
        this.name = name;
        this.windowAffected = windowAffected;
        this.trigger = trigger;
        this.steps = new ArrayList<>(steps);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWindowAffected() {
        return windowAffected;
    }

    public void setWindowAffected(String windowAffected) {
        this.windowAffected = windowAffected;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public List<WorkflowStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public void setSteps(List<WorkflowStep> steps) {
        this.steps = new ArrayList<>(steps);
    }

    @Override
    public String toString() {
        return name == null || name.isBlank() ? "(unnamed workflow)" : name;
    }
}
