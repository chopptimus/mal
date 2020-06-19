#define PCRE2_CODE_UNIT_WIDTH 8

#include <pcre2.h>
#include "reader.h"

char *reader_next(struct Reader *reader)
{
    char *token = reader->tokens->token;
    reader->tokens = reader->tokens->next;
    return token;
}

char *reader_peek(struct Reader *reader)
{
    return reader->tokens->token;
}

void tokenize(struct TokenList *tl)
{
}
