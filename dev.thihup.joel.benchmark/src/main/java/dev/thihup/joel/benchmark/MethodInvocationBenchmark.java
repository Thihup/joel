package dev.thihup.joel.benchmark;

import dev.thihup.joel.benchmark.model.BenchmarkModels.Calculator;
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
public class MethodInvocationBenchmark {

    @Param({"JOEL", "GLASSFISH", "TOMCAT"})
    public String implementation = "JOEL";

    private ELContext context;
    private ValueExpression noArgExpr;
    private ValueExpression singleArgExpr;
    private ValueExpression multiArgExpr;
    private ValueExpression stringMethodExpr;

    @Setup(Level.Trial)
    public void setup() {
        ExpressionFactory factory = ELImplementation.valueOf(implementation).createFactory();
        context = new StandardELContext(factory);

        Calculator calc = new Calculator();
        Person person = new Person("Bob", 25, true, null, Collections.emptyList());

        context.getVariableMapper().setVariable("calc", factory.createValueExpression(calc, Calculator.class));
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

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
