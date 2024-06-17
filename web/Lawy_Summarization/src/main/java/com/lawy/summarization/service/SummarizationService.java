package com.lawy.summarization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.recap.Summarizer;
import org.recap.graph.Graph;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
@Log4j2
@RequiredArgsConstructor
public class SummarizationService {
    private final ObjectMapper mapper;
    private final Summarizer summarizer = new Summarizer(); // recap 요약기

    public synchronized void requestSummarization(WebSocketSession session, String text) {
        // 텍스트 전처리
        text = text.replaceAll("([0-9])\\.", "$1"); // 날짜 수정 및 '1.', '2.'와 같은 숫자 수정

        String summarizedText = ""; // 요약된 텍스트가 저장될 변수

        for (String summarizedSentence : summarizer.summarize(text, Graph.SimilarityMethods.COSINE_SIMILARITY)) {
            summarizedText = summarizedText.concat("- " + summarizedSentence + "\n\n");
        }

        sendMessage(session, summarizedText);
    }

    public void sendMessage(WebSocketSession session, String text) {
        try {
            if (session.isOpen()) { // 세션이 열려 있을 경우에만 메시지를 전송함
                session.sendMessage(new TextMessage(text)); // 메시지 내용을 문자열로 변환하여 메시지를 전송함
                log.info(session.getId() + " ← " + text);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
