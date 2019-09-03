package io.disc99.protoc.gen.spring.generator;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;

@Immutable
public abstract class AbstractDescriptor {

    /**
     * The non-qualified name of this descriptor.
     */
    protected final String name;

    /**
     * The Java package this descriptor's code will be in.
     */
    protected final String javaPkgName;

    /**
     * The protobuf package that defined the descriptor.
     */
    protected final String protoPkgName;

    /**
     * The outer class the descriptor's code will be in.
     */
    protected final FileDescriptorProcessingContext.OuterClass outerClass;

    protected final Registry registry;

    /**
     * The names of the outer messages for this descriptor.
     * Only non-empty for nested {@link MessageDescriptor} and {@link EnumDescriptor}
     * definitions.
     */
    private final List<String> outerMessages;

    protected AbstractDescriptor(@Nonnull final FileDescriptorProcessingContext context,
                              @Nonnull final String name) {
        this.name = name;
        this.javaPkgName = context.getJavaPackage();
        this.protoPkgName = context.getProtobufPackage();
        this.outerClass = context.getOuterClass();

        this.registry = context.getRegistry();

        // Report the name of the newly instantiated descriptor
        // to the outer class. This is to track name collisions
        // between the outer class and the descriptors within it.
        outerClass.onNewDescriptor(name);

        // Make a copy, because the outers in the context change
        // during processing.
        this.outerMessages = new ArrayList<>(context.getOuters());
    }

    /**
     * Get the unqualified name of this message. For example:
     * message Msg { <-- Msg
     *     message Msg2 { <-- Msg2
     *         message Msg3 {} <-- Msg3
     *     }
     * }
     * @return The unqualified name of the message.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Gets the name of this message within the overarching outer class
     * that's generated for every .proto.
     * <p>
     * If the message is NOT nested, this name is equivalent to
     * {@link AbstractDescriptor#getName}.
     * <p>
     * Otherwise, it's the path to the nested message. For example:
     * message Msg { <-- Msg
     *     message Msg2 { <-- Msg.Msg2
     *         message Msg3 {} <-- Msg.Msg2.Msg3
     *     }
     * }
     * @return The name of this message qualified within the overarching
     *         outer class.
     */
    @Nonnull
    public String getNameWithinOuterClass() {
        if (outerMessages == null || outerMessages.isEmpty()) {
            return name;
        } else {
            return StringUtils.join(outerMessages, ".") + "." + name;
        }
    }

    /**
     * Get the fully qualified name of the class this descriptor
     * will generate.
     *
     * @return The name.
     */
    @Nonnull
    public String getQualifiedName() {
        return javaPkgName + "." + outerClass.getPluginJavaClass() + "." + getNameWithinOuterClass();
    }

    @Nonnull
    public String getJavaPkgName() {
        return javaPkgName;
    }

    @Nonnull
    public String getProtoPkgName() {
        return protoPkgName;
    }

    /**
     * Get the name of the original Java class generated by the
     * protobuf compiler for the message this descriptor relates
     * to.
     *
     * @return The name.
     */
    @Nonnull
    public String getQualifiedOriginalName() {
        // If multiple files is enabled, the protobuf compiler will NOT nest the classes for
        // messages in an outer class.
        if (outerClass.isMultipleFilesEnabled()) {
            return javaPkgName + "." + getNameWithinOuterClass();
        } else {
            return javaPkgName + "." + outerClass.getProtoJavaClass() + "." + getNameWithinOuterClass();
        }
    }

    /**
     * Get the fully qualified name of the protobuf message
     * this descriptor relates to.
     *
     * @return The name.
     */
    @Nonnull
    public String getQualifiedProtoName() {
        return protoPkgName + "." + getNameWithinOuterClass();
    }
}
