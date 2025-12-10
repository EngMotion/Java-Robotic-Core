
package com.lucaf.robotic_core.config.impl;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class BaseConfig implements Cloneable {
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (this == other) return true;
        if (this.getClass() != other.getClass()) return false;
        return this.toString().equals(other.toString());
    }

    @Override
    public Object clone() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return gson.fromJson(json, this.getClass());
    }

    public String hash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(this.toString().getBytes());
            byte[] array = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends BaseConfig> T fromString(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseConfig> T cloneConfig() {
        return (T) this.clone();
    }
}
