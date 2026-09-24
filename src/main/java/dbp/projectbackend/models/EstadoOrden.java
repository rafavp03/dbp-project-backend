package dbp.projectbackend.models;

public enum EstadoOrden {
    PENDIENTE,   // registrada, aun no se entrega / recibe
    COMPLETADA,  // venta entregada o compra recibida (ya movio stock)
    ANULADA      // no cuenta para reportes ni para la IA
}
