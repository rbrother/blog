# IntelliJ IDEA + Cursive Setup Guide

## Issue Fixed

The "Cannot resolve symbol 'defn'" error was caused by Cursive using "IDE" resolve mode instead of "Leiningen" mode.

**Fixed:** Changed `.idea/ClojureProjectResolveSettings.xml` to use LEININGEN scheme.

## Steps to Complete Setup in IntelliJ

### 1. Restart IntelliJ IDEA
After the change to ClojureProjectResolveSettings.xml, restart IntelliJ to pick up the changes.

### 2. Sync Leiningen Project
- Go to **Tools** → **Leiningen** → **Sync Project**
- Or right-click on `project.clj` → **Leiningen** → **Sync Project**

This will:
- Download all Clojure/ClojureScript dependencies
- Configure the project classpath
- Resolve all symbols like `defn`, `ns`, etc.

### 3. Verify Setup
Open a Clojure file (e.g., `src/brotherus/blog/core.cljs`) and check:
- ✅ `defn` should be highlighted and resolvable
- ✅ `ns` should be recognized
- ✅ Auto-completion should work
- ✅ No red underlines on standard Clojure forms

### 4. Configure REPL (Optional but Recommended)

#### For Shadow-CLJS REPL:
1. Go to **Run** → **Edit Configurations**
2. Click **+** → **Clojure REPL** → **Remote**
3. Name: "Shadow-CLJS REPL"
4. Host: localhost
5. Port: 8777 (from shadow-cljs.edn)
6. Click **OK**

#### To Start REPL:
```bash
# In terminal:
npm run watch
```

Then in IntelliJ:
- **Run** → **Shadow-CLJS REPL**
- Or use the Run button in toolbar

### 5. Project Structure Settings

Verify these are correct:
1. **File** → **Project Structure**
2. **Project**:
   - SDK: Java 21 (or your installed version)
   - Language level: SDK default
3. **Modules**:
   - Should show `brotherus-blog`
   - Source folders: `src`, `resources`
   - Test folders: `test`

### 6. Build Tools Configuration

Your project uses:
- **Leiningen** (for dependency management & IntelliJ integration)
- **Shadow-CLJS** (for actual ClojureScript compilation)
- **npm** (for JavaScript tooling)

All three work together:
- `project.clj` - tells IntelliJ what dependencies exist
- `shadow-cljs.edn` - tells Shadow-CLJS how to build
- `package.json` - npm scripts for build commands

## Troubleshooting

### If symbols still don't resolve:

1. **Invalidate Caches**:
   - **File** → **Invalidate Caches...** → **Invalidate and Restart**

2. **Re-sync Leiningen**:
   - Delete `.idea/` directory
   - Reopen project in IntelliJ
   - Let it re-import from `project.clj`

3. **Check Dependencies**:
   ```bash
   lein deps :tree
   ```
   Should show all dependencies without errors

4. **Verify Cursive is Enabled**:
   - **File** → **Settings** → **Plugins**
   - Ensure "Cursive" plugin is installed and enabled

### If REPL won't connect:

1. Ensure Shadow-CLJS is running:
   ```bash
   npm run watch
   ```

2. Check nREPL port in `shadow-cljs.edn`: 8777

3. Verify REPL configuration matches port

## Common Commands

```bash
# Build for development
npm run watch

# Build for production (Lambda)
npm run release-lambda

# Deploy to AWS
./deploy-lambda.sh --skip-infra

# Run tests (if you add them)
lein test
```

## File Locations

- Clojure/ClojureScript source: `src/`
- Tests: `test/`
- Resources: `resources/`
- Build output: `target/`
- Lambda deployment package: `target/lambda/`

## Current Status

✅ Leiningen installed: 2.10.0
✅ Java SDK: 21
✅ Dependencies downloaded
✅ Project configuration: Fixed (LEININGEN mode)
✅ Ready for development!

## Notes

- The `project.clj` is a "dummy" file primarily for IntelliJ
- `shadow-cljs.edn` is the actual build configuration
- This is normal for Shadow-CLJS projects
- Both files need to be maintained with dependencies

