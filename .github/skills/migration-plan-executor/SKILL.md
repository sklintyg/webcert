---
name: migration-plan-executor
description: Execute a detailed, sub-stepped implementation plan (such as migration/detailed-plan-step-13.md) safely, thoroughly, and verifiably. Use this when following a multi-step migration or refactoring plan where completeness, ordering, and continuous verification matter — and where the result must be a runnable, deployable application.
license: MIT
---

# Migration plan executor skill

Use this skill when the task is to **follow a detailed implementation plan to completion**,
where:
- The plan is divided into numbered sub-steps.
- Each sub-step is well-defined (describes what to change, why, and what the result looks like).
- The codebase must remain buildable and deployable after **every** sub-step — not just at the end.
- Nothing may be silently skipped or partially implemented.

## Core mandate

> **Every sub-step must be implemented in full. The application must compile, pass tests, and
> start successfully after the final sub-step. If any sub-step cannot be completed cleanly,
> STOP, report the blocker, and wait for instructions — do not skip forward.**

## Execution workflow

### Phase 0 — Read and load the plan

1. Read the entire plan document before writing a single line of code.
2. Parse the **Progress Tracker** table to get the canonical ordered list of sub-steps.
3. Load all sub-steps into the session SQL `todos` table:

```sql
INSERT INTO todos (id, title, description) VALUES
  ('13.1', 'Add Spring Boot plugin', 'Full description from plan...'),
  ('13.2', 'Switch WAR to JAR',      '...'),
  ...;
```

4. Insert dependency edges for any sub-steps that must complete before others:

```sql
INSERT INTO todo_deps (todo_id, depends_on) VALUES
  ('13.3', '13.1'),
  ('13.3', '13.2'),
  ...;
```

5. Summarize the plan back to the user:
   - Total number of sub-steps
   - Which sub-steps touch build files (Gradle/Maven)
   - Which sub-steps delete files
   - Which sub-steps are marked ⚠️ Critical
   - The verification sub-step (usually the last one)

6. Ask the user: **"Shall I start from sub-step N.1, or resume from a specific step?"**

### Phase 1 — Execute sub-steps one at a time

For each sub-step:

1. **Mark `in_progress`** in the todos table before touching any file.
2. **Re-read the sub-step** in full from the plan document. Do not rely on memory.
3. **Make the changes** described:
   - Create, edit, rename, or delete files exactly as specified.
   - Do not add functionality not described in the plan.
   - Do not omit functionality described in the plan.
4. **Run the micro-verification** (see below) immediately after each sub-step.
5. **Mark `done`** only after verification passes.
6. **Update the plan's Progress Tracker** in the markdown file: change `⬜ TODO` to `✅ DONE`.

### Phase 2 — Micro-verification after each sub-step

The intensity of verification scales with the sub-step's risk level.

| Sub-step type | Minimum verification |
|---|---|
| Build file change (Gradle/Maven) | `./gradlew help` — confirms Gradle can parse the build files |
| New/renamed Java file | `./gradlew compileJava` — confirms the file compiles |
| Deleted Java file | `./gradlew compileJava` + grep for remaining references |
| Filter/servlet/security config | `./gradlew test` — functional tests may catch wiring errors |
| ⚠️ Critical sub-step | `./gradlew build` — full build including all tests |
| Final verification sub-step | `./gradlew build` + manual startup check (see Phase 3) |

**Reference check after every deletion:**
Before deleting any file, grep the entire codebase for its class name and all import references.
If any reference remains after deletion, fix it before proceeding.

```powershell
# Example: confirm no references remain after deleting ApplicationInitializer.java
Select-String -Path "**\*.java" -Pattern "ApplicationInitializer" -Recurse
```

### Phase 3 — Final end-to-end verification

After the last sub-step (the plan's verification sub-step), run the full verification sequence
described in the plan. At minimum:

1. **Full build:**
   ```bash
   ./gradlew clean build
   ```
   Expected: zero compilation errors, all tests pass.

2. **Startup check:**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=<profiles>'
   ```
   or equivalent. Watch for:
   - `Started <Application> in N seconds` — context loaded successfully.
   - No `UnsatisfiedDependencyException`, `NoSuchBeanDefinitionException`, or `BeanDefinitionOverrideException`.
   - No `Cannot determine embedded database driver class` or similar startup failures.

3. **Smoke test key endpoints** (if applicable):
   - `GET /actuator/health` → `{"status":"UP"}`
   - `GET /api/...` → expected response (not 500)

4. **Report results:**
   - All sub-steps completed: ✅
   - Build: ✅ / ❌
   - Tests: ✅ / ❌ (N passed, M failed)
   - Startup: ✅ / ❌
   - Any warnings or known deferred issues

## Handling blockers

If a sub-step cannot be completed as described:

1. **STOP immediately.** Do not skip the sub-step or work around it silently.
2. **Describe the blocker** precisely:
   - Which sub-step is blocked
   - What the plan says should happen
   - What the actual state of the code is
   - Why the two are incompatible
3. **Offer options** to the user:
   - Fix the discrepancy (update the plan or the code)
   - Skip with explicit acknowledgement (mark as `blocked`)
   - Revert to the last known-good state

```sql
UPDATE todos SET status = 'blocked',
  description = description || ' [BLOCKED: <reason>]'
WHERE id = '<step-id>';
```

Never silently skip a ⚠️ Critical sub-step. These are marked critical precisely because
skipping them causes failures that are hard to diagnose later.

## File operation discipline

### Before creating a file
- Confirm the target path does not already exist.
- Confirm the parent directory exists.
- Confirm the package declaration matches the directory path.

### Before editing a file
- Read the current state of the file.
- Verify the `old_str` you intend to replace is exactly present.
- Make only the changes described in the sub-step — do not opportunistically refactor
  surrounding code.

### Before deleting a file
Run ALL of these checks:

```powershell
# 1. Find all Java import references
Select-String -Path "**\*.java" -Pattern "import.*<ClassName>" -Recurse

# 2. Find all @Import references
Select-String -Path "**\*.java" -Pattern "@Import.*<ClassName>" -Recurse

# 3. Find all XML/resource references
Select-String -Path "**\*.xml","**\*.properties","**\*.yml" -Pattern "<ClassName>" -Recurse
```

Only delete once all references have been cleaned up or are confirmed to also be deleted
in this same sub-step.

### Before renaming a class
1. Run a project-wide grep for the old class name.
2. Update ALL import statements across ALL modules.
3. Update ALL `@Import({...})` annotations that reference the old name.
4. Update ALL test classes that reference the old name.
5. Rename the file.
6. Compile to confirm.

## Progress tracking

Use the session SQL database to track every sub-step:

```sql
-- Before starting a sub-step
UPDATE todos SET status = 'in_progress' WHERE id = '13.3';

-- After completing and verifying
UPDATE todos SET status = 'done' WHERE id = '13.3';

-- Query what's next
SELECT t.* FROM todos t
WHERE t.status = 'pending'
AND NOT EXISTS (
  SELECT 1 FROM todo_deps td
  JOIN todos dep ON td.depends_on = dep.id
  WHERE td.todo_id = t.id AND dep.status != 'done'
)
ORDER BY t.id LIMIT 1;
```

Also update the **Progress Tracker table** in the plan's markdown file after each sub-step
(change `⬜ TODO` → `✅ DONE` or `🔴 BLOCKED`).

## Risk-aware execution

### ⚠️ Critical sub-steps
Sub-steps marked ⚠️ Critical in the plan (or that involve):
- Bean conflicts that prevent application startup
- Security configuration changes
- Filter chain ordering
- Class deletions that remove the application entry point

...require a **full `./gradlew build`** immediately after completion — not just `compileJava`.

### Deletion sub-steps
Never delete a file without:
1. Confirming it is truly dead code (no live references).
2. Confirming all its responsibilities have been migrated (use the plan's pre-deletion
   checklist if one exists).

### Multi-module changes
When a change spans multiple Gradle modules (e.g., moving a class from `redis-cache` to `web`):
1. Add the new file first.
2. Update all import references to point to the new location.
3. Compile to verify all modules resolve correctly.
4. Delete the old file only after step 2 and 3 pass.

## Completion criteria

The plan execution is complete when ALL of the following are true:

- [ ] Every sub-step in the Progress Tracker is marked `✅ DONE`
- [ ] `./gradlew clean build` exits with code 0 (zero test failures)
- [ ] The application starts without errors under the target profile set
- [ ] No compilation warnings introduced by the migration remain unaddressed
- [ ] The plan's own verification sub-step has been executed and passed

If any criterion is not met, the work is **not done** — even if all code changes have been
made. The application must be runnable and deployable.

## Reporting

After every 3–5 sub-steps (or after any Critical sub-step), provide a concise status update:

```
✅ 13.1 — Spring Boot Gradle plugin added
✅ 13.2 — WAR → JAR, bootJar task verified
✅ 13.3 — RehabstodApplication created, compiles
⏳ 13.4 — In progress: removing @EnableWebMvc from WebConfig
```

At the end, provide a full completion report:

```
## Step 13 — Execution complete

Sub-steps: 17/17 ✅
Build: ./gradlew clean build → SUCCESS (0 failures)
Tests: 312 passed, 0 failed
Startup: Started RehabstodApplication in 8.4 seconds
Warnings: none

Known deferred items:
- redis-cache module dissolution → Step 17
- HsaConfig rename in integration module → separate cleanup
```

## Anti-patterns to avoid

| Anti-pattern | Why it breaks the plan |
|---|---|
| Skipping a sub-step because it "seems obvious" | Plans are written to be complete; skipping causes silent regressions |
| Implementing future steps' work while doing the current step | Each step is designed to leave the app deployable; over-stepping breaks that guarantee |
| Merging sub-step 13.Xa and 13.Xb into a single change | Sub-steps split for a reason (risk isolation, verification checkpoints) |
| Ignoring a build warning introduced by the migration | Warnings often become errors in CI or later compiler versions |
| Assuming a deleted file has no references | Always grep; compilers only catch imports, not reflection or XML references |
| "Fixing" unrelated code while doing a migration sub-step | Violates the single-concern principle; makes rollback harder |
| Moving directly to the verification sub-step without doing all prior steps | The verification sub-step validates the sum of all changes, not a shortcut |

## Example prompts

- `Use /migration-plan-executor to implement migration/detailed-plan-step-13.md starting from sub-step 13.1`
- `Use /migration-plan-executor to resume migration/detailed-plan-step-13.md from sub-step 13.6`
- `Use /migration-plan-executor to execute the next pending sub-step in migration/detailed-plan-step-13.md`
- `Use /migration-plan-executor to run the final verification for migration/detailed-plan-step-13.md`
