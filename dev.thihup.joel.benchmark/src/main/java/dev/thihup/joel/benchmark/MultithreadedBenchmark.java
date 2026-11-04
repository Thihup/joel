package dev.thihup.joel.benchmark;

import dev.thihup.joel.benchmark.model.BenchmarkModels.Person;
import dev.thihup.joel.benchmark.util.ELImplementation;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@Threads(8)
public class MultithreadedBenchmark {

    @Param({"JOEL", "GLASSFISH", "TOMCAT"})
    public String implementation = "JOEL";

    private ExpressionFactory factory;
    private ValueExpression precompiledExpr;

    @State(Scope.Thread)
    public static class ThreadContext {
        public ELContext context;

        @Setup(Level.Iteration)
        public void setup(MultithreadedBenchmark benchmark) {
            context = new StandardELContext(benchmark.factory);
            Person person = new Person("ConcurrentPerson", 30, true, null, Collections.emptyList());
            context.getVariableMapper().setVariable("person", benchmark.factory.createValueExpression(person, Person.class));
        }
    }

    @Setup(Level.Trial)
    public void setup() {
        factory = ELImplementation.valueOf(implementation).createFactory();
        StandardELContext setupContext = new StandardELContext(factory);
        Person person = new Person("SharedPerson", 30, true, null, Collections.emptyList());
        setupContext.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));
        precompiledExpr = factory.createValueExpression(setupContext, "${person.name}", Object.class);
    }

    @Benchmark
    public Object concurrentEvaluatePrecompiled(ThreadContext threadContext) {
        return precompiledExpr.getValue(threadContext.context);
    }

    @Benchmark
    public Object concurrentParseSameExpression(ThreadContext threadContext) {
        return factory.createValueExpression(threadContext.context, "${person.name}", Object.class);
    }

    @Benchmark
    public Object concurrentParseDiverseExpressions(ThreadContext threadContext) {
        int id = ThreadLocalRandom.current().nextInt(100);
        return factory.createValueExpression(threadContext.context, "${person.age + " + id + "}", Object.class);
    }

    @Benchmark
    public Object concurrentCacheEviction(ThreadContext threadContext) {
        int id = ThreadLocalRandom.current().nextInt(10000);
        return factory.createValueExpression(threadContext.context, "${person.age + " + id + "}", Object.class);
    }
}
