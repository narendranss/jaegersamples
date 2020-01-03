package org.wildcraft.sample;

import com.google.common.collect.ImmutableMap;
import io.jaegertracing.internal.JaegerObjectFactory;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.Reference;
import io.jaegertracing.internal.clock.Clock;
import io.jaegertracing.internal.clock.SystemClock;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import org.apache.commons.lang3.tuple.Pair;
import org.wildcraft.util.Tracing;
import org.wildcraft.util.UUIDUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloManualChained {

    private final Tracer tracer;

    private final JaegerObjectFactory jaegerObjectFactory = new JaegerObjectFactory();

    private final Clock clock = new SystemClock();

    private final Map<String, Object> tags = new HashMap<String, Object>();

    private HelloManualChained(Tracer tracer) {
        this.tracer = tracer;
    }

    private Pair<Long, Long> getUUIDAsPair() {
        String uuid = UUIDUtil.generateUUID();
        System.out.println(uuid);
        Pair<Long, Long> pair = UUIDUtil.convertToLongPair(uuid);
        System.out.println(pair);
        return pair;
    }

    private Span createSpanWithGUID(Pair<Long, Long> pair, String operationName, Span parent) {
        byte flagSampled = 1;
        JaegerSpanContext spanContext = null;
        List<Reference> references = new ArrayList<>();
        if(parent!=null) {
            JaegerSpanContext parentContext = (JaegerSpanContext) parent.context();
            spanContext = jaegerObjectFactory.createSpanContext(parentContext.getTraceIdHigh(), parentContext.getTraceIdLow(), pair.getRight(), parentContext.getSpanId(), parentContext.getFlags(), new HashMap<String, String>(),null);
            references.add(new Reference((JaegerSpanContext) parent.context(), References.CHILD_OF));
        }else {
            spanContext = jaegerObjectFactory.createSpanContext(pair.getLeft(), pair.getRight(), pair.getRight(), 0, flagSampled, new HashMap<String, String>(),null);
            Reference reference = new Reference(spanContext, References.CHILD_OF);
            references.add(reference);
        }

        long startTimeNanoTicks = 0;
        boolean computeDurationViaNanoTicks = false;
        long startTimeMicroseconds =0;
        if (startTimeMicroseconds == 0) {
            startTimeMicroseconds = clock.currentTimeMicros();
            if (!clock.isMicrosAccurate()) {
                startTimeNanoTicks = clock.currentNanoTicks();
                computeDurationViaNanoTicks = true;
            }
        }
        return jaegerObjectFactory.createSpan((JaegerTracer) tracer, operationName, spanContext, startTimeMicroseconds, startTimeNanoTicks, computeDurationViaNanoTicks, tags, references);
    }

    private void sayHello(String helloTo) {

        Pair<Long, Long> pair = getUUIDAsPair();
        Span span = createSpanWithGUID(pair, "say-manual-hello", null);

        //Span span = tracer.buildSpan("say-manual-hello").start();
        //span.setTag("hello-to", helloTo);

        String helloStr = formatString(Pair.of(1L, 2L), span, helloTo);
        printHello(Pair.of(3L, 4L), span, helloStr);
        span.setTag("hello-fin", helloTo);
        span.finish();
    }



    private  String formatString(Pair<Long, Long> pair, Span rootSpan, String helloTo) {
        Span span = createSpanWithGUID(pair, "formatString", rootSpan);
        //Span span = tracer.buildSpan("formatString").asChildOf(rootSpan).start();
        try {
            String helloStr = String.format("Hello, %s!", helloTo);
            span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
            return helloStr;
        } finally {
            span.finish();
        }
    }

    private void printHello(Pair<Long, Long> pair, Span rootSpan, String helloStr) {
        Span span = createSpanWithGUID(pair, "printHello", rootSpan);
        //Span span = tracer.buildSpan("formatString").asChildOf(rootSpan).start();
        try {
            System.out.println(helloStr);
            span.log(ImmutableMap.of("event", "println"));
        } finally {
            span.finish();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expecting one argument");
        }

        String helloTo = args[0];
        try (JaegerTracer tracer = Tracing.init("hello-world-usertraceid")) {
            new HelloManualChained(tracer).sayHello(helloTo);
        }
    }
}
