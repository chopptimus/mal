#include <stdio.h>
#include <stdlib.h>
#include "reader.h"

#define PROMPT printf("user> ")

char *read(char *input)
{
    return input;
}

char *eval(char *sexp)
{
    return sexp;
}

char *print(char *value)
{
    return value;
}

void rep(char *input)
{
    printf("%s", print(eval(read(input))));
}

int main()
{
    char *linep = NULL;
    size_t linecap = 0;
    PROMPT;
    int ret = getline(&linep, &linecap, stdin);
    while (ret > -1) {
        rep(linep);
        PROMPT;
        ret = getline(&linep, &linecap, stdin);
    }
}
