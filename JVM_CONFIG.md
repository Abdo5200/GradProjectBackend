# JVM Configuration for Native Access

This project has been configured to handle the Java native access warning by enabling native access for all unnamed modules.

## Configuration Applied

### 1. Maven Configuration (pom.xml)
The Spring Boot Maven plugin has been configured with the JVM argument:
```xml
<jvmArguments>--enable-native-access=ALL-UNNAMED</jvmArguments>
```

This means when you run:
```bash
mvn spring-boot:run
```
The JVM argument will be automatically applied.

### 2. Production Deployment

#### Linux/Mac:
```bash
./run.sh
```

#### Windows:
```cmd
run.bat
```

#### Manual JAR execution:
```bash
java --enable-native-access=ALL-UNNAMED -jar target/gradproject-0.0.1-SNAPSHOT.jar
```

### 3. IDE Configuration

If you're running the application from your IDE (IntelliJ IDEA, Eclipse, VS Code), you need to add the JVM argument manually:

#### IntelliJ IDEA:
1. Go to `Run` → `Edit Configurations...`
2. Select your Spring Boot application configuration
3. In `VM options`, add: `--enable-native-access=ALL-UNNAMED`
4. Click `Apply` and `OK`

#### Eclipse:
1. Right-click on your project → `Run As` → `Run Configurations...`
2. Select your Spring Boot application
3. Go to `Arguments` tab
4. In `VM arguments`, add: `--enable-native-access=ALL-UNNAMED`
5. Click `Apply` and `Run`

#### VS Code:
Add to `.vscode/launch.json`:
```json
{
    "configurations": [
        {
            "type": "java",
            "request": "launch",
            "mainClass": "com.example.gradproject.GradProjectBackend",
            "vmArgs": "--enable-native-access=ALL-UNNAMED"
        }
    ]
}
```

## What This Fixes

This configuration eliminates the warning:
```
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::loadLibrary has been called by io.netty.util.internal.NativeLibraryUtil
```

The warning appears because Netty (used by Spring Boot) needs to load native libraries for optimal performance. Enabling native access allows this without warnings.

## Security Note

This configuration is safe for trusted libraries like Netty. The `ALL-UNNAMED` flag allows all code in unnamed modules (like your application) to access native methods, which is appropriate for Spring Boot applications.

