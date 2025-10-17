/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.vibecoding.wizard.DatabaseSchemaContext;
import com.vibecoding.wizard.DatabaseSchemaLoader;
import com.vibecoding.wizard.DatabaseTokenParser;
import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectSettings;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.PromptBuilder;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.TaskType;

public final class DatabaseSupportTests {
    private DatabaseSupportTests() {
    }

    public static void run(TestContext ctx) {
        testSchemaLoader(ctx);
        testTokenParser(ctx);
        testSchemaContext(ctx);
        testPromptBuilderIntegration(ctx);
    }

    private static void testSchemaLoader(TestContext ctx) {
        Path tempFile = null;
        try {
            // Create a temporary SQL schema file
            tempFile = Files.createTempFile("test-schema", ".sql");
            String sqlSchema = 
                "CREATE TABLE users (\n" +
                "    id INT PRIMARY KEY,\n" +
                "    email VARCHAR(255),\n" +
                "    username VARCHAR(100)\n" +
                ");\n\n" +
                "CREATE TABLE orders (\n" +
                "    id INT PRIMARY KEY,\n" +
                "    user_id INT,\n" +
                "    total DECIMAL(10,2)\n" +
                ");";
            Files.writeString(tempFile, sqlSchema);

            // Test schema loading
            DatabaseSchemaLoader loader = DatabaseSchemaLoader.create(tempFile);
            ctx.assertTrue("Loader created", loader != null);
            ctx.assertFalse("Not loaded yet", loader.isLoaded());

            loader.load();
            ctx.assertTrue("Loader loaded", loader.isLoaded());

            // Test schema content
            String content = loader.getFullContent();
            ctx.assertTrue("Full content loaded", content.contains("CREATE TABLE users"));
            ctx.assertTrue("Full content has orders", content.contains("CREATE TABLE orders"));

            // Test table detection
            Set<String> tables = loader.getTableNames();
            ctx.assertTrue("Has users table", tables.contains("users"));
            ctx.assertTrue("Has orders table", tables.contains("orders"));
            ctx.assertEquals("Table count", 2, tables.size());

            // Test column detection
            Set<String> userColumns = loader.getColumnNames("users");
            ctx.assertTrue("Has email column", userColumns.contains("email"));
            ctx.assertTrue("Has username column", userColumns.contains("username"));
            ctx.assertTrue("Has id column", userColumns.contains("id"));

            // Test case-insensitive lookups
            ctx.assertTrue("Table exists (case insensitive)", loader.hasTable("USERS"));
            ctx.assertTrue("Column exists (case insensitive)", loader.hasColumn("users", "EMAIL"));

            // Test suggestions
            List<String> suggestions = loader.suggestSimilarTables("user", 3);
            ctx.assertTrue("Suggests users", suggestions.contains("users") || suggestions.size() > 0);

            // Test CSV format
            Path csvFile = Files.createTempFile("test-schema", ".csv");
            String csvSchema = "products\nid\nname\nprice\n\ncategories\nid\nname";
            Files.writeString(csvFile, csvSchema);

            DatabaseSchemaLoader csvLoader = DatabaseSchemaLoader.create(csvFile);
            csvLoader.load();
            Set<String> csvTables = csvLoader.getTableNames();
            ctx.assertTrue("CSV: Has products table", csvTables.contains("products"));
            ctx.assertTrue("CSV: Has categories table", csvTables.contains("categories"));

            Set<String> productColumns = csvLoader.getColumnNames("products");
            ctx.assertTrue("CSV: Has price column", productColumns.contains("price"));

            Files.deleteIfExists(csvFile);

        } catch (Exception e) {
            ctx.assertTrue("Schema loader test failed: " + e.getMessage(), false);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    private static void testTokenParser(TestContext ctx) {
        Path tempFile = null;
        try {
            // Create schema
            tempFile = Files.createTempFile("test-schema", ".sql");
            String sqlSchema = 
                "CREATE TABLE customers (\n" +
                "    id INT PRIMARY KEY,\n" +
                "    email VARCHAR(255),\n" +
                "    name VARCHAR(100)\n" +
                ");";
            Files.writeString(tempFile, sqlSchema);

            DatabaseSchemaLoader loader = DatabaseSchemaLoader.create(tempFile);
            loader.load();
            DatabaseTokenParser parser = DatabaseTokenParser.create(loader);

            // Test valid table token
            String prompt1 = "Create CRUD endpoints for {customers}";
            DatabaseTokenParser.ParsedTokens tokens1 = parser.parseTokens(prompt1);
            ctx.assertTrue("Found table token", tokens1.hasTokens());
            ctx.assertEquals("Table token count", 1, tokens1.getTableTokenCount());
            ctx.assertTrue("All tokens valid", tokens1.areAllValid());
            ctx.assertTrue("References customers", tokens1.getReferencedTables().contains("customers"));

            // Test valid field token
            String prompt2 = "Validate {customers.email} and ensure {customers.name} is not null";
            DatabaseTokenParser.ParsedTokens tokens2 = parser.parseTokens(prompt2);
            ctx.assertTrue("Found field tokens", tokens2.hasTokens());
            ctx.assertEquals("Field token count", 2, tokens2.getFieldTokenCount());
            ctx.assertTrue("All field tokens valid", tokens2.areAllValid());
            Set<String> fields = tokens2.getReferencedFields("customers");
            ctx.assertTrue("References email field", fields.contains("email"));
            ctx.assertTrue("References name field", fields.contains("name"));

            // Test invalid table token
            String prompt3 = "Use {invalid_table} for storage";
            DatabaseTokenParser.ParsedTokens tokens3 = parser.parseTokens(prompt3);
            ctx.assertFalse("Invalid table not valid", tokens3.areAllValid());
            List<String> errors3 = tokens3.getValidationErrors();
            ctx.assertTrue("Has validation error", errors3.size() > 0);
            ctx.assertTrue("Error mentions table", errors3.get(0).contains("invalid_table"));

            // Test invalid field token
            String prompt4 = "Check {customers.invalid_field}";
            DatabaseTokenParser.ParsedTokens tokens4 = parser.parseTokens(prompt4);
            ctx.assertFalse("Invalid field not valid", tokens4.areAllValid());
            List<String> errors4 = tokens4.getValidationErrors();
            ctx.assertTrue("Has field validation error", errors4.size() > 0);

            // Test mixed tokens
            String prompt5 = "Use {customers} and validate {customers.email}";
            DatabaseTokenParser.ParsedTokens tokens5 = parser.parseTokens(prompt5);
            ctx.assertEquals("Mixed: 1 table token", 1, tokens5.getTableTokenCount());
            ctx.assertEquals("Mixed: 1 field token", 1, tokens5.getFieldTokenCount());
            ctx.assertTrue("Mixed: all valid", tokens5.areAllValid());

        } catch (Exception e) {
            ctx.assertTrue("Token parser test failed: " + e.getMessage(), false);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    private static void testSchemaContext(TestContext ctx) {
        Path tempFile = null;
        try {
            // Create schema
            tempFile = Files.createTempFile("test-schema", ".sql");
            String sqlSchema = "CREATE TABLE inventory (id INT, item VARCHAR(50));";
            Files.writeString(tempFile, sqlSchema);

            DatabaseSchemaLoader loader = DatabaseSchemaLoader.create(tempFile);
            loader.load();
            DatabaseTokenParser parser = DatabaseTokenParser.create(loader);
            DatabaseTokenParser.ParsedTokens tokens = parser.parseTokens("Use {inventory} for storage");

            // Test context creation for Java
            DatabaseSchemaContext contextJava = DatabaseSchemaContext.create(loader, tokens, ProgrammingLanguage.JAVA);
            ctx.assertTrue("Has schema content", contextJava.getFullSchemaContent().contains("inventory"));
            ctx.assertTrue("Has referenced tables", contextJava.hasReferencedTables());
            ctx.assertTrue("References inventory", contextJava.getReferencedTables().contains("inventory"));
            
            String ormRec = contextJava.getOrmRecommendation();
            ctx.assertTrue("Java ORM mentions JPA", ormRec.contains("JPA") || ormRec.contains("Hibernate"));
            
            String migrationRec = contextJava.getMigrationRecommendation();
            ctx.assertTrue("Java migration mentions Flyway", migrationRec.contains("Flyway") || migrationRec.contains("Liquibase"));

            // Test context for Python
            DatabaseSchemaContext contextPython = DatabaseSchemaContext.create(loader, tokens, ProgrammingLanguage.PYTHON);
            String pythonOrm = contextPython.getOrmRecommendation();
            ctx.assertTrue("Python ORM mentions SQLAlchemy", pythonOrm.contains("SQLAlchemy"));
            
            String pythonMigration = contextPython.getMigrationRecommendation();
            ctx.assertTrue("Python migration mentions Alembic", pythonMigration.contains("Alembic"));

            // Test context for C#
            DatabaseSchemaContext contextCSharp = DatabaseSchemaContext.create(loader, tokens, ProgrammingLanguage.CSHARP);
            String csharpOrm = contextCSharp.getOrmRecommendation();
            ctx.assertTrue("C# ORM mentions Entity Framework", csharpOrm.contains("Entity Framework"));

            // Test structured summary
            String summary = contextJava.generateStructuredSummary();
            ctx.assertTrue("Summary contains table", summary.contains("inventory"));

        } catch (Exception e) {
            ctx.assertTrue("Schema context test failed: " + e.getMessage(), false);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    private static void testPromptBuilderIntegration(TestContext ctx) {
        Path tempSchema = null;
        Path tempDir = null;
        try {
            // Create a schema file
            tempSchema = Files.createTempFile("test-schema", ".sql");
            String sqlSchema = 
                "CREATE TABLE books (\n" +
                "    id INT PRIMARY KEY,\n" +
                "    title VARCHAR(255),\n" +
                "    author VARCHAR(100),\n" +
                "    isbn VARCHAR(20)\n" +
                ");";
            Files.writeString(tempSchema, sqlSchema);

            // Create project directory with dummy file
            tempDir = Files.createTempDirectory("promptbuilder-db-test");
            Files.writeString(tempDir.resolve("dummy.java"), "public class Dummy {}");

            // Create config and project settings
            InitialConfig config = new InitialConfig(ProgrammingLanguage.JAVA, ProjectStyle.GUI,
                EnumSet.of(TargetOs.LINUX), tempDir);

            ProjectSettings settings = new ProjectSettings();
            settings.setDatabaseDescription("PostgreSQL 15");
            settings.setDatabaseDefinitionFile(tempSchema);

            MainTaskData data = new MainTaskData();
            data.setProjectOverview("Library management system using {books} table");
            data.setChangeDescription("Add validation for {books.isbn}");

            // Build prompt with database support
            String prompt = PromptBuilder.buildPrimaryPrompt(TaskType.MODIFY_EXISTING_SOFTWARE, config, data, settings);

            // Verify database content is included
            ctx.assertTrue("Prompt has database schema", prompt.contains("Database Schema Definition"));
            ctx.assertTrue("Prompt has schema content", prompt.contains("CREATE TABLE books"));
            ctx.assertTrue("Prompt has database system", prompt.contains("PostgreSQL 15"));
            ctx.assertTrue("Prompt has ORM guideline", prompt.contains("Database Implementation Guidelines"));
            ctx.assertTrue("Prompt mentions JPA", prompt.contains("JPA") || prompt.contains("Hibernate"));
            ctx.assertTrue("Prompt mentions migrations", prompt.contains("Migration"));
            ctx.assertTrue("Prompt has referenced tables", prompt.contains("Referenced Database Elements") || 
                                                           prompt.contains("books"));
            ctx.assertTrue("Prompt warns about credentials", prompt.contains("NEVER log") || 
                                                            prompt.contains("credentials"));

            // Test with invalid tokens
            MainTaskData dataInvalid = new MainTaskData();
            dataInvalid.setProjectOverview("Use {invalid_table} and {books.invalid_field}");
            
            String promptInvalid = PromptBuilder.buildPrimaryPrompt(TaskType.GENERATE_APP_OR_SCRIPT, config, dataInvalid, settings);
            ctx.assertTrue("Prompt shows validation errors", promptInvalid.contains("Validation Errors") || 
                                                             promptInvalid.contains("not found"));

            // Test without database file
            ProjectSettings settingsNoDb = new ProjectSettings();
            String promptNoDb = PromptBuilder.buildPrimaryPrompt(TaskType.GENERATE_APP_OR_SCRIPT, config, data, settingsNoDb);
            ctx.assertFalse("No DB prompt doesn't have schema", promptNoDb.contains("Database Schema Definition"));

        } catch (Exception e) {
            ctx.assertTrue("Prompt builder integration test failed: " + e.getMessage(), false);
        } finally {
            try {
                if (tempSchema != null) {
                    Files.deleteIfExists(tempSchema);
                }
                if (tempDir != null) {
                    Files.deleteIfExists(tempDir.resolve("dummy.java"));
                    Files.deleteIfExists(tempDir);
                }
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
}
