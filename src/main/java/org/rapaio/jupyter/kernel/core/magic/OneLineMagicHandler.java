package org.rapaio.jupyter.kernel.core.magic;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.b;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.br;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.each;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.join;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.space;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.texts;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;

public record OneLineMagicHandler(
        String syntaxMatcher,
        String syntaxHelp,
        List<String> documentation,
        Predicate<MagicSnippet> canHandlePredicate,
        OneLineMagicEvalFunction evalFunction,
        BiFunction<RapaioKernel, MagicSnippet, DisplayData> inspectFunction,
        BiFunction<RapaioKernel, MagicSnippet, CompleteMatches> completeFunction
) {

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private String syntaxMatcher;
        private String syntaxHelp;
        private List<String> documentation;
        private Predicate<MagicSnippet> canHandlePredicate;
        private OneLineMagicEvalFunction evalFunction;
        private BiFunction<RapaioKernel, MagicSnippet, DisplayData> inspectFunction = null;
        private BiFunction<RapaioKernel, MagicSnippet, CompleteMatches> completeFunction;

        public Builder syntaxMatcher(String syntaxMatcher) {
            this.syntaxMatcher = syntaxMatcher;
            return this;
        }

        public Builder syntaxHelp(String syntaxHelp) {
            this.syntaxHelp = syntaxHelp;
            return this;
        }

        public Builder documentation(List<String> documentation) {
            this.documentation = documentation;
            return this;
        }

        public Builder canHandlePredicate(Predicate<MagicSnippet> canHandlePredicate) {
            this.canHandlePredicate = canHandlePredicate;
            return this;
        }

        public Builder evalFunction(OneLineMagicEvalFunction evalFunction) {
            this.evalFunction = evalFunction;
            return this;
        }

        public Builder inspectFunction(BiFunction<RapaioKernel, MagicSnippet, DisplayData> inspectFunction) {
            this.inspectFunction = inspectFunction;
            return this;
        }

        public Builder completeFunction(BiFunction<RapaioKernel, MagicSnippet, CompleteMatches> completeFunction) {
            this.completeFunction = completeFunction;
            return this;
        }

        public OneLineMagicHandler build() {
            return new OneLineMagicHandler(syntaxMatcher, syntaxHelp, documentation, canHandlePredicate, evalFunction,
                    inspectFunction != null ? inspectFunction : this::defaultInspect,
                    completeFunction);
        }

        private DisplayData defaultInspect(RapaioKernel kernel, MagicSnippet magicSnippet) {
            String html = join(
                    texts("Syntax: "), br(),
                    join(
                            b(texts(syntaxHelp)),
                            br(),
                            each(documentation, line -> join(space(4), texts(line), br()))
                    )
            ).render();

            StringBuilder sb = new StringBuilder();
            sb.append("Syntax:\n");
            sb.append(ANSI.start().bold().text(syntaxHelp).render()).append("\n");
            for (var line : documentation) {
                sb.append("    ").append(line).append("\n");
            }

            DisplayData dd = DisplayData.withHtml(html);
            dd.putText(sb.toString());
            return dd;
        }

    }
}
