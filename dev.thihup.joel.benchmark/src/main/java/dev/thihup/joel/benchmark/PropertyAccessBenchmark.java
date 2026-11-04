package dev.thihup.joel.benchmark;

import dev.thihup.joel.benchmark.model.BenchmarkModels.Address;
import dev.thihup.joel.benchmark.model.BenchmarkModels.Country;
import dev.thihup.joel.benchmark.model.BenchmarkModels.Person;
import dev.thihup.joel.benchmark.util.ELImplementation;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class PropertyAccessBenchmark {

    @Param({"JOEL", "GLASSFISH", "TOMCAT"})
    public String implementation = "JOEL";

    private ELContext context;
    private ValueExpression singlePropertyExpr;
    private ValueExpression nestedPropertyExpr;
    private ValueExpression primitivePropertyExpr;
    private ValueExpression booleanPropertyExpr;
    private ValueExpression setPropertyExpr;

    private Person person;

    @Setup(Level.Trial)
    public void setup() {
        ExpressionFactory factory = ELImplementation.valueOf(implementation).createFactory();
        context = new StandardELContext(factory);

        Country country = new Country("Brazil", "BR");
        Address address = new Address("Av. Paulista", "Sao Paulo", "01311-000", country);
        person = new Person("Alice", 30, true, address, List.of("developer", "java"));

        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

        singlePropertyExpr = factory.createValueExpression(context, "${person.name}", Object.class);
        nestedPropertyExpr = factory.createValueExpression(context, "${person.address.country.name}", Object.class);
        primitivePropertyExpr = factory.createValueExpression(context, "${person.age}", Object.class);
        booleanPropertyExpr = factory.createValueExpression(context, "${person.active}", Object.class);
        setPropertyExpr = factory.createValueExpression(context, "${person.name}", Object.class);
    }

    @Benchmark
    public Object singleProperty() {
        return singlePropertyExpr.getValue(context);
    }

    @Benchmark
    public Object nestedProperty() {
        return nestedPropertyExpr.getValue(context);
    }

    @Benchmark
    public Object primitiveProperty() {
        return primitivePropertyExpr.getValue(context);
    }

    @Benchmark
    public Object booleanProperty() {
        return booleanPropertyExpr.getValue(context);
    }

    @Benchmark
    public void setProperty() {
        setPropertyExpr.setValue(context, "Alice Updated");
    }
}
