package no.bekk.boss.bpep.view;

import no.bekk.boss.bpep.generator.BuilderGenerator;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class CreateDialog extends AbstractModalDialog {

    public CreateDialog(final Shell parent) {
        super(parent);
    }

    public void show(final ICompilationUnit compilationUnit) throws JavaModelException {
        final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.CENTER);
        shell.setSize(300, 300);
        shell.setText("Generate Builder Pattern Code");
        shell.setLayout(new GridLayout(2, false));
        final Button executeButton = new Button(shell, SWT.PUSH);
        executeButton.setText("Generate");
        shell.setDefaultButton(executeButton);
        final Button cancelButton = new Button(shell, SWT.PUSH);
        cancelButton.setText("Cancel");

        final Listener clickListener = new Listener() {
            public void handleEvent(final Event event) {
                if (event.widget == executeButton) {
                    new BuilderGenerator().generate(compilationUnit);
                    shell.dispose();
                } else
                    shell.dispose();
            }
        };

        executeButton.addListener(SWT.Selection, clickListener);
        cancelButton.addListener(SWT.Selection, clickListener);

        display(shell);
    }
}
