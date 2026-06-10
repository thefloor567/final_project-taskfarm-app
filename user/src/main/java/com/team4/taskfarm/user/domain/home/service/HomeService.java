package com.team4.taskfarm.user.domain.home.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.farm.TbFarm;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.home.dto.HomeResponse;
import com.team4.taskfarm.user.domain.home.repository.HomeFarmRepository;
import com.team4.taskfarm.user.domain.home.repository.HomeTodoRepository;
import com.team4.taskfarm.user.domain.home.repository.HomeUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeService {

	private final HomeUserRepository userRepository;
	private final HomeFarmRepository farmRepository;
	private final HomeTodoRepository todoRepository;
	
	@Transactional(readOnly = true)
	public HomeResponse getHome(Long idxUser) {
		TbUser user = userRepository.findById(idxUser)
				.orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));
		
		TbFarm farm = farmRepository.findByIdxUser(idxUser).orElse(null);
		int water = (farm != null) ? farm.getDrops() : 0;
		int coin = (farm != null) ? farm.getCoin() : 0;
		
		long totalTodo = todoRepository.countByIdxUserAndDeleteDateIsNull(idxUser);
		long doneTodo = todoRepository.countByIdxUserAndIsDoneTrueAndDeleteDateIsNull(idxUser);
		
		return HomeResponse.builder()
				.nickName(user.getNickname())
				.level(user.getLevel())
				.water(water)
				.coin(coin)
				.xpNow(user.expInCurrentLevel())
				.xpMax(user.expNeededForLevel())
				.totalTodo(totalTodo)
				.doneTodo(doneTodo)
				.build();
	}
}
