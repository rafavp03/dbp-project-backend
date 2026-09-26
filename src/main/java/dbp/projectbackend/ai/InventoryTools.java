package dbp.projectbackend.ai;

import dbp.projectbackend.dtos.response.report.LowStockDTO;
import dbp.projectbackend.services.ReportService;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class InventoryTools {

    private static final int MAX_ROWS = 30;

    private final ReportService reporteService;
    private final Long empresaId;

    public InventoryTools(ReportService reporteService, Long empresaId) {
        this.reporteService = reporteService;
        this.empresaId = empresaId;
    }

    @Tool(description = "Tallas y colores que llegaron a su stock minimo o menos, es decir, lo que hay que reponer pronto. "
            + "Ordenado de menor a mayor stock.")
    public List<LowStockDTO> lowStock() {
        return reporteService.lowStock(empresaId).stream().limit(MAX_ROWS).toList();
    }
}
