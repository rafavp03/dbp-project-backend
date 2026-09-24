package dbp.projectbackend.repositories;

import dbp.projectbackend.models.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {
    List<CategoryModel> findByEmpresaId(Long empresaId);
    List<CategoryModel> findByEmpresaIdAndActivoTrue(Long empresaId);
    boolean existsByEmpresaIdAndNombreIgnoreCase(Long empresaId, String nombre);
}
