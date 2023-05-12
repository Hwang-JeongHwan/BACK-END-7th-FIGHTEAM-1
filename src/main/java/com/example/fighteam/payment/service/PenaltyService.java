package com.example.fighteam.payment.service;

import com.example.fighteam.payment.domain.Apply;
import com.example.fighteam.payment.domain.History;
import com.example.fighteam.payment.domain.HistoryType;
import com.example.fighteam.payment.domain.Member;
import com.example.fighteam.payment.repository.ApplyRepository;
import com.example.fighteam.payment.repository.HistoryRepository;
import com.example.fighteam.payment.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PenaltyService {


    private final ApplyRepository applyRepository;
    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;


    public int penaltyLogic (Long id, int cost) {

        Apply findApply = applyRepository.findByMember(id);
        findApply.minusUserDeposit(cost);
        Long postId = findApply.getPost().getId();

        int countMember = applyRepository.findCountByPostId(postId); //벌금을 내는 사람은 빼고 계산

        int returnPenalty = cost / countMember;//벌금 / 같은 팀원의 수
        List<Apply> anotherApply = applyRepository.findAnotherApply(id, postId);

        //여기 벌크연산하는게 나을듯
        for (Apply apply : anotherApply) {
            //벌금을 내지않은 팀원들에게 패널티 금액 분배
            apply.plusUserDeposit(returnPenalty);
//            System.out.println("apply.getUserDeposit() = " + apply.getUserDeposit());
        }

        return findApply.getUserDeposit();
    }

    public int returnDeposit(Long id) {
        //팀 프로젝트 종료후 보증금 반환
        Apply findApply = applyRepository.findByMember(id);
        int userDeposit = findApply.getUserDeposit();
        Member findMember = findApply.getMember();
        findMember.plusDeposit(userDeposit);

        History history = new History(findMember, findApply, HistoryType.REFUND, userDeposit, findMember.getDeposit());
        historyRepository.saveHistory(history);
        return userDeposit;

    }
}
