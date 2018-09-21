package com.example.clova_sample;

import com.linecorp.clova.extension.boot.handler.annnotation.*;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.session.SessionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.linecorp.clova.extension.boot.message.speech.OutputSpeech.text;

@CEKRequestHandler
public class BunshinSkillHandler {

    private static final Logger log = LoggerFactory.getLogger(BunshinSkillHandler.class);

    // スロット名
    public static final String MODE = "mode";

    // スロット値
    public static final String DEFAULT_MODE = "defaultMode";
    public static final String ERROR_MESSAGE = "すみません、よく聞き取れないでござる";

    @LaunchMapping
    CEKResponse handleLaunch(SessionHolder sessionHolder) {

        return CEKResponse.builder()
                .outputSpeech(text("ようこそ。ユーザどの。唱えたい術を伝えてほしいでござる"))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("InvokeMode")
    CEKResponse handleInvokeModeIntent(SessionHolder sessionHolder,
                                       @SlotValue Optional<String> mode) {

        log.info("mode:" + mode.orElse(DEFAULT_MODE));
        log.info("User Instance:" + sessionHolder.getSession().getUser());
        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        // マルチターン対話のために入力されたモードをsessionに格納
        sessionHolder.getSessionAttributes().put(MODE, mode.orElse(DEFAULT_MODE));

        String outputSpeechText = callbackMode(mode.orElse(DEFAULT_MODE));

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("FripPage")
    CEKResponse handleFripPageIntent(SessionHolder sessionHolder,
                                     @SlotValue Optional<String> order) {

        String outputSpeechText = "入力されたテキストは" + order.orElse("デフォルト値") + "です";

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.YesIntent")
    CEKResponse handleYesIntent(SessionHolder sessionHolder) {

        return CEKResponse.builder()
                .outputSpeech(text(String.join("。", ERROR_MESSAGE))) // 複数行のテキストを連結して発話
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.NoIntent")
    CEKResponse handleNoIntent(SessionHolder sessionHolder) {

        return CEKResponse.builder()
                .outputSpeech(text(String.join("。", ERROR_MESSAGE))) // 複数行のテキストを連結して発話
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.GuideIntent")
    CEKResponse handleGuideIntent(SessionHolder sessionHolder) {

        return CEKResponse.builder()
                .outputSpeech(text(String.join("。", ERROR_MESSAGE))) // 複数行のテキストを連結して発話
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.CancelIntent")
    CEKResponse handleCancelIntent() {
        return CEKResponse.builder()
                .outputSpeech(text("さらばでござる"))
                .shouldEndSession(true)
                .build();
    }

    @SessionEndedMapping
    CEKResponse handleSessionEnded() {
        log.info("サンプルスキルを終了しました．");
        return CEKResponse.empty();
    }

// ------------- private ----------------

    private String callbackMode(String mode) {
        switch (mode) {
            case "分身":
                return "分身の術を起動します．あなたの代わりに秘密の呪文はこれこれ。";
            default:
                return "聞き取れませんでした";
        }
    }

}
