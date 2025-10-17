/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.model;

import java.util.Objects;

/**
 * Represents a user-defined event with a name, trigger description, and action description.
 * Events are not traditional GUI events, but rather logical events defined by the user.
 */
public class EventDefinition {
    private String name;
    private String triggerDescription;
    private String actionDescription;

    public EventDefinition() {
        this("", "", "");
    }

    public EventDefinition(String name, String triggerDescription, String actionDescription) {
        this.name = Objects.requireNonNull(name, "name");
        this.triggerDescription = Objects.requireNonNull(triggerDescription, "triggerDescription");
        this.actionDescription = Objects.requireNonNull(actionDescription, "actionDescription");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getTriggerDescription() {
        return triggerDescription;
    }

    public void setTriggerDescription(String triggerDescription) {
        this.triggerDescription = Objects.requireNonNull(triggerDescription, "triggerDescription");
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = Objects.requireNonNull(actionDescription, "actionDescription");
    }

    @Override
    public String toString() {
        return name.isEmpty() ? "(unnamed event)" : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventDefinition)) {
            return false;
        }
        EventDefinition that = (EventDefinition) o;
        return Objects.equals(name, that.name)
            && Objects.equals(triggerDescription, that.triggerDescription)
            && Objects.equals(actionDescription, that.actionDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, triggerDescription, actionDescription);
    }

    /**
     * Creates a deep copy of this event definition.
     */
    public EventDefinition copy() {
        return new EventDefinition(name, triggerDescription, actionDescription);
    }
}
