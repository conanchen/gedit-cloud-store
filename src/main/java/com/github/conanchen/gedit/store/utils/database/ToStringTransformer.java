package com.github.conanchen.gedit.store.utils.database;

import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;

import java.util.UUID;

/**
 * @author hai
 * @description transformer
 * @email hilin2333@gmail.com
 * @date 23/01/2018 4:54 PM
 */
public class ToStringTransformer implements UUIDTypeDescriptor.ValueTransformer {
    public static final ToStringTransformer INSTANCE = new ToStringTransformer();

    public String transform(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    public UUID parse(Object value) {
        StringBuilder sb = new StringBuilder((String) value);
        sb.insert(8, "-")
                .insert(13, "-")
                .insert(18, "-")
                .insert(23, "-");
        return UUID.fromString(sb.toString());
    }
}
