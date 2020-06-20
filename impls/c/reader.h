#define PCRE2_CODE_UNIT_WIDTH 8

#include <stdlib.h>
#include <pcre2.h>

struct Pcre2State {
    int n;
    pcre2_code *re;
    int *errornumber;
    PCRE2_SIZE *erroroffset;
    pcre2_match_data *match_data;
};

struct Tokenizer {
    struct Pcre2State *regex;
    char *source;
};

struct TokenList {
    struct TokenList *next;
    char *token;
};

struct Reader {
    struct TokenList *tokens;
};

int reader_init_pcre2(struct Pcre2State *);
char *reader_next(struct Reader *);
char *reader_peek(struct Reader *);
