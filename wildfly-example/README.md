Langchain4J WildFly Example
============================

This simple application is aiming to provide a demonstrator of the WildFly AI Feature Pack.

## Setting up Ollama

You will need to have either **docker** or **podman** installed.

To start Ollama and select the proper model (aka `llama3.1:8b) executethe following commands:

```shell
podman run -d --rm --name ollama --replace --pull=always -p 11434:11434 -v ollama:/root/.ollama --stop-signal=SIGKILL mirror.gcr.io/ollama/ollama

podman exec -it ollama ollama run llama3.1:8b
```
To quit the Ollama prompt, type **/bye**.

##  Provisioning using the [WildFly Maven Plugin](https://github.com/wildfly/wildfly-maven-plugin/)

This exaple remies on [WildFly Glow](https://docs.wildfly.org/wildfly-glow/) to configure and provision the server and the sample application using [WildFly Maven Plugin](http://github.com/wildfly/wildfly-maven-plugin) like this:

```xml
...
<configuration>
  <discoverProvisioningInfo>
    <spaces>
      <space>incubating</space>
    </spaces>
    <version>${version.wildfly.server}</version>
  </discoverProvisioningInfo>
  <name>ROOT.war</name>
  <packagingScripts>
    <packaging-script>
      <scripts>
        <script>./src/scripts/configure_logs.cli</script>
      </scripts>
    </packaging-script>
  </packagingScripts>
</configuration>
...
```

The JBoss CLI script configure the log level to make sure that Requests and Responses can be properly traced.

##  Building and running the example application

You build using Apache Maven with the following command:

```shell
mvn clean install
```
You can now start the server:
```shell
 ./target/server/bin/standalone.sh 
```
You can interact with the application using:
* a simple REST endpoint over the very miniaml AIService [ChatBot](http://localhost:8080/)

Once you have finished you can stop Ollama using:
```shell
podman stop ollama
```