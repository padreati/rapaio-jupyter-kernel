package org.rapaio.jupyter.kernel.core.display.text;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.java.CompilerException;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;

import jdk.jshell.DeclarationSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;

public class ANSITextTest {

    @Test
    void sandboxTest() {
        String code = """
            for(int i=0; i<10; i++) {
                y=12;
            }
            """;
        JavaEngine engine = JavaEngine.builder().build();
        CompilerException ce = assertThrows(CompilerException.class, () -> engine.eval(code));

        List<String> msgs = new ArrayList<>();
        SnippetEvent event = ce.getBadSnippetCompilation();
        Snippet snippet = event.snippet();
        var diagnostics = engine.getShell().diagnostics(snippet).toList();
        for (var d : diagnostics) {
            msgs.addAll(ANSIText.compileErrorSourceCode(snippet.source(), (int) d.getPosition(),
                    (int) d.getStartPosition(), (int) d.getEndPosition()));

            msgs.addAll(ANSIText.compileErrorMessages(d.getMessage(Locale.getDefault())));
            msgs.add("");
        }
        // Declaration snippets are unique in that they can be active with unresolved references
        if (snippet instanceof DeclarationSnippet declarationSnippet) {
            List<String> unresolvedDependencies = engine.getShell().unresolvedDependencies(declarationSnippet).toList();
            if (!unresolvedDependencies.isEmpty()) {
                msgs.addAll(ANSIText.compileErrorSourceCode(snippet.source(), -1, -1, -1));
                msgs.addAll(ANSIText.compileErrorMessages("Unresolved dependencies:"));
                unresolvedDependencies.forEach(dep -> msgs.addAll(ANSIText.compileErrorMessages("   - " + dep)));
            }
        }

        for(var msg : msgs) {
            System.out.println(msg);
        }

    }
}
