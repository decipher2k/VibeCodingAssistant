/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.Serializable;

public final class WorkflowStep implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String description;
    private String requirements;
    private boolean stopIfRequirementNotMet;
    private boolean waitForRequirement;

    public WorkflowStep() {
        this.description = "";
        this.requirements = "";
        this.stopIfRequirementNotMet = false;
        this.waitForRequirement = false;
    }

    public WorkflowStep(String description) {
        this.description = description;
        this.requirements = "";
        this.stopIfRequirementNotMet = false;
        this.waitForRequirement = false;
    }

    public WorkflowStep(String description, String requirements, boolean stopIfRequirementNotMet) {
        this.description = description;
        this.requirements = requirements;
        this.stopIfRequirementNotMet = stopIfRequirementNotMet;
        this.waitForRequirement = false;
    }

    public WorkflowStep(String description, String requirements, boolean stopIfRequirementNotMet, boolean waitForRequirement) {
        this.description = description;
        this.requirements = requirements;
        this.stopIfRequirementNotMet = stopIfRequirementNotMet;
        this.waitForRequirement = waitForRequirement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public boolean isStopIfRequirementNotMet() {
        return stopIfRequirementNotMet;
    }

    public void setStopIfRequirementNotMet(boolean stopIfRequirementNotMet) {
        this.stopIfRequirementNotMet = stopIfRequirementNotMet;
    }

    public boolean isWaitForRequirement() {
        return waitForRequirement;
    }

    public void setWaitForRequirement(boolean waitForRequirement) {
        this.waitForRequirement = waitForRequirement;
    }

    @Override
    public String toString() {
        return description == null || description.isBlank() ? "(empty step)" : description;
    }
}
