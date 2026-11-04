package dev.thihup.joel.benchmark;

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
public class CompilationVsEvaluationBenchmark {

    private ELProcessor processor;
    private ExpressionFactory factory;
    private ELContext context;

    private static final String SIMPLE_EXPR = "${person.name}";
    private static final String COMPLEX_EXPR = "${person.age >= 18 && person.active ? person.name : 'Unknown'}";

    private ValueExpression precompiledSimple;
    private ValueExpression precompiledComplex;

    @Setup(Level.Trial)
    public void setup() {
        processor = new ELProcessor();
        factory = ExpressionFactory.newInstance();
        context = processor.getELManager().getELContext();

        Person person = new Person("Daniel", 32, true, null, Collections.emptyList());
        processor.defineBean("person", person);

        precompiledSimple = factory.createValueExpression(context, SIMPLE_EXPR, Object.class);
        precompiledComplex = factory.createValueExpression(context, COMPLEX_EXPR, Object.class);
    }

    @Benchmark
    public Object compileSimple() {
        return factory.createValueExpression(context, SIMPLE_EXPR, Object.class);
    }

    @Benchmark
    public Object compileComplex() {
        return factory.createValueExpression(context, COMPLEX_EXPR, Object.class);
    }

    @Benchmark
    public Object evaluatePrecompiledSimple() {
        return precompiledSimple.getValue(context);
    }

    @Benchmark
    public Object evaluatePrecompiledComplex() {
        return precompiledComplex.getValue(context);
    }

    @Benchmark
    public Object parseAndEvaluateDynamic() {
        return processor.eval("person.name");
    }
}

