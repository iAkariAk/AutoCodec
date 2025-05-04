package io.github.iakariak.autocodec.annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public final class TypeMirrors {
    private final Elements elements;
    private final Types types;

    public boolean isSameTypeErasure(TypeMirror one, TypeMirror other) {
        return types.isSameType(types.erasure(one), types.erasure(other));
    }

    public boolean isOptional(TypeMirror type) {
        return isSameTypeErasure(types.erasure(type), TYPE_OPTIONAL);
    }

    public boolean isList(TypeMirror type) {
        return isSameTypeErasure(types.erasure(type), TYPE_LIST);
    }

    public boolean isCollection(TypeMirror type) {
        return isSameTypeErasure(types.erasure(type), TYPE_COLLECTION);
    }

    public TypeMirrors(ProcessingEnvironment env) {
        elements = env.getElementUtils();
        types = env.getTypeUtils();
        TYPE_STRING = elements.getTypeElement("java.lang.String").asType();
        // Primitive types
        TYPE_PRIMITIVE_BOOLEAN = types.getPrimitiveType(TypeKind.BOOLEAN);
        TYPE_PRIMITIVE_BYTE = types.getPrimitiveType(TypeKind.BYTE);
        TYPE_PRIMITIVE_SHORT = types.getPrimitiveType(TypeKind.SHORT);
        TYPE_PRIMITIVE_INT = types.getPrimitiveType(TypeKind.INT);
        TYPE_PRIMITIVE_LONG = types.getPrimitiveType(TypeKind.LONG);
        TYPE_PRIMITIVE_FLOAT = types.getPrimitiveType(TypeKind.FLOAT);
        TYPE_PRIMITIVE_DOUBLE = types.getPrimitiveType(TypeKind.DOUBLE);
        TYPE_PRIMITIVE_BYTE_ARRAY = types.getArrayType(types.getPrimitiveType(TypeKind.BYTE));

        // Boxed types
        TYPE_BOOLEAN = elements.getTypeElement("java.lang.Boolean").asType();
        TYPE_BYTE = elements.getTypeElement("java.lang.Byte").asType();
        TYPE_SHORT = elements.getTypeElement("java.lang.Short").asType();
        TYPE_INTEGER = elements.getTypeElement("java.lang.Integer").asType();
        TYPE_LONG = elements.getTypeElement("java.lang.Long").asType();
        TYPE_FLOAT = elements.getTypeElement("java.lang.Float").asType();
        TYPE_DOUBLE = elements.getTypeElement("java.lang.Double").asType();
        TYPE_BYTE_BUFFER = elements.getTypeElement("java.nio.ByteBuffer").asType();
        TYPE_INT_STREAM = elements.getTypeElement("java.util.stream.IntStream").asType();
        TYPE_LONG_STREAM = elements.getTypeElement("java.util.stream.LongStream").asType();
        TYPE_OPTIONAL = elements.getTypeElement("java.util.Optional").asType();
        TYPE_OPTIONAL_INT = elements.getTypeElement("java.util.OptionalInt").asType();
        TYPE_LIST = elements.getTypeElement("java.util.List").asType();
        TYPE_COLLECTION = elements.getTypeElement("java.util.Collection").asType();

        // Other classes
        TYPE_REGISTRY_FRIENDLY_BYTE_BUF = elements.getTypeElement("net.minecraft.network.RegistryFriendlyByteBuf").asType();
        TYPE_BYTE_BUF_CODECS = elements.getTypeElement("net.minecraft.network.codec.ByteBufCodecs").asType();
        TYPE_RECORD_CODEC_BUILDER = elements.getTypeElement("com.mojang.serialization.codecs.RecordCodecBuilder").asType();
        TYPE_CODEC = elements.getTypeElement("com.mojang.serialization.Codec").asType();
        TYPE_STREAM_CODEC = elements.getTypeElement("net.minecraft.network.codec.StreamCodec").asType();
        TYPE_TAG = elements.getTypeElement("net.minecraft.nbt.Tag").asType();
        TYPE_COMPOUND_TAG = elements.getTypeElement("net.minecraft.nbt.CompoundTag").asType();
        // Optional<CompoundTag>
        TYPE_OPTIONAL_COMPONENT_TAG = types.getDeclaredType((TypeElement) types.asElement(TYPE_OPTIONAL), TYPE_COMPOUND_TAG);
        TYPE_VECTOR3F = elements.getTypeElement("org.joml.Vector3f").asType();
        TYPE_QUATERNIONF = elements.getTypeElement("org.joml.Quaternionf").asType();
        TYPE_GAME_PROFILE = elements.getTypeElement("com.mojang.authlib.GameProfile").asType();

    }

    public final TypeMirror TYPE_STRING;

    // Primitive types
    public final TypeMirror TYPE_PRIMITIVE_BOOLEAN;
    public final TypeMirror TYPE_PRIMITIVE_BYTE;
    public final TypeMirror TYPE_PRIMITIVE_SHORT;
    public final TypeMirror TYPE_PRIMITIVE_INT;
    public final TypeMirror TYPE_PRIMITIVE_LONG;
    public final TypeMirror TYPE_PRIMITIVE_FLOAT;
    public final TypeMirror TYPE_PRIMITIVE_DOUBLE;
    public final TypeMirror TYPE_PRIMITIVE_BYTE_ARRAY;

    // Boxed types
    public final TypeMirror TYPE_BOOLEAN;
    public final TypeMirror TYPE_BYTE;
    public final TypeMirror TYPE_SHORT;
    public final TypeMirror TYPE_INTEGER;
    public final TypeMirror TYPE_LONG;
    public final TypeMirror TYPE_FLOAT;
    public final TypeMirror TYPE_DOUBLE;
    public final TypeMirror TYPE_BYTE_BUFFER;
    public final TypeMirror TYPE_INT_STREAM;
    public final TypeMirror TYPE_LONG_STREAM;
    public final TypeMirror TYPE_OPTIONAL;
    public final TypeMirror TYPE_OPTIONAL_INT;
    public final TypeMirror TYPE_COLLECTION;
    public final TypeMirror TYPE_LIST;

    // Other types
    public final TypeMirror TYPE_REGISTRY_FRIENDLY_BYTE_BUF;
    public final TypeMirror TYPE_BYTE_BUF_CODECS;
    public final TypeMirror TYPE_RECORD_CODEC_BUILDER;
    public final TypeMirror TYPE_CODEC;
    public final TypeMirror TYPE_STREAM_CODEC;
    public final TypeMirror TYPE_TAG;
    public final TypeMirror TYPE_COMPOUND_TAG;
    public final TypeMirror TYPE_OPTIONAL_COMPONENT_TAG;
    public final TypeMirror TYPE_VECTOR3F;
    public final TypeMirror TYPE_QUATERNIONF;
    public final TypeMirror TYPE_GAME_PROFILE;

}
