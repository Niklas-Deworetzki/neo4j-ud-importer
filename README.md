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


## Running Experiments



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
  This command creates and runs a new docker container with the `neo4j:latest` image.
  After the command termintes (done by pressing Ctrl-C), the container will be removed from Docker, but its data will be persisted.
  The different `--mount` flags tell Docker, how to map directories from your filesystem into the container.
  This is done to persist data.
  The different `--publish` flags make the database instance accessible on your machine.
  And finally, the `--env` flags configure the database to have (1) no authorization, (2) enable the *apoc* plugins and (3) create optimized access for functions within the *apoc* plugins.
  These plugins are interacted with by the Neo4Corpus tool to make importing your corpus faster.
</details>

