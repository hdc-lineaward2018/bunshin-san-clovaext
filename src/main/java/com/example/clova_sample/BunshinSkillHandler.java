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
import java.util.*;

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
    private Map<String, User> currentUserMap = new HashMap<>();
    private Map<String, Book> currentBookMap = new HashMap<>();
    private static final String URLFORAPI = "https://9vh78csui5.execute-api.ap-northeast-1.amazonaws.com/Mock/";

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
    CEKResponse handleCancelIntent(SessionHolder sessionHolder) {
        log.info("分身さんを終了しました．");
        String outputSpeechText = "さらばでござる。";
        sessionHolder.getSessionAttributes().clear();
        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(true)
                .build();
    }

    @SessionEndedMapping
    CEKResponse handleSessionEnded(SessionHolder sessionHolder) {
        log.info("分身さんを終了しました．");
        String outputSpeechText = "さらばでござる。";
        User currentUser = currentUserMap.get(sessionHolder.getSession().getUser().getUserId());
        sessionHolder.getSessionAttributes().clear();
        currentUserMap.remove(currentUser.getlineuserid());
        currentBookMap.remove(currentUser.getlineuserid() + currentUser.getcurrentbookid());
        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(true)
                .build();
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

                User currentUser = currentUserMap.get(sessionHolder.getSession().getUser().getUserId());
                Book currentBook = currentBookMap.get(currentUser.getlineuserid() + currentUser.getcurrentbookid());
                // set mode to session
                sessionHolder.getSessionAttributes().put(MODE, MODE_BUNSHIN);
                // set mode to status
                sessionHolder.getSessionAttributes().put(STATUS, STATUS_START);
                return "私は" + currentUser.getname() + "でござる。" + currentBook.getname() + "の巻物を読み上げても良いでござるか？";
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

        User currentUser = currentUserMap.get(sessionHolder.getSession().getUser().getUserId());
        Book currentBook = currentBookMap.get(currentUser.getlineuserid() + currentUser.getcurrentbookid());
        Integer currentPage = currentUser.getcurrentsectionsequence();
        currentPage++;
        log.info("currentPage: " + currentPage);
        String result = "";
        if(currentBook.gettalklist().size() == currentPage){
            Talk currentTalk = currentBook.gettalklist().get(currentUser.getcurrentsectionsequence());
            result =  currentTalk.getS();
            result += " ... 巻物を読み終わったでござる。他に唱えたい術を伝えて欲しいでござる";
            // set default to User Sequence
            currentUser.setcurrentsectionsequence(0);
            // set status to session
            sessionHolder.getSessionAttributes().put(STATUS, STATUS_STOP);
            // set mode to session
            sessionHolder.getSessionAttributes().put(MODE, MODE_DEFAULT);

        }else if (currentBook.gettalklist().size() > currentUser.getcurrentsectionsequence() && currentUser.getcurrentsectionsequence() >= 0){
            Talk currentTalk = currentBook.gettalklist().get(currentUser.getcurrentsectionsequence());
            result = currentTalk.getS();
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

        User currentUser = currentUserMap.get(sessionHolder.getSession().getUser().getUserId());
        String result = "";
        if (currentOrder.equals("次")) {
            currentUser.setcurrentsectionsequence(currentUser.getcurrentsectionsequence() + 1);
            result = callbackText(sessionHolder);
        } else if (currentOrder.equals("前")) {
            currentUser.setcurrentsectionsequence(currentUser.getcurrentsectionsequence() - 1);
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
        String url = URLFORAPI + "users/" + sessionHolder.getSession().getUser().getUserId();
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
                        log.info("statuscode : " + response.code());
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
                        User currentUser = new ObjectMapper().readValue(item.toString(), User.class);;
                        log.info("0000007" + currentUser.getname());
                        log.info("0000008" + currentUser.getlineuserid());
                        currentUserMap.put(currentUser.getlineuserid(), currentUser);
                        callGetBookAPI(currentUser.getlineuserid(), currentUser.getcurrentbookid());
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
    private void callGetBookAPI(String lineUserId, String bookId) throws IOException {
        // user request
        String url = URLFORAPI + "users/" + lineUserId + "/books/" + bookId;
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
                        log.info("0000011" + response);
                        ResponseBody body = response.body();
                        log.info("0000012" + body);
                        String result = body.string();
                        log.info("0000013" + result);
                        // get from response
                        JSONObject resdto = new JSONObject(result);
                        log.info("0000014" + resdto);
                        JSONArray items = resdto.getJSONArray("Items");
                        log.info("0000015" + items);
                        JSONObject item = items.getJSONObject(0);
                        // get book from response
                        Book currentBook = new ObjectMapper().readValue(item.toString(), Book.class);
                        log.info("0000118" + currentBook.getname());
                        JSONArray talks = item.getJSONArray("talklist");
                        log.info("0001118" + talks.toString());
                        log.info("0001118" + talks.length());
                        currentBook.gettalklist().clear();
                        for(Integer i = 0; i < talks.length(); i++){
                            log.info("0001118" + talks.getJSONObject(i).get("S").toString());
                            Talk t = new Talk();
                            t.sets(talks.getJSONObject(i).get("S").toString());
                            currentBook.gettalklist().add(t);
                        }

                        currentBookMap.put(currentBook.getlineuserid() + currentBook.getbookid(), currentBook);
                        log.info("0000018" + currentBook.getname());
                        log.info("0000019" + currentBook.gettalklist().get(0).getS());
                        log.info("0000019" + currentBook.gettalklist().get(1).getS());
//                        log.info("0000019" + currentBook.gettalklist().get(3).gets());
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
