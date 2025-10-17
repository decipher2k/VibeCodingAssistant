/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.util.ArrayList;
import java.util.List;

public final class BuildCommandPlanner {
    private BuildCommandPlanner() {
    }

    public static BuildPlan plan(ProgrammingLanguage language, ProjectStyle projectStyle, InitialConfig config) {
        List<List<String>> commands = new ArrayList<>();
        String description;
        
        // Detect if we need Wine for .NET projects
        boolean useWineForDotnet = false;
        if (language == ProgrammingLanguage.CSHARP && config != null) {
            String currentOs = System.getProperty("os.name").toLowerCase();
            boolean isWindows = currentOs.contains("win");
            boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
            useWineForDotnet = !isWindows && targetIncludesWindows;
        }

        switch (language) {
            case CSHARP:
                if (useWineForDotnet) {
                    commands.add(List.of("wine", "dotnet", "build"));
                    description = "Run wine dotnet build (cross-compiling for Windows)";
                } else {
                    commands.add(List.of("dotnet", "build"));
                    description = "Run dotnet build";
                }
                break;
            case CPP:
                commands.add(List.of("cmake", "--build", "build"));
                description = "Run cmake --build build";
                break;
            case JAVA:
                commands.add(List.of("mvn", "package"));
                description = "Run mvn package";
                break;
            case PYTHON:
                commands.add(List.of("python", "-m", "compileall", "."));
                description = "Run python -m compileall for syntax verification";
                break;
            case PHP:
                commands.add(List.of("php", "-l"));
                description = "Run PHP syntax check";
                break;
            case RUST:
                commands.add(List.of("cargo", "build"));
                description = "Run cargo build";
                break;
            case GO:
                commands.add(List.of("go", "build"));
                description = "Run go build";
                break;
            case JAVASCRIPT:
                commands.add(List.of("npm", "run", "build"));
                description = "Run npm run build";
                break;
            case RUBY:
                commands.add(List.of("bundle", "exec", "rake", "build"));
                description = "Run bundle exec rake build";
                break;
            default:
                commands.add(List.of("sh", "-c", "./build.sh"));
                description = "Run custom build script ./build.sh";
                break;
        }

        return new BuildPlan(commands, description);
    }

    public static List<String> getRunCommand(ProgrammingLanguage language, ProjectStyle projectStyle, InitialConfig config) {
        // Detect if we need Wine for .NET projects
        boolean useWineForDotnet = false;
        if (language == ProgrammingLanguage.CSHARP && config != null) {
            String currentOs = System.getProperty("os.name").toLowerCase();
            boolean isWindows = currentOs.contains("win");
            boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
            useWineForDotnet = !isWindows && targetIncludesWindows;
        }

        switch (language) {
            case CSHARP:
                if (useWineForDotnet) {
                    return List.of("wine", "dotnet", "run");
                } else {
                    return List.of("dotnet", "run");
                }
            case JAVA:
                // For Java, we'd need to know the main class, so return null
                return null;
            case PYTHON:
                // For Python, we'd need to know the main script, so return null
                return null;
            case RUST:
                return List.of("cargo", "run");
            case GO:
                return List.of("go", "run", ".");
            case JAVASCRIPT:
                return List.of("npm", "start");
            default:
                // No generic run command for other languages
                return null;
        }
    }

    public static final class BuildPlan {
        private final List<List<String>> commands;
        private final String description;

        private BuildPlan(List<List<String>> commands, String description) {
            this.commands = commands;
            this.description = description;
        }

        public List<List<String>> getCommands() {
            return commands;
        }

        public String getDescription() {
            return description;
        }
    }
}
