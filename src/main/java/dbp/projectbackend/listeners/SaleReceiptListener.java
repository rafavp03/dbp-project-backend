package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.events.SaleRegisteredEvent;
import dbp.projectbackend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Envía al cliente el detalle de su compra cuando la venta tiene un cliente con correo.
 * Muchas ventas de Gamarra son por WhatsApp o Instagram, así que el cliente recibe su
 * constancia sin que el vendedor tenga que armarla a mano. No es un comprobante SUNAT.
 */
@Component
@RequiredArgsConstructor
public class SaleReceiptListener {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICATIONS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSaleRegistered(SaleRegisteredEvent event) {
        if (!event.hasClientEmail()) {
            return;
        }
        emailService.send(
                event.getClienteCorreo(),
                "Gracias por tu compra en " + event.getEmpresa(),
                "email/comprobante-venta",
                Map.of(
                        "nombre", event.getClienteNombre(),
                        "empresa", event.getEmpresa(),
                        "ventaId", event.getVentaId(),
                        "fecha", event.getFecha().format(DATE_FORMAT),
                        "medioPago", event.getMedioPago().name(),
                        "total", event.getTotal(),
                        "lineas", event.getLineas()
                )
        );
    }
}
