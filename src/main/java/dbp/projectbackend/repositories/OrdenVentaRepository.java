package dbp.projectbackend.repositories;

import dbp.projectbackend.models.OrdenVentaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenVentaRepository extends JpaRepository<OrdenVentaModel, Long> {

}
