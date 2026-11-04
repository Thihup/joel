package dev.thihup.joel.benchmark;

import jakarta.el.ELContext;
import jakarta.el.ELProcessor;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class LambdaStreamBenchmark {

    private ELContext context;
    private ValueExpression lambdaDirectExpr;
    private ValueExpression streamFilterMapExpr;
    private ValueExpression streamReduceExpr;

    @Setup(Level.Trial)
    public void setup() {
        ELProcessor processor = new ELProcessor();
        ExpressionFactory factory = ExpressionFactory.newInstance();
        context = processor.getELManager().getELContext();

        List<Integer> numbers = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());
        processor.defineBean("numbers", numbers);

        lambdaDirectExpr = factory.createValueExpression(context, "${((x, y) -> x * 2 + y)(10, 20)}", Object.class);
        streamFilterMapExpr = factory.createValueExpression(context, "${numbers.stream().filter(x -> x > 50).map(x -> x * 2).toList()}", Object.class);
        streamReduceExpr = factory.createValueExpression(context, "${numbers.stream().reduce(0, (a, b) -> a + b)}", Object.class);
    }

    @Benchmark
    public Object lambdaDirect() {
        return lambdaDirectExpr.getValue(context);
    }

    @Benchmark
    public Object streamFilterMap() {
        return streamFilterMapExpr.getValue(context);
    }

    @Benchmark
    public Object streamReduce() {
        return streamReduceExpr.getValue(context);
    }
}

