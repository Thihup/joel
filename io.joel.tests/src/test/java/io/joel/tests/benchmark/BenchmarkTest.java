package io.joel.tests.benchmark;


import jakarta.el.ELProcessor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.infra.Blackhole;

public class BenchmarkTest {

    private static final ELProcessor PROCESSOR = new ELProcessor();

    @Benchmark
    @Fork(1)
    public void benchmark(Blackhole blackhole) {
        blackhole.consume(PROCESSOR.eval("1+1"));
    }
}
