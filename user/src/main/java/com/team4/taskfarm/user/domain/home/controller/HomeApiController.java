package com.team4.taskfarm.user.domain.home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.home.dto.HomeResponse;
import com.team4.taskfarm.user.domain.home.service.HomeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/home")
@RequiredArgsConstructor
public class HomeApiController extends UserBaseController{
	
	private final HomeService homeService;
	
	@GetMapping
    public ResponseEntity<ApiResponse<HomeResponse>> getHome() {
        return ok(homeService.getHome(getCurrentUserIdx()));
    }
}
