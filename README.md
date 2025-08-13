# Organization Analyzer (Java 17, Maven, Strategy Rules)
![Java 17](https://img.shields.io/badge/Java-17-007396?logo=java)

A CLI that analyzes an organization structure from a CSV and reports:
- managers who earn **less** than they should (by how much),
- managers who earn **more** than they should (by how much),
- employees whose reporting line to the CEO is **too long** (by how much).

## Business Rules
- A manager must earn **at least 20% more** than the **average salary of their direct subordinates**.
- A manager must earn **no more than 50% more** than the **average salary of their direct subordinates**.
- Reporting line is too long if an employee has **more than 4 managers** *between* them and the CEO.

### Salary Rules – Exceptions
- **Underpaid Manager Rule** – checks if a manager’s salary is at least 20% higher than the average salary of their **direct subordinates**.  
  **Exception:** the **CEO is excluded** from this check, since their compensation is typically determined beyond immediate team averages.
- **Overpaid Manager Rule** – checks if a manager’s salary is no more than 50% higher than the average salary of their **direct subordinates**.  
  **Exception:** the **CEO is excluded** from this check for the same reason as above.
- **Reporting Line Too Long Rule** – checks if an employee has more than a configured maximum number of managers between them and the CEO.  
  **No exception for the CEO** here – the CEO is the top node.

## Architecture (SOLID + Strategy)
- `Employee` – domain model (`double salary`, `Integer managerId`, `List<Employee> directReports`).
- `EmployeeReader` / `EmployeeCsvReader` – input port for CSV parsing.
- `OrganizationFactory` – builds the org graph and validates: exactly **one CEO**, **acyclic**, all nodes reach the CEO.
- `OrgGraph` – DFS and `managersBetween` (**public** so rules can use it).
- `SalaryPolicy` / `DefaultSalaryPolicy` – thresholds (1.20× min, 1.50× max).
- `Rule<F extends Finding>` – Strategy interface.
- Findings – one file per finding: `UnderpaidManagerFinding`, `OverpaidManagerFinding`, `ReportingLineTooLongFinding`.
- Rules – `UnderpaidManagerRule`, `OverpaidManagerRule`, `ReportingLineTooLongRule`.
- `RuleEngine` – runs a collection of rules and aggregates results.
- `ConsolePrinter` – grouped printing for human-readable output.
- `OrgAnalyzer` – wires everything together.

### Extensibility
Add a rule in three steps:
1. Define a `Finding` record if needed.
2. Implement `Rule<YourFinding>` with the evaluation logic.
3. Register the rule in `OrgAnalyzer`’s `rules` list.  
No changes to existing rules/engine are required (Open/Closed Principle).

## CSV Format
Header:
```
Id,firstName,lastName,salary,managerId
```
- `salary` is a `double` (e.g., `90000.0`), `managerId` is empty for the CEO.
A dataset with **exactly 100 employees** is available at `src/main/resources/employees.sample.csv` and intentionally triggers **all** rules.

## Build & Run
```bash
mvn clean package
java -jar target/org-analyzer-1.0.0.jar src/main/resources/employees.sample.csv
```

## Tests
JUnit 5 tests at `src/test/java/com/bigcompany/OrgAnalyzerRuleEngineTest.java` validate parsing, boundary conditions, and long-chain detection.

## Possible Improvements

- **Configurable max-between threshold**  
  Currently, the maximum allowed number of managers between an employee and the CEO is fixed at **4**, as required by the original specification.  
  For additional flexibility, this value could be made configurable via a CLI parameter (e.g. `--max-between=6`) without breaking existing behavior (default would remain `4`).  
  This would allow the same codebase to be reused in scenarios where the company policy changes without requiring a new deployment.

- **Configurable salary thresholds**  
  The 20% minimum and 50% maximum salary margins for managers are currently fixed in code.  
  Making these values configurable via CLI or configuration file would enable easy adaptation to policy changes.

- **Alternative output formats**  
  In addition to the current console output, the application could support output in formats such as JSON or CSV for easier integration with reporting tools or dashboards.

- **Internationalization (i18n)**  
  Messages and outputs are currently in English.  
  Support for multiple languages could be added by externalizing message strings to resource bundles.

- **Unit test coverage expansion**  
  Additional test cases could cover:
  - Managers exactly at the threshold limits (1.20× and 1.50× average subordinate salary)
  - Employees exactly at the max-between boundary
  - Input file with disconnected employees or multiple CEOs (invalid cases)

- **Error handling and validation improvements**  
  The current implementation validates CEO count, connectivity, and cycles.  
  Additional checks could include:
  - Duplicate IDs
  - Negative or zero salaries
  - Missing referenced managers

- **Performance optimizations**  
  The current implementation is efficient for up to 1000 employees as per specification.  
  If scaling to much larger datasets is required, algorithms and data structures could be revisited (e.g., parallel processing for rule evaluation).

