package org.crumbs.json;

import org.crumbs.json.exception.JsonMarshalException;
import org.crumbs.json.exception.JsonUnmarshalException;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class JsonMapperTest {

    static class ComplexPojo {
        String someString = "aaa";
        transient String transientString = "bbb";
        int someInt = 233;
        Integer someInteger = 234234;
        long someLong = 114L;
        Long someLongObj = 44444L;
        float someFloat = 22.2f;
        Float someFloatObj = 43242.4f;
        double someDouble = 123.3341;
        Double someDoubleObj = 31313.33;
        boolean someBoolean = false;
        Boolean someBooleanObj = true;

        Boolean nullBool = null;
        String nullString = null;

        InnerComplexPojo pojo;
        Map<String, Object> innerMap;
        {
            pojo = new InnerComplexPojo();
            innerMap = new HashMap<>();
            innerMap.put("firstEntryString", "someMapEntryVal");
            innerMap.put("secondEntryDouble", 121212.33);
        }


    }

    static class InnerComplexPojo {
        String someStringInner = "bbb";
        int someIntInner = 54545;

        Set<String> set;

        {
            set = new HashSet<>();
            set.add("firstSetEntry");
            set.add("secondSetEntry");
        }

    }

    @Test
    public void shouldSerializeJson() throws JsonMarshalException, IllegalAccessException {
        ComplexPojo complexPojo = new ComplexPojo();

        JsonMapper jsonMapper = new JsonMapper();

        String jsonString = jsonMapper.marshal(complexPojo);

        System.out.println(jsonString);
    }

    static class UnmarshalTestPojo {
        private String someString;
        private int someInt;
        private double someDouble;
        private boolean someBoolean;
        private List<String> someArray;

        private Inner someObject;

    }

    static class Inner {
        private InnerString innerString;
        private Map<String, Object> someEmptyObject;
        private String someNullKey;
    }

    static class InnerString {
        private String value;
    }

    @Test
    public void shouldBuildTree() throws JsonUnmarshalException {
        String input = FileUtil.readResource("testInput.json");
        UnmarshalTestPojo result = new JsonMapper().unmarshal(input.getBytes(), UnmarshalTestPojo.class);
        Assert.assertNotNull(result);
    }

    static class Pojo2 {
        private List<InnerString> list;
    }

    static class UpdatePasswordRequest {
        private long id;
        private String key;
        private String username;
        private String password;
        private int someInt;

        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {
            return "UpdatePasswordRequest{" +
                    "id=" + id +
                    ", key='" + key + '\'' +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", someInt=" + someInt +
                    '}';
        }
    }

    @Test
    public void shouldUnmarshalSuccessfully() throws JsonUnmarshalException {
        String input = FileUtil.readResource("testInput3.json");
        UpdatePasswordRequest result = new JsonMapper().unmarshal(input.getBytes(), UpdatePasswordRequest.class);
        Assert.assertNotNull(result);
        Assert.assertEquals("some username", result.getUsername());
        System.out.println(result);
    }
}
