package com.fdj.nicemallbackend.system.mapper;

import com.fdj.nicemallbackend.system.entity.ReceiptAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xns
 * @since 2019-10-14
 */
@Mapper
public interface ReceiptAddressMapper extends BaseMapper<ReceiptAddress> {

    int save(ReceiptAddress receiptAddress);

    List<ReceiptAddress> selectByuserId(Long userId);

    @Delete("delete from m_receipt_address where address_id = #{addressId}")
    int deleteByaddressId(Long addressId);
}
