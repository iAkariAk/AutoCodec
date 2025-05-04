package io.github.iakariak.autocodec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * CodecMapping a codec for record component
 *
 * Use the type via `~` in the expression
 * e.g. if wanting specify `net.minecraft.world.item.ItemStack.STREAM_CODEC`, you can use `~.STREAM_CODEC`
 *
 * @param codec expression that return `Codec`
 * @param streamCodec expression that return `StreamCodec`
 */
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.SOURCE)
public @interface CodecMapping {
    String value() default "";

    String codec() default "";

    String streamCodec() default "";
}
