package dev.thihup.joel.benchmark;

import dev.thihup.joel.benchmark.model.BenchmarkModels.Address;
import dev.thihup.joel.benchmark.model.BenchmarkModels.Country;
import dev.thihup.joel.benchmark.model.BenchmarkModels.Person;
import dev.thihup.joel.benchmark.model.BenchmarkModels.PersonRecord;
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
    private ValueExpression nullIntermediateExpr;
    private ValueExpression primitivePropertyExpr;
    private ValueExpression booleanPropertyExpr;
    private ValueExpression recordPropertyExpr;
    private ValueExpression setPropertyExpr;

    @Setup(Level.Trial)
    public void setup() {
        ExpressionFactory factory = ELImplementation.valueOf(implementation).createFactory();
        context = new StandardELContext(factory);

        Country country = new Country("Brazil", "BR");
        Address address = new Address("Av. Paulista", "Sao Paulo", "01311-000", country);
        Person person = new Person("Alice", 30, true, address, List.of("developer", "java"));
        Person personNullAddress = new Person("Bob", 25, false, null, List.of("tester"));
        PersonRecord personRecord = new PersonRecord("Charlie", 40, true);

        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));
        context.getVariableMapper().setVariable("personNullAddress", factory.createValueExpression(personNullAddress, Person.class));
        context.getVariableMapper().setVariable("personRecord", factory.createValueExpression(personRecord, PersonRecord.class));

        singlePropertyExpr = factory.createValueExpression(context, "${person.name}", Object.class);
        nestedPropertyExpr = factory.createValueExpression(context, "${person.address.country.name}", Object.class);
        nullIntermediateExpr = factory.createValueExpression(context, "${personNullAddress.address.country.name}", Object.class);
        primitivePropertyExpr = factory.createValueExpression(context, "${person.age}", Object.class);
        booleanPropertyExpr = factory.createValueExpression(context, "${person.active}", Object.class);
        recordPropertyExpr = factory.createValueExpression(context, "${personRecord.name}", Object.class);
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
    public Object nullIntermediateProperty() {
        return nullIntermediateExpr.getValue(context);
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
    public Object recordProperty() {
        return recordPropertyExpr.getValue(context);
    }

    @Benchmark
    public void setProperty() {
        setPropertyExpr.setValue(context, "Alice Updated");
    }
}
