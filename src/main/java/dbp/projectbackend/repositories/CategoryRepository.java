package dbp.projectbackend.repositories;

import dbp.projectbackend.models.CategoryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {
    List<CategoryModel> findByEmpresaIdAndActivoTrue(Long empresaId);
    boolean existsByEmpresaIdAndNombreIgnoreCase(Long empresaId, String nombre);

    Page<CategoryModel> findByEmpresaId(Long empresaId, Pageable pageable);

    @Query("""
            SELECT c FROM CategoryModel c
            WHERE c.empresa.id = :empresaId
              AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<CategoryModel> searchByEmpresaId(@Param("empresaId") Long empresaId,
                                          @Param("search") String search,
                                          Pageable pageable);
}
