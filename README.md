# AutoCodec

The project provide an automatized generator with  Minecraft Network Codec
including `Codec` and `StreamCodec` to simplify coding network packet via APT.

## Usage
**About implementation from maven, please wait its publishing.**
But you can refer `example` module to learn more.

The `auto-codec-annoation` module provides following annotations.

**AutoCodec**: Adding the annotation to `Foo` class, the compiler will 
generate a `FooCodecs` class in same package of your class.

***Note**: You only can add it to a record class*  
```java
@AutoCodec
public record Message(
        GameProfile source,
        String content
) implements CustomedPayload {
    // ... implement type method
} 
```

Currently, the apt support map builtin codec(`ByteBufCodecs`) and `Optional<T>`


**`CodecMapping`**: Specify an expression returning a `Codec`,
placeholder `~` will be replaced the type of field you annotated field.