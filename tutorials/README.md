## Running the examples from the command line

You can just execute each tutorial from your IDE. In case you want to produce a jar with all the dependencies, do the following.

1. Package the project with the `complete` profile:

```shell
mvn -Pcomplete package
```
2. Run individual apps, such as `_00_HelloWorld`, with the following command:

```shell
java -cp ./target/tutorials-0.33.0-jar-with-dependencies.jar _00_HelloWorld "what is Java?"
```

## Running the examples as GraalVM native images

In case you want to produce a native executable version of your app, same command as above works with `native-image`:

```shell
native-image -cp ./target/tutorials-0.33.0-jar-with-dependencies.jar _00_HelloWorld -o native-helloworld

```

You can then run it as a native executable, and pass your prompt like before:

```shell
./native-helloworld "what is Java?"
```