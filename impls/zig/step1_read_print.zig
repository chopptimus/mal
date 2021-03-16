const std = @import("std");

const readLine = @import("util.zig").readLine;

const List = struct {
    first: LispValue, rest: List
};

const LispValue = union {
    int: i64, float: f64, bool: bool, list: *List
};

// fn recursiveDescent(alloc: *Allocator, buf: []u8) !?*LispValue {
//     switch (buf[0]) {
//     };
// }

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
        var buf: [1000]u8 = undefined;
        try stdout.print("user=> ", .{});
        if (try readLine(stdin, buf[0..])) |line| {
            try stdout.print("{s}\n", .{rep(line)});
        } else {
            break;
        }
    }
}
