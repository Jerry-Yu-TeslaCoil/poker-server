package com.game.pokerserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.pokerserver.domain.DataVO;
import lombok.extern.slf4j.Slf4j;
import table.rule.decision.DecisionRequest;
import table.rule.decision.PlayerDecision;
import table.rule.decision.impl.CallDecision;
import table.rule.decision.impl.FoldDecision;
import table.rule.decision.impl.RaiseDecision;
import table.vo.privateinfo.PlayerPrivateVO;
import table.vo.publicinfo.PublicVO;

import java.math.BigDecimal;

@Slf4j
public class DataJsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String convertToJson(PublicVO publicVO) {
        return convertToJson("PUBLIC_VO", publicVO);
    }

    public static String convertToJson(PlayerPrivateVO privateVO) {
        return convertToJson("PRIVATE_VO", privateVO);
    }

    public static String convertToJson(DecisionRequest decisionRequest) {
        return convertToJson("DECISION_REQUEST", decisionRequest);
    }

    public static String convertToJson(String messageType, Object message) {
        try {
            DataVO dataVO = new DataVO(messageType, message);

            return objectMapper.writeValueAsString(dataVO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON convert failed", e);
        }
    }

    public static PlayerDecision convertToPlayerDecision(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(message);

            String dataType = rootNode.get("dataType").asText();

            JsonNode dataNode = rootNode.get("data");

            String decision = dataNode.get("decision").asText();
            int amountInt = dataNode.get("amount").asInt();      // 转换为int 4

            if (!dataType.equals("PLAYER_DECISION")) {
                throw new RuntimeException("Convert failed. DataType is not PLAYER_DECISION");
            }

            return switch (decision) {
                case "CALL" -> new CallDecision();
                case "RAISE" -> new RaiseDecision(new BigDecimal(amountInt));
                case "FOLD" -> new FoldDecision();
                default -> throw new RuntimeException("Convert failed. Unknown decision: " + decision);
            };

        } catch (Exception e) {
            log.error("convertToPlayerDecision failed", e);
            throw new RuntimeException("Convert failed", e);
        }
    }
}

/*
{"dataType":"PLAYER_DECISION","data":{"decision":"CALL","amount":"0"}}
 */