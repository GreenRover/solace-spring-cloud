# Solace Spring Boot

## Overview

This project includes and builds all the Solace starters for Spring Boot.
This includes the Java (JCSMP) starter and the JMS starter.

## Table of contents
* [Quickstart Guide](#quickstart-guide)
    * [Quickstart Guide - Spring Cloud Version Compatibility](#quickstart-guide---spring-cloud-version-compatibility)
    * [Quickstart Guide - Cloud Stream Binder](#quickstart-guide---cloud-stream-binder)
    * [Quickstart Guide - Cloud Connector](#quickstart-guide---cloud-connector)
* [Building Locally](#building-locally)
    * [Maven Project Structure](#maven-project-structure)
* [Additional Information](#additional-information)
    * [Solace Java Spring Boot Starter](#solace-java-spring-boot-starter)
    * [Solace JMS Spring Boot Starter](#solace-jms-spring-boot-starter)
    * [Solace Java CFEnv](#solace-java-cfenv)
* [Additional Meta-Information](#additional-meta-information)
    * [Contributing](#contributing)
    * [Authors](#authors)
    * [License](#license)
    * [Support](#support)
    * [Resources](#resources)
---

## Quickstart Guide

TODO

To get started, we need to pull in 2 dependencies:
1. 
2. `solace-spring-cloud-bom`

Once these dependencies are declared, we can automatically autowire Solace Spring Boot beans.

### Quickstart Guide - Spring Cloud Version Compatibility

The `solace-spring-boot-bom` will guarantee that the versions of the Solace Spring Boot starters and autoconfigurations are what works with your version of Spring Boot.
Consult the table below to determine what version of the BOM you need for your version of Spring Boot.

| Spring Cloud         |Solace Spring Cloud BOM|Spring Boot      |Solace Spring Boot BOM|
|----------------------|-----------------------|-----------------|----------------------|
|Hoxton.RC1            |1.0.0                  | 2.2.0           |1.0.0                 |
|Hoxton.RELEASE        |                       | 2.2.1           |                      |
|                      |                       | 2.2.2-SNAPSHOT  |                      |

Note that since Spring Cloud depends on Spring Boot, the Spring Boot BOM will be included implicitly by default.


### Quickstart Guide - Cloud Stream Binder

TODO

### Quickstart Guide - Cloud Connector

TODO

#### Maven Quickstart
```xml
    <!-- Add me to your POM.xml -->
    <properties>
        <spring.cloud.version>Hoxton.RELEASE</spring.cloud.version>

        <!-- Consult the README versioning table -->
        <solace.spring.cloud.version>1.0.0</solace.spring.cloud.version>
    </properties>

    <dependencyManagement>
        <groupId>com.solace.spring.cloud</groupId>
        <artifactId>solace-spring-cloud-bom</artifactId>
        <version>${solace.spring.cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependencyManagement>
```

#### Gradle 4 Quickstart
```groovy
    /* Add me to your build.gradle */
    buildscript {
        ext {
            springCloudVersion = 'Hoxton.RELEASE'
                                                 
            // Consult the README versioning table
            solaceSpringCloudBomVersion = '1.0.0'
        }
        dependencies {
            classpath 'io.spring.gradle:dependency-management-plugin:1.0.8.RELEASE'
        }
    }

    apply plugin: 'io.spring.dependency-management'

    dependencyManagement {
        imports {
            mavenBom "com.solace.spring.cloud:solace-spring-cloud-bom:${solaceSpringCloudBomVersion}"
        }
    }
```

Note: Gradle 4 isn't natively compatible with Maven BOM's. Thus, we have to use the Spring's dependency management plugin.

#### Gradle 5 Quickstart
```groovy
    /* Add me to your build.gradle */
    buildscript {
        ext {
            springCloudVersion = 'Hoxton.RELEASE'

            // Consult the README versioning table
            solaceSpringCloudBomVersion = '1.0.0'
        }
    }
    
    dependencies {
        implementation(platform("com.solace.spring.cloud:solace-spring-cloud-bom:${solaceSpringCloudBomVersion}"))
    }
```

## Building Locally

To build the artifacts locally, simply clone this repository and run `mvn package` at the root of the project.
This will build everything.

```bash
git clone https://github.com/SolaceProducts/solace-spring-cloud.git
cd solace-spring-cloud
mvn package
```

If you want to install the latest versions of all the artifacts locally, you can also run a 'mvn install'
```bash
git clone https://github.com/SolaceProducts/solace-spring-cloud.git
cd solace-spring-cloud
mvn install
```

### Maven Project Structure

```
solace-spring-cloud-build (root)
<-- solace-spring-cloud-bom
<-- solace-spring-cloud-parent 
    <-- solace-spring-cloud-connector
    <-- solace-spring-cloud-stream-binder [spring-cloud-stream-binder-solace-core]
    <-- solace-spring-cloud-stream-starter [spring-cloud-starter-stream-solace]
    <-- solace-spring-cloud-stream-autoconfigure [spring-cloud-stream-binder-solace]

Where <-- indicates the parent of the project
```

All subprojects are included as modules of solace-spring-boot-build. Running `mvn install` at the root of the project will install all subprojects.

#### solace-spring-cloud-build

This POM defines build-related plugins and profiles that are inherited by the BOM as well as the starters and autoconfiguration.
The version of this POM should match the version of Spring Boot and Spring Cloud that the build will target.

Please do not put dependency related properties here - they belong in solace-spring-cloud-parent. The exceptions to this, naturally, are the versions of the Solace starters as well as the version of Spring Boot this build targets.
If it shouldn't be inherited by the BOM, it doesn't go here.

#### solace-spring-cloud-parent

This POM defines common properties and dependencies for the Spring Cloud starters and autoconfigurations. 

If a starter or autoconfiguration shares a dependency with another starter, it is a good idea to specify it as a property in this POM to keep things tidy. It would not be beneficial to have two versions of a common library be included in the starter if a common version works with both.

#### solace-spring-cloud-bom

The BOM (Bill of Materials) defines the exact version of starters to use for a specific version of Spring Boot and Spring Cloud. This is done to ensure compatibility with that specific version of Spring Boot, and to make version management easier for Spring Cloud applications.

#### solace-spring-cloud-stream-starter [spring-cloud-starter-stream-solace]

Spring Cloud starter for Cloud Streams. It includes its respective autoconfiguration as a dependency.

#### solace-spring-cloud-stream-autoconfigure [spring-cloud-stream-binder-solace]

Spring Cloud autoconfiguration.

#### solace-spring-cloud-connector

This project is a Spring cloud connector for connecting to Solace PubSub+ services.

#### solace-spring-cloud-stream-binder [spring-cloud-stream-binder-solace-core]

This project is the implementation of the Spring Cloud binder for Solace PubSub+ services.

## Additional Information

You can find additional information about each of the projects in their respective README's.

## Additional Meta-Information

### Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on the process for submitting pull requests to us.

### Authors

See the list of [contributors](https://github.com/SolaceProducts/solace-spring-cloud/graphs/contributors) who participated in this project.

### License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

### Code of Conduct
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v1.4%20adopted-ff69b4.svg)](CODE_OF_CONDUCT.md)
Please note that this project is released with a Contributor Code of Conduct. By participating in this project you agree to abide by its terms.

### Support

#### Support Email
support@solace.com

#### Solace Developer Community
https://solace.community

### Resources

For more information about Spring Boot Auto-Configuration and Starters try these resources:
- [Spring Docs - Spring Boot Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#using-boot-auto-configuration)
- [Spring Docs - Developing Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#boot-features-developing-auto-configuration)
- [GitHub Tutorial - Master Spring Boot Auto-Configuration](//github.com/snicoll-demos/spring-boot-master-auto-configuration)

For more information about Cloud Foundry and the Solace PubSub+ service these resources:
- [Solace PubSub+ for Pivotal Cloud Foundry](http://docs.pivotal.io/solace-messaging/)
- [Cloud Foundry Documentation](http://docs.cloudfoundry.org/)
- For an introduction to Cloud Foundry: https://www.cloudfoundry.org/

For more information about Spring Cloud try these resources:
- [Spring Cloud](http://projects.spring.io/spring-cloud/)
- [Spring Cloud Connectors](http://cloud.spring.io/spring-cloud-connectors/)
- [Spring Cloud Connectors Docs](http://cloud.spring.io/spring-cloud-connectors/spring-cloud-connectors.html)
- [Spring Cloud Connectors GitHub](https://github.com/spring-cloud/spring-cloud-connectors)

For more information about Solace technology in general please visit these resources:

- The Solace Developer Portal website at: https://solace.dev

```
.......................HELLO FROM THE OTTER SIDE...........
............................www.solace.com.................
...........................................................
...........................@@@@@@@@@@@@@@@@@@@.............
........................@@                    @@...........
.....................@      #              #     @.........
....................@       #              #      @........
.....................@          @@@@@@@@@        @.........
......................@        @@@@@@@@@@@      @..........
.....................@           @@@@@@@         @.........
.....................@              @            @.........
.....................@    \_______/   \________/ @.........
......................@         |       |        @.........
.......................@        |       |       @..........
.......................@         \_____/       @...........
....@@@@@...............@                      @...........
..@@     @...............@                     @...........
..@       @@.............@                     @...........
..@        @@............@                     @...........
..@@        @............@                     @...........
....@       @............@                      @..........
.....@@     @...........@                        @.........
.......@     @.........@                          @........
........@     @........@                           @.......
........@       @@@@@@                              @......
.........@                                            @....
.........@                                             @...
..........@@                                           @...
............@                                          @...
.............@                              @          @...
...............@                             @         @...
.................@                            @        @...
..................@                            @       @...
...................@                           @       @...
...................@                           @       @...
...................@                          @        @...
...................@                         @        @....
..................@                         @         @....
..................@                        @         @.....
..................@                       @          @.....
..................@                       @         @@.....
..................@                        @       @ @.....
..................@                          @@@@@   @.....
..................@                                  @.....
..................@                                  @.....
```