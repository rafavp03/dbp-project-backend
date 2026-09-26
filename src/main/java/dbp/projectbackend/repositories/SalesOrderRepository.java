package dbp.projectbackend.repositories;

import dbp.projectbackend.models.SalesOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrderModel, Long> {

}
