#include <assert.h>
#include <pcre.h>
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

int main()
{
    test_reader_peek();
    test_reader_next();
    return 0;
}
