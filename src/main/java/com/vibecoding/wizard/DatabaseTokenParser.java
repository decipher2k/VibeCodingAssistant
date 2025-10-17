/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and validates database tokens ({TABLE} and {TABLE.FIELD}) in user prompts.
 * Validates tokens against a loaded database schema.
 */
public final class DatabaseTokenParser {
    private final DatabaseSchemaLoader schemaLoader;
    private final Pattern tableTokenPattern;
    private final Pattern fieldTokenPattern;
    
    private DatabaseTokenParser(DatabaseSchemaLoader schemaLoader) {
        this.schemaLoader = schemaLoader;
        // Pattern to match {TABLE} tokens
        this.tableTokenPattern = Pattern.compile("\\{([\\w]+)\\}");
        // Pattern to match {TABLE.FIELD} tokens
        this.fieldTokenPattern = Pattern.compile("\\{([\\w]+)\\.([\\w]+)\\}");
    }
    
    /**
     * Creates a new DatabaseTokenParser with the given schema loader.
     * 
     * @param schemaLoader The schema loader to use for validation
     * @return A new DatabaseTokenParser instance
     */
    public static DatabaseTokenParser create(DatabaseSchemaLoader schemaLoader) {
        if (schemaLoader == null) {
            throw new IllegalArgumentException("Schema loader cannot be null");
        }
        if (!schemaLoader.isLoaded()) {
            throw new IllegalStateException("Schema loader must be loaded before creating parser");
        }
        return new DatabaseTokenParser(schemaLoader);
    }
    
    /**
     * Parses a prompt string to extract all database tokens.
     * 
     * @param prompt The user prompt containing tokens
     * @return ParsedTokens containing all found tokens
     */
    public ParsedTokens parseTokens(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return new ParsedTokens();
        }
        
        ParsedTokens tokens = new ParsedTokens();
        
        // Find all {TABLE.FIELD} tokens first (more specific pattern)
        Matcher fieldMatcher = fieldTokenPattern.matcher(prompt);
        Set<String> processedPositions = new HashSet<>();
        
        while (fieldMatcher.find()) {
            String fullToken = fieldMatcher.group(0);
            String tableName = fieldMatcher.group(1);
            String fieldName = fieldMatcher.group(2);
            int position = fieldMatcher.start();
            
            processedPositions.add(position + "-" + fieldMatcher.end());
            
            ValidationResult validation = validateFieldToken(tableName, fieldName);
            tokens.addFieldToken(tableName, fieldName, fullToken, validation);
        }
        
        // Find all {TABLE} tokens (excluding those that are part of {TABLE.FIELD})
        Matcher tableMatcher = tableTokenPattern.matcher(prompt);
        
        while (tableMatcher.find()) {
            String fullToken = tableMatcher.group(0);
            String tableName = tableMatcher.group(1);
            int position = tableMatcher.start();
            String posKey = position + "-" + tableMatcher.end();
            
            // Skip if this position was already processed as part of a field token
            if (processedPositions.contains(posKey)) {
                continue;
            }
            
            // Check if this looks like it should be a field token (has a dot after)
            if (position + fullToken.length() < prompt.length() &&
                prompt.charAt(position + fullToken.length()) == '.') {
                continue;
            }
            
            ValidationResult validation = validateTableToken(tableName);
            tokens.addTableToken(tableName, fullToken, validation);
        }
        
        return tokens;
    }
    
    /**
     * Validates a table token against the schema.
     */
    private ValidationResult validateTableToken(String tableName) {
        if (schemaLoader.hasTable(tableName)) {
            return ValidationResult.valid();
        }
        
        List<String> suggestions = schemaLoader.suggestSimilarTables(tableName, 3);
        String message = "Table '" + tableName + "' not found in schema.";
        
        if (!suggestions.isEmpty()) {
            message += " Did you mean: " + String.join(", ", suggestions) + "?";
        }
        
        return ValidationResult.invalid(message, suggestions);
    }
    
    /**
     * Validates a field token against the schema.
     */
    private ValidationResult validateFieldToken(String tableName, String fieldName) {
        if (!schemaLoader.hasTable(tableName)) {
            List<String> tableSuggestions = schemaLoader.suggestSimilarTables(tableName, 3);
            String message = "Table '" + tableName + "' not found in schema.";
            
            if (!tableSuggestions.isEmpty()) {
                message += " Did you mean: " + String.join(", ", tableSuggestions) + "?";
            }
            
            return ValidationResult.invalid(message, tableSuggestions);
        }
        
        if (!schemaLoader.hasColumn(tableName, fieldName)) {
            Set<String> columns = schemaLoader.getColumnNames(tableName);
            String message = "Field '" + fieldName + "' not found in table '" + tableName + "'.";
            
            if (!columns.isEmpty()) {
                List<String> columnList = new ArrayList<>(columns);
                message += " Available columns: " + String.join(", ", columnList.subList(0, Math.min(5, columnList.size())));
                if (columns.size() > 5) {
                    message += ", ...";
                }
            }
            
            return ValidationResult.invalid(message, new ArrayList<>(columns));
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Container for parsed tokens with validation results.
     */
    public static final class ParsedTokens {
        private final Map<String, TokenInfo> tableTokens = new HashMap<>();
        private final Map<String, Map<String, TokenInfo>> fieldTokens = new HashMap<>();
        
        private ParsedTokens() {
        }
        
        private void addTableToken(String tableName, String rawToken, ValidationResult validation) {
            if (!tableTokens.containsKey(tableName)) {
                tableTokens.put(tableName, new TokenInfo(rawToken, validation));
            }
        }
        
        private void addFieldToken(String tableName, String fieldName, String rawToken, ValidationResult validation) {
            if (!fieldTokens.containsKey(tableName)) {
                fieldTokens.put(tableName, new HashMap<>());
            }
            fieldTokens.get(tableName).put(fieldName, new TokenInfo(rawToken, validation));
        }
        
        /**
         * Gets all unique table names referenced in tokens.
         */
        public Set<String> getReferencedTables() {
            Set<String> tables = new HashSet<>();
            tables.addAll(tableTokens.keySet());
            tables.addAll(fieldTokens.keySet());
            return tables;
        }
        
        /**
         * Gets all field references for a specific table.
         */
        public Set<String> getReferencedFields(String tableName) {
            if (fieldTokens.containsKey(tableName)) {
                return new HashSet<>(fieldTokens.get(tableName).keySet());
            }
            return new HashSet<>();
        }
        
        /**
         * Checks if all tokens are valid.
         */
        public boolean areAllValid() {
            for (TokenInfo info : tableTokens.values()) {
                if (!info.validation.isValid()) {
                    return false;
                }
            }
            
            for (Map<String, TokenInfo> fields : fieldTokens.values()) {
                for (TokenInfo info : fields.values()) {
                    if (!info.validation.isValid()) {
                        return false;
                    }
                }
            }
            
            return true;
        }
        
        /**
         * Gets all validation errors.
         */
        public List<String> getValidationErrors() {
            List<String> errors = new ArrayList<>();
            
            for (Map.Entry<String, TokenInfo> entry : tableTokens.entrySet()) {
                if (!entry.getValue().validation.isValid()) {
                    errors.add(entry.getValue().rawToken + ": " + entry.getValue().validation.errorMessage);
                }
            }
            
            for (Map.Entry<String, Map<String, TokenInfo>> tableEntry : fieldTokens.entrySet()) {
                for (Map.Entry<String, TokenInfo> fieldEntry : tableEntry.getValue().entrySet()) {
                    if (!fieldEntry.getValue().validation.isValid()) {
                        errors.add(fieldEntry.getValue().rawToken + ": " + fieldEntry.getValue().validation.errorMessage);
                    }
                }
            }
            
            return errors;
        }
        
        /**
         * Checks if any tokens were found.
         */
        public boolean hasTokens() {
            return !tableTokens.isEmpty() || !fieldTokens.isEmpty();
        }
        
        /**
         * Gets the count of table tokens.
         */
        public int getTableTokenCount() {
            return tableTokens.size();
        }
        
        /**
         * Gets the count of field tokens.
         */
        public int getFieldTokenCount() {
            int count = 0;
            for (Map<String, TokenInfo> fields : fieldTokens.values()) {
                count += fields.size();
            }
            return count;
        }
    }
    
    /**
     * Information about a parsed token.
     */
    private static final class TokenInfo {
        final String rawToken;
        final ValidationResult validation;
        
        TokenInfo(String rawToken, ValidationResult validation) {
            this.rawToken = rawToken;
            this.validation = validation;
        }
    }
    
    /**
     * Result of token validation.
     */
    public static final class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final List<String> suggestions;
        
        private ValidationResult(boolean valid, String errorMessage, List<String> suggestions) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.suggestions = suggestions;
        }
        
        /**
         * Creates a valid validation result.
         */
        public static ValidationResult valid() {
            return new ValidationResult(true, null, new ArrayList<>());
        }
        
        /**
         * Creates an invalid validation result.
         */
        public static ValidationResult invalid(String errorMessage, List<String> suggestions) {
            return new ValidationResult(false, errorMessage, suggestions);
        }
        
        /**
         * Checks if the validation passed.
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * Gets the error message (null if valid).
         */
        public String getErrorMessage() {
            return errorMessage;
        }
        
        /**
         * Gets suggestions for correction (empty if valid or no suggestions).
         */
        public List<String> getSuggestions() {
            return new ArrayList<>(suggestions);
        }
    }
}
