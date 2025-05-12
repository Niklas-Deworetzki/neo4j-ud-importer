# Running Experiments using Neo4j as a Corpus Search Tool

This directory contains scripts and measurements for running Neo4j as a corpus search tool and running benchmarks on it.

Dependencies:
1. Python dependencies are listed in [requirements.txt](experiments/requirements.txt).
2. An executable JAR file named `neo4jud.jar` must be present in this directory. It is obtained by building the main project and moving the build artifact here.
3. The tools `make`, `git` and `docker` are required for various scripts.

# Downloading Example UD Corpora

To download the set of UD corpora used in our evaluations, you can run the following command to download all corpus files and set them up for the respective corpus tools.

```
make corpora
```

# Encoding Grew Corpora

To let Grew index all downloaded corpora, you can run the following command:

```
make grew
```

This will start up a grew instance serving all corpora. Press Ctrl-C to stop the server, after it has started.
The first start will take some time, as Grew automagically encodes all corpora.
Note that the first start will require around 20GB of memory.

# Encoding Neo4j Corpora

To automatically encode all downloaded corpora using both encoding strategies available for Neo4j, you can run the following command:

```
make neo4j
```

Running this command will build a total of 20 Neo4j-databases (10 langauges * 2 encoding strategies).
This will take some time and storage space.
But the entire process is automated, spawning a Docker container for each database, running the JAR tool to import data and then stopping the Docker container.
Once execution completes, a CSV file `encoding-<TIMESTAMP>.csv` will be present, showing details about the encoded corpora.

# Running Benchmarks

To automatically run the benchmark suite, run the following command:

```
make benchmark
```

This will run the file `run_queries.py`, executing the set of queries present in the [queries directory](experiments/queries) on the previously encoded corpora.
**Note** that you are required to fulfil all dependencies before running this command and that you need to manually `make neo4j` and `make grew` first.

After the benchmarks have been completed, you can find the results in the [results](experiments/results) directory.
There should be a sub-directory present with the timestamp of when you started execution of the benchmarks.


