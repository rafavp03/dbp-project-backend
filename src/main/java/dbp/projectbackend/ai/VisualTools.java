package dbp.projectbackend.ai;

import dbp.projectbackend.dtos.response.VisualColumnDTO;
import dbp.projectbackend.dtos.response.VisualDTO;
import dbp.projectbackend.dtos.response.VisualPointDTO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VisualTools {

    private static final int MAX_POINTS = 50;
    private static final int MAX_CARDS = 6;
    private static final int MAX_COLUMNS = 8;
    private static final String REGISTERED = "Visual registrado";

    public enum ChartType { BARRAS, LINEAS }

    public enum ValueFormat { MONEDA, NUMERO, PORCENTAJE }

    public record CardInput(String etiqueta, String valor, String nota) {}

    private volatile VisualDTO visual;

    public Optional<VisualDTO> visual() {
        return Optional.ofNullable(visual);
    }

    @Tool(description = "Muestra un grafico junto a tu respuesta. BARRAS para comparar cantidades entre categorias "
            + "(por ejemplo ventas por medio de pago o por producto); LINEAS para una evolucion en el tiempo. "
            + "Usa solo cifras que obtuviste con tus otras herramientas. Maximo 50 puntos. Un solo visual por respuesta.")
    public String mostrarGrafico(
            @ToolParam(description = "BARRAS o LINEAS") ChartType tipo,
            @ToolParam(description = "Titulo corto del grafico", required = false) String titulo,
            @ToolParam(description = "MONEDA para soles, PORCENTAJE para porcentajes, NUMERO para cantidades") ValueFormat formato,
            @ToolParam(description = "Nombre de cada punto, en el mismo orden que los valores") List<String> etiquetas,
            @ToolParam(description = "Valor numerico de cada punto, en el mismo orden que las etiquetas") List<Double> valores) {
        if (tipo == null || formato == null) {
            return "No se registro el grafico: indica el tipo y el formato.";
        }
        if (etiquetas == null || valores == null || etiquetas.isEmpty() || etiquetas.size() != valores.size()) {
            return "No se registro el grafico: etiquetas y valores deben tener la misma cantidad y no estar vacios.";
        }
        if (etiquetas.size() > MAX_POINTS) {
            return "No se registro el grafico: maximo " + MAX_POINTS + " puntos.";
        }

        List<VisualPointDTO> datos = new ArrayList<>();
        for (int i = 0; i < etiquetas.size(); i++) {
            String etiqueta = clean(etiquetas.get(i));
            Double valor = valores.get(i);
            if (etiqueta == null || valor == null || !Double.isFinite(valor)) {
                return "No se registro el grafico: cada punto necesita una etiqueta y un valor numerico.";
            }
            datos.add(new VisualPointDTO(etiqueta, valor, null));
        }

        visual = new VisualDTO(tipo.name(), clean(titulo), formato.name().toLowerCase(), datos, null, null);
        return REGISTERED;
    }

    @Tool(description = "Muestra un resumen de pocas cifras como tarjetas junto a tu respuesta (por ejemplo total vendido, "
            + "ganancia y ticket promedio). Maximo 6 tarjetas. Usa solo cifras que obtuviste con tus otras herramientas. "
            + "Un solo visual por respuesta.")
    public String mostrarTarjetas(
            @ToolParam(description = "Titulo corto del resumen", required = false) String titulo,
            @ToolParam(description = "Tarjetas: etiqueta, valor ya formateado como texto (por ejemplo S/ 4,270.50) y nota opcional")
            List<CardInput> tarjetas) {
        if (tarjetas == null || tarjetas.isEmpty() || tarjetas.size() > MAX_CARDS) {
            return "No se registraron las tarjetas: debe haber entre 1 y " + MAX_CARDS + ".";
        }

        List<VisualPointDTO> datos = new ArrayList<>();
        for (CardInput tarjeta : tarjetas) {
            String etiqueta = tarjeta == null ? null : clean(tarjeta.etiqueta());
            String valor = tarjeta == null ? null : clean(tarjeta.valor());
            if (etiqueta == null || valor == null) {
                return "No se registraron las tarjetas: cada una necesita etiqueta y valor.";
            }
            datos.add(new VisualPointDTO(etiqueta, valor, clean(tarjeta.nota())));
        }

        visual = new VisualDTO("TARJETAS", clean(titulo), ValueFormat.NUMERO.name().toLowerCase(), datos, null, null);
        return REGISTERED;
    }

    @Tool(description = "Muestra una tabla junto a tu respuesta, util para listas con varias columnas (stock bajo, "
            + "mercaderia parada, ranking de productos). Maximo 8 columnas y 50 filas. Usa solo datos que obtuviste con "
            + "tus otras herramientas. Un solo visual por respuesta.")
    public String mostrarTabla(
            @ToolParam(description = "Titulo corto de la tabla", required = false) String titulo,
            @ToolParam(description = "Nombre de cada columna") List<String> encabezados,
            @ToolParam(description = "Filas de la tabla; cada fila es una lista de textos, uno por columna, en el mismo orden que los encabezados")
            List<List<String>> filas) {
        if (encabezados == null || encabezados.isEmpty() || encabezados.size() > MAX_COLUMNS) {
            return "No se registro la tabla: debe haber entre 1 y " + MAX_COLUMNS + " columnas.";
        }
        if (filas == null || filas.isEmpty() || filas.size() > MAX_POINTS) {
            return "No se registro la tabla: debe haber entre 1 y " + MAX_POINTS + " filas.";
        }

        List<VisualColumnDTO> columnas = new ArrayList<>();
        for (int i = 0; i < encabezados.size(); i++) {
            String encabezado = clean(encabezados.get(i));
            if (encabezado == null) {
                return "No se registro la tabla: cada columna necesita un nombre.";
            }
            columnas.add(new VisualColumnDTO("c" + i, encabezado));
        }

        List<Map<String, Object>> registros = new ArrayList<>();
        for (List<String> fila : filas) {
            if (fila == null || fila.size() != columnas.size()) {
                return "No se registro la tabla: cada fila debe tener una celda por columna.";
            }
            Map<String, Object> registro = new LinkedHashMap<>();
            for (int i = 0; i < columnas.size(); i++) {
                String celda = fila.get(i);
                registro.put(columnas.get(i).clave(), celda == null ? "" : celda.strip());
            }
            registros.add(registro);
        }

        visual = new VisualDTO("TABLA", clean(titulo), null, null, columnas, registros);
        return REGISTERED;
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}
