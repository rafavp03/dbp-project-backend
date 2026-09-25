package dbp.projectbackend.ai;

import dbp.projectbackend.dtos.reportes.StockBajoDTO;
import dbp.projectbackend.services.ReporteService;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class HerramientasInventario {

    private static final int MAX_FILAS = 30;

    private final ReporteService reporteService;
    private final Long empresaId;

    public HerramientasInventario(ReporteService reporteService, Long empresaId) {
        this.reporteService = reporteService;
        this.empresaId = empresaId;
    }

    @Tool(description = "Tallas y colores que llegaron a su stock minimo o menos, es decir, lo que hay que reponer pronto. "
            + "Ordenado de menor a mayor stock.")
    public List<StockBajoDTO> stockBajo() {
        return reporteService.stockBajo(empresaId).stream().limit(MAX_FILAS).toList();
    }
}
