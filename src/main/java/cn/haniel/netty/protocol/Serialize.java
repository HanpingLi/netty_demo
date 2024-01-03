package cn.haniel.netty.protocol;

import com.google.gson.*;
import lombok.Getter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 序列化枚举
 *
 * @author hanping
 * @date 2024-01-01
 */
@Getter
public enum Serialize {

    /**
     * 使用 Java 自身的序列化算法
     */
    JAVA(0) {
        @Override
        public <T> byte[] serialize(T object) {
            try {
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                final ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(object);
                return bos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("SerializerAlgorithm.Java 序列化错误", e);
            }
        }

        @Override
        public <T> T deserialize(Class<T> clz, byte[] bytes) {
            try {
                final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                return (T) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("SerializerAlgorithm.Java 反序列化错误", e);
            }
        }
    },

    /**
     * 使用 GSON 进行 JSON 序列化算法
     */
    JSON(1) {
        @Override
        public <T> byte[] serialize(T object) {
            return GSON.toJson(object).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public <T> T deserialize(Class<T> clz, byte[] bytes) {
            final String json = new String(bytes, StandardCharsets.UTF_8);
            return GSON.fromJson(json, clz);
        }
    },

    UNKNOWN(-1) {
        @Override
        <T> byte[] serialize(T object) {
            return new byte[0];
        }

        @Override
        <T> T deserialize(Class<T> clz, byte[] bytes) {
            return null;
        }
    };

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();

    /**
     * 序列化类型
     * 0 - jdk，1 - json
     */
    private final int type;

    Serialize(int type) {
        this.type = type;
    }

    public static Serialize typeEnum(int type) {
        for (Serialize value : values()) {
            int valueType = value.getType();
            if (type == valueType) {
                return value;
            }
        }
        return UNKNOWN;
    }


    /**
     * 序列化
     *
     * @param object 被序列化的对象
     * @param <T>    被序列化对象类型
     * @return 序列化后的字节数组
     */
    abstract <T> byte[] serialize(T object);

    /**
     * 反序列化
     *
     * @param clz   反序列化的目标类的Class对象
     * @param bytes 被反序列化的字节数组
     * @param <T>   反序列化目标类
     * @return 反序列化后的对象
     */
    abstract <T> T deserialize(Class<T> clz, byte[] bytes);

    /**
     * Gson 原生不支持 Class 对象的序列化/反序列化，需自行添加
     * Class 类的序列化/反序列化方式
     */
    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            // json --> Class 对象
            try {
                return Class.forName(json.getAsString());
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            // Class 对象 --> json
            return new JsonPrimitive(src.getName());
        }
    }
}
