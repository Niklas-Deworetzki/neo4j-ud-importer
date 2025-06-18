# Import UD-Treebanks into Neo4j Databases

This repository contains supplementary material for the publication

> Niklas Deworetzki, Peter Ljunglöf 2025. Graph Databases for Fast Queries in UD Treebanks. In: SyntaxFest 2025, Ljubljana Slovenia.

---

## Getting Started

To get started, you need an executable version of this tool, a [current Java installation](https://www.java.com/en/download/help/download_options.html), a running [instance of Neo4j](https://neo4j.com/docs/operations-manual/current/installation/) and some [UD corpus files](https://universaldependencies.org/).
Then it is as easy as running the following command, where `UD-CORPUS` should be replaced with the path to your corpus.
It can either be the path to a single `.conllu` file, or a directory containing arbitrarily many files.

```bash
java -jar neo4corpus.jar \
  --database neo4j \
  --host localhost \
  --port 7687 \
  UD-CORPUS
```

The provided options for database, host and port are actually the *default options* of this tool and for Neo4j.
So if you're running your instance locally, you can omit these options completely.

## Building

This project is configured and built via [Gradle](https://gradle.org/install/).
To build the project, it suffices to run the following command in this root directory.
Afterwards you will find an executable `.jar` file under the `build/libs` directory.

```bash
./gradlew jar
```

You will find the source code for the importer tool in [the source code directory](src/main/kotlin).

## Running Experiments

If you want to run the experiments presented in our paper or try your own experiments, feel free to read the instructions in the [experiments directory](experiments).
You will be required to build this project or download the current published executable.

## Running Neo4j in Docker

An easy way to get started is by running Neo4j in Docker.
The following command assumes that there are two directories `logs` and `data` in your current directory, which will be used to persist data.
**Note** that the contents of these directories will be written by the docker user, so you might need superuser permissions to modify these files after.

```bash
docker run --rm  \
  --env NEO4J_AUTH=none  \
  --env NEO4J_PLUGINS=\[\"apoc\"\]  \
  --env NEO4J_dbms_security_procedures_unrestricted=apoc.\\\*  \
  --publish 7474:7474  \
  --publish 7687:7687  \
  --mount type=bind,src=$(pwd)/logs,dst=/logs  \
  --mount type=bind,src=$(pwd)/data,dst=/data  \
  neo4j:latest
```

<details>
  <summary>Click here to see what the command does</summary>
  This command creates and runs a new docker container with the <code>neo4j:latest</code> image.<br/>
  After the command termintes (done by pressing Ctrl-C), the container will be removed from Docker, but its data will be persisted.<br/>
  The different <code>--mount</code> flags tell Docker, how to map directories from your filesystem into the container.
  This is done to persist data.<br/>
  The different <code>--publish</code> flags make the database instance accessible on your machine.<br/>
  And finally, the <code>--env</code> flags configure the database to have (1) no authorization, (2) enable the <emph>apoc</emph> plugins and (3) create optimized access for functions within the <em>apoc</em> plugins.
  These plugins are interacted with by the Neo4Corpus tool to make importing your corpus faster.
</details>

