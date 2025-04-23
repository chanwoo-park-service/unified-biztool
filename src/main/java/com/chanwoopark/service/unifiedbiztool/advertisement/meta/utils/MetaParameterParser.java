package com.chanwoopark.service.unifiedbiztool.advertisement.meta.utils;

import java.util.List;

public class MetaParameterParser {
    public static List<Integer> getGenders(String raw) {
        return switch (raw) {
            case "남자":
                yield List.of(1);
            case "여자":
                yield List.of(2);
            case "모든 성별":
                yield List.of(1, 2);
            default:
                throw new IllegalStateException("Unexpected value: " + raw);
        };
    }

    public static List<String> getGeoLocation(String raw) {
        return switch (raw) {
            case "대한민국":
                yield List.of("KR");
            case "일본":
                yield List.of("JP");
            default:
                throw new IllegalStateException("Unexpected value: " + raw);
        };
    }

    public static List<Integer> getLocales(String raw) {
        return switch (raw) {
            case "한국어":
                yield List.of(3);
            case "영어":
                yield List.of(4);
            default:
                throw new IllegalStateException("Unexpected value: " + raw);
        };
    }

}
