package com.github.conanchen.gedit.store.utils;


import java.lang.reflect.Field;
import java.util.*;

/**
 * Entity Utils 提供实体的一些工具方法
 * Created by yaobo on 2017/3/20.
 */
public class EntityUtils<T,S> {
    /**
     * 设置entity的createDate,updateDate
     *
     * @param entity
     * @param <T>
     */
    public static <T> void setEntitiesDate(T... entity) {
        for (T t : entity) {
            setEntityDate("id", t);
        }
    }

    /**
     * 设置entity的createDate,updateDate, 需要指定id字段名称
     *
     * @param entity
     * @param <T>
     */
    public static <T> void setEntitiesDate(String idField, T... entity) {
        for (T t : entity) {
            setEntityDate(idField, t);
        }
    }

    public static <T> T setEntityDate(String idField, T entity) {
        if (entity == null) {
            return null;
        }
        try {
            Object id = ReflectionUtils.getFieldValue(entity, idField);
            Field createDate = ReflectionUtils.getAccessibleField(entity, "createDate");
            Field updateDate = ReflectionUtils.getAccessibleField(entity, "updateDate");
            if (id == null && createDate != null) {
                ReflectionUtils.setFieldValue(entity, "createDate", new Date());
            }
            if (updateDate != null) {
                ReflectionUtils.setFieldValue(entity, "updateDate", new Date());
            }
        } catch (Exception e) {

        }
        return entity;
    }

    /**
     * 将每个entity的id提成list
     *
     * @param entities
     * @param <T>
     * @return
     */
    public static <T> List<Long> createIdList(List<T> entities) {
        return createIdList(entities, "id");
    }

    public static <T> List<Long> createIdList(List<T> entities, String idFiled) {
        List<Long> ids = new ArrayList<Long>();
        for (T entity : entities) {
            Long id = (Long) ReflectionUtils.getFieldValue(entity, idFiled);
            ids.add(id);
        }
        return ids;
    }
    public static <T> Set<Long> createIdSet(List<T> entities, String idFiled) {
        Set<Long> ids = new HashSet<>();
        for (T entity : entities) {
            Long id = (Long) ReflectionUtils.getFieldValue(entity, idFiled);
            ids.add(id);
        }
        return ids;
    }

    /**
     * 截取对应的字段数据，返回一个list
     * @param entities
     * @param field
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T,S> List<S> createFieldList(List<T> entities, String field) {
        List<S> values = new ArrayList<S>();
        for (T entity : entities) {
            S t = (S) ReflectionUtils.getFieldValue(entity, field);
            values.add(t);
        }
        return values;
    }
    /**
     * 把entity从list->map. id为key, entity为value
     *
     * @param entities
     * @param <T>
     * @return
     */
    public static <T> Map<Long, T> createEntityMap(List<T> entities) {
        return createEntityMap(entities, "id");
    }

    public static <T> Map<Long, T> createEntityMap(List<T> entities, String idFiled) {
        Map<Long, T> map = new HashMap<>();
        for (T entity : entities) {
            map.put((Long) ReflectionUtils.getFieldValue(entity, idFiled), entity);
        }
        return map;
    }

    public static <T> Map<String, T> createEntityMapByString(List<T> entities, String idFiled) {
        Map<String, T> map = new HashMap<>();
        for (T entity : entities) {
            map.put((String) ReflectionUtils.getFieldValue(entity, idFiled), entity);
        }
        return map;
    }

    public static <T> Map<Integer, T> createEntityMapByInt(List<T> entities) {
        return createEntityMapByInt(entities, "id");
    }

    public static <T> Map<Integer, T> createEntityMapByInt(List<T> entities, String idFiled) {
        Map<Integer, T> map = new HashMap<>();
        for (T entity : entities) {
            map.put((Integer) ReflectionUtils.getFieldValue(entity, idFiled), entity);
        }
        return map;
    }
}
