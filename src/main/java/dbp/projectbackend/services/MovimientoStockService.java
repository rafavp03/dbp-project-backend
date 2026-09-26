package dbp.projectbackend.services;

import dbp.projectbackend.dtos.AjusteStockDTO;
import dbp.projectbackend.dtos.MovimientoStockDTO;
import dbp.projectbackend.dtos.MovimientoStockResponseDTO;
import dbp.projectbackend.dtos.VarianteProductoResponseDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.MovimientoStockModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.models.VarianteProductoModel;
import dbp.projectbackend.repositories.MovimientoStockRepository;
import dbp.projectbackend.repositories.VarianteProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final VarianteProductoRepository varianteRepository;

    @Transactional
    public MovimientoStockResponseDTO registrarEntrada(UserModel currentUser, MovimientoStockDTO dto) {
        VarianteProductoModel variante = findOwnedVariante(currentUser, dto.varianteId());
        MovimientoStockModel movimiento = MovimientoStockModel.entrada(variante, dto.cantidad(), dto.motivo());
        return toDTO(movimientoStockRepository.save(movimiento));
    }

    @Transactional
    public MovimientoStockResponseDTO registrarSalida(UserModel currentUser, MovimientoStockDTO dto) {
        VarianteProductoModel variante = findOwnedVariante(currentUser, dto.varianteId());
        MovimientoStockModel movimiento = MovimientoStockModel.salida(variante, dto.cantidad(), dto.motivo());
        return toDTO(movimientoStockRepository.save(movimiento));
    }

    @Transactional
    public MovimientoStockResponseDTO registrarDevolucion(UserModel currentUser, MovimientoStockDTO dto) {
        VarianteProductoModel variante = findOwnedVariante(currentUser, dto.varianteId());
        MovimientoStockModel movimiento = MovimientoStockModel.devolucion(variante, dto.cantidad(), dto.motivo());
        return toDTO(movimientoStockRepository.save(movimiento));
    }

    @Transactional
    public MovimientoStockResponseDTO registrarAjuste(UserModel currentUser, AjusteStockDTO dto) {
        VarianteProductoModel variante = findOwnedVariante(currentUser, dto.varianteId());
        MovimientoStockModel movimiento = MovimientoStockModel.ajuste(variante, dto.nuevoStock(), dto.motivo());
        return toDTO(movimientoStockRepository.save(movimiento));
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockResponseDTO> getKardexByVariante(UserModel currentUser, Long varianteId) {
        findOwnedVariante(currentUser, varianteId);
        return movimientoStockRepository.findByVarianteIdOrderByFechaDesc(varianteId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VarianteProductoResponseDTO> getStockBajo(UserModel currentUser) {
        return varianteRepository.findStockBajo(currentUser.getEmpresa().getId()).stream()
                .map(v -> new VarianteProductoResponseDTO(
                        v.getId(),
                        v.getProducto().getId(),
                        v.getProducto().getNombre(),
                        v.getTalla(),
                        v.getColor(),
                        v.getSku(),
                        v.getStock(),
                        v.getStockMinimo(),
                        v.getActivo(),
                        v.isStockBajo()
                ))
                .toList();
    }

    private VarianteProductoModel findOwnedVariante(UserModel currentUser, Long varianteId) {
        VarianteProductoModel variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante con id " + varianteId + " no encontrada."));

        if (!variante.getProducto().getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Variante con id " + varianteId + " no encontrada.");
        }
        return variante;
    }

    private MovimientoStockResponseDTO toDTO(MovimientoStockModel movimiento) {
        return new MovimientoStockResponseDTO(
                movimiento.getId(),
                movimiento.getVariante().getId(),
                movimiento.getVariante().getNombreCompleto(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockResultante(),
                movimiento.getMotivo(),
                movimiento.getFecha()
        );
    }
}