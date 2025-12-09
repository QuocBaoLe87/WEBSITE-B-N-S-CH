package com.v.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.v.model.ReturnRequest;
import com.v.model.ReturnStatus;
import com.v.model.User;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    /**
     * Lấy tất cả yêu cầu trả hàng của một khách hàng
     */
    List<ReturnRequest> findByCustomerOrderByCreatedAtDesc(User customer);

    /**
     * Lấy yêu cầu theo trạng thái
     */
    List<ReturnRequest> findByStatusOrderByCreatedAtDesc(ReturnStatus status);

    /**
     * Lấy tất cả yêu cầu (admin) sắp xếp theo ngày tạo giảm dần
     */
    List<ReturnRequest> findAllByOrderByCreatedAtDesc();

    /**
     * Kiểm tra xem một đơn hàng đã có yêu cầu trả hàng chưa (để tránh spam)
     */
    @Query("SELECT COUNT(r) FROM ReturnRequest r WHERE r.order.id = :orderId AND r.status IN :statuses")
    long countByOrderIdAndStatusIn(@Param("orderId") Long orderId, @Param("statuses") List<ReturnStatus> statuses);
}
