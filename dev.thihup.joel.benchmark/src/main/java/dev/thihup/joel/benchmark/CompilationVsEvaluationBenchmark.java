package dev.thihup.joel.benchmark;

import dev.thihup.joel.benchmark.model.BenchmarkModels.Person;
import dev.thihup.joel.benchmark.util.ELImplementation;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
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

    @Param({"JOEL", "GLASSFISH", "TOMCAT"})
    public String implementation = "JOEL";

    private ExpressionFactory factory;
    private ELContext context;

    private static final String SIMPLE_EXPR = "${person.name}";
    private static final String COMPLEX_EXPR = "${person.age >= 18 && person.active ? person.name : 'Unknown'}";
    private static final String TEMPLATE_EXPR = "Hello, ${person.name}! You are ${person.age} years old.";

    private ValueExpression precompiledSimple;
    private ValueExpression precompiledComplex;
    private ValueExpression precompiledTemplate;

    @Setup(Level.Trial)
    public void setup() {
        factory = ELImplementation.valueOf(implementation).createFactory();
        context = new StandardELContext(factory);

        Person person = new Person("Daniel", 32, true, null, Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

        precompiledSimple = factory.createValueExpression(context, SIMPLE_EXPR, Object.class);
        precompiledComplex = factory.createValueExpression(context, COMPLEX_EXPR, Object.class);
        precompiledTemplate = factory.createValueExpression(context, TEMPLATE_EXPR, Object.class);
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
    public Object compileTemplateText() {
        return factory.createValueExpression(context, TEMPLATE_EXPR, Object.class);
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
    public Object evaluatePrecompiledTemplateText() {
        return precompiledTemplate.getValue(context);
    }

    @Benchmark
    public Object parseAndEvaluateDynamic() {
        return factory.createValueExpression(context, SIMPLE_EXPR, Object.class).getValue(context);
    }

    @Benchmark
    public Object parseAndEvaluateDynamicTemplateText() {
        return factory.createValueExpression(context, TEMPLATE_EXPR, Object.class).getValue(context);
    }
}
