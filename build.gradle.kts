plugins {
	`java-library`
	`maven-publish`
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
}

// Disable bootJar task since this is a library
tasks.bootJar {
	enabled = false
}

// Enable plain jar
tasks.jar {
	enabled = true
	archiveClassifier = ""
}

val groupId = "com.github.thinhnk55"
val artifactId = "kha-be-common"
val versionId = "1.0.8"

group = groupId
version = versionId

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.flywaydb:flyway-core:11.8.2")
	runtimeOnly("org.flywaydb:flyway-database-postgresql:11.8.2")
	runtimeOnly("org.postgresql:postgresql:42.7.5")
	implementation("io.hypersistence:hypersistence-utils-hibernate-60:3.9.4")


	compileOnly("org.projectlombok:lombok:1.18.38")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
	annotationProcessor("org.projectlombok:lombok:1.18.38")
	implementation("org.mapstruct:mapstruct:1.6.3")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

	implementation("com.nimbusds:nimbus-jose-jwt:10.3")
	implementation("org.bouncycastle:bcprov-jdk18on:1.80")
	implementation("org.bouncycastle:bcpkix-jdk18on:1.80")
	implementation("org.casbin:jcasbin:1.81.0")
	// unit test
	testImplementation("org.mockito:mockito-core:5.12.0")
	testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			groupId = groupId   // Sử dụng biến cho groupId
			artifactId = artifactId  // Sử dụng biến cho artifactId
			version = versionId  // Sử dụng biến cho version
		}
	}
	repositories {
		maven {
			url = uri("https://jitpack.io")
		}
	}
}

java {
	withSourcesJar()
	withJavadocJar()
}

tasks.javadoc {
	options {
		(this as StandardJavadocDocletOptions).addStringOption("Xdoclint:all,-missing", "-quiet")
	}
}
