package com.example.clova_sample;

import com.example.clova_sample.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.clova.extension.boot.handler.annnotation.*;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.session.SessionHolder;
import jdk.nashorn.internal.parser.JSONParser;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;

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
    private static final String STATUS_DEFAULT = "default";
    private static final String STATUS_START = "start";
    private static final String STATUS_INPROGRESS = "inProgress";
    private static final String STATUS_END = "end";

    // Error Message
    private static final String ERROR_MESSAGE = "すみません、よく聞き取れないでござる";

    @LaunchMapping
    CEKResponse handleLaunch(SessionHolder sessionHolder) throws IOException {

        String response = "{\"name\" : \"いわせ あつや\", \"lineUserID\" : \"id\", \"currentSectionSequence\" : 0}";
        // get from response
        User currentUser = new ObjectMapper().readValue(response, User.class);
        // set user to session
        sessionHolder.getSessionAttributes().put(USER, currentUser);
        // set mode to session
        sessionHolder.getSessionAttributes().put(MODE, MODE_DEFAULT);
        // set status to session
        sessionHolder.getSessionAttributes().put(STATUS, STATUS_DEFAULT);

        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        return CEKResponse.builder()
                .outputSpeech(text(currentUser.getName() + "殿、お待ちしておりました。唱えたい術を伝えて欲しいでござる。"))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("InvokeMode")
    CEKResponse handleInvokeModeIntent(SessionHolder sessionHolder,
                                       @SlotValue Optional<String> mode) throws IOException {

        String currentMode = mode.orElse(MODE_DEFAULT);
        // set mode to session
        sessionHolder.getSessionAttributes().put(MODE, currentMode);
        // invoke callbackMode
        String outputSpeechText = callbackMode(currentMode, sessionHolder);

        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("FripPage")
    CEKResponse handleFripPageIntent(SessionHolder sessionHolder,
                                     @SlotValue Optional<String> order) throws IOException {

        // get mode from session
        String mode = (String) sessionHolder.getSessionAttributes().get(MODE);
        // get status from session
        String status = (String) sessionHolder.getSessionAttributes().get(STATUS);
        String outputSpeechText = "";
        if (mode.equals(MODE_BUNSHIN) && status.equals(STATUS_INPROGRESS)) {

            String currentOrder = order.orElse(ORDER_DEFAULT);
            flipPage(sessionHolder, currentOrder);

        } else {
            outputSpeechText = ERROR_MESSAGE;
        }

        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.YesIntent")
    CEKResponse handleYesIntent(SessionHolder sessionHolder) throws IOException {

        // get mode from session
        String mode = (String) sessionHolder.getSessionAttributes().get(MODE);
        // get status from session
        String status = (String) sessionHolder.getSessionAttributes().get(STATUS);
        String outputSpeechText = "";
        if (mode.equals(MODE_BUNSHIN) && status.equals(STATUS_START)) {

            // set status to session
            sessionHolder.getSessionAttributes().put(STATUS, STATUS_INPROGRESS);
            outputSpeechText = callbackText(sessionHolder);

        } else {
            outputSpeechText = ERROR_MESSAGE;
        }

        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.NoIntent")
    CEKResponse handleNoIntent(SessionHolder sessionHolder) {

        // get mode from session
        String mode = (String) sessionHolder.getSessionAttributes().get(MODE);
        // get status from session
        String status = (String) sessionHolder.getSessionAttributes().get(STATUS);
        String outputSpeechText = "";
        if (mode.equals(MODE_BUNSHIN) && status.equals(STATUS_START)) {
            // set status to session
            sessionHolder.getSessionAttributes().put(STATUS, STATUS_DEFAULT);
            // set mode to session
            sessionHolder.getSessionAttributes().put(MODE, MODE_DEFAULT);
            outputSpeechText = "中止でござるか。他に唱えたい術を伝えて欲しいでござる。";

        } else {
            outputSpeechText = ERROR_MESSAGE;
        }

        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText)) // 複数行のテキストを連結して発話
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.GuideIntent")
    CEKResponse handleGuideIntent(SessionHolder sessionHolder) {

        String outputSpeechText = "術を唱えることで、色々できるでござる。";

        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText)) // 複数行のテキストを連結して発話
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.CancelIntent")
    CEKResponse handleCancelIntent() {

        String outputSpeechText = "さらばでござる。";

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(true)
                .build();
    }

    @SessionEndedMapping
    CEKResponse handleSessionEnded(SessionHolder sessionHolder) {
        log.info("分身さんを終了しました．");
        sessionHolder.getSessionAttributes().clear();
        return CEKResponse.empty();
    }

// ------------- private ----------------

    /**
     * モードによって読み上げテキストを返す
     * @param mode
     * @param sessionHolder
     * @return
     * @throws IOException
     */
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

                String response = "{\"name\" : \"いわせの書\", \"id\" : \"id\"}";
                // get book from response
                Book currentBook = new ObjectMapper().readValue(response, Book.class);
                // set book to session
                sessionHolder.getSessionAttributes().put(BOOK, currentBook);
                // get user from session
                Object obj = sessionHolder.getSessionAttributes().get(USER);
                User currentUser = (User)obj;

                // set mode to session
                sessionHolder.getSessionAttributes().put(MODE, MODE_BUNSHIN);
                return "私は" + currentUser.getName() + "でござる。" + currentBook.getName() + "の巻物を読み上げても良いでござるか？";
            default:
                return ERROR_MESSAGE;
        }
    }

    /**
     * ブック内の読み上げテキストを返す
     * @param sessionHolder
     * @return
     * @throws IOException
     */
    private String callbackText(SessionHolder sessionHolder) throws IOException {

        User currentUser = (User) sessionHolder.getSessionAttributes().get(USER);
        Book currentBook = (Book) sessionHolder.getSessionAttributes().get(BOOK);
        String result = "";
        if(currentBook.getTalkList().size() == currentUser.getCurrentSectionSequence() + 1){
            Talk currentTalk = currentBook.getTalkList().get(currentUser.getCurrentSectionSequence());
            result =  currentTalk.getText();
            // set status to session
            sessionHolder.getSessionAttributes().put(STATUS, STATUS_DEFAULT);
            // set mode to session
            sessionHolder.getSessionAttributes().put(MODE, MODE_DEFAULT);
            result += "巻物を読み終わったでござる。他に唱えたい術を伝えて欲しいでござる";

        }else if (currentBook.getTalkList().size() > currentUser.getCurrentSectionSequence() && currentUser.getCurrentSectionSequence() >= 0){
            Talk currentTalk = currentBook.getTalkList().get(currentUser.getCurrentSectionSequence());
            result = currentTalk.getText();
        }else{
            result = ERROR_MESSAGE;
        }

        return result;
    }

    /**
     * ページめくり処理
     * @param sessionHolder
     * @param currentOrder
     * @return
     */
    private String flipPage(SessionHolder sessionHolder, String currentOrder) throws IOException {

        String result = "";
        User currentUser = (User) sessionHolder.getSessionAttributes().get(USER);
        Integer currentPage = currentUser.getCurrentSectionSequence();

        if (currentOrder.equals("次")) {
            currentPage++;
            result = callbackText(sessionHolder);
        } else if (currentOrder.equals("前")) {
            currentPage--;
            result = callbackText(sessionHolder);
        } else {
            result = ERROR_MESSAGE;
        }
        return result;
    }

    /**
     * 外部API叩き用サンプル Clovaでは現状使用不可
     * @param sessionHolder
     * @return
     * @throws IOException
     */
    private String callAPISample(SessionHolder sessionHolder) throws IOException {
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
            JSONObject resdto = new JSONObject(result);
            log.info("0000004" + resdto);
            Boolean boo = resdto.getBoolean("success");
            JSONObject bar = resdto.getJSONObject("result");
            log.info("0000005" + bar.getString("name"));
            result = bar.getString("name");

        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return result;
    }
}
