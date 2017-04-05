# TROY
A TRainable Office secretarY design using the Soar cognitive architecture.

## Install and Build
### Prerequisites
* Java 1.8
* [MVN](http://maven.apache.org)
  * Your Maven `settings.xml` file must include an entry for the CHAT repository (see below)
* Soar - https://github.com/SoarGroup/Soar/tree/releases/9.5.1

### Steps
1. You must first install Soar libraries on your system.
  * Soar requires SWIG to build the libraries from source. Make sure SWIG is installed on your system.
  * Check out the Soar source code from https://github.com/SoarGroup/Soar/tree/releases/9.5.1
  * Build the Soar code by executing `python scons/scons.py all` from the top-level Soar directory.
  * Install the Soar libraries `libSoar.dylib` and `libJava_sml_ClientInterface.jnilib` on your system. They should be in the library search path Java uses on your system. On Mac machines you can place them in `~/Library/Java/Extensions`.
2. Compile Troy:
  * mvn compile
3. Execute in the console:
  * mvn exec:java

## Updating Maven Settings
Maven uses a `settigs.xml` for global configuration parameters. The file is
located in your Maven directory (e.g. ~/.m2/settings.xml). If you already 
have a `settings.xml` file include the following in it:
```
    <server>
    <id>chat-maven-repo-s3</id>
      <username>username</username>
      <password>password</password>
    </server>
```

You can get the correct values for the username and password from the CHAT
private Confluence site. If you need a  complete `settings.xml` file, it
should look like this:
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>

    <server>
    <id>chat-maven-repo-s3</id>
      <username>username</username>
      <password>password</password>
    </server>

  </servers>

</settings>
```

