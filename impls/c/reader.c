#define PCRE2_CODE_UNIT_WIDTH 8

#include <pcre2.h>
#include "reader.h"

pcre2_code *TOKEN_REGEX = NULL;
char *TOKEN_PATTERN = "abc";

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

int reader_init_pcre2(struct Pcre2State *pcre2_state)
{
    // pcre2_state->re = pcre2_compile(
    //     (PCRE2_SPTR)pattern,
    //     PCRE2_ZERO_TERMINATED,
    //     0,
    //     pcre2_state->errornumber,
    //     pcre2_state->erroroffset,
    //     NULL);

    // if (!pcre2_state->re) {
    //     return *pcre2_state->errornumber;
    // }
    // 
    // pcre2_state->n = 0;
    return 0;
}

int reader_tokenize(struct TokenList *tl)
{
    // Ew globalness
    if (!TOKEN_REGEX) {
        TOKEN_REGEX = pcre2_compile(
            (PCRE2_SPTR)TOKEN_PATTERN, PCRE2_ZERO_TERMINATED, 0,
            NULL, NULL, NULL);

        if (TOKEN_REGEX == NULL)
            exit(1);
    }


    return 0;
}
