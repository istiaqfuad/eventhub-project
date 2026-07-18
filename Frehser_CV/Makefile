MAIN      = main
# xelatex loads the fontawesome5 OTF fonts directly (no map registration / no sudo)
LATEX     = xelatex
LATEXFLAGS = -interaction=nonstopmode -halt-on-error

.PHONY: all clean cleanall

all: $(MAIN).pdf

# Run twice so links/refs resolve correctly
$(MAIN).pdf: $(MAIN).tex
	$(LATEX) $(LATEXFLAGS) $(MAIN).tex
	$(LATEX) $(LATEXFLAGS) $(MAIN).tex

clean:
	rm -f $(MAIN).aux $(MAIN).log $(MAIN).out $(MAIN).fls $(MAIN).fdb_latexmk

cleanall: clean
	rm -f $(MAIN).pdf
