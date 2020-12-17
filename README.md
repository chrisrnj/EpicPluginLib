# EpicPluginLib  [![Travis Status](https://travis-ci.org/Epicnicity322/EpicPluginLib.svg?branch=master)](https://travis-ci.org/Epicnicity322/EpicPluginLib)

A library that helps you manage the basic functionality of your plugin more easily. This library can read and write yaml
configurations, send messages, handle commands, update, log errors, access reflection (only bukkit), and log messages to
console.

Although this was made for bukkit and sponge plugins, this library can also read and write yaml configurations and log
errors for any java program.

## Gradle/maven/sbt/leiningen dependency

EpicPluginLib can be added as dependency through [jitpack](https://jitpack.io/#Epicnicity322/EpicPluginLib) repository.
If you are using maven, add this repository and dependency. Latest available
version: [![](https://jitpack.io/v/Epicnicity322/EpicPluginLib.svg)](https://jitpack.io/#Epicnicity322/EpicPluginLib)

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Epicnicity322</groupId>
        <artifactId>EpicPluginLib</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Compiling

#### Pre requirements:

* Terminal or [Git bash](https://git-scm.com/downloads)
* Maven

#### Procedure:

1. [Install maven](https://maven.apache.org/install.html)
2. Clone this repository to your computer `git clone https://github.com/Epicnicity322/EpicPluginLib.git`
3. Enter the created folder `cd EpicPluginLib`
4. Run maven `mvn clean install`

A folder named target will be created, inside it there will be EpicPluginLib.jar.

## Usage

All the documentation about class constructors is on the wiki.

You can shade this library in your program if you want, but some methods can only work if you are running the jar
separately.

#### Bukkit

To add this library as a dependency to your bukkit plugin, add this to your plugin.yml:

```yaml
depend: [EpicPluginLib]
```

#### Sponge

To add this library as a dependency to your sponge plugin, add this to your @Plugin annotation:

```java
dependencies = @Dependency(id = "epicpluginlib")
```
