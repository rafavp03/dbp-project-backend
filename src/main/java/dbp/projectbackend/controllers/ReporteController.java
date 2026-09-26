package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.reportes.*;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.ReporteService;
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
@RequestMapping("/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService service;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenVentasDTO> resumen(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = fin(hasta);
        return ResponseEntity.ok(service.resumen(empresaId(user), inicio(desde, fin), fin));
    }

    @GetMapping("/top-productos")
    public ResponseEntity<List<ProductoRankingDTO>> topProductos(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "UNIDADES") CriterioRanking criterio,
            @RequestParam(defaultValue = "10") int limite) {
        LocalDate fin = fin(hasta);
        return ResponseEntity.ok(service.topProductos(empresaId(user), inicio(desde, fin), fin, criterio, limite));
    }

    @GetMapping("/ventas-agrupadas")
    public ResponseEntity<List<VentasAgrupadasDTO>> ventasAgrupadas(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam AgrupacionVentas por) {
        LocalDate fin = fin(hasta);
        return ResponseEntity.ok(service.ventasAgrupadas(empresaId(user), inicio(desde, fin), fin, por));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<StockBajoDTO>> stockBajo(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.stockBajo(empresaId(user)));
    }

    @GetMapping("/stock-parado")
    public ResponseEntity<List<ProductoParadoDTO>> stockParado(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(defaultValue = "60") int dias) {
        return ResponseEntity.ok(service.stockParado(empresaId(user), dias));
    }

    private Long empresaId(UserModel user) {
        return user.getEmpresa().getId();
    }

    private LocalDate fin(LocalDate hasta) {
        return hasta != null ? hasta : LocalDate.now();
    }

    private LocalDate inicio(LocalDate desde, LocalDate fin) {
        return desde != null ? desde : fin.minusDays(29);
    }
}
