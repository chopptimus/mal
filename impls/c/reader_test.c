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
    char *pattern = "[\\s,]*(~@|[\\[\\]{}()'`~^@]|\"(?:\\\\.|[^\\\\\"])*\"?|;.*|[^\\s\\[\\]{}('\"`,;)]*)";
    int errornumber;
    PCRE2_SIZE erroroffset;
    pcre2_code *re = pcre2_compile(
        (PCRE2_SPTR)pattern,
        PCRE2_ZERO_TERMINATED,
        0,
        &errornumber,
        &erroroffset,
        NULL);

    if (re == NULL) {
        PCRE2_UCHAR buffer[256];
        pcre2_get_error_message(errornumber, buffer, sizeof(buffer));
        printf("PCRE2 compilation failed at offset %d: %s\n", (int)erroroffset,
               buffer);
        exit(1);
    }

    pcre2_match_data *match_data;
    match_data = pcre2_match_data_create_from_pattern(re, NULL);

    PCRE2_SPTR subject = (PCRE2_SPTR)"(hello)";
    int rc = pcre2_match(
        re,
        subject,
        strlen((char *)subject),
        0,
        0,
        match_data,
        NULL);

    if (rc < 0) {
        switch(rc) {
            case PCRE2_ERROR_NOMATCH:
                printf("No match\n");
                break;
            default:
                printf("Matching error %d\n", rc);
                break;
        }
        pcre2_match_data_free(match_data);
        pcre2_code_free(re);
        exit(1);
    }
    pcre2_match_data_free(match_data);
    pcre2_code_free(re);

    PCRE2_SIZE *ovector = pcre2_get_ovector_pointer(match_data);

    if (rc == 0) {
        printf("ovector was not big enough for all the captured substrings\n");
        exit(1);
    }

    for (int i = 0; i < rc; i++) {
        PCRE2_SPTR substring_start = subject + ovector[2*i];
        PCRE2_SIZE substring_length = ovector[2*i+1] - ovector[2*i];
        printf("%d: %.*s\n", i, (int)substring_length, (char *)substring_start);
    }

    size_t subject_length = strlen((char *)subject);
    uint32_t newline;
    pcre2_pattern_info(re, PCRE2_INFO_NEWLINE, &newline);
    int crlf_is_newline = newline == PCRE2_NEWLINE_ANY ||
        newline == PCRE2_NEWLINE_CRLF ||
        newline == PCRE2_NEWLINE_ANYCRLF;
    for (;;) {
        uint32_t options = 0;                   /* Normally no options */
        PCRE2_SIZE start_offset = ovector[1];   /* Start at end of previous match */

        /* If the previous match was for an empty string, we are finished if we are
           at the end of the subject. Otherwise, arrange to run another match at the
           same point to see if a non-empty match can be found. */

        if (ovector[0] == ovector[1]) {
            if (ovector[0] == subject_length) break;
            options = PCRE2_NOTEMPTY_ATSTART | PCRE2_ANCHORED;
        }

        /* If the previous match was not an empty string, there is one tricky case to
           consider. If a pattern contains \K within a lookbehind assertion at the
           start, the end of the matched string can be at the offset where the match
           started. Without special action, this leads to a loop that keeps on matching
           the same substring. We must detect this case and arrange to move the start on
           by one character. The pcre2_get_startchar() function returns the starting
           offset that was passed to pcre2_match(). */

        else {
            PCRE2_SIZE startchar = pcre2_get_startchar(match_data);
            if (start_offset <= startchar) {
                if (startchar >= subject_length) break;   /* Reached end of subject.   */
                start_offset = startchar + 1;             /* Advance by one character. */
            }
        }

        /* Run the next matching operation */

        rc = pcre2_match(
            re,                   /* the compiled pattern */
            subject,              /* the subject string */
            subject_length,       /* the length of the subject */
            start_offset,         /* starting offset in the subject */
            options,              /* options */
            match_data,           /* block for storing the result */
            NULL);                /* use default match context */

        /* This time, a result of NOMATCH isn't an error. If the value in "options"
           is zero, it just means we have found all possible matches, so the loop ends.
           Otherwise, it means we have failed to find a non-empty-string match at a
           point where there was a previous empty-string match. In this case, we do what
           Perl does: advance the matching position by one character, and continue. We
           do this by setting the "end of previous match" offset, because that is picked
           up at the top of the loop as the point at which to start again.

           There are two complications: (a) When CRLF is a valid newline sequence, and
           the current position is just before it, advance by an extra byte. (b)
           Otherwise we must ensure that we skip an entire UTF character if we are in
           UTF mode. */

        if (rc == PCRE2_ERROR_NOMATCH) {
            if (options == 0) break;                    /* All matches found */
            ovector[1] = start_offset + 1;              /* Advance one code unit */
            if (crlf_is_newline &&                      /* If CRLF is a newline & */
                start_offset < subject_length - 1 &&    /* we are at CRLF, */
                subject[start_offset] == '\r' &&
                subject[start_offset + 1] == '\n')
                ovector[1] += 1;                          /* Advance by one more. */
            continue;    /* Go round the loop again */
        }

        /* Other matching errors are not recoverable. */

        if (rc < 0) {
            printf("Matching error %d\n", rc);
            pcre2_match_data_free(match_data);
            pcre2_code_free(re);
            exit(1);
        }

        /* Match succeded */

        printf("\nMatch succeeded again at offset %d:%d\n", (int)ovector[0], (int)ovector[1]);

        /* The match succeeded, but the output vector wasn't big enough. This
           should not happen. */

        if (rc == 0)
            printf("ovector was not big enough for all the captured substrings\n");

        /* As before, show substrings stored in the output vector by number, and then
           also any named substrings. */

        for (int i = 0; i < rc; i++) {
            PCRE2_SPTR substring_start = subject + ovector[2*i];
            size_t substring_length = ovector[2*i+1] - ovector[2*i];
            printf("%2d: %.*s\n", i, (int)substring_length, (char *)substring_start);
        }
    }      /* End of loop to find second and subsequent matches */
}

int main()
{
    test_reader_peek();
    test_reader_next();
    test_regex();
    return 0;
}
