PDFLATEX=/usr/bin/pdflatex
SOURCE=PDS.tex
RESULT=PDS.pdf

$(RESULT): $(SOURCE)
	$(PDFLATEX) $(SOURCE)
	$(PDFLATEX) $(SOURCE)

clean:
	rm -f $(RESULT) *.aux *.log *.toc *.out *~
