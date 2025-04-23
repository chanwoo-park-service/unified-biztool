package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import lombok.Getter;

import java.util.List;

@Getter
public class MetaListResponse<T> {
    private List<T> data;

    private Paging paging;

    @Getter
    public static class Paging {
        private Cursors cursors;
        private String previous;
        private String next;

        @Getter
        public static class Cursors {
            private String before;
            private String after;
        }
    }
}