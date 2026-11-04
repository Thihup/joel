package dev.thihup.joel.benchmark;

import jakarta.el.ELContext;
import jakarta.el.ELProcessor;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class CollectionBenchmark {

    private ELContext context;
    private ValueExpression mapAccessExpr;
    private ValueExpression listAccessExpr;
    private ValueExpression arrayAccessExpr;

    @Setup(Level.Trial)
    public void setup() {
        ELProcessor processor = new ELProcessor();
        ExpressionFactory factory = ExpressionFactory.newInstance();
        context = processor.getELManager().getELContext();

        Map<String, Object> map = Map.of(
                "key1", "val1",
                "key2", "val2",
                "targetKey", "found"
        );
        List<String> list = List.of("zero", "one", "two", "three", "four", "five");
        String[] array = new String[]{"alpha", "bravo", "charlie", "delta"};

        processor.defineBean("map", map);
        processor.defineBean("list", list);
        processor.defineBean("array", array);

        mapAccessExpr = factory.createValueExpression(context, "${map['targetKey']}", Object.class);
        listAccessExpr = factory.createValueExpression(context, "${list[3]}", Object.class);
        arrayAccessExpr = factory.createValueExpression(context, "${array[2]}", Object.class);
    }

    @Benchmark
    public Object mapLookup() {
        return mapAccessExpr.getValue(context);
    }

    @Benchmark
    public Object listIndex() {
        return listAccessExpr.getValue(context);
    }

    @Benchmark
    public Object arrayIndex() {
        return arrayAccessExpr.getValue(context);
    }
}

