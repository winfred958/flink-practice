package com.winfred.core.source.entity.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
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
public class TradeEntity implements Serializable {

  private static final long serialVersionUID = -3279410760510161747L;

  @JsonProperty(value = "de_duplication_key")
  @SerializedName(value = "de_duplication_key")
  private String de_duplication_key;
  private String uni_order_id;
  private Long data_from;
  private String partner;
  private String plat_code;
  private String order_id;
  private String uni_shop_id;
  private String uni_id;
  private String guide_id;
  private String shop_id;
  private String plat_account;
  private Double total_fee;
  private Double item_discount_fee;
  private Double trade_discount_fee;
  private Double adjust_fee;
  private Double post_fee;
  private Double discount_rate;
  private Double payment_no_postfee;
  private Double payment;
  private String pay_time;
  private Long product_num;
  private String order_status;
  private String is_refund;
  private Double refund_fee;
  private String insert_time;
  private String created;
  private String endtime;
  private String modified;
  private String trade_type;
  private String receiver_name;
  private String receiver_country;
  private String receiver_state;
  private String receiver_city;
  private String receiver_district;
  private String receiver_town;
  private String receiver_address;
  private String receiver_mobile;
  private String trade_source;
  private String delivery_type;
  private String consign_time;
  private Long orders_num;
  private Long is_presale;
  private String presale_status;
  private String first_fee_paytime;
  private String last_fee_paytime;
  private Double first_paid_fee;
  private String tenant;
  private String tidb_modified;
  private Double step_paid_fee;
  private String seller_flag;
  private Long is_used_store_card;
  private Double store_card_used;
  private Double store_card_basic_used;
  private Double store_card_expand_used;
  private Long order_promotion_num;
  private Long item_promotion_num;
  private String buyer_remark;
  private String seller_remark;
  private String trade_business_type;
  private String part;
}
