# Vibe Coding Assistant

Vibe Coding Assistant allows you to create apps, modules and scripts using a simple GUI - No coding skills required!<br>
The apps will be automatically created using the GitHub Copilot Plus AI according to the data that you have enterd into Vibe Coding Assistant.<br><br>
It is a desktop companion for GitHub Copilot CLI users and packages project discovery, prompt engineering, build execution, and failure remediation into a guided wizard so you can automate non-trivial software tasks with confidence.
(Work in progress)<br>
<br>

[Documentation](https://github.com/decipher2k/VibeCodingAssistant/wiki)
[Demo Project](https://github.com/decipher2k/VibeCodingAssistant/raw/refs/heads/main/demo-enterprise-app.vcp)

<img width="1402" height="897" alt="Bildschirmfoto_20251016_074447" src="https://github.com/user-attachments/assets/1ec177fe-97e0-498a-894e-2e80ee9ad991" />



UPDATE: Automatic dependency installation is now available!<br>
**Windows**: The dependency installer will automatically install npm, GitHub Copilot CLI, Java and DotNet SDK 9.0<br>
**Linux**: The dependency installer will automatically install .NET SDK 9.0 using Wine (Wine must be installed separately)<br>
<br>
AI model used: Claude Sonnet 4.5 (Premium)<br>
<br>
<b>Please note: <br>
Each run costs 1 GitHub Copliot Plus premium credit.<br>
If a run fails, there will be upt to 10 retries, which can cost up to 10 GitHub Copilot Plus premium credits. This shouldn't happen normaly and is just a fallback mechanism.<br>
Each finetuning will cost 1 more credit, thus it is advised to note all changes required and let the AI perform them in one run.<br><br>
Vibe Coding Assistant is still a beta. It can eventually destroy an existing codebase.<br>
The generated code can be prone to errors and may contain security flaws. <br>
Don't use in a production environment!
<b>
<br><br>
## 🧰 Requirements

- **Operating System:** Linux, Windows, or macOS
- **JRE 25** (Java Runtime Environment)
- **npm** (Node Package Manager)
- **GitHub Copilot CLI** installed and authenticated
- **A GitHub Copilot Pro account** (paid)
- **Build toolchain** for the language you intend to automate:
  - **C#** – .NET SDK with `dotnet build`.
  - **C++** – `cmake` plus a configured build directory.
  - **Java** – Gradle or Maven (`gradle build` / `mvn package`).
  - **Python** – CPython (`python -m compileall`).
  etc


## �📦 Installation Instructions

### Installing JRE 25

#### Linux

**Ubuntu/Debian:**
```bash
# Download Oracle JDK 25
wget https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.deb
sudo dpkg -i jdk-25_linux-x64_bin.deb

# Or use SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 25-open
```

**Fedora/RHEL:**
```bash
# Download Oracle JDK 25
wget https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.rpm
sudo rpm -ivh jdk-25_linux-x64_bin.rpm

# Or use SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 25-open
```

**Arch Linux:**
```bash
# Install OpenJDK 25 (when available in repos)
sudo pacman -S jdk-openjdk

# Or use SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 25-open
```

Verify installation:
```bash
java -version
```

#### Windows

1. Download Oracle JDK 25 from [Oracle's website](https://www.oracle.com/java/technologies/downloads/)
2. Run the installer (`.exe` file)
3. Follow the installation wizard
4. Add Java to your PATH:
   - Open System Properties → Environment Variables
   - Add `C:\Program Files\Java\jdk-25\bin` to the `Path` variable
5. Verify installation in Command Prompt or PowerShell:
```powershell
java -version
```

#### macOS

**Using Homebrew (recommended):**
```bash
brew install openjdk@25
```

**Or download from Oracle:**
1. Download the `.dmg` file from [Oracle's website](https://www.oracle.com/java/technologies/downloads/)
2. Open the `.dmg` file and run the installer
3. Follow the installation wizard

Verify installation:
```bash
java -version
```

### Installing npm

#### Linux

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install nodejs npm
```

**Fedora/RHEL:**
```bash
sudo dnf install nodejs npm
```

**Arch Linux:**
```bash
sudo pacman -S nodejs npm
```

**Using nvm (Node Version Manager - recommended for all distros):**
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.0/install.sh | bash
source ~/.bashrc  # or ~/.zshrc
nvm install node
```

Verify installation:
```bash
npm -v
node -v
```

#### Windows

**Using official installer:**
1. Download Node.js from [nodejs.org](https://nodejs.org/)
2. Run the installer (includes npm)
3. Follow the installation wizard

**Using winget:**
```powershell
winget install OpenJS.NodeJS
```

**Using Chocolatey:**
```powershell
choco install nodejs
```

Verify installation:
```powershell
npm -v
node -v
```

#### macOS

**Using Homebrew:**
```bash
brew install node
```

**Using official installer:**
1. Download Node.js from [nodejs.org](https://nodejs.org/)
2. Run the `.pkg` installer
3. Follow the installation wizard

**Using nvm (recommended):**
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.0/install.sh | bash
source ~/.zshrc  # or ~/.bash_profile
nvm install node
```

Verify installation:
```bash
npm -v
node -v
```

### Installing GitHub Copilot CLI

After npm is installed, install GitHub Copilot CLI globally:

#### All Platforms (Linux, Windows, macOS)

```bash
npm install -g @github/copilot-cli
```

#### Authenticate with GitHub

Run the following command and follow the authentication flow:

```bash
github-copilot-cli
then enter /auth in the Github Copilot CLI prompt
```

This will:
1. Open your browser to authenticate with GitHub
2. Link your GitHub account with the CLI
3. Verify your GitHub Copilot Pro subscription

Verify installation:
```bash
github-copilot-cli --version
```

**Note:** GitHub Copilot CLI requires an active GitHub Copilot Pro subscription.

### First-Time Authentication

When you first launch Vibe Coding Assistant, if the GitHub Copilot CLI is not authenticated, you'll see an authentication dialog with the following options:

1. **Register for GitHub Copilot** – Click the "Register..." link to open the GitHub Copilot subscription page in your browser
   - If your browser doesn't open automatically, the URL will be displayed in a dialog
   - You can also find the URL in the console output
   
2. **Launch Authentication** – Click "Launch GitHub Copilot CLI to Authenticate" to start the authentication process
   - This opens a terminal session with the Copilot CLI
   - Enter `/login` in the CLI prompt
   - Follow the browser-based authentication flow
   - After successful authentication, restart Vibe Coding Assistant

**Troubleshooting Authentication:**
- The application uses multiple methods to open browsers (Java Desktop API, Windows `rundll32`, macOS `open`, Linux `xdg-open`)
- If automatic browser opening fails, copy the URL from the dialog or console
- Check that your default browser is properly configured
- Corporate or security policies may prevent automatic browser launching

## 🧭 Overview

The wizard walks you through three conversational screens to capture context about your codebase, intended changes, and environment. It then assembles a rich Copilot prompt, drives the Copilot CLI, and executes the resulting plan while tracking logs, compilation output, and retries inside a single window.

Use it when you want a repeatable way to generate new components or applications from structured requirements.

## 🔧 Core Functions

- **Project intake & validation** – verify language, project style, and environment prerequisites before any work begins.
- **Prompt composition** – combines task metadata, custom dialogs, and recent outcomes into a reproducible Copilot request.
- **Automated test generation** – creates comprehensive unit tests and integration tests for all generated or modified code.
- **Build automation** – chooses the correct build tool (Gradle, Maven, dotnet, cmake, python, custom script) and executes it.
- **Test execution** – automatically runs unit tests and integration tests after building to verify code quality.
- **Self-healing loop** – captures compiler output, feeds it back into Copilot, and limits retries to protect your workflow time.
- **Knowledge capture** – Dialog editors let you curate workflows, task templates, and form layouts for your team.

## 🧑‍💻 Using the Wizard

1. **Initial Setup** – Confirm runtime specifics (language, project style, operating systems). The wizard validates your selections and surfaces missing prerequisites.
2. **Task Selection** – Pick from curated task categories (create app, modify code, fix build, author documentation, etc.) or load a saved dialog definition.
3. **Configure Details** – Supply requirements in structured forms. Multi-line editors help you capture acceptance criteria, code snippets, and notes.
4. **Run & Monitor** – Watch the live console for Copilot responses, command execution, and build logs. You can pause or stop the workflow at any time.
5. **Automated Testing** – The wizard automatically generates unit tests and integration tests for all code, then executes them to verify correctness.
6. **Resolve Issues** – Use the compilation error dialog to inspect remaining problems and retry with refined instructions if the automated loop exhausts its attempts.

## 🧪 Testing Features

The Vibe Coding Assistant automatically creates and executes tests for all generated or modified code:

- **Unit Tests** – Tests individual functions, methods, and classes in isolation
  - Covers normal use cases, edge cases, and error conditions
  - Uses appropriate testing frameworks (JUnit, NUnit/xUnit, pytest, Jest, etc.)
  - Aims for 80%+ code coverage of critical paths
  
- **Integration Tests** – Tests interactions between components (when applicable)
  - Verifies end-to-end workflows and user scenarios
  - Tests database operations, file I/O, and API calls
  - Validates that integrated components work together correctly

- **Automatic Execution** – After building, all tests are run automatically
  - Unit tests run first to verify individual components
  - Integration tests run second to verify component interactions
  - Build fails if tests don't pass, triggering the self-healing loop

- **Framework Support** – Works with standard test commands:
  - Java/Gradle: `gradle test`
  - Java/Maven: `mvn test`
  - C#/.NET: `dotnet test`
  - Python: `pytest`
  - JavaScript/Node: `npm test`


## �️ Database Support

Vibe Coding Assistant includes first-class database support to help you generate database-aware applications with proper ORM integration and migrations.

### Configuring Database Support

1. **Open Project Settings** from the File menu or toolbar
2. Navigate to the **Database** tab
3. Configure two settings:

#### Database System Description
Describe the database system you're using (e.g., "PostgreSQL 15", "MySQL 8.0", "SQLite 3.40"). Include version information and any relevant connection details like host, port, and database name. This information helps the AI generate appropriate connection code.

**Example:**
```
PostgreSQL 15 on localhost:5432, database: myapp_db
```

#### Database Definition File
Attach a schema definition file that describes your database structure. Supported formats:
- **SQL DDL files** (`.sql`) - CREATE TABLE statements
- **CSV files** (`.csv`) - Simple table/column listings

**SQL Example:**
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    total DECIMAL(10,2),
    status VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**CSV Example:**
```
users
id
email
username
created_at

orders
id
user_id
total
status
```

### Using Database Tokens in Prompts

Reference specific tables and fields in your prompts using token syntax:

- **`{TABLE}`** - References an entire table
- **`{TABLE.FIELD}`** - References a specific column in a table

**Examples:**
- "Create CRUD endpoints for {users}"
- "Add validation to ensure {users.email} is unique"
- "Generate a report joining {users} and {orders} on {orders.user_id}"

The system will:
1. ✅ Validate tokens against your schema
2. ⚠️ Warn about invalid table or field references
3. 💡 Suggest similar table names if you mistype
4. 📋 Include relevant schema context in AI requests

### ORM and Migration Support

Based on your programming language, the AI will automatically:

- **Java**: Use JPA/Hibernate with proper entity annotations, generate Flyway or Liquibase migrations
- **C#**: Use Entity Framework Core with DbContext, generate EF Core migrations
- **Python**: Use SQLAlchemy with declarative models, generate Alembic migrations
- **JavaScript**: Use Prisma, TypeORM, or Sequelize with appropriate schema definitions
- **Ruby**: Use ActiveRecord with Rails conventions and migrations
- **PHP**: Use Doctrine or Eloquent with migration files
- **Go**: Use GORM or sqlx with struct tags and migrations

The generated code will:
- Define database models using idiomatic ORMs for the target language
- Include migration files to version schema changes
- Wire database connections using settings from "Database Description"
- Follow security best practices (no hardcoded credentials)

### Security Note

⚠️ The "Database Description" field is used for generating connection code. **NEVER include passwords or sensitive credentials directly.** Instead, reference environment variables or secure configuration methods in your description:

```
PostgreSQL 15 on ${DB_HOST}:${DB_PORT}
Database: ${DB_NAME}
Use environment variables for credentials
```

## 🚀 Setup & Launch

### Option A: Run with Gradle (requires Gradle 8+)

1. Install a compatible Gradle distribution or add the Gradle Wrapper.
2. From the project root run:
   - `gradle run` to launch the wizard.
   - `gradle jar` to produce `build/libs/vibe-coding-wizard.jar` for later use.

### Option B: Manual compilation with `javac`

1. Create an output directory: `mkdir -p out`.
2. Compile sources: `javac -d out (find src/main/java -name "*.java")`.
3. Start the app: `java -cp out com.vibecoding.wizard.VibeCodingWizardApp`.

> Prefer a single runnable artifact? After `gradle jar`, launch with `java -jar build/libs/vibe-coding-wizard.jar`.

## �️ Customization & Extensibility

- Tailor prompt phrasing in `PromptBuilder.java` to match your team’s tone or templates.
- Extend `BuildCommandPlanner.java` to support new languages, test runners, or deployment hooks.
- Integrate with alternative CLIs by swapping out `CopilotCliService.java`.
- Adjust theming and layout via `ThemeManager.java`, `UiUtils.java`, and `FormLayoutBuilder.java`.

## 🧪 Development Notes

- Run unit tests with `gradle test` (requires Gradle and JDK 21+).
- Headless UI tests live under `src/test/java/com/vibecoding/wizard/tests/` and can be executed the same way.
- The application builds cleanly without external dependencies; ensure your `JAVA_HOME` matches the required toolchain version.

## ❓ Troubleshooting

- **Copilot CLI not found:** Run `npm install -g @github/copilot` and verify the binary is on your `PATH`.
- **Copilot CLI not authenticated:** When the authentication dialog appears, click "Launch GitHub Copilot CLI to Authenticate" and follow the browser-based authentication flow. After completing authentication, restart the application.
- **"Register..." link doesn't open browser:** 
  - The application tries multiple methods to open your browser (Java Desktop API, OS-specific commands)
  - If automatic opening fails, a dialog will display the URL to manually copy: `https://github.com/features/copilot`
  - On Windows, ensure your default browser is properly configured in Windows settings
  - Check console output for detailed debugging information about browser opening attempts
  - If you're in a restricted environment (corporate policy, antivirus, etc.), you may need to manually open the URL
- **Build command fails immediately:** Check that the detected project style matches a configured build tool; override defaults in the wizard if needed. Make sure that the build tools required for the chosen programing language are installed.
- **UI fails to start:** Confirm you compiled with a compatible JDK and that Swing is available (it ships with the standard JDK).
- **Retry loop stops early:** The wizard caps retries at 10 iterations to avoid infinite paths. Adjust logic in `TaskExecutionDialog.java` if you need different limits.

## 📄 License

This project ships under the Apache 2.0 license and is Copyright (c) 2025 by Dennis Michael Heine
