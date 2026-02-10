# Native Image Plugin for Amper

An [Amper](https://github.com/JetBrains/amper) plugin that builds GraalVM native images from JVM applications.

## How It Works

The plugin defines two tasks:

1. **`provision`** — Locates or downloads a GraalVM installation. Checks `GRAALVM_HOME` and `JAVA_HOME` environment
   variables first; if neither points to a GraalVM with `native-image`, it provisions one automatically using Amper's
   JDK provisioning.

2. **`buildNativeImage`** — Runs `native-image` to compile the application JAR and its runtime classpath into a
   standalone native executable.

`provision` depends on `buildNativeImage`.

## Plugin Settings

Configure the plugin in your `module.yaml`:

```yaml
product: jvm/app

plugins:
  native-image-plugin:
    enabled: true
    mainClass: MainKt           # Required — fully qualified main class
    # version: "25"             # GraalVM major version (default: 25)
    # distribution: ORACLE      # ORACLE or COMMUNITY (default: ORACLE)
```

| Setting        | Required | Default  | Description                                   |
|----------------|----------|----------|-----------------------------------------------|
| `mainClass`    | Yes      | —        | Fully qualified name of the main class        |
| `version`      | No       | `25`     | GraalVM major Java version                    |
| `distribution` | No       | `ORACLE` | GraalVM distribution: `ORACLE` or `COMMUNITY` |

## Building

Build the native image:

```sh
./amper :<moduleName>:buildNativeImage@native-image-plugin
```

The native executable is produced in `build/tasks/_<moduleName>_buildNativeImage@native-image-plugin/nativeImage`.
