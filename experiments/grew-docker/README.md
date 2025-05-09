# Docker Image for Grew-match

This `Dockerfile` provides a docker image bundling various grew tools, as well as some tooling to easily import and serve multiple corpora.

To serve a grew instance with some corpora, mount the directory containing the corpora to the docker instances `/data` directory and create a `corpus.json` file in the root directory of each corpus you want to be served.
This template can be used to create the individual `corpus.json` files:

```json
{
  "id": "UD_English-EWT",
  "config": "ud",
  "lang": "en"
}
```

Invoking `make docker` in this directory (or one of the parent directories) will build the docker provided docker image and tag it as `grewmatch`.
