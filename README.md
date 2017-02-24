[![Build Status](https://travis-ci.org/sul-dlss/ld4p-marc21-to-xml.svg?branch=master)](https://travis-ci.org/sul-dlss/ld4p-marc21-to-xml)
[![Coverage Status](https://coveralls.io/repos/github/sul-dlss/ld4p-marc21-to-xml/badge.svg?branch=master)](https://coveralls.io/github/sul-dlss/ld4p-marc21-to-xml?branch=master)
[![Dependency Status](https://gemnasium.com/badges/github.com/sul-dlss/ld4p-marc21-to-xml.svg)](https://gemnasium.com/github.com/sul-dlss/ld4p-marc21-to-xml)

# ld4p-marc21-to-xml
Convert marc21 data into marcxml, with authority ids resolved to URIs via Symphony

## Development

### Compiling and Executing

See One Time Setup below to set up dependencies

To compile and package the maven project:

  `mvn clean package`

The resulting packaged JAR at `java/target/xform-marc21-to-xml-jar-with-dependencies.jar` includes all dependencies.  The packaged JAR can be copied to a convenient location and used on the CLASSPATH or the command line, e.g.
```
$ cp java/target/xform-marc21-to-xml-jar-with-dependencies.jar ~/lib/ld4p_conversion.jar
$ LD4P_JAR=~/lib/ld4p_conversion.jar
$ java -cp ${LD4P_JAR} edu.stanford.MarcToXML -h
usage: edu.stanford.MarcToXML
 -h,--help               help message
 -i,--inputFile <arg>    MARC input file (binary .mrc file expected;
                         required)
 -l,--logFile <arg>      Log file output (default: log/MarcToXML.log)
 -o,--outputPath <arg>   MARC XML output path (default:
                         ENV["LD4P_MARCXML"])
 -r,--replace            Replace existing XML files (default: false)
```

### Code Coverage Reports

To run the tests and view a coverage report from the command line:
```
mvn clean cobertura:cobertura
ls -l target/site/cobertura/
firefox target/site/cobertura/index.html
```

The [Travis CI](https://travis-ci.org/sul-dlss/ld4p-marc21-to-xml) builds run tests and submit
a coverage report to [Coveralls](https://coveralls.io/github/sul-dlss/ld4p-marc21-to-xml).
To update Coveralls from the command line, try:

  `mvn clean test cobertura:cobertura coveralls:report -DrepoToken=yourcoverallsprojectrepositorytoken`

## Deployment

Capistrano is used for deployment.

1. On your laptop, run

    `bundle`

  to install the Ruby capistrano gems and other dependencies for deployment.

2. Set up shared directories on the remote VM:

    ```
    cd ld4p-marc21-to-xml
    mkdir shared
    mkdir shared/log
    ```

3. Deploy code to remote VM:

    `cap dev deploy`

  This will also build and package the code on the remote VM with Maven.

4. Run a test marc21 file through the converter to ensure it works on remote VM:

    `cap dev deploy:run_test`

## One Time Setup

Dependencies
- Java 8
- Maven 3
- Oracle maven artifact access (see below)

The Oracle JDBC maven artifacts require a license, follow the instructions at:
- http://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm
- https://blogs.oracle.com/dev2dev/entry/oracle_maven_repository_instructions_for

Once the Oracle sign-up/sign-in and license agreement is accepted, add the sign-in
credentials to maven settings.  Follow maven instructions to encrypt the passwords, see
- https://maven.apache.org/guides/mini/guide-encryption.html
  - encrypt a maven master password:

          $ mvn --encrypt-master-password
          Master password: TYPE_YOUR_PASSWD_HERE
          {L+bX9REL8CAH/EkcFM4NPLUxjaEZ6nQ79feSk+xDxhE=}

  - add the encrypted maven master password to `~/.m2/settings-security.xml` in a block like:

          <settingsSecurity>
              <master>{L+bX9REL8CAH/EkcFM4NPLUxjaEZ6nQ79feSk+xDxhE=}</master>
          </settingsSecurity>

  - encrypt oracle server password:

          $ mvn --encrypt-password
          Password: TYPE_YOUR_PASSWD_HERE
          {JhJfPXeAJm0HU9VwsWngQS5qGreK29EQ3fdm/7Q7A7c=}

  - add this encrypted oracle server password to `~/.m2/settings.xml` as a `server` element using this template:

          <settings>
            <servers>
              <server>
                <id>maven.oracle.com</id>
                <username>your_oracle_username</username>
                <password>{JhJfPXeAJm0HU9VwsWngQS5qGreK29EQ3fdm/7Q7A7c=}</password>
                <configuration>
                  <basicAuthScope>
                    <host>ANY</host>
                    <port>ANY</port>
                    <realm>OAM 11g</realm>
                  </basicAuthScope>
                  <httpConfiguration>
                    <all>
                      <params>
                        <property>
                          <name>http.protocol.allow-circular-redirects</name>
                          <value>%b,true</value>
                        </property>
                      </params>
                    </all>
                  </httpConfiguration>
                </configuration>
              </server>
            </servers>
          </settings>

- For additional information about maven settings, see
    - https://maven.apache.org/settings.html
    - https://books.sonatype.com/nexus-book/reference/_adding_credentials_to_your_maven_settings.html
