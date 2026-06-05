package com.team4.taskfarm.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchDto {

    // 현재 페이지 번호 — 기본값 1
    private int page = 1;

    // 페이지당 표시 개수 — 기본값 10, 변경 시 해당 요청에만 적용
    private int size = 10;

    // 검색어 — 제목 등 키워드 검색 시 사용 (null 이면 전체 조회)
    private String keyword;

    // 카테고리 필터 — 분류 선택 필터 시 사용 (null 이면 전체)
    private String category;

    // 상태 필터 — 상태 필터 시 사용 (null 이면 전체)
    private String status;

    // 정렬 기준 컬럼명 — 기본값 createDate
    private String sortField = "createDate";

    // 정렬 방향 — ASC / DESC, 기본값 DESC (최신순)
    private String sortDir = "DESC";
}
