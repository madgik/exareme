RUSTDOC_OPTS:= --html-in-header header.inc
RUSTDOC_OPTS+= --markdown-css css/rust.css
RUSTDOC_OPTS+= -o html/

PAGES:= $(wildcard *.md)
HTML:= $(patsubst %.md,html/%.html, $(PAGES))

all: $(HTML)

html:
	mkdir -p ./html
	cp -R css fonts html

html/%.html: %.md | html
	rustdoc $< $(RUSTDOC_OPTS)

clean:
	rm -rf html/
