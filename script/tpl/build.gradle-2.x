apply plugin: 'maven'
apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'

version = "1.1.1"

// Specify JDK version - may vary in different scenarios
sourceCompatibility = 1.8
targetCompatibility = 1.8

[compileJava, compileTestJava, javadoc]*.options*.encoding = 'UTF-8'
// In this section you declare where to find the dependencies of your project
repositories {
    mavenLocal()
    mavenCentral()
    maven { url "https://dl.bintray.com/ethereum/maven/" }
    maven {
        url "http://maven.aliyun.com/nexus/content/groups/public/"
    }
}

List lombok = [
    "org.projectlombok:lombok:1.16.14"
]

def logger_version = "2.1"
List logger = [
    "org.slf4j:jul-to-slf4j:1.7.10",
    "org.apache.logging.log4j:log4j-api:$logger_version",
    "org.apache.logging.log4j:log4j-core:$logger_version",
    "org.apache.logging.log4j:log4j-slf4j-impl:$logger_version",
    "org.apache.logging.log4j:log4j-web:$logger_version"
]

def spring_version = "4.1.8.RELEASE"
List spring = [
    "org.springframework:spring-core:$spring_version",
    "org.springframework:spring-beans:$spring_version",
    "org.springframework:spring-context:$spring_version",
    "org.springframework:spring-tx:$spring_version",
    "org.springframework:spring-jdbc:$spring_version",
    "org.springframework:spring-test:$spring_version"
]

List apache_commons = [
    "org.apache.commons:commons-collections4:4.1",
    "org.apache.commons:commons-lang3:3.5",
    "commons-cli:commons-cli:1.3.1"
]

List jcommander = [
	"com.beust:jcommander:1.72"
]

// In this section you declare the dependencies for your production and test code
List jackson = [
    "com.fasterxml.jackson.core:jackson-databind:2.8.8.1",
    "com.github.fge:json-schema-validator:2.2.6",
    "com.github.reinert:jjschema:1.16",
    "com.google.zxing:core:3.3.0"
]

List weidentity = [
    "com.webank:weidentity-java-sdk:1.3.0-rc.3",
]

// In this section you declare the dependencies for your production and test code
dependencies {
    compile logger, spring, lombok, apache_commons, jackson, weidentity, jcommander
}

sourceSets {
    main {
        java {
            srcDirs = ['src/main/java']
        }
        resources {
            srcDirs = ['src/main/resources']
        }
    }
}

jar {
    destinationDir file('dist/app')
    archiveName project.name + '-' + version + '.jar'

    exclude '**/*.xml'
    exclude '**/*.properties'

    doLast {
        copy {
            from configurations.runtime
            into 'dist/lib'
        }
    }
}
