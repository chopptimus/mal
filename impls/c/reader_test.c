#define PCRE2_CODE_UNIT_WIDTH 8

#include <assert.h>
#include <pcre2.h>
#include <stdio.h>
#include <string.h>
#include "reader.h"

struct Reader *test_reader()
{
    struct TokenList *first, *second;
    first = malloc(sizeof(struct TokenList));
    second = malloc(sizeof(struct TokenList));
    struct Reader *reader = malloc(sizeof(struct Reader));

    first->next = second;
    first->token = "foo";

    second->next = NULL;
    second->token = "bar";

    reader->tokens = first;

    return reader;
}

void test_reader_peek()
{
    struct Reader *reader = test_reader();

    assert(strcmp(reader_peek(reader), "foo") == 0);
}

void test_reader_next()
{
    struct Reader *reader = test_reader();

    char *token = reader_next(reader);
    assert(strcmp(token, "foo") == 0);
    token = reader_next(reader);
    assert(strcmp(token, "bar") == 0);
}

void test_regex()
{
    char *pattern = "abc";
    pcre2_code *re = pcre2_compile(
        (PCRE2_SPTR)pattern, PCRE2_ZERO_TERMINATED, 0,
        NULL, NULL, NULL);

    if (re == NULL) {
        printf("Failed to compile regex\n");
        exit(1);
    }

    pcre2_match_data *match_data;
    match_data = pcre2_match_data_create_from_pattern(re, NULL);
}

int main()
{
    test_reader_peek();
    test_reader_next();
    test_regex();
    return 0;
}
