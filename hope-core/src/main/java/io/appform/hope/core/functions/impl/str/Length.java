/*
 * Copyright 2019. Santanu Sinha
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

package io.appform.hope.core.functions.impl.str;

import io.appform.hope.core.Value;
import io.appform.hope.core.functions.FunctionImplementation;
import io.appform.hope.core.functions.HopeFunction;
import io.appform.hope.core.utils.Converters;
import io.appform.hope.core.values.NumericValue;
import io.appform.hope.core.values.StringValue;
import io.appform.hope.core.visitors.Evaluator;

/**
 * Returns {@link NumericValue} length of provided evaluated {@link StringValue} parameter.
 */
@FunctionImplementation("str.len")
public class Length extends HopeFunction<NumericValue> {
    private final Value arg;

    public Length(Value arg) {
        this.arg = arg;
    }

    @Override
    public NumericValue apply(Evaluator.EvaluationContext evaluationContext) {
        return new NumericValue((double)Converters.stringValue(evaluationContext, arg, "").length());
    }
}
