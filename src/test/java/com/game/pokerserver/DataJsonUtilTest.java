package com.game.pokerserver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.game.pokerserver.domain.DataVO;
import com.game.pokerserver.domain.PlayerIdentity;
import com.game.pokerserver.util.DataJsonUtil;
import control.vo.PlayerPersonalVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import table.card.PokerCard;
import table.card.Rank;
import table.card.Suit;
import table.card.impl.FixedPokerCard;
import table.rule.decision.DecisionRequest;
import table.rule.decision.PlayerDecision;
import table.state.GameState;
import table.vo.privateinfo.PlayerPrivateVO;
import table.vo.publicinfo.PlayerPublicVO;
import table.vo.publicinfo.PotPublicVO;
import table.vo.publicinfo.PublicVO;
import table.vo.publicinfo.TablePublicVO;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class DataJsonUtilTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testPublicVOConvertToJson() {
        // 创建测试数据
        TablePublicVO tablePublicVO = new TablePublicVO(
                new BigDecimal(1),
                new BigDecimal(24),
                GameState.INIT,
                2,
                null,
                new PokerCard[5]
        );

        PlayerPublicVO[] playerPublicVO = new PlayerPublicVO[3];
        playerPublicVO[0] = new PlayerPublicVO(
                new PlayerPersonalVO<>(new PlayerIdentity("Jackson")),
                null,
                new BigDecimal(24),
                new BigDecimal(0),
                true,
                new BigDecimal(0)
        );
        playerPublicVO[1] = new PlayerPublicVO(
                new PlayerPersonalVO<>(new PlayerIdentity("Bob")),
                null,
                new BigDecimal(22),
                new BigDecimal(2),
                true,
                new BigDecimal(0)
        );
        playerPublicVO[2] = new PlayerPublicVO(
                new PlayerPersonalVO<>(new PlayerIdentity("Robot")),
                null,
                new BigDecimal(24),
                new BigDecimal(0),
                false,
                new BigDecimal(0)
        );

        PotPublicVO potPublicVO = new PotPublicVO(new BigDecimal(2));
        PublicVO publicVO = new PublicVO(tablePublicVO, playerPublicVO, potPublicVO);

        String jsonString = DataJsonUtil.convertToJson(publicVO);

        log.info(jsonString);

        try {
            mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            mapper.addMixIn(PlayerPersonalVO.class, PlayerPersonalVOMinIn.class);

            DataVO parsedPublicVO = mapper.readValue(jsonString, DataVO.class);

            assertNotNull(parsedPublicVO, "Parsed DataVO should not be null");

            PublicVO parsedData = mapper.convertValue(parsedPublicVO.getData(), PublicVO.class);
            assertEquals(publicVO.tablePublicVO().basicBet(),
                    parsedData.tablePublicVO().basicBet(),
                    "Basic bet should match after serialization");

            assertEquals(publicVO.playerPublicVO().length,
                    parsedData.playerPublicVO().length,
                    "Player count should match after serialization");

        } catch (Exception e) {
            log.error("JSON parse failed: {}", e.getMessage());
            fail("JSON parsing should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testPlayerPrivateVOConvertToJson() {
        PlayerPrivateVO playerPrivateVO = new PlayerPrivateVO(
                new PokerCard[]{
                        new FixedPokerCard(Suit.clubs, Rank.NINE),
                        new FixedPokerCard(Suit.hearts, Rank.TEN)
                });
        String jsonString = DataJsonUtil.convertToJson(playerPrivateVO);
        log.info(jsonString);
    }

    @Test
    public void testDecisionRequestConvertToJson() {
        DecisionRequest request = new DecisionRequest(new BigDecimal(5), new BigDecimal(2));
        String jsonString = DataJsonUtil.convertToJson(request);
        log.info(jsonString);
    }

    @Test
    public void testJsonConvertToPlayerDecision() {
        String jsonString = "{\"dataType\":\"PLAYER_DECISION\", \"data\":{\"decision\":\"RAISE\", \"amount\":\"4\"}}";
        log.info(jsonString);
        PlayerDecision playerDecision = DataJsonUtil.convertToPlayerDecision(jsonString);
        log.info(playerDecision.toString());
    }
}

abstract class PlayerPersonalVOMinIn<T> {

    @JsonCreator
    public PlayerPersonalVOMinIn(@JsonProperty("info") T info) {
    }

    @JsonGetter("info")
    public abstract T getInfo();
}