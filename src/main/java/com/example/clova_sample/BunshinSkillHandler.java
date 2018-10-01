package com.example.clova_sample;

import com.example.clova_sample.dto.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.clova.extension.boot.handler.annnotation.*;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.session.SessionHolder;
import jdk.nashorn.internal.parser.JSONParser;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
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
    private static final String STATUS_STOP = "stop";
    private static final String STATUS_START = "start";
    private static final String STATUS_INPROGRESS = "inProgress";

    // Variable for API
    private User currentUser = new User();
    private Book currentBook = new Book();

    // Error Message
    private static final String ERROR_MESSAGE = "すみません、よく聞き取れないでござる";

    @LaunchMapping
    CEKResponse handleLaunch(SessionHolder sessionHolder) throws IOException {

        // set mode to session
        sessionHolder.getSessionAttributes().put(MODE, MODE_DEFAULT);
        // set status to session
        sessionHolder.getSessionAttributes().put(STATUS, STATUS_STOP);
        // call getUserAPI
        callGetUserAPI(sessionHolder);

        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        return CEKResponse.builder()
                .outputSpeech(text("殿、お待ちしておりました。唱えたい術を伝えて欲しいでござる。"))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("InvokeMode")
    CEKResponse handleInvokeModeIntent(SessionHolder sessionHolder,
                                       @SlotValue Optional<String> mode) throws IOException {

        log.info("0000007" + currentUser.getName());

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

        log.info("order: " + order.orElse(ORDER_DEFAULT));
        // get mode from session
        String mode = sessionHolder.getSessionAttributes().get(MODE).toString();
        // get status from session
        String status = sessionHolder.getSessionAttributes().get(STATUS).toString();
        String outputSpeechText = "";
        if (mode.equals(MODE_BUNSHIN) && status.equals(STATUS_INPROGRESS)) {

            String currentOrder = order.orElse(ORDER_DEFAULT);
            outputSpeechText = flipPage(sessionHolder, currentOrder);

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
        String mode = sessionHolder.getSessionAttributes().get(MODE).toString();
        // get status from session
        String status = sessionHolder.getSessionAttributes().get(STATUS).toString();
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
        String mode = sessionHolder.getSessionAttributes().get(MODE).toString();
        // get status from session
        String status = sessionHolder.getSessionAttributes().get(STATUS).toString();
        String outputSpeechText = "";
        if (mode.equals(MODE_BUNSHIN) && status.equals(STATUS_START)) {
            // set status to session
            sessionHolder.getSessionAttributes().put(STATUS, STATUS_STOP);
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

                // set mode to session
                sessionHolder.getSessionAttributes().put(MODE, MODE_BUNSHIN);
                // set mode to status
                sessionHolder.getSessionAttributes().put(STATUS, STATUS_START);
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

        log.info("currentBook.getTalkList().size(): " + currentBook.getTalkList().size());

        Integer currentPage = currentUser.getCurrentSectionSequence();
        currentPage++;
        log.info("currentPage: " + currentPage);
        String result = "";
        if(currentBook.getTalkList().size() == currentPage){
            Talk currentTalk = currentBook.getTalkList().get(currentUser.getCurrentSectionSequence());
            result =  currentTalk.getText();
            result += "巻物を読み終わったでござる。他に唱えたい術を伝えて欲しいでござる";
            // set status to session
            sessionHolder.getSessionAttributes().put(STATUS, STATUS_STOP);
            // set mode to session
            sessionHolder.getSessionAttributes().put(MODE, MODE_DEFAULT);

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
        if (currentOrder.equals("NEXT")) {
            currentUser.setCurrentSectionSequence(currentUser.getCurrentSectionSequence() + 1);
            result = callbackText(sessionHolder);
        } else if (currentOrder.equals("BEFORE")) {
            currentUser.setCurrentSectionSequence(currentUser.getCurrentSectionSequence() - 1);
            result = callbackText(sessionHolder);
        } else {
            result = ERROR_MESSAGE;
        }
        return result;
    }

    /**
     * ユーザ取得API呼び出し
     * @param sessionHolder
     * @return
     * @throws IOException
     */
    private void callGetUserAPI(SessionHolder sessionHolder) throws IOException {
        // user request
        String url = "https://bm8fzu0fne.execute-api.ap-northeast-1.amazonaws.com/lineboot2018_dev/user?lineuserid=" + sessionHolder.getSession().getUser().getUserId();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);

        try {
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call onCall, IOException e) {

                }

                @Override
                public void onResponse(Call onCall,Response response) throws IOException {
                    try {
                        log.info("0000001" + response);
                        ResponseBody body = response.body();
                        log.info("0000002" + body);
                        String result = body.string();
                        log.info("0000003" + result);
                        // get from response
                        JSONObject resdto = new JSONObject(result);
                        log.info("0000004" + resdto);
                        JSONArray items = resdto.getJSONArray("Items");
                        log.info("0000005" + items);
                        JSONObject item = items.getJSONObject(0);
                        log.info("0000006" + item.getString("name"));
                        currentUser = new ObjectMapper().readValue(item.toString(), User.class);;
                        log.info("0000007" + currentUser.getName());
                        log.info("0000008" + currentUser.getlineuserid());
                        callGetBookAPI(currentUser.getlineuserid());
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * ブック取得API呼び出し
     * @return
     * @throws IOException
     */
    private void callGetBookAPI(String lineUserId) throws IOException {
        // user request
        String url = "https://bm8fzu0fne.execute-api.ap-northeast-1.amazonaws.com/lineboot2018_dev/User?lineuserid=" + lineUserId;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);

        try {
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call onCall, IOException e) {

                }

                @Override
                public void onResponse(Call onCall,Response response) throws IOException {
                    try {
                        log.info("0000001" + response);
                        ResponseBody body = response.body();
                        log.info("0000002" + body);
                        String result = body.string();
                        log.info("0000003" + result);
                        // get from response
                        JSONObject resdto = new JSONObject(result);
                        log.info("0000004" + resdto);
//                        JSONArray items = resdto.getJSONArray("Items");
//                        log.info("0000005" + items);
//                        JSONObject item = items.getJSONObject(0);
//                        log.info("0000006" + item.getString("name"));
                        log.info("0000007" + currentUser.getlineuserid());
                        String sample = "{\"name\" : \"いわせの書\", \"id\" : \"sampleId\"}";
                        // get book from response
                        currentBook = new ObjectMapper().readValue(sample, Book.class);
                        log.info("0000018" + currentBook.getName());
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
