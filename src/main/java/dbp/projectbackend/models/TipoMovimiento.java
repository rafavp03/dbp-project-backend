package dbp.projectbackend.models;

public enum TipoMovimiento {
    ENTRADA,    // ingreso de mercaderia (ej. recepcion de una orden de compra)
    SALIDA,     // egreso de mercaderia (ej. despacho de una orden de venta)
    AJUSTE      // correccion por inventario fisico (el stock se fija al valor contado)
}
