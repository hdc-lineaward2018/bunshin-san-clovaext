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
    CEKResponse handleLaunch(SessionHolder sessionHolder) {

        /* 待機時間テスト
        * 結果：
        5s: x
        3s: x
        2.5s: x
        2s: 成功　9/10
        1.5s : 成功　9/10
        1s: o
        */
//        this.sleep(1500);

        this.getUserInfo(sessionHolder);

        User currentUser = new User();

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


        /* 待機時間テスト
        * 結果：
        5s: x
        3s: x
        2s: 成功 10/10
        1.5s :
        1s:
        */
        this.sleep(2000);

        return CEKResponse.builder()
                .outputSpeech(text("処理成功"))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.YesIntent")
    CEKResponse handleYesIntent(SessionHolder sessionHolder) throws IOException {

        /* 待機時間テスト
        * 結果：
        5s: x
        3s: x
        2s: 成功　10/10
        1.5s :
        1s:
        */
        this.sleep(2000);

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


    private void getUserInfo(SessionHolder sessionHolder){
        long start = System.currentTimeMillis();

//        // user request
//        String url = "https://script.google.com/macros/s/AKfycbwS1yJ-1fjjW-aH9UlIQXIqIwVLuOnFK2spBVlAojryJ9rz3Uvt/exec?lineUserId="+ Math.random();
//
//        log.info("XXX DEBUG XXX : " + url);
//
//        OkHttpClient client = new OkHttpClient();
//        String result = "";
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//        Call call = client.newCall(request);
//        try {
//            Response response = call.execute();
//            ResponseBody body = response.body();
//            result = body.string();
//
//            log.info("-------- result = " + result);
//
//
//            // get from response
////            ResponseDTO resdto = new ObjectMapper().readValue(result, ResponseDTO.class);
////            log.info("0000004" + resdto.toString());
//        }catch(Exception e){
//            e.getMessage();
//        }

        // original
        // user request
        String url = "https://script.google.com/macros/s/AKfycbwS1yJ-1fjjW-aH9UlIQXIqIwVLuOnFK2spBVlAojryJ9rz3Uvt/exec?lineUserId=" + sessionHolder.getSession().getUser().getUserId();
        OkHttpClient client = new OkHttpClient();
        String result = "";
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            log.info("0000001" + response);
            ResponseBody body = response.body();
            log.info("0000002" + body);
            result = body.string();
            log.info("0000003" + result);
            // get from response
            ResponseDTO resdto = new ObjectMapper().readValue(result, ResponseDTO.class);
            log.info("0000004" + resdto.toString());
        }catch(IOException e){
            e.getMessage();
        }

        long end = System.currentTimeMillis();
        log.info("XXX DEBUG XXX : 処理時間　＝　" + (end - start)  + "ms");
    }

    private void sleep(int millis){

        try {

            long start = System.currentTimeMillis();
            Thread.sleep(millis);
            long end = System.currentTimeMillis();
            log.info("XXX DEBUG XXX : 処理時間　＝　" + (end - start)  + "ms");

        }catch(Exception e){
        }
    }
}
