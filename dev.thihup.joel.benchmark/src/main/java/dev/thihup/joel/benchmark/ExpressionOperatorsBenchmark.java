package dev.thihup.joel.benchmark;

import dev.thihup.joel.benchmark.model.BenchmarkModels.OrderItem;
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
public class ExpressionOperatorsBenchmark {

    @Param({"JOEL", "GLASSFISH", "TOMCAT"})
    public String implementation = "JOEL";

    private ELContext context;
    private ValueExpression arithmeticExpr;
    private ValueExpression logicalExpr;
    private ValueExpression ternaryExpr;
    private ValueExpression concatExpr;
    private ValueExpression emptyExpr;

    @Setup(Level.Trial)
    public void setup() {
        ExpressionFactory factory = ELImplementation.valueOf(implementation).createFactory();
        context = new StandardELContext(factory);

        OrderItem item = new OrderItem("Mechanical Keyboard", 129.99, 2);
        Person person = new Person("Carlos", 28, true, null, Collections.emptyList());

        context.getVariableMapper().setVariable("item", factory.createValueExpression(item, OrderItem.class));
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));
        context.getVariableMapper().setVariable("discount", factory.createValueExpression(0.15, Double.class));
        context.getVariableMapper().setVariable("emptyList", factory.createValueExpression(Collections.emptyList(), Object.class));

        arithmeticExpr = factory.createValueExpression(context, "${item.price * item.quantity * (1.0 - discount)}", Object.class);
        logicalExpr = factory.createValueExpression(context, "${person.age >= 18 && person.active && item.quantity > 0}", Object.class);
        ternaryExpr = factory.createValueExpression(context, "${person.active ? 'User is Active' : 'User is Inactive'}", Object.class);
        concatExpr = factory.createValueExpression(context, "${'Hello, ' += person.name += '!'}", Object.class);
        emptyExpr = factory.createValueExpression(context, "${empty emptyList}", Object.class);
    }

    @Benchmark
    public Object arithmetic() {
        return arithmeticExpr.getValue(context);
    }

    @Benchmark
    public Object logicalAndComparison() {
        return logicalExpr.getValue(context);
    }

    @Benchmark
    public Object ternary() {
        return ternaryExpr.getValue(context);
    }

    @Benchmark
    public Object stringConcat() {
        return concatExpr.getValue(context);
    }

    @Benchmark
    public Object emptyCheck() {
        return emptyExpr.getValue(context);
    }
}
