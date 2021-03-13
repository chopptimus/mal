const LibExeObjStep = @import("std").build.LibExeObjStep;
const Builder = @import("std").build.Builder;
const builtin = @import("builtin");

const warn = @import("std").debug.warn;

pub fn build(b: *Builder) void {
    const mode = b.standardReleaseOptions();

    const exes = [_]*LibExeObjStep{
        b.addExecutable("step0_repl", "step0_repl.zig"),
    };

    for (exes) |exe| {
        exe.setBuildMode(mode);
        const run_cmd = exe.run();
        const step = b.step(exe.name, exe.name);
        step.dependOn(&run_cmd.step);
        b.default_step.dependOn(&exe.step);
        b.installArtifact(exe);
    }
}
