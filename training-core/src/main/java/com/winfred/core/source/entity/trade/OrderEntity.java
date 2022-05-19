package com.winfred.core.source.entity.trade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author winfred
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = 6635783003778019296L;

    private String de_duplication_key;
    private String uni_order_item_id;
    private Long data_from;
    private String partner;
    private String uni_order_id;
    private String plat_code;
    private String order_item_id;
    private String order_id;
    private String uni_shop_id;
    private String uni_id;
    private String shop_id;
    private String plat_account;
    private String outer_product_id;
    private String outer_sku_id;
    private String product_id;
    private String sku_id;
    private Long product_num;
    private Double price;
    private Double discount_fee;
    private Double adjust_fee;
    private Double total_fee;
    private Double receive_payment;
    private Double payment;
    private Double discount_price;
    private Double item_discount_rate;
    private Long is_refund;
    private String refund_status;
    private String uni_refund_status;
    private Double refund_fee;
    private String uni_refund_id;
    private String refund_id;
    private String consign_time;
    private String logistics_company;
    private String logistics_no;
    private String created;
    private String pay_time;
    private String uni_product_id;
    private String order_status;
    private String trade_type;
    private String modified;
    private String tenant;
    private String tidb_modified;
    private String product_name;
    private Long promotion_num;
    private String sku_properties_name;
    private String order_item_status;
    private String guider;
    private String buyer_is_rate;
    private String part;
}
