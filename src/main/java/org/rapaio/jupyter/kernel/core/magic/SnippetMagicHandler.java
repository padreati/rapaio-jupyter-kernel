package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.*;

public record SnippetMagicHandler(
        MagicSnippet.Type type,
        String syntaxMatcher,
        List<String> syntaxHelp,
        String syntaxPrefix,
        List<String> documentation,
        Predicate<MagicSnippet> canHandlePredicate,
        MagicFunction<Object> evalFunction,
        MagicFunction<DisplayData> inspectFunction,
        MagicFunction<CompleteMatches> completeFunction
) {

    public static Builder lineMagic() {
        return new Builder(MagicSnippet.Type.MAGIC_LINE);
    }

    public static Builder cellMagic() {
        return new Builder(MagicSnippet.Type.MAGIC_CELL);
    }


    public static final class Builder {
        private final MagicSnippet.Type type;
        private String syntaxMatcher;
        private List<String> syntaxHelp;
        private String syntaxPrefix;
        private List<String> documentation;
        private Predicate<MagicSnippet> canHandlePredicate;
        private MagicFunction<Object> evalFunction;
        private MagicFunction<DisplayData> inspectFunction = null;
        private MagicFunction<CompleteMatches> completeFunction;

        private Builder(MagicSnippet.Type type) {
            this.type = type;
        }

        public Builder syntaxMatcher(String syntaxMatcher) {
            this.syntaxMatcher = syntaxMatcher;
            return this;
        }

        public Builder syntaxHelp(List<String> syntaxHelp) {
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

        public SnippetMagicHandler build() {

            Objects.requireNonNull(syntaxMatcher, "Syntax matcher is null");
            Objects.requireNonNull(syntaxHelp, "Syntax help is null");
            Objects.requireNonNull(syntaxPrefix, "Syntax prefix is null");
            Objects.requireNonNull(documentation, "Documentation is null");
            Objects.requireNonNull(canHandlePredicate, "Handler predicate is null");
            Objects.requireNonNull(evalFunction, "Eval function is null");

            return new SnippetMagicHandler(
                    type,
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
                            each(syntaxHelp, line -> join(b(texts(line)), br())),
                            each(documentation, line -> join(space(4), texts(line), br()))
                    )
            ).render();

            StringBuilder sb = new StringBuilder();
            sb.append("Syntax:\n");
            sb.append(ANSI.start().bold().text(String.join("\n", syntaxHelp)).render()).append("\n");
            for (var line : documentation) {
                sb.append("    ").append(line).append("\n");
            }

            DisplayData dd = DisplayData.withHtml(html);
            dd.putText(sb.toString());
            return dd;
        }

    }
}
