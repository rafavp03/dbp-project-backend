package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.ChartSpecDTO;
import dbp.projectbackend.dtos.response.ChartResponseDTO;
import dbp.projectbackend.dtos.response.report.*;
import dbp.projectbackend.enums.SalesGrouping;
import dbp.projectbackend.enums.RankingCriterion;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.ChartService;
import dbp.projectbackend.services.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService service;
    private final ChartService chartService;

    @GetMapping("/summary")
    public ResponseEntity<SalesSummaryDTO> summary(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = endDate(hasta);
        return ResponseEntity.ok(service.summary(enterpriseId(user), startDate(desde, fin), fin));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<ProductRankingDTO>> topProducts(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "UNIDADES") RankingCriterion criterio,
            @RequestParam(defaultValue = "10") int limite) {
        LocalDate fin = endDate(hasta);
        return ResponseEntity.ok(service.topProducts(enterpriseId(user), startDate(desde, fin), fin, criterio, limite));
    }

    @GetMapping("/grouped-sales")
    public ResponseEntity<List<GroupedSalesDTO>> groupedSales(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam SalesGrouping por) {
        LocalDate fin = endDate(hasta);
        return ResponseEntity.ok(service.groupedSales(enterpriseId(user), startDate(desde, fin), fin, por));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockDTO>> lowStock(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.lowStock(enterpriseId(user)));
    }

    @GetMapping("/stale-stock")
    public ResponseEntity<List<StaleProductDTO>> staleStock(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(defaultValue = "60") int dias) {
        return ResponseEntity.ok(service.staleStock(enterpriseId(user), dias));
    }

    @GetMapping("/restock-suggestions")
    public ResponseEntity<List<RestockSuggestionDTO>> restockSuggestions(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(defaultValue = "30") int dias,
            @RequestParam(defaultValue = "21") int cobertura) {
        return ResponseEntity.ok(service.restockSuggestions(enterpriseId(user), dias, cobertura));
    }

    /** Calcula un grafico a partir de su receta. No usa IA: sirve para graficos guardados y predefinidos. */
    @PostMapping("/chart")
    public ResponseEntity<ChartResponseDTO> chart(@AuthenticationPrincipal UserModel user,
                                                  @Valid @RequestBody ChartSpecDTO spec) {
        return ResponseEntity.ok(chartService.build(enterpriseId(user), spec));
    }

    private Long enterpriseId(UserModel user) {
        return user.getEmpresa().getId();
    }

    private LocalDate endDate(LocalDate hasta) {
        return hasta != null ? hasta : LocalDate.now();
    }

    private LocalDate startDate(LocalDate desde, LocalDate fin) {
        return desde != null ? desde : fin.minusDays(29);
    }
}
