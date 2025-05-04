package io.github.iakariak.autocodec.annotation;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.Optional;

public class Utils {
    public static Optional<String> getStreamCodecExpression(CodecMapping maooing) {
        if (maooing == null) return Optional.empty();
        if (!maooing.streamCodec().isEmpty()) return Optional.of(maooing.streamCodec());
        if (!maooing.value().isEmpty()) return Optional.of(maooing.value());
        return Optional.empty();
    }

    public static Optional<String> getCodecExpression(CodecMapping mapping) {
        if (mapping == null) return Optional.empty();
        if (!mapping.codec().isEmpty()) return Optional.of(mapping.codec());
        if (!mapping.value().isEmpty()) return Optional.of(mapping.value());
        return Optional.empty();
    }

    public static String expandExpression(RecordComponentElement element, String expression) {
        DeclaredType type = (DeclaredType) element.asType();
        TypeElement typeElement = (TypeElement) type.asElement();
        var typeName = typeElement.getQualifiedName().toString();
        return expression.replace("~", typeName);
    }

    public static Optional<String> getSpecificalCodec(RecordComponentElement component) {
        return Utils.getCodecExpression(component.getAnnotation(CodecMapping.class))
                .map(s -> Utils.expandExpression(component, s));
    }

    public static Optional<String> getSpecificalStreamCodec(RecordComponentElement component) {
        return Utils.getStreamCodecExpression(component.getAnnotation(CodecMapping.class))
                .map(s -> Utils.expandExpression(component, s));
    }
}
