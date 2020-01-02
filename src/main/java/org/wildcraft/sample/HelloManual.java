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
import io.opentracing.Tracer;

import org.wildcraft.util.Tracing;

import java.util.*;

public class HelloManual {

    private final Tracer tracer;

    private final JaegerObjectFactory jaegerObjectFactory = new JaegerObjectFactory();

    private final Clock clock = new SystemClock();

    private final Map<String, Object> tags = new HashMap<String, Object>();

    private HelloManual(Tracer tracer) {
        this.tracer = tracer;
    }

    private void sayHello(String helloTo) {
        byte flagSampled = 1;
        JaegerSpanContext spanContext = jaegerObjectFactory.createSpanContext(0, 150, 150, 0, flagSampled, new HashMap<String, String>(),null);

        Reference reference = new Reference(spanContext, References.CHILD_OF);

        List<Reference> references = new ArrayList<>();
        references.add(reference);

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

        System.out.println("startTimeMicroseconds: "+startTimeMicroseconds);
        System.out.println("startTimeNanoTicks: "+startTimeNanoTicks);
        Span span = jaegerObjectFactory.createSpan((JaegerTracer) tracer, "say-manual-hello", spanContext, 1577104503120000L, 33383308737599L, computeDurationViaNanoTicks, tags, references);

        //Span span = tracer.buildSpan("say-manual-hello").start();
        span.setTag("hello-to", helloTo);
        span.setTag("hello-fin", helloTo);

        String helloStr = formatString(span, helloTo);
        printHello(span, helloStr);

        span.finish();
    }

    private  String formatString(Span rootSpan, String helloTo) {
        Span span = tracer.buildSpan("formatString").asChildOf(rootSpan).start();
        try {
            String helloStr = String.format("Hello, %s!", helloTo);
            span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
            return helloStr;
        } finally {
            span.finish();
        }
    }

    private void printHello(Span rootSpan, String helloStr) {
        Span span = tracer.buildSpan("printHello").asChildOf(rootSpan).start();
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
        try (JaegerTracer tracer = Tracing.init("hello-usertraceid-world")) {
            new HelloManual(tracer).sayHello(helloTo);
        }
    }
}
