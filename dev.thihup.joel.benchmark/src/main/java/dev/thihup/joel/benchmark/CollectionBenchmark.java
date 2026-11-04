package dev.thihup.joel.benchmark;

import dev.thihup.joel.benchmark.util.ELImplementation;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
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

    @Param({"JOEL", "GLASSFISH", "TOMCAT"})
    public String implementation = "JOEL";

    private ELContext context;
    private ValueExpression mapAccessExpr;
    private ValueExpression mapDynamicKeyExpr;
    private ValueExpression mapMissingKeyExpr;
    private ValueExpression nestedMapExpr;
    private ValueExpression mapWriteExpr;
    private ValueExpression listAccessExpr;
    private ValueExpression listCoercedIndexExpr;
    private ValueExpression arrayAccessExpr;

    @Setup(Level.Trial)
    public void setup() {
        ExpressionFactory factory = ELImplementation.valueOf(implementation).createFactory();
        context = new StandardELContext(factory);

        Map<String, Object> map = new HashMap<>(Map.of(
                "key1", "val1",
                "key2", "val2",
                "targetKey", "found"
        ));
        Map<String, Object> nestedMap = Map.of(
                "level1", Map.of(
                        "level2", "nestedValue"
                )
        );
        List<String> list = List.of("zero", "one", "two", "three", "four", "five");
        String[] array = new String[]{"alpha", "bravo", "charlie", "delta"};

        context.getVariableMapper().setVariable("map", factory.createValueExpression(map, Map.class));
        context.getVariableMapper().setVariable("nestedMap", factory.createValueExpression(nestedMap, Map.class));
        context.getVariableMapper().setVariable("dynamicKey", factory.createValueExpression("targetKey", String.class));
        context.getVariableMapper().setVariable("list", factory.createValueExpression(list, List.class));
        context.getVariableMapper().setVariable("array", factory.createValueExpression(array, String[].class));

        mapAccessExpr = factory.createValueExpression(context, "${map['targetKey']}", Object.class);
        mapDynamicKeyExpr = factory.createValueExpression(context, "${map[dynamicKey]}", Object.class);
        mapMissingKeyExpr = factory.createValueExpression(context, "${map['nonExistentKey']}", Object.class);
        nestedMapExpr = factory.createValueExpression(context, "${nestedMap.level1.level2}", Object.class);
        mapWriteExpr = factory.createValueExpression(context, "${map['key1']}", Object.class);
        listAccessExpr = factory.createValueExpression(context, "${list[3]}", Object.class);
        listCoercedIndexExpr = factory.createValueExpression(context, "${list['3']}", Object.class);
        arrayAccessExpr = factory.createValueExpression(context, "${array[2]}", Object.class);
    }

    @Benchmark
    public Object mapLookup() {
        return mapAccessExpr.getValue(context);
    }

    @Benchmark
    public Object mapDynamicKeyLookup() {
        return mapDynamicKeyExpr.getValue(context);
    }

    @Benchmark
    public Object mapMissingKeyLookup() {
        return mapMissingKeyExpr.getValue(context);
    }

    @Benchmark
    public Object nestedMapLookup() {
        return nestedMapExpr.getValue(context);
    }

    @Benchmark
    public void mapWrite() {
        mapWriteExpr.setValue(context, "val1_updated");
    }

    @Benchmark
    public Object listIndex() {
        return listAccessExpr.getValue(context);
    }

    @Benchmark
    public Object listCoercedIndex() {
        return listCoercedIndexExpr.getValue(context);
    }

    @Benchmark
    public Object arrayIndex() {
        return arrayAccessExpr.getValue(context);
    }
}
