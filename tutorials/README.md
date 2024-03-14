## Packaging as a fat jar

You can just execute each tutorial from your IDE. In case you want to produce a fat jar with all the dependencies, do the following.

1. Package the project with the `complete` profile:

```shell
mvn -Pcomplete package
```
2. Run individual apps, such as `_00_HelloWorld`, with the following command:

```shell
java -cp ./target/tutorials-0.28.0-jar-with-dependencies.jar _00_HelloWorld
```