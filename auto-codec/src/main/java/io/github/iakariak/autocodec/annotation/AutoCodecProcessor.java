package io.github.iakariak.autocodec.annotation;

import com.google.auto.service.AutoService;
import com.palantir.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class AutoCodecProcessor extends AbstractProcessor {
    private Types types;
    private Elements elements;
    private Messager messager;
    private Filer filer;
    private TypeMirrors mirrors;

    @Override

    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(AutoCodec.class.getCanonicalName());
    }

    @Override
    public void init(ProcessingEnvironment env) {
        super.init(env);
        elements = env.getElementUtils();
        types = env.getTypeUtils();
        messager = env.getMessager();
        filer = env.getFiler();
        mirrors = new TypeMirrors(env);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(AutoCodec.class)) {
                var autoCodecA = element.getAnnotation(AutoCodec.class);
                if (element.getKind() != ElementKind.RECORD) {
                    messager.printError("Only record class can be generated.");
                    continue;
                }
                var components = ((TypeElement) element).getRecordComponents();

                var genClassBuilder = TypeSpec.classBuilder(element.getSimpleName() + "Codes")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
                if (autoCodecA.isGenerateCodec()) {
                    genClassBuilder.addField(generateCodec(element, components));
                }
                if (autoCodecA.isGenerateStreamCodec()) {
                    genClassBuilder.addField(generateStreamCodec(element, components));
                }
                String packageName = elements.getPackageOf(element).getQualifiedName().toString();
                JavaFile.builder(packageName, genClassBuilder.build()).build()
                        .writeTo(filer);

            }
        } catch (Exception e) {
            var out = new StringWriter();
            var pw = new PrintWriter(out);
            e.printStackTrace(pw);
            messager.printError(out.toString());
        }
        return false;

    }

    private FieldSpec generateCodec(Element element, List<? extends RecordComponentElement> components) {
        TypeMirror recordType = element.asType();
        var codecTypeErasure = types.erasure(mirrors.TYPE_CODEC);
        var codecType = types.getDeclaredType(
                (TypeElement) types.asElement(mirrors.TYPE_CODEC),
                recordType
        );
        var initBlockBuilder = CodeBlock.builder()
                .add("$T.create( i -> i.group(\n", types.erasure(mirrors.TYPE_RECORD_CODEC_BUILDER))
                .indent()
                .add(CodeBlock.join(components.stream().map(c -> {
                            var specificalCodec = Utils.getSpecificalCodec(c);
                            var codec = specificalCodec.map(pc -> CodeBlock.of("$L", pc))
                                    .or(() -> Optional.ofNullable(getBuitinCodecField(c)).map(bc -> CodeBlock.of("$T.$L", codecTypeErasure, bc)))
                                    .orElseGet(() -> CodeBlock.of("$T.CODEC", c.asType()));
                            var componentName = c.getSimpleName().toString();
                            return CodeBlock.of(
                                    "$L.fieldOf(\"$L\").forGetter($T::$L)",
                                    codec, componentName, element.asType(), componentName
                            );
                        }
                ).toList(), ",\n"))
                .add("\n")
                .unindent()
                .add(").apply(i, $T::new))", recordType);
        return FieldSpec.builder(TypeName.get(codecType), "CODEC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(initBlockBuilder.build())
                .build();
    }

    private FieldSpec generateStreamCodec(Element element, List<? extends RecordComponentElement> components) {
        var selfType = TypeName.get(element.asType());
        var encodeBlockBuilder = CodeBlock.builder();
        var decodeBlockBuilder = CodeBlock.builder();
        for (RecordComponentElement component : components) {
            var name = component.getSimpleName().toString();
            var type = component.asType();
            var isOptional = mirrors.isOptional(type);
//            var isList = mirrors.isList(type);
//            var isCollection = mirrors.isCollection(type);
            var isBoxed = isOptional /*|| isList || isCollection*/;
            var unboxedType = isBoxed
                    ? ((DeclaredType) type).getTypeArguments().getFirst()
                    : type;
            var specificalCodec = Utils.getSpecificalStreamCodec(component);
            var codecCalling = specificalCodec.map(CodeBlock::of)
                    .or(() -> getBuiltinStreamCodecField(component).map(fieldName -> CodeBlock.of("$T.$L", mirrors.TYPE_BYTE_BUF_CODECS, fieldName)))
                    .orElseGet(() -> CodeBlock.of("$T.STREAM_CODEC", unboxedType));
            if (isOptional) {
                codecCalling = CodeBlock.of("$T.optional($L)", mirrors.TYPE_BYTE_BUF_CODECS, codecCalling);
            }
            encodeBlockBuilder.addStatement("$L.encode(buf, value.$L())", codecCalling, name);
            decodeBlockBuilder.addStatement("$T v_$L = $L.decode(buf)", type, name, codecCalling);
        }

        decodeBlockBuilder.addStatement(
                "return new $T($L)",
                selfType,
                components.stream()
                                .

                        map(component -> "v_" + component.getSimpleName())
                                .

                        collect(Collectors.joining(","))
        );

        var streamCodecType = types.getDeclaredType(
                (TypeElement) types.asElement(mirrors.TYPE_STREAM_CODEC),
                mirrors.TYPE_REGISTRY_FRIENDLY_BYTE_BUF,
                element.asType()
        );
        var steamCodec = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(streamCodecType)
                .addMethod(MethodSpec.methodBuilder("encode")
                        .addParameter(TypeName.get(mirrors.TYPE_REGISTRY_FRIENDLY_BYTE_BUF), "buf")
                        .addParameter(selfType, "value")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.VOID)
                        .addCode(encodeBlockBuilder.build())
                        .build())
                .addMethod(MethodSpec.methodBuilder("decode")
                        .addParameter(TypeName.get(mirrors.TYPE_REGISTRY_FRIENDLY_BYTE_BUF), "buf")
                        .returns(selfType)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addCode(decodeBlockBuilder.build())
                        .build())
                .build();
        return FieldSpec.builder(TypeName.get(streamCodecType), "STREAM_CODEC")
                        .

                addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .

                initializer("$L", steamCodec)
                        .

                build();
    }


    private String getBuitinCodecField(RecordComponentElement component) {
        var type = component.asType();

        if (types.isSameType(type, mirrors.TYPE_BOOLEAN) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_BOOLEAN)) {
            return "BOOL";
        } else if (types.isSameType(type, mirrors.TYPE_BYTE) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_BYTE)) {
            return "BYTE";
        } else if (types.isSameType(type, mirrors.TYPE_SHORT) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_SHORT)) {
            return "SHORT";
        } else if (types.isSameType(type, mirrors.TYPE_INTEGER) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_INT)) {
            return "INT";
        } else if (types.isSameType(type, mirrors.TYPE_LONG) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_LONG)) {
            return "LONG";
        } else if (types.isSameType(type, mirrors.TYPE_FLOAT) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_FLOAT)) {
            return "FLOAT";
        } else if (types.isSameType(type, mirrors.TYPE_DOUBLE) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_DOUBLE)) {
            return "DOUBLE";
        } else if (types.isSameType(type, mirrors.TYPE_BYTE_BUFFER)) {
            return "BYTE_BUFFER";
        } else if (types.isSameType(type, mirrors.TYPE_INT_STREAM)) {
            return "INT_STREAM";
        } else if (types.isSameType(type, mirrors.TYPE_LONG_STREAM)) {
            return "LONG_STREAM";
        } else if (types.isSameType(type, mirrors.TYPE_STRING)) {
            return "STRING";
        }

        return null;
    }


    private Optional<String> getBuiltinStreamCodecField(RecordComponentElement component) {
        var type = component.asType();

        if (types.isSameType(type, mirrors.TYPE_BOOLEAN) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_BOOLEAN)) {
            return Optional.of("BOOL");
        } else if (types.isSameType(type, mirrors.TYPE_BYTE) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_BYTE)) {
            return Optional.of("BYTE");
        } else if (types.isSameType(type, mirrors.TYPE_SHORT) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_SHORT)) {
            return Optional.of("SHORT");
        } else if (types.isSameType(type, mirrors.TYPE_INTEGER) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_INT)) {
            return Optional.of("INT");
        } else if (types.isSameType(type, mirrors.TYPE_LONG) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_LONG)) {
            return Optional.of("LONG");
        } else if (types.isSameType(type, mirrors.TYPE_FLOAT) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_FLOAT)) {
            return Optional.of("FLOAT");
        } else if (types.isSameType(type, mirrors.TYPE_DOUBLE) || types.isSameType(type, mirrors.TYPE_PRIMITIVE_DOUBLE)) {
            return Optional.of("DOUBLE");
        } else if (types.isSameType(type, mirrors.TYPE_PRIMITIVE_BYTE_ARRAY)) {
            return Optional.of("BYTE_ARRAY");
        } else if (types.isSameType(type, mirrors.TYPE_STRING)) {
            return Optional.of("STRING_UTF8");
        } else if (types.isSameType(type, mirrors.TYPE_TAG)) {
            return Optional.of("TAG");
        } else if (types.isSameType(type, mirrors.TYPE_COMPOUND_TAG)) {
            return Optional.of("COMPOUND_TAG");
        } else if (types.isSameType(type, mirrors.TYPE_OPTIONAL_COMPONENT_TAG)) {
            return Optional.of("OPTIONAL_COMPOUND_TAG");
        } else if (types.isSameType(type, mirrors.TYPE_OPTIONAL_INT)) {
            return Optional.of("OPTIONAL_VAR_INT");
        } else if (types.isSameType(type, mirrors.TYPE_VECTOR3F)) {
            return Optional.of("VECTOR3F");
        } else if (types.isSameType(type, mirrors.TYPE_QUATERNIONF)) {
            return Optional.of("QUATERNIONF");
        } else if (types.isSameType(type, mirrors.TYPE_GAME_PROFILE)) {
            return Optional.of("GAME_PROFILE");
        }

        return Optional.empty();
    }
}



