package com.example.clova_sample;

import com.example.clova_sample.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.clova.extension.boot.handler.annnotation.*;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.session.SessionHolder;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.linecorp.clova.extension.boot.message.speech.OutputSpeech.text;

@CEKRequestHandler
public class BunshinSkillHandler {

    private static final Logger log = LoggerFactory.getLogger(BunshinSkillHandler.class);

    // Session Variable
    private static final String USER = "user";
    private static final String MODE = "mode";
    private static final String BOOK = "book";
    private static final String STATUS = "status";

    // Mode
    private static final String MODE_DEFAULT = "defaultMode";
    private static final String MODE_BUNSHIN = "bunshinMode";

    // Order
    private static final String ORDER_DEFAULT = "defaultOrder";

    // Status
    private static final String STATUS_START = "start";
    private static final String STATUS_INPROGRESS = "inProgress";
    private static final String STATUS_END = "end";

    // Error Message
    private static final String ERROR_MESSAGE = "すみません、よく聞き取れないでござる";

    @LaunchMapping
    CEKResponse handleLaunch(SessionHolder sessionHolder) throws IOException {

        // user request
        String url = "https://script.google.com/macros/s/AKfycbwS1yJ-1fjjW-aH9UlIQXIqIwVLuOnFK2spBVlAojryJ9rz3Uvt/exec&lineUserId=" + sessionHolder.getSession().getUser().getUserId();
        OkHttpClient client = new OkHttpClient();
        String result = "";
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            ResponseBody body = response.body();
            result = body.string();
            // get from response
            ResponseDTO resdto = new ObjectMapper().readValue(result, ResponseDTO.class);
            log.info(resdto.toString());
        }catch(IOException e){
            e.getMessage();
        }
        User currentUser = new User();
//        // get from response
//        User currentUser = new ObjectMapper().readValue(result, User.class);
//        // set user to session
//        sessionHolder.getSessionAttributes().put(USER, currentUser);
//        // set mode to session
//        sessionHolder.getSessionAttributes().put(MODE, MODE_DEFAULT);

        return CEKResponse.builder()
                .outputSpeech(text(currentUser.getName() + "殿、お待ちしておりました。唱えたい術を伝えて欲しいござる。"))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("InvokeMode")
    CEKResponse handleInvokeModeIntent(SessionHolder sessionHolder,
                                       @SlotValue Optional<String> mode) throws IOException {

        log.info("mode:" + mode.orElse(MODE_DEFAULT));
        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        String currentMode = mode.orElse(MODE_DEFAULT);
        // set mode to session
        sessionHolder.getSessionAttributes().put(MODE, currentMode);
        // invoke callbackMode
        String outputSpeechText = callbackMode(currentMode, sessionHolder);

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("FripPage")
    CEKResponse handleFripPageIntent(SessionHolder sessionHolder,
                                     @SlotValue Optional<String> order) {

        // get mode from session
        String mode = (String) sessionHolder.getSessionAttributes().get(MODE);
        // get status from session
        String status = (String) sessionHolder.getSessionAttributes().get(STATUS);
        String outputSpeechText = "";
        if (mode.equals(MODE_BUNSHIN) && status.equals(STATUS_INPROGRESS)) {
            String currentOrder = order.orElse(ORDER_DEFAULT);
            if (currentOrder.equals("次")) {
                outputSpeechText = "フリップページ作成中。次です。";
            } else if (currentOrder.equals("前")) {
                outputSpeechText = "フリップページ作成中。前です。";
            } else {
                outputSpeechText = ERROR_MESSAGE;
            }
        } else {
            outputSpeechText = ERROR_MESSAGE;
        }

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.YesIntent")
    CEKResponse handleYesIntent(SessionHolder sessionHolder) throws IOException {

        String mode = (String) sessionHolder.getSessionAttributes().get(MODE);
        String status = (String) sessionHolder.getSessionAttributes().get(STATUS);
        List<Talk> talkList = new ArrayList<>();
        List<String> outputSpeechTexts = new ArrayList<>();
        if (mode.equals(MODE_BUNSHIN) && status.equals(STATUS_START)) {

            User currentUser = (User) sessionHolder.getSessionAttributes().get(USER);
            //
            // SectionRequestとセリフ格納処理
            //
//                //Section取得リクエスト
//                String url = "https://api.line.me/v2/profile";
//                OkHttpClient client = new OkHttpClient();
//                Request request = new Request.Builder()
//                        .url(url)
//                        .build();
//                Call call = client.newCall(request);
//                try {
//                    Response response = call.execute();
//                    ResponseBody body = response.body();
//                    String result = body.string();
//                    log.info(result);
//                } catch (IOException e) {
//                    e.getMessage();
//                }

            String response = "{\"name\" : \"いわせ あつや\", \"id\" : \"id\", \"sequence\" : 1, \"records\" : [ { \"name\" : \"ああああ\" }, { \"name\" : \"いいいい\" }]}";

            // get Section from response
            Section currentSection = new ObjectMapper().readValue(response, Section.class);
            // set message to outputSpeechTexts
            for(Talk t : talkList){
                outputSpeechTexts.add(t.getText());
            }
            // set mode to session
            sessionHolder.getSessionAttributes().put(MODE, MODE_BUNSHIN);


            // set status to session
            sessionHolder.getSessionAttributes().put(STATUS, STATUS_INPROGRESS);

        } else {
            outputSpeechTexts.add(ERROR_MESSAGE);
        }

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
                .outputSpeech(text("さらばでござる。"))
                .shouldEndSession(true)
                .build();
    }

    @SessionEndedMapping
    CEKResponse handleSessionEnded() {
        log.info("サンプルスキルを終了しました．");
        return CEKResponse.empty();
    }

// ------------- private ----------------

    private String callbackMode(String mode, SessionHolder sessionHolder) throws IOException {
        switch (mode) {
            case "分身":

//                //Book取得リクエスト
//                String url = "https://api.line.me/v2/profile";
//                OkHttpClient client = new OkHttpClient();
//                Request request = new Request.Builder()
//                        .url(url)
//                        .build();
//                Call call = client.newCall(request);
//                try {
//                    Response response = call.execute();
//                    ResponseBody body = response.body();
//                    String result = body.string();
//                    log.info(result);
//                } catch (IOException e) {
//                    e.getMessage();
//                }

                String response = "{\"name\" : \"いわせ あつや\", \"id\" : \"id\"}";

                // get book from response
                Book currentBook = new ObjectMapper().readValue(response, Book.class);
                // set book to session
                sessionHolder.getSessionAttributes().put(BOOK, currentBook);
                // get user from session
                User currentUser = (User) sessionHolder.getSessionAttributes().get(USER);
                // set mode to session
                sessionHolder.getSessionAttributes().put(MODE, MODE_BUNSHIN);
                return "私は" + currentUser.getName() + "でござる。" + currentBook.getName() + "の巻物を読み上げても良いでござるか？";
            default:
                return ERROR_MESSAGE;
        }
    }

}
