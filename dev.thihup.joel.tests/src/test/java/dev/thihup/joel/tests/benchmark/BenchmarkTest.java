package dev.thihup.joel.tests.benchmark;


import jakarta.el.ELProcessor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;

public class BenchmarkTest {

    private static final ELProcessor PROCESSOR = new ELProcessor();

    @Benchmark
    @Fork(1)
    public Object benchmark() {
        return PROCESSOR.eval("1+1");
    }
}
