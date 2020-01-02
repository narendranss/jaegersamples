Hello World Client Program for Jaeger

1. HelloManual sample submits 3 traces to jaeger agent.
2. HelloManualChained sample has the following feature
    1. Tries to Inject the Span ID
    2. Tries to Chain the method calls to parent span based on Span ID
        i.e., If parent span id is already present, it will not create new parent span but will try link the child to the
        parent despite it coming from any thread.

The second sample is a hack as of now. Usually as per Jaeger all "linked spans" belong to same thread. Jaeger also introduce a concept called scope with that in mind.

A sample related to Jaeger  scope is present in below path.
https://github.com/yurishkuro/opentracing-tutorial/blob/master/java/src/main/java/lesson02/solution/HelloActive.java