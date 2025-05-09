FROM ubuntu

ARG OCAML_VERSION=5.2.1

# get required packages
RUN apt-get update \
	&& apt-get install -y \
	python3 \
	git \
	opam \
	make wget m4 unzip librsvg2-bin curl bubblewrap \
	libcairo2-dev libev-dev libgmp-dev libssl-dev pkg-config

RUN useradd --create-home --shell /bin/bash grewmatch
WORKDIR /home/grewmatch
USER grewmatch

# Prepare installation of grew_match
ENV GREW_MATCH_DIR=/home/grewmatch
RUN mkdir corpusbank

# Install all opam dependencies
RUN opam init --disable-sandboxing --yes
RUN opam switch create $OCAML_VERSION
RUN eval $(opam env) \
	&& opam remote add grew "http://opam.grew.fr" \
	&& opam install --yes dream dep2pictlib grew

ENV OPAM_SWITCH_PREFIX="/home/grewmatch/.opam/$OCAML_VERSION" \
	CAML_LD_LIBRARY_PATH="/home/grewmatch/.opam/$OCAML_VERSION/lib/stublibs:/home/grewmatch/.opam/$OCAML_VERSION/lib/ocaml/stublibs:/home/grewmatch/.opam/$OCAML_VERSION/lib/ocaml" \
	OCAML_TOPLEVEL_PATH="/home/grewmatch/.opam/$OCAML_VERSION/lib/toplevel" \
	MANPATH=":/home/grewmatch/.opam/$OCAML_VERSION/man" \
	PATH="/home/grewmatch/.opam/$OCAML_VERSION/bin:$PATH"

# Set up backend
RUN git clone https://github.com/grew-nlp/grew_match_dream.git
COPY --chown=grewmatch \
	backend.json grew_match_dream/config.json
EXPOSE 8100

# Set up frontend
RUN git clone https://gitlab.inria.fr/grew/grew_match.git
COPY --chown=grewmatch \
	frontend.json grew_match/instance.json
EXPOSE 8000

# Set up entry point
COPY --chown=grewmatch --chmod=744 \
	entry.sh entry.sh
COPY --chown=grewmatch \
	build-corpusbank.py build-corpusbank.py
RUN chmod u+x entry.sh

# Hacky modify source code to bind to another interface
# so grew server comes online in docker network.
RUN sed -i \
	's/Dream.run/Dream.run ~interface:"0.0.0.0"/' \
	grew_match_dream/src/main.ml

CMD ["./entry.sh"]
