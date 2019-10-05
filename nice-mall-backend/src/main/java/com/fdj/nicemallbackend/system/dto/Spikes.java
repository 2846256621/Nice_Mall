package com.fdj.nicemallbackend.system.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Classname spike
 * @Description TODO
 * @Date 19-10-5 下午9:39
 * @Created by xns
 */
@Data
public class Spikes {

    Long goodsId;

    BigDecimal goodsCurPrice;

    BigDecimal goodsPrePrice;

    LocalDateTime startTime;

    LocalDateTime endTime;

    Long storeGoodsNumber;
}
