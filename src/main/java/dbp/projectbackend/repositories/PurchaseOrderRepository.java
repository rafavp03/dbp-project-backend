package dbp.projectbackend.repositories;

import dbp.projectbackend.models.PurchaseOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderModel, Long> {

}
