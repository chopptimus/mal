clojure_MODE ?= clj
SOURCES_UTIL = src/mal/readline.$(clojure_MODE)

mal.jar:
	lein with-profile stepA uberjar
	cp target/stepA_mal.jar $@

SHELL := bash
mal: mal.jar
	cat <(echo -e '#!/bin/sh\nexec java -jar "$$0" "$$@"') mal.jar > $@
	chmod +x mal

target/%.jar:
	lein with-profile $(word 1,$(subst _, ,$*)) uberjar

clean:
	rm -rf target/ mal.jar mal
