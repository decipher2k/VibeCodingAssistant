/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads and parses database schema definition files.
 * Supports various schema formats (SQL, CSV, etc.) and extracts table and column information.
 */
public final class DatabaseSchemaLoader {
    private final Path schemaFile;
    private String fullContent;
    private Map<String, Set<String>> tableColumns;
    private boolean loaded;
    
    private DatabaseSchemaLoader(Path schemaFile) {
        this.schemaFile = schemaFile;
        this.tableColumns = new HashMap<>();
        this.loaded = false;
    }
    
    /**
     * Creates a new DatabaseSchemaLoader for the given schema file.
     * 
     * @param schemaFile Path to the schema definition file
     * @return A new DatabaseSchemaLoader instance
     */
    public static DatabaseSchemaLoader create(Path schemaFile) {
        if (schemaFile == null) {
            throw new IllegalArgumentException("Schema file path cannot be null");
        }
        return new DatabaseSchemaLoader(schemaFile);
    }
    
    /**
     * Loads the schema file content and parses table/column information.
     * 
     * @throws IOException If the file cannot be read
     * @throws IllegalStateException If the file does not exist or is not readable
     */
    public void load() throws IOException {
        if (!Files.exists(schemaFile)) {
            throw new IllegalStateException("Schema file does not exist: " + schemaFile.toAbsolutePath());
        }
        
        if (!Files.isReadable(schemaFile)) {
            throw new IllegalStateException("Schema file is not readable: " + schemaFile.toAbsolutePath());
        }
        
        // Read full file content
        this.fullContent = Files.readString(schemaFile);
        
        // Parse schema to extract table and column information
        parseSchema();
        
        this.loaded = true;
    }
    
    /**
     * Parses the schema content to extract table and column information.
     * Supports SQL DDL and basic CSV formats.
     */
    private void parseSchema() {
        String fileExtension = getFileExtension(schemaFile);
        
        if ("sql".equalsIgnoreCase(fileExtension)) {
            parseSqlSchema();
        } else if ("csv".equalsIgnoreCase(fileExtension)) {
            parseCsvSchema();
        } else {
            // Try SQL parsing as default fallback
            parseSqlSchema();
        }
    }
    
    /**
     * Parses SQL DDL to extract CREATE TABLE statements and column definitions.
     */
    private void parseSqlSchema() {
        // Pattern to match CREATE TABLE statements (case-insensitive, handles various formats)
        // Matches: CREATE TABLE table_name ( ... )
        Pattern tablePattern = Pattern.compile(
            "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?[`\"\\[]?([\\w]+)[`\"\\]]?\\s*\\(",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        );
        
        // Pattern to match column definitions within CREATE TABLE
        // Matches: column_name TYPE, or `column_name` TYPE,
        Pattern columnPattern = Pattern.compile(
            "[`\"\\[]?([\\w]+)[`\"\\]]?\\s+[\\w]+",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher tableMatcher = tablePattern.matcher(fullContent);
        
        while (tableMatcher.find()) {
            String tableName = tableMatcher.group(1);
            
            // Find the end of this CREATE TABLE statement
            int startPos = tableMatcher.end();
            int endPos = findClosingParenthesis(fullContent, startPos);
            
            if (endPos > startPos) {
                String tableDefinition = fullContent.substring(startPos, endPos);
                Set<String> columns = new HashSet<>();
                
                // Extract column names from the table definition
                String[] lines = tableDefinition.split("\\r?\\n");
                for (String line : lines) {
                    line = line.trim();
                    
                    // Skip constraints and other non-column definitions
                    if (line.toUpperCase(Locale.ROOT).matches("^(PRIMARY\\s+KEY|FOREIGN\\s+KEY|CONSTRAINT|UNIQUE|CHECK|INDEX|KEY).*")) {
                        continue;
                    }
                    
                    Matcher columnMatcher = columnPattern.matcher(line);
                    if (columnMatcher.find()) {
                        String columnName = columnMatcher.group(1);
                        // Validate it's not a SQL keyword
                        if (!isSqlKeyword(columnName)) {
                            columns.add(columnName);
                        }
                    }
                }
                
                tableColumns.put(tableName, columns);
            }
        }
    }
    
    /**
     * Parses CSV schema format.
     * Expected format: First row = table name, subsequent rows = column names.
     */
    private void parseCsvSchema() {
        String[] lines = fullContent.split("\\r?\\n");
        String currentTable = null;
        Set<String> columns = null;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                if (currentTable != null && columns != null) {
                    tableColumns.put(currentTable, columns);
                    currentTable = null;
                    columns = null;
                }
                continue;
            }
            
            if (currentTable == null) {
                // First non-empty line is the table name
                currentTable = line.replaceAll("[^\\w]", "");
                columns = new HashSet<>();
            } else {
                // Subsequent lines are column names
                String columnName = line.split(",")[0].trim().replaceAll("[^\\w]", "");
                if (!columnName.isEmpty()) {
                    columns.add(columnName);
                }
            }
        }
        
        // Add the last table if present
        if (currentTable != null && columns != null) {
            tableColumns.put(currentTable, columns);
        }
    }
    
    /**
     * Finds the closing parenthesis matching the opening one at the start position.
     */
    private int findClosingParenthesis(String text, int startPos) {
        int depth = 1;
        for (int i = startPos; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Checks if a word is a common SQL keyword that should not be treated as a column name.
     */
    private boolean isSqlKeyword(String word) {
        String upper = word.toUpperCase(Locale.ROOT);
        return upper.equals("PRIMARY") || upper.equals("FOREIGN") || upper.equals("KEY") ||
               upper.equals("CONSTRAINT") || upper.equals("UNIQUE") || upper.equals("CHECK") ||
               upper.equals("INDEX") || upper.equals("REFERENCES") || upper.equals("NOT") ||
               upper.equals("NULL") || upper.equals("DEFAULT") || upper.equals("AUTO_INCREMENT") ||
               upper.equals("AUTOINCREMENT");
    }
    
    /**
     * Gets the file extension from a path.
     */
    private String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
    
    /**
     * Gets the full content of the schema file.
     * 
     * @return The full schema file content
     * @throws IllegalStateException If load() has not been called
     */
    public String getFullContent() {
        ensureLoaded();
        return fullContent;
    }
    
    /**
     * Gets all table names defined in the schema.
     * 
     * @return Set of table names (case as defined in schema)
     * @throws IllegalStateException If load() has not been called
     */
    public Set<String> getTableNames() {
        ensureLoaded();
        return new HashSet<>(tableColumns.keySet());
    }
    
    /**
     * Gets all column names for a specific table.
     * 
     * @param tableName The table name (case-insensitive)
     * @return Set of column names, or empty set if table not found
     * @throws IllegalStateException If load() has not been called
     */
    public Set<String> getColumnNames(String tableName) {
        ensureLoaded();
        
        // Try exact match first
        if (tableColumns.containsKey(tableName)) {
            return new HashSet<>(tableColumns.get(tableName));
        }
        
        // Try case-insensitive match
        for (Map.Entry<String, Set<String>> entry : tableColumns.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(tableName)) {
                return new HashSet<>(entry.getValue());
            }
        }
        
        return new HashSet<>();
    }
    
    /**
     * Checks if a table exists in the schema.
     * 
     * @param tableName The table name (case-insensitive)
     * @return true if the table exists
     * @throws IllegalStateException If load() has not been called
     */
    public boolean hasTable(String tableName) {
        ensureLoaded();
        return tableColumns.keySet().stream()
            .anyMatch(t -> t.equalsIgnoreCase(tableName));
    }
    
    /**
     * Checks if a column exists in a specific table.
     * 
     * @param tableName The table name (case-insensitive)
     * @param columnName The column name (case-insensitive)
     * @return true if the column exists in the table
     * @throws IllegalStateException If load() has not been called
     */
    public boolean hasColumn(String tableName, String columnName) {
        ensureLoaded();
        Set<String> columns = getColumnNames(tableName);
        return columns.stream().anyMatch(c -> c.equalsIgnoreCase(columnName));
    }
    
    /**
     * Finds the closest matching table names using simple string similarity.
     * 
     * @param tableName The table name to match
     * @param maxSuggestions Maximum number of suggestions to return
     * @return List of suggested table names
     * @throws IllegalStateException If load() has not been called
     */
    public List<String> suggestSimilarTables(String tableName, int maxSuggestions) {
        ensureLoaded();
        
        List<String> suggestions = new ArrayList<>();
        String lowerInput = tableName.toLowerCase(Locale.ROOT);
        
        // First, add tables that contain the input string
        for (String table : tableColumns.keySet()) {
            if (table.toLowerCase(Locale.ROOT).contains(lowerInput)) {
                suggestions.add(table);
            }
        }
        
        // If we need more, add tables where input contains the table name
        if (suggestions.size() < maxSuggestions) {
            for (String table : tableColumns.keySet()) {
                if (!suggestions.contains(table) && lowerInput.contains(table.toLowerCase(Locale.ROOT))) {
                    suggestions.add(table);
                    if (suggestions.size() >= maxSuggestions) {
                        break;
                    }
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Gets the path to the schema file.
     * 
     * @return The schema file path
     */
    public Path getSchemaFilePath() {
        return schemaFile;
    }
    
    /**
     * Checks if the schema has been loaded.
     * 
     * @return true if load() has been called successfully
     */
    public boolean isLoaded() {
        return loaded;
    }
    
    private void ensureLoaded() {
        if (!loaded) {
            throw new IllegalStateException("Schema not loaded. Call load() first.");
        }
    }
}
