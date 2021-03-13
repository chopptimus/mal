const std = @import("std");
const warn = @import("std").debug.warn;

fn READ(a: []u8) []u8 {
    return a;
}

fn EVAL(a: []u8) []u8 {
    return a;
}

fn PRINT(a: []u8) []u8 {
    return a;
}

fn rep(input: []u8) []u8 {
    var read_input = READ(input);
    var eval_input = EVAL(read_input);
    var print_input = PRINT(eval_input);
    return print_input;
}

pub fn main() !void {
    const stdout = std.io.getStdOut().writer();
    const stdin = std.io.getStdIn().reader();
    while (true) {
        var buf: [100]u8 = undefined;
        _ = try stdout.write("user=> ");
        if (try stdin.readUntilDelimiterOrEof(buf[0..], '\n')) |line| {
            var output = rep(line);
            _ = try stdout.write(output);
            _ = try stdout.write("\n");
        } else {
            break;
        }
    }
}
