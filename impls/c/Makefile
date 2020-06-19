SRCS = step0_repl.c step1_read_print.c patterns_test.c reader_test.c
OBJS = $(SRCS:%.c=%.o)
BINS = $(OBJS:%.o=%)
OTHER_OBJS = patterns.o reader.o
OTHER_HDRS = patterns.h reader.h

all: $(BINS)

dist: mal

mal: $(word $(words $(BINS)),$(BINS))
	cp $< $@

$(OBJS) $(OTHER_OBJS): %.o: %.c $(OTHER_HDRS)

$(patsubst %.o,%,$(filter step%,$(OBJS))): $(OTHER_OBJS)
$(patsubst %.o,%,$(filter %test.o,$(OBJS))): $(OTHER_OBJS)
$(BINS): %: %.o

.PHONY: test clean

clean:
	rm -f $(OBJS) $(BINS) $(OTHER_OBJS) mal

test: reader_test
	./reader_test
