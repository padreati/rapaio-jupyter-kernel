package org.rapaio.jupyter.kernel.display.table;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.display.html.Html;
import org.rapaio.jupyter.kernel.global.Global;

/**
 * Default renderer for table display model.
 */
public class DefaultTableDisplayRenderer implements DisplayRenderer {

    private static final Map<MimeType, Function<TableDisplay, DisplayData>> supportedTypes = Map.of(
            MimeType.HTML, DefaultTableDisplayRenderer::displayHtml,
            MimeType.MARKDOWN, DefaultTableDisplayRenderer::displayMarkdown);

    @Override
    public Class<?> rendererClass() {
        return TableDisplay.class;
    }

    @Override
    public boolean canRender(String mimeType) {
        MimeType mime = MimeType.from(mimeType, Global.options().display().defaultMime());
        return supportedTypes.containsKey(mime);
    }

    @Override
    public DisplayData render(String mimeType, Object o) {
        if (!(o instanceof TableDisplay td)) {
            return null;
        }
        MimeType mime = MimeType.from(mimeType, Global.options().display().defaultMime());
        if (supportedTypes.containsKey(mime)) {
            return supportedTypes.get(mime).apply(td);
        }
        return null;
    }

    private static DisplayData displayHtml(TableDisplay td) {
        String id = UUID.randomUUID().toString();

        int maxRows = Global.options().display().maxRows();
        maxRows = maxRows == 0 ? td.getRows() : Math.min(maxRows, td.getRows());

        int maxCols = Global.options().display().maxCols();
        maxCols = maxCols == 0 ? td.getCols() : Math.min(maxCols, td.getCols());

        boolean hasMaxRows = td.getRows() > maxRows;
        boolean hasMaxCols = td.getCols() > maxCols;

        StringBuilder sb = new StringBuilder();
        int border = Global.options().display().html().border();
        sb.append(String.format("<table border=\"" + border + "\" class=\"dataframe\" id=\"%s\">\n", id));

        boolean showIndex = Global.options().display().showIndex();

        if (td.hasHeader()) {
            sb.append("<thead>\n");
            sb.append("<tr>");
            if (showIndex) {
                sb.append("<th></th>");
            }
            for (int j = 0; j < maxCols; j++) {
                sb.append(String.format("<th style=\"text-align: center;\">%s</th>", Html.encode(td.columnName(j))));
            }
            if (hasMaxCols) {
                sb.append("<th>...</th>");
            }
            sb.append("</tr>\n");
            sb.append("</thead>\n");
        }

        sb.append("<tbody>\n");
        for (int i = 0; i < maxRows; i++) {
            sb.append("<tr>");
            if (showIndex) {
                sb.append(String.format("<th>%d</th>", i));
            }
            for (int j = 0; j < maxCols; j++) {
                sb.append(String.format("<td style=\"text-align: %s;\">%s</td>", td.columnAlignment(j).html(),
                        Html.encode(td.getFormattedValue(i, j))));
            }
            if (hasMaxCols) {
                sb.append("<td>...</td>");
            }
            sb.append("</tr>\n");
        }
        if (hasMaxRows) {
            sb.append("<tr>");
            if (Global.options().display().showIndex()) {
                sb.append("<th>...</th>");
            }
            sb.append("<td>...</td>".repeat(Math.max(0, maxCols)));
            if (hasMaxCols) {
                sb.append("<td>...</td>");
            }
            sb.append("</tr>\n");
        }
        sb.append("</tbody>\n");
        sb.append("</table>");
        sb.append(String.format("<p>%d rows × %d columns</p>", td.getRows(), td.getCols()));

        DisplayData dd = DisplayData.fromHtml(sb.toString());
        dd.setDisplayId(id);
        return dd;
    }

    private static DisplayData displayMarkdown(TableDisplay td) {
        String id = UUID.randomUUID().toString();

        int maxRows = Global.options().display().maxRows();
        maxRows = maxRows == 0 ? td.getRows() : Math.min(maxRows, td.getRows());

        int maxCols = Global.options().display().maxCols();
        maxCols = maxCols == 0 ? td.getCols() : Math.min(maxCols, td.getCols());

        boolean hasMaxRows = td.getRows() > maxRows;
        boolean hasMaxCols = td.getCols() > maxCols;

        StringBuilder sb = new StringBuilder();
        if (td.hasHeader()) {
            sb.append("| ");
            if (Global.options().display().showIndex()) {
                sb.append(" |");
            }
            for (int j = 0; j < maxCols; j++) {
                sb.append(String.format("%s |", td.columnName(j)));
            }
            if (hasMaxCols) {
                sb.append(" ... |");
            }
            sb.append("\n");
            sb.append("| ");
            if (Global.options().display().showIndex()) {
                sb.append("--- |");
            }
            sb.append("--- |".repeat(Math.max(0, td.getCols())));
            if (hasMaxCols) {
                sb.append(" --- |");
            }
            sb.append("\n");
        }

        for (int i = 0; i < maxRows; i++) {
            sb.append("| ");
            if (Global.options().display().showIndex()) {
                sb.append(String.format("**%d** |", i));
            }
            for (int j = 0; j < maxCols; j++) {
                sb.append(String.format("%s |", td.getFormattedValue(i, j)));
            }
            if (hasMaxCols) {
                sb.append(" ... |");
            }
            sb.append("\n");
        }
        if (hasMaxRows) {
            sb.append("| ");
            if (Global.options().display().showIndex()) {
                sb.append(" |");
            }
            sb.append("... |".repeat(maxCols));
            if (hasMaxCols) {
                sb.append(" ... |");
            }
            sb.append("\n");
        }

        DisplayData dd = DisplayData.withType(MimeType.MARKDOWN.longType(), sb.toString());
        dd.setDisplayId(id);
        return dd;
    }
}
