#define PCRE2_CODE_UNIT_WIDTH 8

#include <pcre2.h>

#define TOKEN_REGEX "[\s,]*(~@|[\[\]{}()'`~^@]|\"(?:\\.|[^\\\"])*\"?|;.*|[^\s\[\]{}('\"`,;)]*)"

pcre2_code re;
