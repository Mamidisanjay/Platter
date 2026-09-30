package com.platter.search;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "platter-restaurants")
public class RestaurantSearchDocument {
    @Id private Long id;
    @Field(type = FieldType.Text) private String name;
    @Field(type = FieldType.Text) private String cuisine;
    @Field(type = FieldType.Double) private BigDecimal rating;
    @Field(type = FieldType.Integer) private Integer deliveryTimeMinutes;
    @Field(type = FieldType.Keyword) private String priceTier;
    @Field(type = FieldType.Keyword) private String tag;
    @Field(type = FieldType.Boolean) private boolean available;
    protected RestaurantSearchDocument() { }
    public RestaurantSearchDocument(Long id, String name, String cuisine, BigDecimal rating, Integer deliveryTimeMinutes, String priceTier, String tag, boolean available) { this.id=id; this.name=name; this.cuisine=cuisine; this.rating=rating; this.deliveryTimeMinutes=deliveryTimeMinutes; this.priceTier=priceTier; this.tag=tag; this.available=available; }
    public Long getId(){return id;} public String getName(){return name;} public String getCuisine(){return cuisine;} public BigDecimal getRating(){return rating;} public Integer getDeliveryTimeMinutes(){return deliveryTimeMinutes;} public String getPriceTier(){return priceTier;} public String getTag(){return tag;} public boolean isAvailable(){return available;}
}
