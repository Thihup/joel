package dev.thihup.joel.benchmark;

import dev.thihup.joel.benchmark.model.BenchmarkModels.BenchmarkFunctions;
import dev.thihup.joel.benchmark.model.BenchmarkModels.Calculator;
import dev.thihup.joel.benchmark.model.BenchmarkModels.Person;
import dev.thihup.joel.benchmark.util.ELImplementation;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Method;
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
    private ValueExpression overloadLongExpr;
    private ValueExpression overloadDoubleExpr;
    private ValueExpression overloadStringExpr;
    private ValueExpression staticMethodExpr;
    private ValueExpression functionMapperExpr;
    private ValueExpression multiArgFixedExpr;

    @Setup(Level.Trial)
    public void setup() throws NoSuchMethodException {
        ExpressionFactory factory = ELImplementation.valueOf(implementation).createFactory();
        StandardELContext stdContext = new StandardELContext(factory);
        context = stdContext;

        Calculator calc = new Calculator();
        Person person = new Person("Bob", 25, true, null, Collections.emptyList());

        context.getVariableMapper().setVariable("calc", factory.createValueExpression(calc, Calculator.class));
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

        context.getImportHandler().importClass("java.lang.Math");

        Method formatGreetingMethod = BenchmarkFunctions.class.getMethod("formatGreeting", String.class);
        stdContext.getFunctionMapper().mapFunction("fn", "formatGreeting", formatGreetingMethod);

        noArgExpr = factory.createValueExpression(context, "${calc.ping()}", Object.class);
        singleArgExpr = factory.createValueExpression(context, "${calc.increment(10)}", Object.class);
        multiArgExpr = factory.createValueExpression(context, "${calc.add(10, 20)}", Object.class);
        stringMethodExpr = factory.createValueExpression(context, "${person.name.toUpperCase()}", Object.class);
        overloadLongExpr = factory.createValueExpression(context, "${calc.compute(10)}", Object.class);
        overloadDoubleExpr = factory.createValueExpression(context, "${calc.compute(10.5)}", Object.class);
        overloadStringExpr = factory.createValueExpression(context, "${calc.compute('text')}", Object.class);
        staticMethodExpr = factory.createValueExpression(context, "${Math.max(10, 20)}", Object.class);
        functionMapperExpr = factory.createValueExpression(context, "${fn:formatGreeting(person.name)}", Object.class);
        multiArgFixedExpr = factory.createValueExpression(context, "${calc.sumAll(1, 2, 3, 4)}", Object.class);
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

    @Benchmark
    public Object overloadResolutionLong() {
        return overloadLongExpr.getValue(context);
    }

    @Benchmark
    public Object overloadResolutionDouble() {
        return overloadDoubleExpr.getValue(context);
    }

    @Benchmark
    public Object overloadResolutionString() {
        return overloadStringExpr.getValue(context);
    }

    @Benchmark
    public Object staticMethodInvocation() {
        return staticMethodExpr.getValue(context);
    }

    @Benchmark
    public Object functionMapperInvocation() {
        return functionMapperExpr.getValue(context);
    }

    @Benchmark
    public Object multiArgFixedMethod() {
        return multiArgFixedExpr.getValue(context);
    }
}
