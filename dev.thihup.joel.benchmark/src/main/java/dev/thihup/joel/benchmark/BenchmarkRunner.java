package dev.thihup.joel.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        String pattern = ".*Benchmark.*";
        List<String> implementations = new ArrayList<>();

        for (String arg : args) {
            if (arg.startsWith("--impl=")) {
                String val = arg.substring("--impl=".length());
                implementations.addAll(List.of(val.split(",")));
            } else if (arg.equals("--compare")) {
                implementations.addAll(List.of("JOEL", "GLASSFISH", "TOMCAT"));
            } else if (!arg.startsWith("-")) {
                pattern = arg;
            }
        }

        ChainedOptionsBuilder builder = new OptionsBuilder()
                .include(pattern)
                .forks(1)
                .warmupIterations(2)
                .measurementIterations(3);

        if (!implementations.isEmpty()) {
            builder.param("implementation", implementations.toArray(new String[0]));
        }

        new Runner(builder.build()).run();
    }
}
