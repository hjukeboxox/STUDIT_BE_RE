package com.studit.backend.domain.recruit.service;

import com.studit.backend.domain.recruit.RecruitStatus;
import com.studit.backend.domain.recruit.RegisterStatus;
import com.studit.backend.domain.recruit.StudyCategory;
import com.studit.backend.domain.recruit.dto.StudyRecruitRequest;
import com.studit.backend.domain.recruit.dto.StudyRecruitResponse;
import com.studit.backend.domain.recruit.entity.StudyRecruit;
import com.studit.backend.domain.recruit.entity.StudyRegister;
import com.studit.backend.domain.recruit.repository.StudyRecruitRepository;
import com.studit.backend.domain.recruit.repository.StudyRegisterRepository;
import com.studit.backend.domain.user.entity.User;
import com.studit.backend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyRecruitService {

    private final StudyRecruitRepository studyRecruitRepository;
    private final StudyRegisterRepository studyRegisterRepository;
    private final UserRepository userRepository;

    // TODO: pointService 에서 구현될 수 있도록 수정 (실제 차감 + 로그)
    // 스터디 모집글 생성
    @Transactional
    public void createRecruit(StudyRecruitRequest.Create request, Long leaderId) {

        // 모집자 정보 가져오기
        User leader = userRepository.findById(leaderId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 모집자가 가진 포인트가 예치금보다 적으면 예외 발생
        if (leader.getPoint() < request.getDeposit()) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        // 모집자의 포인트 차감
        leader.deductPoint(request.getDeposit());

        // 모집글 생성
        StudyRecruit studyRecruit = StudyRecruit.builder()
                .leader(leader)
                .title(request.getTitle())
                .description(request.getDescription())
                .tags(String.join(",", request.getTags()))
                .category(request.getCategory())
                .goalTime(request.getGoalTime())
                .deposit(request.getDeposit())
                .maxMembers(request.getMaxMembers())
                .studyStartAt(request.getRecruitEndAt())
                .studyEndAt(request.getStudyEndAt())
                .recruitEndAt(request.getRecruitEndAt())
                .build();

        studyRecruitRepository.save(studyRecruit);
    }

    // 스터디 모집글 목록 조회
    public Page<StudyRecruitResponse.Summary> getAllRecruits(Pageable pageable) {
        Page<StudyRecruit> recruits = studyRecruitRepository.findByStatusOrderByRecruitStartAtDesc(RecruitStatus.ACTIVE, pageable);

        return recruits.map(studyRecruit -> StudyRecruitResponse.Summary.builder()
                .recruitId(studyRecruit.getId())
                .title(studyRecruit.getTitle())
                .category(studyRecruit.getCategory().name())
                .tags(Arrays.asList(studyRecruit.getTags().split(",")))
                .goalTime(studyRecruit.getGoalTime())
                .deposit(studyRecruit.getDeposit())
                .studyStartAt(studyRecruit.getStudyStartAt())
                .studyEndAt(studyRecruit.getStudyEndAt())
                .recruitEndAt(studyRecruit.getRecruitEndAt())
                .currentMembers(studyRegisterRepository.countByStudyRecruitAndStatus(studyRecruit, RegisterStatus.REGISTER))
                .maxMembers(studyRecruit.getMaxMembers())
                .status(studyRecruit.getStatus().name())
                .build());
    }

    // 스터디 모집글 목록 조회 (검색, 필터링 적용)
    public Page<StudyRecruitResponse.Summary> getSearchRecruits(
            String title, StudyCategory category,
            Integer minDeposit, Integer maxDeposit, Integer minGoalTime, Integer maxGoalTime,
            Pageable pageable
    ) {
        // null 값 방지
        int minDepositValue = (minDeposit != null) ? minDeposit : 0;
        int maxDepositValue = (maxDeposit != null) ? maxDeposit : Integer.MAX_VALUE;
        int minGoalTimeValue = (minGoalTime != null) ? minGoalTime : 0;
        int maxGoalTimeValue = (maxGoalTime != null) ? maxGoalTime : Integer.MAX_VALUE;

        Page<StudyRecruit> searchRecruits = studyRecruitRepository.findByFilters(
                title, category, minDepositValue, maxDepositValue, minGoalTimeValue, maxGoalTimeValue, pageable);

        return searchRecruits.map(studyRecruit -> StudyRecruitResponse.Summary.builder()
                .recruitId(studyRecruit.getId())
                .title(studyRecruit.getTitle())
                .category(studyRecruit.getCategory().name())
                .tags(Arrays.asList(studyRecruit.getTags().split(",")))
                .goalTime(studyRecruit.getGoalTime())
                .deposit(studyRecruit.getDeposit())
                .studyStartAt(studyRecruit.getStudyStartAt())
                .studyEndAt(studyRecruit.getStudyEndAt())
                .recruitEndAt(studyRecruit.getRecruitEndAt())
                .currentMembers(studyRegisterRepository.countByStudyRecruitAndStatus(studyRecruit, RegisterStatus.REGISTER))
                .maxMembers(studyRecruit.getMaxMembers())
                .status(studyRecruit.getStatus().name())
                .build());
    }

    // 스터디 모집글 상세 조회
    public StudyRecruitResponse.Detail getDetailRecruit(Long recruitId) {
        StudyRecruit studyRecruit = studyRecruitRepository.findById(recruitId)
                .orElseThrow((() -> new EntityNotFoundException("Recruit not found")));

        return StudyRecruitResponse.Detail.builder()
                .recruitId(studyRecruit.getId())
                .leaderId(studyRecruit.getLeader().getId())
                .leaderNickname(studyRecruit.getLeader().getNickname())
                .title(studyRecruit.getTitle())
                .description(studyRecruit.getDescription())
                .category(studyRecruit.getCategory().name())
                .tags(Arrays.asList(studyRecruit.getTags().split(",")))
                .goalTime(studyRecruit.getGoalTime())
                .deposit(studyRecruit.getDeposit())
                .studyStartAt(studyRecruit.getStudyStartAt())
                .studyEndAt(studyRecruit.getStudyEndAt())
                .recruitEndAt(studyRecruit.getRecruitEndAt())
                .currentMembers(studyRegisterRepository.countByStudyRecruitAndStatus(studyRecruit, RegisterStatus.REGISTER))
                .maxMembers(studyRecruit.getMaxMembers())
                .status(studyRecruit.getStatus().name())
                .build();
    }

    // 스터디 모집글 수정
    public void updateRecruit(Long recruitId, StudyRecruitRequest.Update request, Long userId) {
        StudyRecruit studyRecruit = studyRecruitRepository.findById(recruitId)
                .orElseThrow(() -> new EntityNotFoundException("Recruit not found"));

        // 모집장이 맞는지 확인
        if (!studyRecruit.getLeader().getId().equals(userId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        studyRecruit.update(request);
        studyRecruitRepository.save(studyRecruit);
    }

    // TODO: pointService 에서 구현될 수 있도록 수정 (실제 추가 + 로그)
    // 스터디 모집글 삭제
    @Transactional
    public void deleteRecruit(Long recruitId, Long userId) {
        StudyRecruit studyRecruit = studyRecruitRepository.findById(recruitId)
                .orElseThrow(() -> new EntityNotFoundException("Recruit not found"));

        // 모집장이 맞는지 확인
        if (!studyRecruit.getLeader().getId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 모집자의 포인트 환불
        leader.addPoint(studyRecruit.getDeposit());

        // 가입자들의 포인트 환불
        List<StudyRegister> studyRegisters = studyRegisterRepository.findByStudyRecruit(studyRecruit);
        for (StudyRegister register : studyRegisters) {
            User user = register.getUser();
            user.addPoint(studyRecruit.getDeposit());
        }

        // 모집자의 예치금 환불 처리 (pointService 사용)
//        User leader = studyRecruit.getLeader();
//        pointService.refundDeposit(leader.getId(), studyRecruit.getDeposit());

        // 가입자들의 예치금 환불 처리 (pointService 사용)
//        List<StudyRegister> studyRegisters = studyRegisterRepository.findByStudyRecruit(studyRecruit);
//        for (StudyRegister register : studyRegisters) {
//            User user = register.getUser();
//            pointService.refundDeposit(user.getId(), studyRecruit.getDeposit());
//        }

        studyRecruitRepository.delete(studyRecruit);
    }

    // TODO: pointService 에서 구현될 수 있도록 수정 (실제 차감 + 로그)
    // 스터디 가입
    @Transactional
    public void studyRegister(Long recruitId, Long userId) {
        // 모집글 조회
        StudyRecruit studyRecruit = studyRecruitRepository.findById(recruitId)
                .orElseThrow(() -> new EntityNotFoundException("Recruit not found"));

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 가입자가 가진 포인트가 예치금보다 적으면 예외 발생
        if (user.getPoint() < studyRecruit.getDeposit()) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        // 가입자의 포인트 차감
        user.deductPoint(studyRecruit.getDeposit());

        // 스터디 가입 정보 저장
        StudyRegister studyRegister = StudyRegister.builder()
                .studyRecruit(studyRecruit)
                .user(user)
                .build();

        studyRegisterRepository.save(studyRegister);
    }

    // TODO: pointService 에서 구현될 수 있도록 수정 (실제 추가 + 로그)
    // 스터디 가입 철회
    @Transactional
    public void withdrawRegister(Long recruitId, Long userId) {
        // 가입 정보 조회
        StudyRegister studyRegister = studyRegisterRepository.findByStudyRecruitIdAndUserId(recruitId, userId)
                .orElseThrow(() -> new IllegalStateException("가입한 스터디가 아닙니다."));

        StudyRecruit studyRecruit = studyRegister.getStudyRecruit();
        User user = studyRegister.getUser();

        // 모집 기간이 끝났다면 가입 철회 불가
        if (LocalDateTime.now().isAfter(studyRecruit.getRecruitEndAt())) {
            throw new IllegalStateException("모집이 종료되어 가입을 철회할 수 없습니다.");
        }

        // 모집 기간이 끝나지 않았다면 포인트 환불
        user.addPoint(studyRecruit.getDeposit());

        studyRegisterRepository.delete(studyRegister);
    }
}
