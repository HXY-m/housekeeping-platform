package com.housekeeping.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.housekeeping.order.entity.OrderPaymentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderPaymentMapper extends BaseMapper<OrderPaymentEntity> {
}
