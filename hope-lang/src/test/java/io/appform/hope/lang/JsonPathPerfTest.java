/*
 * Copyright 2022. Santanu Sinha
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package io.appform.hope.lang;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.appform.hope.core.Evaluatable;
import io.appform.hope.core.exceptions.errorstrategy.InjectValueErrorHandlingStrategy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Test performance between different constructs
 */
@Slf4j
public class JsonPathPerfTest {

    public static final ObjectMapper mapper = new ObjectMapper();

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private List<Evaluatable> rules;
        private JsonNode contextNode;
        private HopeLangEngine hopeLangEngine;

        @Setup(Level.Trial)
        public void setUp() throws IOException {
            hopeLangEngine = HopeLangEngine.builder()
                    .errorHandlingStrategy(new InjectValueErrorHandlingStrategy())
                    .build();
            List<String> hopeRules = readAllHopeRulesForJsonPath();
            rules = hopeRules.stream()
                    .map(rule -> hopeLangEngine.parse(rule))
                    .collect(Collectors.toList());
            contextNode = mapper.readTree("{ \"value\": 20, \"string\" : \"Hello\" }");
        }

        private List<String> readAllHopeRulesForJsonPath() throws IOException {
            return Files.readAllLines(Paths.get("src/test/resources/samples/jsonpath.txt"));
        }
    }

    @Test
    public void testPerf() throws Exception {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(5))
                .measurementIterations(10)
                .threads(1)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();
        Collection<RunResult> results = new Runner(opt).run();

        results.forEach(runResult -> System.out.printf("%s:%s:%s:%s%n",
                runResult.getParams().getBenchmark(),
                runResult.getParams().getMode(),
                runResult.getParams().getMeasurement().getCount(),
                runResult.getPrimaryResult().getStatistics().getMean()));
    }

    @SneakyThrows
    @Benchmark
    public void testSingleEval(Blackhole blackhole, BenchmarkState state) {
        state.rules
                .forEach(evaluatable ->
                        blackhole.consume(state.hopeLangEngine.evaluate(evaluatable, state.contextNode)));
    }

    @SneakyThrows
    @Benchmark
    public void testBulkEval(Blackhole blackhole, BenchmarkState state) {
        blackhole.consume(state.hopeLangEngine.evaluate(state.rules, state.contextNode));
    }
}