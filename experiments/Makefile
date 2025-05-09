
clean:
	$(MAKE) -C grew-data clean

docker:
	$(MAKE) -C grew-docker docker

corpora:
	$(MAKE) -C grew-data corpora

grew: docker corpora
	docker run --rm --interactive --tty --mount type=bind,source=$(CURDIR)/grew-data,target=/data grewmatch:latest

neo4j: corpora
	python importer.py grew-data/

benchmark:
	python run_queries.py
