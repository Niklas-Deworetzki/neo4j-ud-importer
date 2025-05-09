#! /bin/sh

eval $(opam env)
python3 build-corpusbank.py

# Compile corpora
grew compile -CORPUSBANK $GREW_MATCH_DIR/corpusbank

cd $GREW_MATCH_DIR/grew_match
python3 -m http.server &

cd $GREW_MATCH_DIR/grew_match_dream
dune exec grew_match_dream config.json
