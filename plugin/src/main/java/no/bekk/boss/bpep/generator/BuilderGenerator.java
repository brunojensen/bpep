package no.bekk.boss.bpep.generator;

import static no.bekk.boss.bpep.resolver.Resolver.getName;
import static no.bekk.boss.bpep.resolver.Resolver.getType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import no.bekk.boss.bpep.resolver.Resolver;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

public class BuilderGenerator implements Generator {

    public void generate(final ICompilationUnit compilationUnit) {
        try {
            final IType clazz = compilationUnit.getTypes()[0];
            final IBuffer buffer = compilationUnit.getBuffer();
            internalGenerat(clazz, buffer);
            for(IType innerClazz : clazz.getTypes()) {
                if(innerClazz.isClass())
                    internalGenerat(innerClazz, buffer);
            }
        } catch (final Exception e) {
            Logger.getAnonymousLogger().info(e.getMessage());
        } finally {
        }
    }

    private void internalGenerat(final IType clazz, final IBuffer buffer) throws JavaModelException, BadLocationException {
        final int pos = clazz.getSourceRange().getOffset() + clazz.getSourceRange().getLength() - 1;
        final List<IField> fields = Resolver.findAllFields(clazz);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("");
        createClassConstructor(pw, clazz, fields);
        createBuilderMethods(pw, clazz, fields);
        pw.println("");
        buffer.replace(pos, 0, sw.toString());
        final String builderSource = buffer.getContents();
        final TextEdit text = ToolFactory.createCodeFormatter(null).format(CodeFormatter.K_COMPILATION_UNIT,
                builderSource, 0, builderSource.length(), 0, "\n");
        if (text != null) {
            final Document simpleDocument = new Document(builderSource);
            text.apply(simpleDocument);
            buffer.setContents(simpleDocument.get());
        }
    }

    private void createClassConstructor(final PrintWriter pw, final IType clazz, final List<IField> fields)
            throws JavaModelException {
        final String clazzName = clazz.getElementName();
        if (!clazz.getMethod("create", null).exists()) {
            pw.println(String.format("public static synchronized %s create() { ", clazzName));
            pw.println(String.format("return new %s();", clazzName));
            pw.println("}");
        }
    }

    private void createBuilderMethods(final PrintWriter pw, final IType clazz, final List<IField> fields)
            throws JavaModelException {
        for (final IField field : fields) {
            final String fieldName = getName(field);
            final String fieldType = getType(field);
            createWithMethod(pw, clazz, fieldName, fieldType);
        }
    }

    private void createWithMethod(final PrintWriter pw, final IType clazz, final String fieldName,
            final String fieldType) {
        final String methodName = getMethodName("with", fieldName);
        if (!methodExists(clazz, methodName)) {
            pw.println(String.format("public %s %s(final %s %s) {", clazz.getElementName(), methodName, fieldType,
                    fieldName));
            pw.print(String.format("this.%s = %s;", fieldName, fieldName));
            pw.println("return this;");
            pw.println("}");
        }
    }

    private boolean methodExists(final IType clazz, final String methodName) {
        try {
            for (final IMethod method : clazz.getMethods()) {
                if (method.getElementName().equals(methodName)) {
                    return true;
                }
            }
        } catch (final JavaModelException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    private String getMethodName(final String prefix, final String fieldName) {
        final IJavaProject javaProject = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject());
        return NamingConventions.getBaseName(
                NamingConventions.VK_INSTANCE_FIELD,
                String.format("%s%s%s", prefix, String.valueOf(fieldName.charAt(0)).toUpperCase(),
                        fieldName.substring(1)), javaProject);
    }
}
