/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

public final class InitialConfig implements Serializable {
    private static final long serialVersionUID = 1L; // Keep same for backward compatibility
    
    private final ProgrammingLanguage programmingLanguage;
    private final ProjectStyle projectStyle;
    private final EnumSet<TargetOs> targetOperatingSystems;
    private transient Path projectDirectory; // Not final because it needs to be set during deserialization, transient to handle custom serialization
    private transient ProgramMode programMode; // For GUI apps: MAIN_WINDOW or MDI - transient for custom serialization
    private transient String projectName; // Name of the project - transient for custom serialization
    private transient ProjectMode ideOrWizardMode; // IDE mode or Wizard mode - transient for custom serialization

    public InitialConfig(ProgrammingLanguage programmingLanguage, ProjectStyle projectStyle,
                         EnumSet<TargetOs> targetOperatingSystems, Path projectDirectory) {
        this(programmingLanguage, projectStyle, targetOperatingSystems, projectDirectory, ProgramMode.MAIN_WINDOW, null, ProjectMode.WIZARD);
    }

    public InitialConfig(ProgrammingLanguage programmingLanguage, ProjectStyle projectStyle,
                         EnumSet<TargetOs> targetOperatingSystems, Path projectDirectory, ProgramMode programMode) {
        this(programmingLanguage, projectStyle, targetOperatingSystems, projectDirectory, programMode, null, ProjectMode.WIZARD);
    }

    public InitialConfig(ProgrammingLanguage programmingLanguage, ProjectStyle projectStyle,
                         EnumSet<TargetOs> targetOperatingSystems, Path projectDirectory, ProgramMode programMode, String projectName) {
        this(programmingLanguage, projectStyle, targetOperatingSystems, projectDirectory, programMode, projectName, ProjectMode.WIZARD);
    }

    public InitialConfig(ProgrammingLanguage programmingLanguage, ProjectStyle projectStyle,
                         EnumSet<TargetOs> targetOperatingSystems, Path projectDirectory, ProgramMode programMode, 
                         String projectName, ProjectMode ideOrWizardMode) {
        this.programmingLanguage = programmingLanguage;
        this.projectStyle = projectStyle;
        this.targetOperatingSystems = EnumSet.copyOf(targetOperatingSystems);
        this.projectDirectory = projectDirectory;
        this.programMode = programMode != null ? programMode : ProgramMode.MAIN_WINDOW;
        this.projectName = projectName;
        this.ideOrWizardMode = ideOrWizardMode != null ? ideOrWizardMode : ProjectMode.WIZARD;
    }

    public ProgrammingLanguage getProgrammingLanguage() {
        return programmingLanguage;
    }

    public ProjectStyle getProjectStyle() {
        return projectStyle;
    }

    public EnumSet<TargetOs> getTargetOperatingSystems() {
        return EnumSet.copyOf(targetOperatingSystems);
    }

    public Path getProjectDirectory() {
        return projectDirectory;
    }

    public ProgramMode getProgramMode() {
        return programMode;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public ProjectMode getIdeOrWizardMode() {
        return ideOrWizardMode;
    }
    
    /**
     * Custom serialization to handle the non-serializable Path field and ProgramMode.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        // Serialize the path as a string
        out.writeObject(projectDirectory != null ? projectDirectory.toString() : null);
        // Serialize the program mode as a string (for backward compatibility)
        out.writeObject(programMode != null ? programMode.name() : null);
        // Serialize the project name
        out.writeObject(projectName);
        // Serialize the IDE/Wizard mode
        out.writeObject(ideOrWizardMode != null ? ideOrWizardMode.name() : null);
    }
    
    /**
     * Custom deserialization to restore the Path field and ProgramMode.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Deserialize the path from a string
        String pathString = (String) in.readObject();
        projectDirectory = pathString != null ? Paths.get(pathString) : null;
        
        // Try to deserialize program mode (may not exist in old files)
        try {
            String programModeString = (String) in.readObject();
            if (programModeString != null) {
                programMode = ProgramMode.valueOf(programModeString);
            } else {
                programMode = ProgramMode.MAIN_WINDOW; // Default for old files
            }
        } catch (Exception e) {
            // If reading fails (old file format), use default
            programMode = ProgramMode.MAIN_WINDOW;
        }
        
        // Try to deserialize project name (may not exist in old files)
        try {
            projectName = (String) in.readObject();
        } catch (Exception e) {
            // If reading fails (old file format), use null
            projectName = null;
        }
        
        // Try to deserialize IDE/Wizard mode (may not exist in old files)
        try {
            String ideOrWizardModeString = (String) in.readObject();
            if (ideOrWizardModeString != null) {
                ideOrWizardMode = ProjectMode.valueOf(ideOrWizardModeString);
            } else {
                ideOrWizardMode = ProjectMode.WIZARD; // Default for old files
            }
        } catch (Exception e) {
            // If reading fails (old file format), use default
            ideOrWizardMode = ProjectMode.WIZARD;
        }
    }
}
