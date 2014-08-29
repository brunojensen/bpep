package no.bekk.boss.bpep.generator;

import org.eclipse.jdt.core.ICompilationUnit;

public interface Generator {
    void generate(ICompilationUnit compilationUnit);
}
