package no.bekk.boss.bpep.resolver;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class Resolver {
    public static String getName(final IField field) {
        return field.getElementName();
    }

    public static String getType(final IField field) {
        try {
            return Signature.toString(field.getTypeSignature());
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final JavaModelException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<IField> findAllFields(final ICompilationUnit compilationUnit) {
        final List<IField> fields = new ArrayList<IField>();
        try {
            final IType clazz = compilationUnit.getTypes()[0];

            for (final IField field : clazz.getFields()) {
                final int flags = field.getFlags();
                final boolean notStatic = !Flags.isStatic(flags);
                if (notStatic)
                    fields.add(field);
            }

        } catch (final JavaModelException e) {
            e.printStackTrace();
        }
        return fields;
    }
    public static List<IField> findAllFields(final IType clazz) {
        final List<IField> fields = new ArrayList<IField>();
        try {
            for (final IField field : clazz.getFields()) {
                final int flags = field.getFlags();
                final boolean notStatic = !Flags.isStatic(flags);
                if (notStatic)
                    fields.add(field);
            }
        } catch (final JavaModelException e) {
            e.printStackTrace();
        }
        return fields;
    }
}
