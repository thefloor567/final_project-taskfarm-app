package com.team4.taskfarm.user.domain.ranking.service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.social.TbFriend;
import com.team4.taskfarm.common.entity.social.TbFriend.Status;
import com.team4.taskfarm.common.entity.social.TbRankSnapshot;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.user.domain.friend.repository.FriendRepository;
import com.team4.taskfarm.user.domain.ranking.dto.RankItemResponse;
import com.team4.taskfarm.user.domain.ranking.dto.RankPageResponse;
import com.team4.taskfarm.user.domain.ranking.repository.RankSnapshotRepository;
import com.team4.taskfarm.user.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {
	
	// 전체 누적 경험치 랭킹 Redis Key.
	private static final String TOTAL_ALL_KEY = "rank:total:all";

	// REDIS 작업 템플릿 => ZSET 사용 ㄱ
	private final StringRedisTemplate redisTemplate;
	
	// REDIS에는 idxUser만 저장해서, 화면에 보여줄 nickname 같은 걸 가져오기 위해 사용
	private final UserRepository userRepository;
	
	// 주간 랭킹 스냅샷 저장하기 위해 사용
	private final RankSnapshotRepository rankSnapshotRepository;
	
	// 친구 랭킹을 만들 때 ACCEPTED 상태의 친구 목록을 조회하기 위해 사용
	private final FriendRepository friendRepository;
	
	// 주간 랭킹 보상을 받기 위한 최소 주간 경험치
	private static final int WEEKLY_RANK_REWARD_MIN_EXP = 100;
	
	// 스냅샷에는 주간 TOP 3까지만 저장
	private static final int WEEKLY_SNAPSHOT_LIMIT = 3;
	
	// 주간 전체 랭킹 TOP 3 코인 보상 지급용 서비스
	private final WeeklyRankRewardService weeklyRankRewardService;
	
	// 누적 랭킹 점수 갱신 => total 랭킹은 tbUser.Exp 기준이어야 함
	public void updateTotalScore(Long idxUser, int totalExp) {
		if (idxUser == null || totalExp < 0) {
			return;
		}

		try {
			redisTemplate.opsForZSet().add(TOTAL_ALL_KEY, String.valueOf(idxUser), totalExp);

			log.info("누적 랭킹 갱신 - idxUser={}, totalExp={}", idxUser, totalExp);

		} catch (Exception e) {
			// 랭킹은 부가 기능이므로 Redis 장애가 보상 지급 전체를 막지 않도록 로그만 남긴다.
			log.warn("누적 랭킹 갱신 실패 - idxUser={}, totalExp={}, reason={}", idxUser, totalExp, e.getMessage());
		}
	}
	
	// 주간 랭킹 점수 증가
	public void addWeeklyScore(Long idxUser, int gainedExp) {
		if (idxUser == null || gainedExp <= 0) {
			return;
		}

		try {
			String key = currentWeeklyKey();

			redisTemplate.opsForZSet().incrementScore(key, String.valueOf(idxUser), gainedExp);

			log.info("주간 랭킹 증가 - key={}, idxUser={}, gainedExp={}", key, idxUser, gainedExp);

		} catch (Exception e) {
			// Redis 문제 때문에 경험치 지급 트랜잭션 전체가 깨지지 않도록 방어
			log.warn("주간 랭킹 증가 실패 - idxUser={}, gainedExp={}, reason={}", idxUser, gainedExp, e.getMessage());
		}
	}
	
	// 현재 주차 Redis Key
	public String currentWeeklyKey() {
		return "rank:weekly:" + currentPeriod();
	}
	
	// 현재 주차 문자열 
	public String currentPeriod() {
		LocalDate now = LocalDate.now();
		WeekFields weekFields = WeekFields.ISO;

		int year = now.get(weekFields.weekBasedYear());
		int week = now.get(weekFields.weekOfWeekBasedYear());

		return year + "W" + String.format("%02d", week);
	}
	
	// 화면에 보여줄 유저 이름을 결정하는 메서드
	private String getDisplayName(TbUser user) {
		return user.getNickname();
	}
	
	// 랭킹 화면 전체 데이터를 만드는 메서드
	public RankPageResponse getRankPage(String type, String scope, Long currentUserId) {

	    String key = switch (type) {
	        case "weekly" -> currentWeeklyKey();
	        case "total" -> TOTAL_ALL_KEY;
	        default -> TOTAL_ALL_KEY;
	    };
	    
	    if ("friend".equals(scope)) {
	        return getFriendRankPage(key, currentUserId);
	    }

	    List<RankItemResponse> top = getTopRankItems(key, 10);
	    RankItemResponse me = getMyRankItem(key, currentUserId);

	    return new RankPageResponse(top, me);
	}
	
	
	// TOP N 랭킹 조회 => Redis ZSET에서 score 높은 순서로 가져온 뒤 social.html이 원하는 필드명에 맞춰 RankItemResponse로 변환한다.
	private List<RankItemResponse> getTopRankItems(String key, int limit) {
	    int safeLimit = limit <= 0 ? 10 : Math.min(limit, 10);

	    Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, safeLimit - 1);

	    List<RankItemResponse> result = new ArrayList<>();

	    if (tuples == null || tuples.isEmpty()) {
	        return result;
	    }
	    
	    // 1. Redis member 값에서 userId만 먼저 전부 뽑는다.
	    List<Long> userIds = tuples.stream()
	            .map(ZSetOperations.TypedTuple::getValue)
	            .filter(Objects::nonNull)
	            .map(Long::valueOf)
	            .toList();

	    // 2. userId 목록으로 유저 정보를 한 번에 조회한다.
	    Map<Long, TbUser> userMap = userRepository.findAllById(userIds)
	            .stream()
	            .collect(Collectors.toMap(TbUser::getIdxUser, user -> user));
	    
	    int rank = 1;

	    // 3. 루프에서는 DB 조회하지 않고 Map에서 꺼내기만 한다.
	    for (ZSetOperations.TypedTuple<String> tuple : tuples) {
	        String member = tuple.getValue();
	        Double score = tuple.getScore();

	        if (member == null || score == null) {
	            continue;
	        }

	        Long idxUser = Long.valueOf(member);
	        TbUser user = userMap.get(idxUser);

	        result.add(new RankItemResponse(
	                rank,
	                idxUser,
	                user != null ? getDisplayName(user) : "알 수 없음",
	                getEquippedTitle(user),
	                score.intValue()
	        ));

	        rank++;
	    }

	    return result;
	}
	
	private RankItemResponse getMyRankItem(String key, Long currentUserId) {
	    if (currentUserId == null) {
	        return null;
	    }

	    Long zeroBasedRank = redisTemplate.opsForZSet()
	            .reverseRank(key, String.valueOf(currentUserId));

	    Double score = redisTemplate.opsForZSet()
	            .score(key, String.valueOf(currentUserId));

	    // Redis 랭킹에 아직 없으면 내 순위 카드 숨김
	    if (zeroBasedRank == null || score == null) {
	        return null;
	    }

	    TbUser user = userRepository.findById(currentUserId).orElse(null);

	    return new RankItemResponse(
	            zeroBasedRank.intValue() + 1,
	            currentUserId,
	            user != null ? getDisplayName(user) : "나",
	            getEquippedTitle(user),
	            score.intValue()
	    );
	}
	
	private String getEquippedTitle(TbUser user) {
	    if (user == null || user.getEquippedTitle() == null) {
	        return null;
	    }

	    return user.getEquippedTitle();
	}
	
	// 현재 주차의 Redis 주간 랭킹을 tbRankSnapshot에 저장
	@Transactional
	public void saveWeeklyRankSnapshot() {

	    // 현재 주차 문자열
	    String period = currentPeriod();

	    // 현재 주차 Redis key
	    String key = currentWeeklyKey();

	    // 이미 해당 주차 데이터가 저장되어 있으면 다시 저장하지 않음
	    if (rankSnapshotRepository.existsByPeriod(period)) {
	        log.info("주간 랭킹 스냅샷 이미 존재 - period={}", period);
	        return;
	    }

	    // Redis ZSET에서 주간 랭킹 전체 조회 => top 3만 저장
	    Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, WEEKLY_SNAPSHOT_LIMIT-1);

	    // 저장할 랭킹 데이터가 없으면 종료
	    if (tuples == null || tuples.isEmpty()) {
	        log.info("저장할 주간 랭킹 데이터 없음 - key={}", key);
	        return;
	    }

	    List<TbRankSnapshot> snapshots = new ArrayList<>();
	    int ranking = 1;

	    for (ZSetOperations.TypedTuple<String> tuple : tuples) {
	        String member = tuple.getValue();
	        Double score = tuple.getScore();

	        if (member == null || score == null) {
	            continue;
	        }

	        Long idxUser = Long.valueOf(member);
	        int weeklyExp = score.intValue();

	        // 주간 랭킹 스냅샷 생성
	        TbRankSnapshot snapshot = TbRankSnapshot.of(period, idxUser, ranking, weeklyExp);

	        snapshots.add(snapshot);
	        ranking++;
	    }
	    
	    // tbRankSnapshot을 DB에 저장
	    List<TbRankSnapshot> savedSnapshots = rankSnapshotRepository.saveAll(snapshots);
	        
	    // 저장된 스냅샷 중 TOP 3에게만 보상 지급 => 4등이면 보상 X
	    for (TbRankSnapshot snapshot : savedSnapshots) {
	        int rewardCoin = getWeeklyRankRewardCoin(snapshot.getRanking());
	        
	        if (rewardCoin <= 0 || snapshot.getWeeklyExp() < WEEKLY_RANK_REWARD_MIN_EXP) {
	            continue;
	        }
	        
	        // 실제 코인 보상 지급
	        weeklyRankRewardService.grantWeeklyRankReward(
	                snapshot.getIdxUser(),
	                rewardCoin,
	                period,
	                snapshot.getRanking(),
	                snapshot.getIdxRankSnapshot()
	        );
	    }
	    
	    log.info("주간 랭킹 스냅샷 저장 완료 - period={}, count={}", period, snapshots.size());
	}
	
	// 친구 랭킹 페이지 생성
	private RankPageResponse getFriendRankPage(String key, Long currentUserId) {
		if (currentUserId == null) {
			return new RankPageResponse(List.of(), null);
		}
		
		// 현재 사용자의 친구 userId 목록 가져오기
		List<Long> friendUserIds = getAcceptedFriendUserIds(currentUserId);
		
		// 친구 랭킹 = 나 + 내 친구들
		LinkedHashSet<Long> targetUserIdSet = new LinkedHashSet<>();
	    targetUserIdSet.add(currentUserId);
	    targetUserIdSet.addAll(friendUserIds);
	    
	    List<Long> targetUserIds = new ArrayList<>(targetUserIdSet);
	    
	    // 화면에 닉네임, 칭호 보여줘야 해서, userId 목록으로 유저 정보를 한 번에 조회
	    Map<Long, TbUser> userMap = userRepository.findAllById(targetUserIds)
	            .stream()
	            .collect(Collectors.toMap(TbUser::getIdxUser, user -> user));
	    
	    // Redis ZSET에서 각 유저의 점수를 가져와 친구 랭킹 후보 생성
	    List<RankItemResponse> ranked = targetUserIds.stream()
	            .map(userId -> {
	                Double score = redisTemplate.opsForZSet()
	                        .score(key, String.valueOf(userId));

	                int exp = score == null ? 0 : score.intValue();

	                TbUser user = userMap.get(userId);

	                return new RankItemResponse(
	                        0, // 정렬 전이라 임시 rank는 0으로 둔다.
	                        userId,
	                        user != null ? getDisplayName(user) : "알 수 없음",
	                        getEquippedTitle(user),
	                        exp
	                );
	            })
	            .sorted(Comparator.comparingInt(RankItemResponse::exp).reversed())
	            .toList();
	    
	    // 정렬된 결과에 실제 rank 부여
	    List<RankItemResponse> top = new ArrayList<>();
	    RankItemResponse me = null;
	    int rank = 1;

	    for (RankItemResponse item : ranked) {
	        RankItemResponse rankedItem = new RankItemResponse(
	                rank,
	                item.userId(),
	                item.nickname(),
	                item.title(),
	                item.exp()
	        );

	        if (item.userId().equals(currentUserId)) {
	            me = rankedItem;
	        }
	        
	        if (top.size() < 10) {
	            top.add(rankedItem);
	        }
	        rank++;
	    }
	    return new RankPageResponse(top, me);
	}
	
	// ACCEPTED 상태인 친구들의 userId만 가져온다.
	private List<Long> getAcceptedFriendUserIds(Long currentUserId) {
		List<TbFriend> friends =
	            friendRepository.findByIdxUserAAndStatusOrIdxUserBAndStatus(
	                    currentUserId, Status.ACCEPTED,
	                    currentUserId, Status.ACCEPTED
	            );
		return friends.stream()
	            .map(friend -> friend.opponentOf(currentUserId))
	            .toList();
	}
	
	// 주간 전체 랭킹 보상 코인 계싼
	private int getWeeklyRankRewardCoin(int ranking) {
	    return switch (ranking) {
	        case 1 -> 1000;
	        case 2 -> 500;
	        case 3 -> 100;
	        default -> 0;
	    };
	}
}
