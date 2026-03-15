# IntelliJ IDEA EnvFile Configuration
# Configuración de EnvFile para IntelliJ IDEA

## 📋 Setup Instructions / Instrucciones de Configuración

### Step 1: Install EnvFile Plugin

1. Open IntelliJ IDEA
2. Go to `File` → `Settings` (Windows/Linux) or `IntelliJ IDEA` → `Preferences` (macOS)
3. Navigate to `Plugins`
4. Click on `Marketplace` tab
5. Search for "EnvFile"
6. Click `Install` on "EnvFile" by Borys Pierov
7. Click `Restart IDE` to apply changes

### Step 2: Configure Run Configuration

1. Open your project in IntelliJ IDEA
2. Go to `Run` → `Edit Configurations...`
3. Select your Spring Boot application configuration (usually `KeyGoApplication` or similar)
4. If you don't have one, create it:
   - Click `+` → `Spring Boot`
   - Set Main class to: `io.cmartinezs.keygo.run.KeyGoApplication`
   - Set Module to: `keygo-run`

5. In the configuration window, go to the `EnvFile` tab
6. Check ✅ "Enable EnvFile"
7. Click `+` to add environment file
8. Navigate to: `keygo-supabase/.env`
9. Select the file and click OK
10. Make sure "Substitute environment variables" is checked
11. Click `Apply` and `OK`

### Step 3: Verify Configuration

1. Click on the dropdown next to the Run button
2. Ensure your Spring Boot configuration is selected
3. Click the Run button (green triangle)
4. Check the console output for loaded environment variables

## 🔄 Alternative: Using .idea/runConfigurations

You can also create a shared run configuration file that team members can use:

### File: .idea/runConfigurations/KeyGo_Supabase_Local.xml

```xml
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="KeyGo Supabase (Local)" type="SpringBootApplicationConfigurationType" factoryName="Spring Boot">
    <option name="ACTIVE_PROFILES" value="supabase,local" />
    <option name="ALTERNATIVE_JRE_PATH" />
    <module name="keygo-run" />
    <option name="SPRING_BOOT_MAIN_CLASS" value="io.cmartinezs.keygo.run.KeyGoApplication" />
    <extension name="net.ashald.envfile">
      <option name="IS_ENABLED" value="true" />
      <option name="IS_SUBST" value="true" />
      <option name="IS_PATH_MACRO_SUPPORTED" value="false" />
      <option name="IS_IGNORE_MISSING_FILES" value="false" />
      <option name="IS_ENABLE_EXPERIMENTAL_INTEGRATIONS" value="false" />
      <ENTRIES>
        <ENTRY IS_ENABLED="true" PARSER="runconfig" IS_EXEC="false" PATH="$PROJECT_DIR$/keygo-supabase/.env" />
      </ENTRIES>
    </extension>
    <method v="2">
      <option name="Make" enabled="true" />
    </method>
  </configuration>
</component>
```

## 💡 Tips / Consejos

1. **Switch Environments Easily**
   ```bash
   # From terminal in IntelliJ
   cd keygo-supabase
   ./scripts/switch-env.sh local   # or desa, prod
   ```
   Then restart your run configuration.

2. **View Loaded Variables**
   - After starting the app, go to `Run` → `View Breakpoints`
   - Or use actuator endpoint: `http://localhost:8080/keygo-server/actuator/env`

3. **Multiple Configurations**
   Create separate run configurations for each environment:
   - `KeyGo Supabase (Local)` → Uses `.env-local`
   - `KeyGo Supabase (Desa)` → Uses `.env-desa`
   - `KeyGo Supabase (Prod)` → Uses `.env-prod` (use with caution!)

4. **Debugging**
   Environment variables will be available in Debug mode as well.

## ⚠️ Common Issues / Problemas Comunes

### Issue: EnvFile tab not visible

**Solution:** Make sure the EnvFile plugin is installed and IDE is restarted.

### Issue: Variables not loading

**Solution:** 
1. Check that "Enable EnvFile" is checked
2. Verify the path to `.env` file is correct
3. Make sure `.env` file exists (run `./scripts/switch-env.sh local` first)
4. Try "Invalidate Caches / Restart" from `File` menu

### Issue: Old values still being used

**Solution:** 
1. Stop the application
2. Run `./scripts/switch-env.sh [environment]`
3. In IntelliJ, click `Build` → `Rebuild Project`
4. Start the application again

## 📝 .gitignore Considerations

The `.idea/` folder should generally be in `.gitignore` except for:
- `.idea/runConfigurations/` (shared run configs)
- `.idea/codeStyles/` (code style settings)

Make sure your `.gitignore` includes:
```
.idea/*
!.idea/runConfigurations/
!.idea/codeStyles/
```

---

**Plugin:** EnvFile by Borys Pierov  
**Plugin URL:** https://plugins.jetbrains.com/plugin/7861-envfile  
**Last Updated:** 2026-03-15

