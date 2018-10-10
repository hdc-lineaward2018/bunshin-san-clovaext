package com.example.clova_sample;

import com.example.clova_sample.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.clova.extension.boot.handler.annnotation.*;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.session.SessionHolder;
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
    private static final String MODE = "mode";
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
    private static final String URLFORAPI = System.getenv("DB_URL");

    // Message
    private static final String LAUNCH_MESSAGE = "殿、お待ちしておりました。唱えたい術を伝えて欲しいでござる。";
    private static final String CANCEL_MESSAGE = "中止でござるか。他に唱えたい術を伝えて欲しいでござる。";
    private static final String GUIDE_MESSAGE = "分身の術と唱えることで、LINEからお助け忍者に登録した巻物を読み上げるでござる。";
    private static final String INVOKE_MESSAGE = "拙者は{0}の分身でござる。巻物を読み上げても良いでござるか？";
    private static final String ERROR_SOUND_MESSAGE = "よく聞き取れないでござる。";
    private static final String ERROR_USER_MESSAGE = "殿の情報をうまく読み取れなかったでござる。LINEからお助け忍者を友だち追加されていることを確認するでござる。";
    private static final String ERROR_BOOK_MESSAGE = "巻物の情報をうまく読み取れなかったでござる。LINEからお助け忍者に巻物が登録されていることを確認するでござる。";
    private static final String ERROR_MODE_MESSAGE = "の術は覚えていないでござる。";
    private static final String END_MESSAGE = "。。。 巻物を読み終わったでござる。他に唱えたい術を伝えて欲しいでござる。";

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
                .outputSpeech(text(LAUNCH_MESSAGE))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("InvokeMode")
    CEKResponse handleInvokeModeIntent(SessionHolder sessionHolder,
                                       @SlotValue Optional<String> mode) throws IOException {


        String currentMode = mode.orElse(MODE_DEFAULT);
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
            outputSpeechText = ERROR_SOUND_MESSAGE;
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
            outputSpeechText = ERROR_SOUND_MESSAGE;
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
            outputSpeechText = CANCEL_MESSAGE;

        } else {
            outputSpeechText = ERROR_SOUND_MESSAGE;
        }

        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.GuideIntent")
    CEKResponse handleGuideIntent(SessionHolder sessionHolder) {

        String outputSpeechText = GUIDE_MESSAGE;

        log.info("SessionAttribute:" + sessionHolder.getSessionAttributes());

        return CEKResponse.builder()
                .outputSpeech(text(outputSpeechText))
                .shouldEndSession(false)
                .build();
    }

    @IntentMapping("Clova.CancelIntent")
    CEKResponse handleCancelIntent(SessionHolder sessionHolder) {
        User currentUser = currentUserMap.get(sessionHolder.getSession().getUser().getUserId());
        sessionHolder.getSessionAttributes().clear();
        currentUserMap.remove(currentUser.getlineuserid());
        currentBookMap.remove(currentUser.getlineuserid() + currentUser.getcurrentbookid());
        return CEKResponse.builder()
                .shouldEndSession(true)
                .build();
    }

    @SessionEndedMapping
    CEKResponse handleSessionEnded(SessionHolder sessionHolder) {
        User currentUser = currentUserMap.get(sessionHolder.getSession().getUser().getUserId());
        sessionHolder.getSessionAttributes().clear();
        currentUserMap.remove(currentUser.getlineuserid());
        currentBookMap.remove(currentUser.getlineuserid() + currentUser.getcurrentbookid());
        return CEKResponse.builder()
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
                User currentUser = new User();
                if(currentUserMap.containsKey(sessionHolder.getSession().getUser().getUserId())) {
                    currentUser = currentUserMap.get(sessionHolder.getSession().getUser().getUserId());
                }else{
                    return ERROR_USER_MESSAGE;
                }
                Book currentBook = new Book();
                if(currentBookMap.containsKey(currentUser.getlineuserid() + currentUser.getcurrentbookid())) {
                    currentBook = currentBookMap.get(currentUser.getlineuserid() + currentUser.getcurrentbookid());
                }else{
                    return ERROR_BOOK_MESSAGE;
                }
                if(currentBook.gettalklist().size() == 0) {
                    return ERROR_BOOK_MESSAGE;
                }
                // set mode to session
                sessionHolder.getSessionAttributes().put(MODE, MODE_BUNSHIN);
                // set mode to status
                sessionHolder.getSessionAttributes().put(STATUS, STATUS_START);
                String result = INVOKE_MESSAGE;
                result = result.replace("{0}",currentUser.getname());

                return result;
            case "defaultMode":
                return ERROR_SOUND_MESSAGE;
            default:
                return mode + ERROR_MODE_MESSAGE;
        }
    }

    /**
     * ブック内の読み上げテキストを返す
     * @param sessionHolder
     * @return
     * @throws IOException
     */
    private String callbackText(SessionHolder sessionHolder) throws IOException {

        String result = "";
        try {
            log.info("currentUserMap : " + currentUserMap.toString());
            User currentUser = currentUserMap.get(sessionHolder.getSession().getUser().getUserId());
            log.info("currentBookMap : " + currentBookMap.toString());
            Book currentBook = currentBookMap.get(currentUser.getlineuserid() + currentUser.getcurrentbookid());
            Integer currentPage = currentUser.getcurrentsectionsequence();
            log.info("currentPage : " + currentPage);
            log.info("currentBook.gettalklist().size() : " + currentBook.gettalklist().size());
            if (currentBook.gettalklist().size() > currentPage && currentPage >= 0){
                result = currentBook.gettalklist().get(currentPage);
            } else if (currentBook.gettalklist().size() == currentPage) {
                result = END_MESSAGE;
                // set default to User Sequence
                currentUser.setcurrentsectionsequence(0);
                // set status to session
                sessionHolder.getSessionAttributes().put(STATUS, STATUS_STOP);
                // set mode to session
                sessionHolder.getSessionAttributes().put(MODE, MODE_DEFAULT);
            } else {
                result = ERROR_SOUND_MESSAGE;
            }
        } catch (Exception e){
            log.info(e.getMessage());
            result = ERROR_SOUND_MESSAGE;
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
        Integer currentPage = currentUser.getcurrentsectionsequence();
        if (currentOrder.equals("次")) {
            currentUser.setcurrentsectionsequence(currentPage + 1);
            result = callbackText(sessionHolder);
        } else if (currentOrder.equals("前") && currentPage != 0) {
            currentUser.setcurrentsectionsequence(currentPage - 1);
            result = callbackText(sessionHolder);
        } else {
            result = ERROR_SOUND_MESSAGE;
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
        String url = URLFORAPI + "/users/" + sessionHolder.getSession().getUser().getUserId();
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
                        ResponseBody body = response.body();
                        String result = body.string();
                        // get from response
                        JSONObject resdto = new JSONObject(result);
                        JSONArray items = resdto.getJSONArray("Items");
                        JSONObject item = items.getJSONObject(0);
                        User currentUser = new ObjectMapper().readValue(item.toString(), User.class);;
                        currentUserMap.put(currentUser.getlineuserid(), currentUser);
                        callGetBookAPI(currentUser.getlineuserid(), currentUser.getcurrentbookid());
                        log.info("currentUser getName : " + currentUser.getname());
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
        String url = URLFORAPI + "/users/" + lineUserId + "/books/" + bookId;
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
                        ResponseBody body = response.body();
                        String result = body.string();
                        // get from response
                        JSONObject resdto = new JSONObject(result);
                        JSONArray items = resdto.getJSONArray("Items");
                        JSONObject item = items.getJSONObject(0);
                        // get book from response
                        Book currentBook = new ObjectMapper().readValue(item.toString(), Book.class);
                        currentBookMap.put(currentBook.getlineuserid() + currentBook.getbookid(), currentBook);
                        log.info("currentBook gettalklist : " + currentBook.gettalklist());
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
