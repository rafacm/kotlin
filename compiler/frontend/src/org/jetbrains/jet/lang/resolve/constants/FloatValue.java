/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.lang.resolve.constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationArgumentVisitor;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;
import org.jetbrains.jet.lang.types.JetType;

/**
 * @author alex.tkachman
 */
public class FloatValue implements CompileTimeConstant<Float> {
    private final float value;

    public FloatValue(float value) {
        this.value = value;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @NotNull
    @Override
    public JetType getType(@NotNull KotlinBuiltIns kotlinBuiltIns) {
        return kotlinBuiltIns.getFloatType();
    }

    @Override
    public <R, D> R accept(AnnotationArgumentVisitor<R, D> visitor, D data) {
        return visitor.visitFloatValue(this, data);
    }

    @Override
    public String toString() {
        return value + ".toFloat()";
    }
}
