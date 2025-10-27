package com.example.nikestore.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    public int item_id;
    public int product_id;
    public String product_name;
    public double price;
    public int quantity;
    public Double subtotal; // or double
    public Integer variant_id; // nullable
    public String variant_size;
    public String image_url;
    public double unitPrice;

    // NEW: Các trường cho chương trình khuyến mãi trong giỏ hàng
    public double discount_percent;
    public double final_price;

    public CartItem() {
    }

    public CartItem(int item_id, int product_id, String product_name, double price, int quantity,
                    Double subtotal, Integer variant_id, String variant_size, String image_url,
                    double unitPrice, double discount_percent, double final_price) {
        this.item_id = item_id;
        this.product_id = product_id;
        this.product_name = product_name;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.variant_id = variant_id;
        this.variant_size = variant_size;
        this.image_url = image_url;
        this.unitPrice = unitPrice;
        this.discount_percent = discount_percent;
        this.final_price = final_price;
    }

    protected CartItem(Parcel in) {
        item_id = in.readInt();
        product_id = in.readInt();
        product_name = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
        if (in.readByte() == 0) {
            subtotal = null;
        } else {
            subtotal = in.readDouble();
        }
        if (in.readByte() == 0) {
            variant_id = null;
        } else {
            variant_id = in.readInt();
        }
        variant_size = in.readString();
        image_url = in.readString();
        unitPrice = in.readDouble();
        // NEW: Read discount fields
        discount_percent = in.readDouble();
        final_price = in.readDouble();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Integer getVariant_id() {
        return variant_id;
    }

    public void setVariant_id(Integer variant_id) {
        this.variant_id = variant_id;
    }

    public String getVariant_size() {
        return variant_size;
    }

    public void setVariant_size(String variant_size) {
        this.variant_size = variant_size;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    // NEW: Getters and setters for discount fields
    public double getDiscount_percent() {
        return discount_percent;
    }

    public void setDiscount_percent(double discount_percent) {
        this.discount_percent = discount_percent;
    }

    public double getFinal_price() {
        return final_price;
    }

    public void setFinal_price(double final_price) {
        this.final_price = final_price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(item_id);
        dest.writeInt(product_id);
        dest.writeString(product_name);
        dest.writeDouble(price);
        dest.writeInt(quantity);
        if (subtotal == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(subtotal);
        }
        if (variant_id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(variant_id);
        }
        dest.writeString(variant_size);
        dest.writeString(image_url);
        dest.writeDouble(unitPrice);
        // NEW: Write discount fields
        dest.writeDouble(discount_percent);
        dest.writeDouble(final_price);
    }
}