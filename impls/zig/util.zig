const std = @import("std");

pub fn readLine(reader: std.fs.File.Reader, buf: []u8) !?[]u8 {
    return reader.readUntilDelimiterOrEof(buf, '\n');
}
