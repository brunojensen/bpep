package no.bekk.boss.bpep.generator;

import static no.bekk.boss.bpep.resolver.Resolver.getName;
import static no.bekk.boss.bpep.resolver.Resolver.getType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

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
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class BuilderGenerator implements Generator {

    private final boolean formatSource = true;

    public void generate(final ICompilationUnit cu, final List<IField> fields) {
        try {
            final IBuffer buffer = cu.getBuffer();
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            pw.println("");
            final IType clazz = cu.getTypes()[0];
            final int pos = clazz.getSourceRange().getOffset() + clazz.getSourceRange().getLength() - 1;
            removeOlds(cu);
            createClassConstructor(pw, clazz, fields);
            createBuilderMethods(pw, clazz, fields);
            if (formatSource) {
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
            } else
                buffer.replace(pos, 0, sw.toString());
        } catch (final JavaModelException e) {
            e.printStackTrace();
        } catch (final MalformedTreeException e) {
            e.printStackTrace();
        } catch (final BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void createClassConstructor(final PrintWriter pw, final IType clazz, final List<IField> fields)
            throws JavaModelException {
        final String clazzName = clazz.getElementName();
        pw.println(String.format("public static synchronized %s create() { ", clazzName));
        pw.println(String.format("return new %s();", clazzName));
        pw.println("}");
    }

    private void createBuilderMethods(final PrintWriter pw, final IType clazz, final List<IField> fields)
            throws JavaModelException {
        final String clazzName = clazz.getElementName();
        for (final IField field : fields) {
            createWithMethod(pw, clazzName, getName(field), getType(field));
        }
    }

    private void removeOlds(ICompilationUnit cu) throws JavaModelException {
        final IType clazz = cu.getTypes()[0];
        for (final IMethod method : clazz.getMethods()) {
            if (method.getElementName().startsWith("with")
                    || method.getElementName().startsWith("add")
                    || method.getElementName().equals("create")) {
                method.delete(true, null);
                break;
            }
        }
    }

    private void createWithMethod(final PrintWriter pw, final String clazzName, final String fieldName,
            final String fieldType) {
        pw.println(String.format("public %s %s(final %s %s) {", clazzName, getMethodName("with", fieldName), fieldType,
                fieldName));
        pw.print(String.format("this.%s = %s;", fieldName, fieldName));
        pw.println("return this;");
        pw.println("}");
    }

    private String getMethodName(final String prefix, final String fieldName) {
        final IJavaProject javaProject = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject());
        return NamingConventions.getBaseName(
                NamingConventions.VK_INSTANCE_FIELD,
                String.format("%s%s%s", prefix, String.valueOf(fieldName.charAt(0)).toUpperCase(),
                        fieldName.substring(1)), javaProject);
    }
}
