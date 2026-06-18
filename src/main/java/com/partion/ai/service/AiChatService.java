package com.partion.ai.service;

import com.partion.ai.dto.AiChatRequest;
import com.partion.ai.dto.AiChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private static final String SYSTEM_PROMPT = """
            당신은 Partion 서비스의 AI 안내 챗봇입니다.

            Partion은 STO(Security Token Offering) 기반 투자와 거래 흐름을 가상 환경에서 체험하는 시뮬레이션 웹 서비스입니다.
            사용자는 예치금을 충전하고, 모집 중인 STO 상품에 투자하거나, 모집이 완료된 상품 토큰을 거래할 수 있습니다.

            Partion 서비스 규칙:
            - 이 서비스는 실제 투자 서비스가 아니라 학습 목적의 시뮬레이션 서비스입니다.
            - 예치금 충전은 Toss Payments Sandbox를 통해 진행됩니다.
            - FUNDING 상태의 상품은 투자할 수 있습니다.
            - 모집률이 100%가 되면 상품은 TRADING 상태가 되고 거래할 수 있습니다.
            - 마감일까지 목표 금액을 달성하지 못한 상품은 CLOSED 처리되고 투자금은 예치금으로 환불됩니다.
            - 사용자는 포트폴리오에서 보유 자산과 총 자산을 확인할 수 있습니다.
            - 거래는 매수/매도 주문을 생성하는 방식으로 진행됩니다.
            - 게시판과 댓글 기능을 통해 사용자 간 의견을 나눌 수 있습니다.

            답변 원칙:
            - Partion 서비스 사용법과 STO 기본 개념을 한국어로 쉽게 설명하세요.
            - 실제 투자 조언, 특정 상품 매수 추천, 수익 보장 표현은 하지 마세요.
            - 사용자의 개인정보, 보유 자산, 투자 내역을 직접 조회할 수 있는 것처럼 답하지 마세요.
            - DB 조회가 필요한 질문은 해당 메뉴에서 확인하라고 안내하세요.
            - 답변은 친절하되 5문장 이내로 간결하게 작성하세요.
            """;

    private final ChatClient chatClient;
    private final AiDocumentSearchService aiDocumentSearchService;

    public AiChatService(
            ChatClient.Builder chatClientBuilder,
            AiDocumentSearchService aiDocumentSearchService
    ) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
        this.aiDocumentSearchService = aiDocumentSearchService;
    }

    public AiChatResponse chat(AiChatRequest request) {
        String context = aiDocumentSearchService.searchRelevantContext(request.getMessage());

        String userPrompt = """
            아래는 Partion 서비스 가이드와 STO 안내 문서에서 검색된 참고 내용입니다.

            [참고 문서]
            %s

            [사용자 질문]
            %s

            참고 문서를 우선적으로 활용해 답변하세요.
            참고 문서에 없는 내용은 추측하지 말고, 서비스 메뉴에서 확인이 필요하다고 안내하세요.
            """.formatted(context, request.getMessage());

        String answer = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        return new AiChatResponse(answer);
    }
}
