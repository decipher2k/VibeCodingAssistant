/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.nio.file.Path;
import java.util.EnumSet;

/**
 * Represents a project template that can be saved and loaded.
 * Contains all the configuration and task data for a project.
 */
public final class ProjectTemplate {
    private final ProgrammingLanguage programmingLanguage;
    private final ProjectStyle projectStyle;
    private final EnumSet<TargetOs> targetOperatingSystems;
    private final TaskType taskType;
    private final MainTaskData taskData;
    private final Path projectDirectory;
    private final ProjectSettings projectSettings;

    public ProjectTemplate(ProgrammingLanguage programmingLanguage,
                          ProjectStyle projectStyle,
                          EnumSet<TargetOs> targetOperatingSystems,
                          TaskType taskType,
                          MainTaskData taskData,
                          Path projectDirectory) {
        this(programmingLanguage, projectStyle, targetOperatingSystems, taskType, taskData, projectDirectory, null);
    }

    public ProjectTemplate(ProgrammingLanguage programmingLanguage,
                          ProjectStyle projectStyle,
                          EnumSet<TargetOs> targetOperatingSystems,
                          TaskType taskType,
                          MainTaskData taskData,
                          Path projectDirectory,
                          ProjectSettings projectSettings) {
        this.programmingLanguage = programmingLanguage;
        this.projectStyle = projectStyle;
        this.targetOperatingSystems = targetOperatingSystems != null ? EnumSet.copyOf(targetOperatingSystems) : EnumSet.noneOf(TargetOs.class);
        this.taskType = taskType;
        this.taskData = taskData;
        this.projectDirectory = projectDirectory;
        this.projectSettings = projectSettings != null ? projectSettings : new ProjectSettings();
    }

    public ProgrammingLanguage getProgrammingLanguage() {
        return programmingLanguage;
    }

    public ProjectStyle getProjectStyle() {
        return projectStyle;
    }

    public EnumSet<TargetOs> getTargetOperatingSystems() {
        return targetOperatingSystems != null ? EnumSet.copyOf(targetOperatingSystems) : EnumSet.noneOf(TargetOs.class);
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public MainTaskData getTaskData() {
        return taskData;
    }

    public Path getProjectDirectory() {
        return projectDirectory;
    }
    
    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    /**
     * Serializes this template to JSON format.
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        // Basic config
        json.append("  \"programmingLanguage\": ").append(jsonString(programmingLanguage != null ? programmingLanguage.name() : null)).append(",\n");
        json.append("  \"projectStyle\": ").append(jsonString(projectStyle != null ? projectStyle.name() : null)).append(",\n");
        json.append("  \"taskType\": ").append(jsonString(taskType != null ? taskType.name() : null)).append(",\n");
        json.append("  \"projectDirectory\": ").append(jsonString(projectDirectory != null ? projectDirectory.toString() : null)).append(",\n");
        
        // Target OS list
        json.append("  \"targetOperatingSystems\": [");
        if (targetOperatingSystems != null && !targetOperatingSystems.isEmpty()) {
            boolean first = true;
            for (TargetOs os : targetOperatingSystems) {
                if (!first) json.append(", ");
                json.append(jsonString(os.name()));
                first = false;
            }
        }
        json.append("],\n");
        
        // Task data
        json.append("  \"taskData\": {\n");
        if (taskData != null) {
            json.append("    \"projectOverview\": ").append(jsonString(taskData.getProjectOverview())).append(",\n");
            json.append("    \"themeDescription\": ").append(jsonString(taskData.getThemeDescription())).append(",\n");
            json.append("    \"expectedBehavior\": ").append(jsonString(taskData.getExpectedBehavior())).append(",\n");
            json.append("    \"actualBehavior\": ").append(jsonString(taskData.getActualBehavior())).append(",\n");
            json.append("    \"errorDetails\": ").append(jsonString(taskData.getErrorDetails())).append(",\n");
            json.append("    \"algorithmDescription\": ").append(jsonString(taskData.getAlgorithmDescription())).append(",\n");
            json.append("    \"changeDescription\": ").append(jsonString(taskData.getChangeDescription())).append(",\n");
            json.append("    \"involvedFiles\": ").append(jsonString(taskData.getInvolvedFiles())).append(",\n");
            
            // Dialogs
            json.append("    \"dialogs\": [");
            if (taskData.getDialogs() != null && !taskData.getDialogs().isEmpty()) {
                boolean first = true;
                for (DialogDefinition dialog : taskData.getDialogs()) {
                    if (!first) json.append(",");
                    json.append("\n      {\n");
                    json.append("        \"name\": ").append(jsonString(dialog.getName())).append(",\n");
                    json.append("        \"windowTitle\": ").append(jsonString(dialog.getWindowTitle())).append(",\n");
                    json.append("        \"description\": ").append(jsonString(dialog.getDescription())).append(",\n");
                    json.append("        \"modal\": ").append(dialog.isModal()).append(",\n");
                    json.append("        \"formLayoutJson\": ").append(jsonString(dialog.getFormLayoutJson())).append(",\n");
                    json.append("        \"showInMdiMenu\": ").append(dialog.isShowInMdiMenu()).append("\n");
                    json.append("      }");
                    first = false;
                }
                json.append("\n    ");
            }
            json.append("],\n");
            
            // Workflows
            json.append("    \"workflowItems\": [");
            if (taskData.getWorkflowItems() != null && !taskData.getWorkflowItems().isEmpty()) {
                boolean first = true;
                for (WorkflowItem item : taskData.getWorkflowItems()) {
                    if (!first) json.append(",");
                    json.append("\n      {\n");
                    json.append("        \"name\": ").append(jsonString(item.getName())).append(",\n");
                    json.append("        \"windowAffected\": ").append(jsonString(item.getWindowAffected())).append(",\n");
                    json.append("        \"trigger\": ").append(jsonString(item.getTrigger())).append(",\n");
                    json.append("        \"steps\": [");
                    if (item.getSteps() != null && !item.getSteps().isEmpty()) {
                        boolean firstStep = true;
                        for (WorkflowStep step : item.getSteps()) {
                            if (!firstStep) json.append(",");
                            json.append("\n          {\n");
                            json.append("            \"description\": ").append(jsonString(step.getDescription())).append(",\n");
                            json.append("            \"requirements\": ").append(jsonString(step.getRequirements())).append(",\n");
                            json.append("            \"stopIfRequirementNotMet\": ").append(step.isStopIfRequirementNotMet()).append(",\n");
                            json.append("            \"waitForRequirement\": ").append(step.isWaitForRequirement()).append("\n");
                            json.append("          }");
                            firstStep = false;
                        }
                        json.append("\n        ");
                    }
                    json.append("]\n");
                    json.append("      }");
                    first = false;
                }
                json.append("\n    ");
            }
            json.append("]\n");
        }
        json.append("  },\n");
        
        // Project settings
        json.append("  \"projectSettings\": {\n");
        if (projectSettings != null) {
            // Global variables
            json.append("    \"globalVariables\": [");
            if (projectSettings.getGlobalVariables() != null && !projectSettings.getGlobalVariables().isEmpty()) {
                boolean first = true;
                for (GlobalVariable var : projectSettings.getGlobalVariables()) {
                    if (!first) json.append(",");
                    json.append("\n      {\n");
                    json.append("        \"name\": ").append(jsonString(var.getName())).append(",\n");
                    json.append("        \"value\": ").append(jsonString(var.getValue())).append("\n");
                    json.append("      }");
                    first = false;
                }
                json.append("\n    ");
            }
            json.append("],\n");
            
            // Project workflows
            json.append("    \"projectWorkflows\": [");
            if (projectSettings.getProjectWorkflows() != null && !projectSettings.getProjectWorkflows().isEmpty()) {
                boolean first = true;
                for (WorkflowItem item : projectSettings.getProjectWorkflows()) {
                    if (!first) json.append(",");
                    json.append("\n      {\n");
                    json.append("        \"name\": ").append(jsonString(item.getName())).append(",\n");
                    json.append("        \"windowAffected\": ").append(jsonString(item.getWindowAffected())).append(",\n");
                    json.append("        \"trigger\": ").append(jsonString(item.getTrigger())).append(",\n");
                    json.append("        \"steps\": [");
                    if (item.getSteps() != null && !item.getSteps().isEmpty()) {
                        boolean firstStep = true;
                        for (WorkflowStep step : item.getSteps()) {
                            if (!firstStep) json.append(",");
                            json.append("\n          {\n");
                            json.append("            \"description\": ").append(jsonString(step.getDescription())).append(",\n");
                            json.append("            \"requirements\": ").append(jsonString(step.getRequirements())).append(",\n");
                            json.append("            \"stopIfRequirementNotMet\": ").append(step.isStopIfRequirementNotMet()).append(",\n");
                            json.append("            \"waitForRequirement\": ").append(step.isWaitForRequirement()).append("\n");
                            json.append("          }");
                            firstStep = false;
                        }
                        json.append("\n        ");
                    }
                    json.append("]\n");
                    json.append("      }");
                    first = false;
                }
                json.append("\n    ");
            }
            json.append("],\n");
            
            // Database description
            json.append("    \"databaseDescription\": ").append(jsonString(projectSettings.getDatabaseDescription())).append("\n");
        }
        json.append("  }\n");
        json.append("}");
        
        return json.toString();
    }

    /**
     * Deserializes a ProjectTemplate from JSON format.
     */
    public static ProjectTemplate fromJson(String json) throws Exception {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be empty");
        }

        // Simple JSON parser for our specific format
        JsonParser parser = new JsonParser(json);
        
        ProgrammingLanguage lang = null;
        String langStr = parser.getString("programmingLanguage");
        if (langStr != null && !langStr.isEmpty()) {
            try {
                lang = ProgrammingLanguage.valueOf(langStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid programming language: " + langStr);
            }
        }
        
        ProjectStyle style = null;
        String styleStr = parser.getString("projectStyle");
        if (styleStr != null && !styleStr.isEmpty()) {
            try {
                style = ProjectStyle.valueOf(styleStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid project style: " + styleStr);
            }
        }
        
        TaskType taskType = null;
        String taskTypeStr = parser.getString("taskType");
        if (taskTypeStr != null && !taskTypeStr.isEmpty()) {
            try {
                taskType = TaskType.valueOf(taskTypeStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid task type: " + taskTypeStr);
            }
        }
        
        Path projectDirectory = null;
        String projectDirStr = parser.getString("projectDirectory");
        if (projectDirStr != null && !projectDirStr.isEmpty()) {
            try {
                projectDirectory = java.nio.file.Paths.get(projectDirStr);
            } catch (Exception e) {
                System.err.println("Warning: Invalid project directory: " + projectDirStr);
            }
        }
        
        EnumSet<TargetOs> targetOs = EnumSet.noneOf(TargetOs.class);
        String[] osArray = parser.getStringArray("targetOperatingSystems");
        if (osArray != null) {
            for (String os : osArray) {
                if (os != null && !os.trim().isEmpty()) {
                    try {
                        targetOs.add(TargetOs.valueOf(os));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Warning: Invalid target OS: " + os);
                    }
                }
            }
        }
        
        MainTaskData taskData = new MainTaskData();
        if (parser.hasKey("taskData")) {
            JsonParser taskDataParser = parser.getObject("taskData");
            if (taskDataParser != null) {
                taskData.setProjectOverview(taskDataParser.getString("projectOverview"));
                taskData.setThemeDescription(taskDataParser.getString("themeDescription"));
                taskData.setExpectedBehavior(taskDataParser.getString("expectedBehavior"));
                taskData.setActualBehavior(taskDataParser.getString("actualBehavior"));
                taskData.setErrorDetails(taskDataParser.getString("errorDetails"));
                taskData.setAlgorithmDescription(taskDataParser.getString("algorithmDescription"));
                taskData.setChangeDescription(taskDataParser.getString("changeDescription"));
                taskData.setInvolvedFiles(taskDataParser.getString("involvedFiles"));
            }
            
            // Parse dialogs
            java.util.List<DialogDefinition> dialogs = new java.util.ArrayList<>();
            JsonParser[] dialogParsers = taskDataParser.getObjectArray("dialogs");
            if (dialogParsers != null) {
                for (JsonParser dialogParser : dialogParsers) {
                    String name = dialogParser.getString("name");
                    String windowTitle = dialogParser.getString("windowTitle");
                    String description = dialogParser.getString("description");
                    boolean modal = dialogParser.getBoolean("modal");
                    String formLayoutJson = dialogParser.getString("formLayoutJson");
                    boolean showInMdiMenu = dialogParser.hasKey("showInMdiMenu") ? dialogParser.getBoolean("showInMdiMenu") : true;
                    dialogs.add(new DialogDefinition(name, windowTitle, description, modal, formLayoutJson, showInMdiMenu));
                }
            }
            taskData.setDialogs(dialogs);
            
            // Parse workflow items
            java.util.List<WorkflowItem> workflowItems = new java.util.ArrayList<>();
            JsonParser[] workflowParsers = taskDataParser.getObjectArray("workflowItems");
            if (workflowParsers != null) {
                for (JsonParser workflowParser : workflowParsers) {
                    WorkflowItem item = new WorkflowItem();
                    item.setName(workflowParser.getString("name"));
                    item.setWindowAffected(workflowParser.getString("windowAffected"));
                    item.setTrigger(workflowParser.getString("trigger"));
                    
                    java.util.List<WorkflowStep> steps = new java.util.ArrayList<>();
                    JsonParser[] stepParsers = workflowParser.getObjectArray("steps");
                    if (stepParsers != null) {
                        for (JsonParser stepParser : stepParsers) {
                            String desc = stepParser.getString("description");
                            String req = stepParser.getString("requirements");
                            boolean stop = stepParser.getBoolean("stopIfRequirementNotMet");
                            boolean wait = stepParser.getBoolean("waitForRequirement");
                            steps.add(new WorkflowStep(desc, req, stop, wait));
                        }
                    }
                    item.setSteps(steps);
                    workflowItems.add(item);
                }
            }
            taskData.setWorkflowItems(workflowItems);
        }
        
        // Parse project settings
        ProjectSettings projectSettings = new ProjectSettings();
        JsonParser settingsParser = parser.getObject("projectSettings");
        if (settingsParser != null && settingsParser.hasKey("globalVariables")) {
            
            // Parse global variables
            JsonParser[] globalVarParsers = settingsParser.getObjectArray("globalVariables");
            if (globalVarParsers != null) {
                java.util.List<GlobalVariable> globalVariables = new java.util.ArrayList<>();
                for (JsonParser varParser : globalVarParsers) {
                    String name = varParser.getString("name");
                    String value = varParser.getString("value");
                    globalVariables.add(new GlobalVariable(name, value));
                }
                projectSettings.setGlobalVariables(globalVariables);
            }
            
            // Parse project workflows
            JsonParser[] projectWorkflowParsers = settingsParser.getObjectArray("projectWorkflows");
            if (projectWorkflowParsers != null) {
                java.util.List<WorkflowItem> projectWorkflows = new java.util.ArrayList<>();
                for (JsonParser workflowParser : projectWorkflowParsers) {
                    WorkflowItem item = new WorkflowItem();
                    item.setName(workflowParser.getString("name"));
                    item.setWindowAffected(workflowParser.getString("windowAffected"));
                    item.setTrigger(workflowParser.getString("trigger"));
                    
                    java.util.List<WorkflowStep> steps = new java.util.ArrayList<>();
                    JsonParser[] stepParsers = workflowParser.getObjectArray("steps");
                    if (stepParsers != null) {
                        for (JsonParser stepParser : stepParsers) {
                            String desc = stepParser.getString("description");
                            String req = stepParser.getString("requirements");
                            boolean stop = stepParser.getBoolean("stopIfRequirementNotMet");
                            boolean wait = stepParser.getBoolean("waitForRequirement");
                            steps.add(new WorkflowStep(desc, req, stop, wait));
                        }
                    }
                    item.setSteps(steps);
                    projectWorkflows.add(item);
                }
                projectSettings.setProjectWorkflows(projectWorkflows);
            }
            
            // Parse database description
            String dbDesc = settingsParser.getString("databaseDescription");
            if (dbDesc != null) {
                projectSettings.setDatabaseDescription(dbDesc);
            }
        }
        
        return new ProjectTemplate(lang, style, targetOs, taskType, taskData, projectDirectory, projectSettings);
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder escaped = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default: escaped.append(c);
            }
        }
        escaped.append("\"");
        return escaped.toString();
    }

    /**
     * Simple JSON parser for reading template files.
     */
    private static final class JsonParser {
        private final String json;

        JsonParser(String json) {
            this.json = json != null ? json.trim() : "";
        }

        String getString(String key) {
            // Find the key in the JSON (without regex, just plain string search)
            String keyPattern = "\"" + key + "\"";
            int keyPos = json.indexOf(keyPattern);
            if (keyPos < 0) {
                return null;
            }
            
            // Find the colon after the key
            int colonPos = json.indexOf(":", keyPos);
            if (colonPos < 0) {
                return null;
            }
            
            // Skip whitespace after colon
            int start = colonPos + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            
            if (start >= json.length()) {
                return null;
            }
            
            
            if (json.charAt(start) == 'n' && json.substring(start).startsWith("null")) {
                return null;
            }
            if (json.charAt(start) != '"') {
                return null;
            }
            
            start++;
            StringBuilder value = new StringBuilder();
            boolean escaped = false;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escaped) {
                    switch (c) {
                        case 'n': value.append('\n'); break;
                        case 'r': value.append('\r'); break;
                        case 't': value.append('\t'); break;
                        case '\\': value.append('\\'); break;
                        case '"': value.append('"'); break;
                        default: value.append(c);
                    }
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    return value.toString();
                } else {
                    value.append(c);
                }
            }
            return null;
        }

        boolean getBoolean(String key) {
            // Find the key in the JSON
            String keyPattern = "\"" + key + "\"";
            int keyPos = json.indexOf(keyPattern);
            if (keyPos < 0) return false;
            
            // Find the colon after the key
            int colonPos = json.indexOf(":", keyPos);
            if (colonPos < 0) return false;
            
            // Skip whitespace after colon
            int start = colonPos + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            
            return start < json.length() && json.substring(start).startsWith("true");
        }

        String[] getStringArray(String key) {
            // Find the key in the JSON
            String keyPattern = "\"" + key + "\"";
            int keyPos = json.indexOf(keyPattern);
            if (keyPos < 0) return null;
            
            // Find the opening bracket
            int start = json.indexOf("[", keyPos);
            if (start < 0) return null;
            
            start++; // Move past the '['
            int end = json.indexOf("]", start);
            if (end < 0) return null;
            
            String arrayContent = json.substring(start, end).trim();
            if (arrayContent.isEmpty()) return new String[0];
            
            java.util.List<String> values = new java.util.ArrayList<>();
            int pos = 0;
            while (pos < arrayContent.length()) {
                while (pos < arrayContent.length() && Character.isWhitespace(arrayContent.charAt(pos))) {
                    pos++;
                }
                if (pos >= arrayContent.length()) break;
                
                if (arrayContent.charAt(pos) == '"') {
                    pos++;
                    StringBuilder value = new StringBuilder();
                    boolean escaped = false;
                    while (pos < arrayContent.length()) {
                        char c = arrayContent.charAt(pos++);
                        if (escaped) {
                            value.append(c);
                            escaped = false;
                        } else if (c == '\\') {
                            escaped = true;
                        } else if (c == '"') {
                            values.add(value.toString());
                            break;
                        } else {
                            value.append(c);
                        }
                    }
                }
                
                while (pos < arrayContent.length() && arrayContent.charAt(pos) != ',') {
                    pos++;
                }
                pos++; // skip comma
            }
            
            return values.toArray(new String[0]);
        }

        boolean hasKey(String key) {
            return json.contains("\"" + key + "\"");
        }

        JsonParser getObject(String key) {
            // Find the key in the JSON
            String keyPattern = "\"" + key + "\"";
            int keyPos = json.indexOf(keyPattern);
            if (keyPos < 0) return new JsonParser("{}");
            
            // Find the opening brace after the key
            int start = json.indexOf("{", keyPos);
            if (start < 0) return new JsonParser("{}");
            
            int braceCount = 1;
            int end = start + 1;
            
            while (end < json.length() && braceCount > 0) {
                char c = json.charAt(end);
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                end++;
            }
            
            if (braceCount != 0) return new JsonParser("{}");
            return new JsonParser(json.substring(start, end));
        }

        JsonParser[] getObjectArray(String key) {
            // Find the key in the JSON
            String keyPattern = "\"" + key + "\"";
            int keyPos = json.indexOf(keyPattern);
            if (keyPos < 0) return null;
            
            // Find the opening bracket after the key
            int start = json.indexOf("[", keyPos);
            if (start < 0) return null;
            
            start++; // Move past the '['
            
            java.util.List<JsonParser> objects = new java.util.ArrayList<>();
            int pos = start;
            while (pos < json.length()) {
                while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                    pos++;
                }
                if (pos >= json.length() || json.charAt(pos) == ']') break;
                
                if (json.charAt(pos) == '{') {
                    int objStart = pos;
                    int braceCount = 1;
                    pos++;
                    while (pos < json.length() && braceCount > 0) {
                        char c = json.charAt(pos);
                        if (c == '{') braceCount++;
                        else if (c == '}') braceCount--;
                        pos++;
                    }
                    objects.add(new JsonParser(json.substring(objStart, pos)));
                }
                
                while (pos < json.length() && json.charAt(pos) != ',' && json.charAt(pos) != ']') {
                    pos++;
                }
                if (pos < json.length() && json.charAt(pos) == ',') pos++;
            }
            
            return objects.toArray(new JsonParser[0]);
        }
    }
}
