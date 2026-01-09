// java
package com.lucaf.robotic_core.handlers;

import com.google.gson.internal.LinkedTreeMap;
import com.lucaf.robotic_core.Pair;
import com.lucaf.robotic_core.config.impl.BaseConfig;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.*;

public class DiffsHandler<T> {

    private final Set<String> blockedNames;
    @Getter
    HashMap<String, Pair<String, String>> diffs = new HashMap<>();

    public DiffsHandler() {
        this(Set.of("header", "next", "root", "parent"));
    }

    public DiffsHandler(Set<String> blockedNames) {
        this.blockedNames = new HashSet<>(blockedNames);
    }

    public HashMap<String, Pair<String, String>> compareConfigsDifferences(T a, T b) {
        loadDiffs("", a, b);
        return diffs;
    }

    public String formatDiffs() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Pair<String, String>> e : diffs.entrySet()) {
            sb.append(String.format(
                    "- %s: '%s' -> '%s'%n",
                    escapeMarkdown(e.getKey()),
                    escapeMarkdown(e.getValue().first),
                    escapeMarkdown(e.getValue().second))
            );
        }
        return sb.toString();
    }

    private void loadDiffs(String root, Object o1, Object o2) {
        if (o1 == null && o2 == null) return;
        if (o1 == null || o2 == null) {
            diffs.put(root.isEmpty() ? "root" : root, new Pair<>(String.valueOf(o1), String.valueOf(o2)));
            return;
        }

        Class<?> clazz = o1.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (blockedNames.contains(field.getName())) continue;

            try {
                field.setAccessible(true);
            } catch (InaccessibleObjectException ignored) {}

            Object v1 = safeGet(field, o1);
            Object v2 = safeGet(field, o2);
            String keyBase = root + field.getName();

            if (v1 == null || v2 == null) {
                if (!Objects.equals(v1, v2)) {
                    diffs.put(keyBase, new Pair<>(String.valueOf(v1), String.valueOf(v2)));
                }
                continue;
            }

            if (isPrimitiveLike(v1)) {
                comparePrimitive(keyBase, v1, v2);
            } else if (v1 instanceof List && v2 instanceof List) {
                compareList(keyBase, (List<?>) v1, (List<?>) v2);
            } else if (isMapLike(v1) && isMapLike(v2)) {
                compareMap(keyBase, toMap(v1), toMap(v2));
            } else if (v1 instanceof BaseConfig && v2 instanceof BaseConfig) {
                loadDiffs(keyBase + ".", v1, v2);
            } else if (field.getType().isEnum()) {
                if (!Objects.equals(v1, v2)) {
                    diffs.put(keyBase, new Pair<>(String.valueOf(v1), String.valueOf(v2)));
                }
            } else {
                loadDiffs(keyBase + ".", v1, v2);
            }
        }
    }

    private Object safeGet(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private boolean isPrimitiveLike(Object v) {
        return v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Character || v.getClass().isPrimitive();
    }

    private void comparePrimitive(String key, Object v1, Object v2) {
        if (!Objects.equals(v1, v2)) {
            diffs.put(key, new Pair<>(String.valueOf(v1), String.valueOf(v2)));
        }
    }

    private void compareList(String key, List<?> l1, List<?> l2) {
        if (l1.size() != l2.size()) {
            diffs.put(key, new Pair<>(listToString(l1, ", "), listToString(l2, ", ")));
            return;
        }
        for (int i = 0; i < l1.size(); i++) {
            Object e1 = l1.get(i);
            Object e2 = l2.get(i);
            String idxKey = key + "[" + i + "]";
            if (e1 instanceof BaseConfig && e2 instanceof BaseConfig) {
                loadDiffs(idxKey + ".", e1, e2);
            } else {
                if (!Objects.equals(e1, e2)) {
                    diffs.put(idxKey, new Pair<>(String.valueOf(e1), String.valueOf(e2)));
                }
            }
        }
    }

    private boolean isMapLike(Object v) {
        return v instanceof Map || v instanceof LinkedTreeMap;
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> toMap(Object v) {
        if (v instanceof LinkedTreeMap) {
            return new HashMap<>((LinkedTreeMap<Object, Object>) v);
        } else if (v instanceof Map) {
            return new HashMap<>((Map<Object, Object>) v);
        } else {
            return Map.of();
        }
    }

    private void compareMap(String key, Map<Object, Object> m1, Map<Object, Object> m2) {
        Set<Object> keys = new LinkedHashSet<>();
        keys.addAll(m1.keySet());
        keys.addAll(m2.keySet());
        for (Object k : keys) {
            Object val1 = m1.get(k);
            Object val2 = m2.get(k);
            String entryKey = key + "." + String.valueOf(k);
            if (val1 instanceof BaseConfig && val2 instanceof BaseConfig) {
                loadDiffs(entryKey + ".", val1, val2);
            } else {
                if (!Objects.equals(val1, val2)) {
                    diffs.put(entryKey, new Pair<>(String.valueOf(val1), String.valueOf(val2)));
                }
            }
        }
    }

    private String listToString(List<?> list, String separator) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Object obj : list) {
            sb.append(obj).append(separator);
        }
        sb.setLength(sb.length() - separator.length());
        return sb.toString();
    }

    private String escapeMarkdown(String str) {
        if (str == null) return "null";
        return str.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("`", "\\`")
                .replace("~", "\\~");
    }

    private HashMap<String, Pair<String, String>> escapeDiffs() {
        HashMap<String, Pair<String, String>> cleanedDiffs = new HashMap<>();
        for (Map.Entry<String, Pair<String, String>> e : new ArrayList<>(diffs.entrySet())) {
            Pair<String, String> p = e.getValue();
            cleanedDiffs.put(e.getKey(), new Pair<>(escapeMarkdown(p.first), escapeMarkdown(p.second)));
        }
        return cleanedDiffs;
    }
}
