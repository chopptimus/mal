#include <stdlib.h>

struct TokenList {
    struct TokenList *next;
    char *token;
};

struct Reader {
    struct TokenList *tokens;
};

char *reader_next(struct Reader *);
char *reader_peek(struct Reader *);
