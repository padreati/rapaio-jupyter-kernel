package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.*;

public record LineMagicHandler(
        String syntaxMatcher,
        String syntaxHelp,
        String syntaxPrefix,
        List<String> documentation,
        Predicate<MagicSnippet> canHandlePredicate,
        MagicFunction<Object> evalFunction,
        MagicFunction<DisplayData> inspectFunction,
        MagicFunction<CompleteMatches> completeFunction
) {

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private String syntaxMatcher;
        private String syntaxHelp;
        private String syntaxPrefix;
        private List<String> documentation;
        private Predicate<MagicSnippet> canHandlePredicate;
        private MagicFunction<Object> evalFunction;
        private MagicFunction<DisplayData> inspectFunction = null;
        private MagicFunction<CompleteMatches> completeFunction;

        public Builder syntaxMatcher(String syntaxMatcher) {
            this.syntaxMatcher = syntaxMatcher;
            return this;
        }

        public Builder syntaxHelp(String syntaxHelp) {
            this.syntaxHelp = syntaxHelp;
            return this;
        }

        public Builder syntaxPrefix(String syntaxPrefix) {
            this.syntaxPrefix = syntaxPrefix;
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

        public Builder evalFunction(MagicFunction<Object> evalFunction) {
            this.evalFunction = evalFunction;
            return this;
        }

        public Builder inspectFunction(MagicFunction<DisplayData> inspectFunction) {
            this.inspectFunction = inspectFunction;
            return this;
        }

        public Builder completeFunction(MagicFunction<CompleteMatches> completeFunction) {
            this.completeFunction = completeFunction;
            return this;
        }

        public LineMagicHandler build() {

            Objects.requireNonNull(syntaxMatcher);
            Objects.requireNonNull(syntaxHelp);
            Objects.requireNonNull(syntaxPrefix);
            Objects.requireNonNull(documentation);
            Objects.requireNonNull(canHandlePredicate);
            Objects.requireNonNull(evalFunction);

            return new LineMagicHandler(
                    syntaxMatcher, syntaxHelp, syntaxPrefix,
                    documentation, canHandlePredicate,
                    evalFunction,
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
