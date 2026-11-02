package dbp.projectbackend.services;

import dbp.projectbackend.exceptions.InvalidOperationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Rango de fechas para filtrar listados: [inicio, finExclusivo).
 * Si falta {@code desde} se toma desde el inicio del historial; si falta {@code hasta}, hasta hoy.
 */
record DateRange(LocalDateTime inicio, LocalDateTime finExclusivo) {

    private static final LocalDate INICIO_HISTORIAL = LocalDate.of(2000, 1, 1);

    static DateRange of(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = desde != null ? desde : INICIO_HISTORIAL;
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        if (fin.isBefore(inicio)) {
            throw new InvalidOperationException("La fecha 'hasta' no puede ser anterior a 'desde'.");
        }
        return new DateRange(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay());
    }
}
