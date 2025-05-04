package io.github.iakariak.autocodec.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoCodec {
    boolean isGenerateCodec() default true;
    boolean isGenerateStreamCodec() default true;
}
