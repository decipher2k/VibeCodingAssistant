/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds database schema context for AI requests.
 * Includes full schema content, referenced tables/fields, and ORM recommendations.
 */
public final class DatabaseSchemaContext {
    private final String fullSchemaContent;
    private final Set<String> referencedTables;
    private final Map<String, Set<String>> referencedFields;
    private final String ormRecommendation;
    private final String migrationRecommendation;
    
    private DatabaseSchemaContext(String fullSchemaContent,
                                  Set<String> referencedTables,
                                  Map<String, Set<String>> referencedFields,
                                  String ormRecommendation,
                                  String migrationRecommendation) {
        this.fullSchemaContent = fullSchemaContent;
        this.referencedTables = new HashSet<>(referencedTables);
        this.referencedFields = new HashMap<>(referencedFields);
        this.ormRecommendation = ormRecommendation;
        this.migrationRecommendation = migrationRecommendation;
    }
    
    /**
     * Creates a DatabaseSchemaContext from schema loader and parsed tokens.
     * 
     * @param schemaLoader The loaded schema
     * @param parsedTokens The parsed and validated tokens
     * @param language The target programming language
     * @return A new DatabaseSchemaContext instance
     */
    public static DatabaseSchemaContext create(DatabaseSchemaLoader schemaLoader,
                                               DatabaseTokenParser.ParsedTokens parsedTokens,
                                               ProgrammingLanguage language) {
        if (schemaLoader == null || !schemaLoader.isLoaded()) {
            throw new IllegalArgumentException("Schema loader must be loaded");
        }
        
        String fullContent = schemaLoader.getFullContent();
        Set<String> tables = parsedTokens != null ? parsedTokens.getReferencedTables() : new HashSet<>();
        
        Map<String, Set<String>> fields = new HashMap<>();
        if (parsedTokens != null) {
            for (String table : tables) {
                fields.put(table, parsedTokens.getReferencedFields(table));
            }
        }
        
        String ormRec = generateOrmRecommendation(language);
        String migrationRec = generateMigrationRecommendation(language);
        
        return new DatabaseSchemaContext(fullContent, tables, fields, ormRec, migrationRec);
    }
    
    /**
     * Generates ORM recommendation based on programming language.
     */
    private static String generateOrmRecommendation(ProgrammingLanguage language) {
        if (language == null) {
            return "Use appropriate database abstraction for the target language.";
        }
        
        switch (language) {
            case JAVA:
                return "Use JPA/Hibernate for ORM. Define entities with @Entity, @Table, @Column annotations. " +
                       "Configure persistence.xml or use Spring Data JPA for simplified repository access.";
                       
            case CSHARP:
                return "Use Entity Framework Core for ORM. Define entity classes with DbContext. " +
                       "Use Code First approach with data annotations or Fluent API for schema mapping.";
                       
            case PYTHON:
                return "Use SQLAlchemy for ORM. Define models inheriting from Base with Table and Column definitions. " +
                       "Use declarative mapping for clear schema representation.";
                       
            case JAVASCRIPT:
                return "Use Prisma, TypeORM, or Sequelize for ORM. For TypeScript, Prisma provides type-safe database access. " +
                       "Define models/entities with decorators or schema files.";
                       
            case RUBY:
                return "Use ActiveRecord (Rails) for ORM. Define models inheriting from ApplicationRecord. " +
                       "Follow Rails conventions for table naming and associations.";
                       
            case PHP:
                return "Use Doctrine ORM or Eloquent (Laravel). Define entity classes with annotations or array mappings. " +
                       "Use migration files for schema versioning.";
                       
            case GO:
                return "Use GORM or sqlx for database access. Define struct models with tags for column mapping. " +
                       "GORM provides ORM features while sqlx offers lighter abstraction.";
                       
            default:
                return "Use an appropriate ORM or database abstraction library for " + language + ". " +
                       "Define models that map to the database schema with proper type mapping.";
        }
    }
    
    /**
     * Generates migration recommendation based on programming language.
     */
    private static String generateMigrationRecommendation(ProgrammingLanguage language) {
        if (language == null) {
            return "Implement database migrations to manage schema changes over time.";
        }
        
        switch (language) {
            case JAVA:
                return "Use Flyway or Liquibase for database migrations. With Gradle/Maven, configure the migration plugin. " +
                       "For JPA, you can also use Hibernate's hbm2ddl for development. " +
                       "Migrations should be versioned SQL or Java files in src/main/resources/db/migration.";
                       
            case CSHARP:
                return "Use EF Core Migrations. Run 'dotnet ef migrations add <MigrationName>' to create migrations. " +
                       "Apply with 'dotnet ef database update'. Migrations are C# classes in Migrations folder.";
                       
            case PYTHON:
                return "Use Alembic for SQLAlchemy migrations. Initialize with 'alembic init'. " +
                       "Create migrations with 'alembic revision --autogenerate -m \"message\"'. " +
                       "Apply with 'alembic upgrade head'. Migration files are Python scripts.";
                       
            case JAVASCRIPT:
                return "Use Prisma Migrate ('prisma migrate dev'), TypeORM migrations ('typeorm migration:create'), " +
                       "or Sequelize migrations ('sequelize migration:create'). " +
                       "Migrations are typically SQL or JS/TS files tracked in version control.";
                       
            case RUBY:
                return "Use Rails migrations. Generate with 'rails generate migration <MigrationName>'. " +
                       "Apply with 'rails db:migrate'. Migrations are Ruby DSL files in db/migrate.";
                       
            case PHP:
                return "Use Laravel migrations ('php artisan make:migration') or Doctrine migrations. " +
                       "Run 'php artisan migrate' to apply. Migrations are PHP classes with up/down methods.";
                       
            case GO:
                return "Use golang-migrate or GORM AutoMigrate. With golang-migrate, write SQL migrations. " +
                       "With GORM, call db.AutoMigrate(&Model{}) for simple cases or write custom SQL for complex changes.";
                       
            default:
                return "Implement a migration system for " + language + " to version database schema changes. " +
                       "Use SQL scripts or a migration framework appropriate for the ecosystem.";
        }
    }
    
    /**
     * Gets the full schema file content.
     */
    public String getFullSchemaContent() {
        return fullSchemaContent;
    }
    
    /**
     * Gets all referenced table names.
     */
    public Set<String> getReferencedTables() {
        return new HashSet<>(referencedTables);
    }
    
    /**
     * Gets referenced fields for a specific table.
     */
    public Set<String> getReferencedFields(String tableName) {
        return referencedFields.getOrDefault(tableName, new HashSet<>());
    }
    
    /**
     * Checks if any tables are referenced.
     */
    public boolean hasReferencedTables() {
        return !referencedTables.isEmpty();
    }
    
    /**
     * Gets the ORM recommendation for the target language.
     */
    public String getOrmRecommendation() {
        return ormRecommendation;
    }
    
    /**
     * Gets the migration recommendation for the target language.
     */
    public String getMigrationRecommendation() {
        return migrationRecommendation;
    }
    
    /**
     * Generates a structured summary of referenced tables and fields for AI context.
     */
    public String generateStructuredSummary() {
        if (!hasReferencedTables()) {
            return "No specific tables or fields referenced in the prompt.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Referenced Tables and Fields:\n");
        
        for (String table : referencedTables) {
            summary.append("- Table: ").append(table).append("\n");
            
            Set<String> fields = referencedFields.get(table);
            if (fields != null && !fields.isEmpty()) {
                summary.append("  Referenced fields: ").append(String.join(", ", fields)).append("\n");
            }
        }
        
        return summary.toString();
    }
}
