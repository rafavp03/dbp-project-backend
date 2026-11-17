package dbp.projectbackend.ai;

import dbp.projectbackend.ai.VisualTools.CardInput;
import dbp.projectbackend.ai.VisualTools.ChartType;
import dbp.projectbackend.ai.VisualTools.ValueFormat;
import dbp.projectbackend.dtos.response.VisualDTO;
import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VisualToolsTest {

    private final VisualTools tools = new VisualTools();

    private static List<String> labels(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add("Etiqueta " + i);
        }
        return list;
    }

    private static List<Double> values(int count) {
        return new ArrayList<>(Collections.nCopies(count, 10.0));
    }

    @Test
    void springAiGeneraEsquemasParaLasTresHerramientas() {
        ToolCallback[] callbacks = ToolCallbacks.from(tools);

        assertThat(callbacks).extracting(callback -> callback.getToolDefinition().name())
                .containsExactlyInAnyOrder("mostrarGrafico", "mostrarTarjetas", "mostrarTabla");
        for (ToolCallback callback : callbacks) {
            assertThat(callback.getToolDefinition().inputSchema()).contains("\"properties\"");
        }
        String graphSchema = Arrays.stream(callbacks)
                .filter(callback -> callback.getToolDefinition().name().equals("mostrarGrafico"))
                .findFirst().orElseThrow().getToolDefinition().inputSchema();
        assertThat(graphSchema).contains("BARRAS").contains("LINEAS").contains("MONEDA");
    }

    @Test
    void sinLlamadasNoHayVisual() {
        assertThat(tools.visual()).isEmpty();
    }

    @Test
    void registraUnGraficoDeBarrasValido() {
        String result = tools.mostrarGrafico(ChartType.BARRAS, " Ventas por pago ", ValueFormat.MONEDA,
                List.of("Yape", "Efectivo"), List.of(1850.0, 920.5));

        assertThat(result).isEqualTo("Visual registrado");
        VisualDTO visual = tools.visual().orElseThrow();
        assertThat(visual.tipo()).isEqualTo("BARRAS");
        assertThat(visual.titulo()).isEqualTo("Ventas por pago");
        assertThat(visual.formato()).isEqualTo("moneda");
        assertThat(visual.datos()).hasSize(2);
        assertThat(visual.datos().get(0).etiqueta()).isEqualTo("Yape");
        assertThat(visual.datos().get(0).valor()).isEqualTo(1850.0);
        assertThat(visual.columnas()).isNull();
        assertThat(visual.filas()).isNull();
    }

    @Test
    void registraUnGraficoDeLineasSinTitulo() {
        tools.mostrarGrafico(ChartType.LINEAS, null, ValueFormat.NUMERO, List.of("Lun", "Mar"), List.of(3.0, 5.0));

        VisualDTO visual = tools.visual().orElseThrow();
        assertThat(visual.tipo()).isEqualTo("LINEAS");
        assertThat(visual.titulo()).isNull();
        assertThat(visual.formato()).isEqualTo("numero");
    }

    @Test
    void rechazaGraficosInvalidosSinGuardarNada() {
        assertThat(tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, List.of("A", "B"), List.of(1.0)))
                .startsWith("No se registro");
        assertThat(tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, List.of(), List.of()))
                .startsWith("No se registro");
        assertThat(tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, labels(51), values(51)))
                .startsWith("No se registro");
        assertThat(tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, List.of(" "), List.of(1.0)))
                .startsWith("No se registro");
        assertThat(tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, List.of("A"), List.of(Double.NaN)))
                .startsWith("No se registro");
        assertThat(tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, List.of("A"),
                Arrays.asList((Double) null))).startsWith("No se registro");
        assertThat(tools.mostrarGrafico(null, null, ValueFormat.NUMERO, List.of("A"), List.of(1.0)))
                .startsWith("No se registro");

        assertThat(tools.visual()).isEmpty();
    }

    @Test
    void aceptaHastaCincuentaPuntos() {
        String result = tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, labels(50), values(50));

        assertThat(result).isEqualTo("Visual registrado");
        assertThat(tools.visual().orElseThrow().datos()).hasSize(50);
    }

    @Test
    void registraTarjetasConNotaOpcional() {
        String result = tools.mostrarTarjetas("Resumen", List.of(
                new CardInput("Total vendido", "S/ 4,270.50", "38 ventas"),
                new CardInput("Ganancia", "S/ 1,200.00", " ")));

        assertThat(result).isEqualTo("Visual registrado");
        VisualDTO visual = tools.visual().orElseThrow();
        assertThat(visual.tipo()).isEqualTo("TARJETAS");
        assertThat(visual.datos()).hasSize(2);
        assertThat(visual.datos().get(0).valor()).isEqualTo("S/ 4,270.50");
        assertThat(visual.datos().get(0).nota()).isEqualTo("38 ventas");
        assertThat(visual.datos().get(1).nota()).isNull();
    }

    @Test
    void rechazaTarjetasInvalidas() {
        List<CardInput> seven = new ArrayList<>(Collections.nCopies(7, new CardInput("A", "1", null)));

        assertThat(tools.mostrarTarjetas(null, seven)).startsWith("No se registraron");
        assertThat(tools.mostrarTarjetas(null, List.of())).startsWith("No se registraron");
        assertThat(tools.mostrarTarjetas(null, List.of(new CardInput("A", " ", null)))).startsWith("No se registraron");
        assertThat(tools.mostrarTarjetas(null, List.of(new CardInput(null, "1", null)))).startsWith("No se registraron");

        assertThat(tools.visual()).isEmpty();
    }

    @Test
    void registraUnaTablaGenerandoClavesYFilas() {
        String result = tools.mostrarTabla("Stock bajo", List.of("Producto", "Stock"),
                List.of(List.of("Polo basico", "2"), Arrays.asList("Casaca", null)));

        assertThat(result).isEqualTo("Visual registrado");
        VisualDTO visual = tools.visual().orElseThrow();
        assertThat(visual.tipo()).isEqualTo("TABLA");
        assertThat(visual.formato()).isNull();
        assertThat(visual.datos()).isNull();
        assertThat(visual.columnas()).extracting("clave").containsExactly("c0", "c1");
        assertThat(visual.columnas()).extracting("titulo").containsExactly("Producto", "Stock");
        assertThat(visual.filas()).hasSize(2);
        assertThat(visual.filas().get(0)).containsEntry("c0", "Polo basico").containsEntry("c1", "2");
        assertThat(visual.filas().get(1)).containsEntry("c0", "Casaca").containsEntry("c1", "");
    }

    @Test
    void rechazaTablasInvalidas() {
        List<String> nineHeaders = labels(9);
        List<List<String>> oneRow = List.of(List.of("a"));
        List<List<String>> fiftyOneRows = new ArrayList<>(Collections.nCopies(51, List.of("a")));

        assertThat(tools.mostrarTabla(null, nineHeaders, oneRow)).startsWith("No se registro");
        assertThat(tools.mostrarTabla(null, List.of(), oneRow)).startsWith("No se registro");
        assertThat(tools.mostrarTabla(null, List.of("A"), List.of())).startsWith("No se registro");
        assertThat(tools.mostrarTabla(null, List.of("A"), fiftyOneRows)).startsWith("No se registro");
        assertThat(tools.mostrarTabla(null, List.of("A", "B"), oneRow)).startsWith("No se registro");
        assertThat(tools.mostrarTabla(null, List.of("A"), List.of(List.of("a", "b")))).startsWith("No se registro");
        assertThat(tools.mostrarTabla(null, List.of(" "), oneRow)).startsWith("No se registro");

        assertThat(tools.visual()).isEmpty();
    }

    @Test
    void unaLlamadaInvalidaNoBorraUnVisualValidoPrevio() {
        tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, List.of("A"), List.of(1.0));
        tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, List.of(), List.of());

        assertThat(tools.visual().orElseThrow().tipo()).isEqualTo("BARRAS");
    }

    @Test
    void laUltimaLlamadaValidaGana() {
        tools.mostrarGrafico(ChartType.BARRAS, null, ValueFormat.NUMERO, List.of("A"), List.of(1.0));
        tools.mostrarTarjetas("Resumen", List.of(new CardInput("Total", "10", null)));

        assertThat(tools.visual().orElseThrow().tipo()).isEqualTo("TARJETAS");
    }
}
