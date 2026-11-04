package dev.thihup.joel.benchmark;

import dev.thihup.joel.benchmark.model.BenchmarkModels.Calculator;
import dev.thihup.joel.benchmark.model.BenchmarkModels.Person;
import jakarta.el.ELContext;
import jakarta.el.ELProcessor;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class MethodInvocationBenchmark {

    private ELContext context;
    private ValueExpression noArgExpr;
    private ValueExpression singleArgExpr;
    private ValueExpression multiArgExpr;
    private ValueExpression stringMethodExpr;

    @Setup(Level.Trial)
    public void setup() {
        ELProcessor processor = new ELProcessor();
        ExpressionFactory factory = ExpressionFactory.newInstance();
        context = processor.getELManager().getELContext();

        processor.defineBean("calc", new Calculator());
        processor.defineBean("person", new Person("Bob", 25, true, null, Collections.emptyList()));

        noArgExpr = factory.createValueExpression(context, "${calc.ping()}", Object.class);
        singleArgExpr = factory.createValueExpression(context, "${calc.increment(10)}", Object.class);
        multiArgExpr = factory.createValueExpression(context, "${calc.add(10, 20)}", Object.class);
        stringMethodExpr = factory.createValueExpression(context, "${person.name.toUpperCase()}", Object.class);
    }

    @Benchmark
    public Object noArgMethod() {
        return noArgExpr.getValue(context);
    }

    @Benchmark
    public Object singleArgMethod() {
        return singleArgExpr.getValue(context);
    }

    @Benchmark
    public Object multiArgMethod() {
        return multiArgExpr.getValue(context);
    }

    @Benchmark
    public Object stringMethod() {
        return stringMethodExpr.getValue(context);
    }
}

