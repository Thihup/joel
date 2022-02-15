package dev.thihup.joel.impl.node;

import jakarta.el.ELClass;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ImportHandler;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueReference;

public record MemberNode(Node object, Node property) implements Node {
    @Override
    public Class<?> getType(ELContext context) {
        if (property instanceof IdentifierNode node)
            return context.getELResolver().getType(context, new ELClass(object.getType(context)), node.value());

        return context.getELResolver().getType(context, new ELClass(object.getType(context)), property.getValue(context));
    }

    @Override
    public Object getValue(ELContext context) {
        try {
            return context.getELResolver().getValue(context, object.getValue(context), property instanceof IdentifierNode node ? node.value() : property.getValue(context));
        } catch (ELException rootCause) {
            if (object instanceof IdentifierNode node) {
                ImportHandler importHandler = context.getImportHandler();
                if (importHandler != null) {
                    Class<?> aClass = importHandler.resolveClass(node.value());
                    if (aClass != null)
                        return context.getELResolver().getValue(context, new ELClass(aClass), property instanceof IdentifierNode identifier ? identifier.value() : property.getValue(context));
                }
            }
            throw new PropertyNotFoundException(prettyPrint(), rootCause);
        }
    }

    public ValueReference valueReference(ELContext context) {
        return new ValueReference(object.getValue(context), property instanceof IdentifierNode node ? node.value() : property.getValue(context));
    }

    @Override
    public String prettyPrint() {
        return "%s.%s".formatted(object.prettyPrint(), property.prettyPrint());
    }
}
