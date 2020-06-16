SRCS = step0_repl.c
OBJS = $(SRCS:%.c=%.o)
BINS = $(OBJS:%.o=%)

all: $(BINS)

dist: mal

mal: $(word $(words $(BINS)),$(BINS))
	cp $< $@

$(OBJS) $(OTHER_OBJS): %.o: %.c  $(OTHER_HDRS)

$(patsubst %.o,%,$(filter step%,$(OBJS))): $(OTHER_OBJS)
$(BINS): %: %.o

clean:
	rm -f $(OBJS) $(BINS) $(OTHER_OBJS) mal
