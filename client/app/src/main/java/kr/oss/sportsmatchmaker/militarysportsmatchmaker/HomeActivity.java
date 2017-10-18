package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;


public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //Helper
    private SessionManager smgr;
    private HashMap<String, String> prof;
    private SimpleAdapter menuAdapter;

    //Widgets
    private TextView textWelcome;
    private Button logoutButton;
    private TextView textQStatus;
    private ListView homeMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        smgr = new SessionManager(getApplicationContext());
        prof = smgr.getProfile();


        // welcome message

        // logout button settings
        logoutButton = (Button) findViewById(R.id.logout);
        String user_name = prof.get(SessionManager.NAME);
        String user_rank = prof.get(SessionManager.RANK);
        textWelcome = (TextView) findViewById(R.id.home_welcome);
        textWelcome.setText("환영합니다, " + user_name + " " + user_rank + "님.\n오늘은 어떤 체육활동을 하시겠어요?");
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smgr.logout();
                finish();
            }
        });

        //TODO: get queue status
        // queue status message
        displayMatchStatus();

        // add adapter to listview. Long boring stuff, so factor into separate method.
        homeMenu = (ListView) findViewById(R.id.home_menu);
        setHomeMenu(homeMenu);
        homeMenu.setOnItemClickListener(this);
    }

    private void displayMatchStatus(){
        textQStatus = (TextView) findViewById(R.id.home_qstatus);

        // initialize asynchttpclient
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String loginURL = Proxy.SERVER_URL + ":" + Proxy.SERVER_PORT + "/process/getUserMatch";
        client.setCookieStore(smgr.myCookies);
        client.get(loginURL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean success = response.getBoolean("result");
                    if (success) {
                        JSONObject match = response.getJSONObject("match");
                        String gameTypeEng = match.getString("activityType");
                        String gameTypeKor = "족구";
                        if (gameTypeEng.equals("football"))
                            gameTypeKor = "축구";
                        else if (gameTypeEng.equals("basketball"))
                            gameTypeKor = "농구";
                        JSONArray players = match.getJSONArray("players");
                        String numPlayers = String.valueOf(players.length());
                        textQStatus.setText("현재 " + numPlayers + "명과 " + gameTypeKor + " 시합 대기중입니다.");
                    }
                    else {
                        String reason = response.getString("reason");
                        if (reason.equals("NoSuchMatchException")) {
                            textQStatus.setText("현재 대기중인 시합이 없습니다. 찾아보세요!");
                        }
                        else {
                            textQStatus.setText("오류: " + reason);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "데이터 오류입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                textQStatus.setText("매치 정보를 가져오지 못했습니다. 다시 접속해주세요.");
            }
        });


    }

    @Override
    // 회원정보 수정후 돌아옴.
    protected void onResume() {
        super.onResume();
        smgr.checkSession();
    }

    private void setHomeMenu(ListView homeMenu){
        ArrayList<HashMap<String, String>> hashMapMenuList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> menu1 = new HashMap<String, String>();
        menu1.put("line1", "전투체육 같이 할 사람 찾기");
        menu1.put("line2", "종목을 고르시면 자동으로 팀원과 상대방을 찾아드립니다.");
        hashMapMenuList.add(menu1);
        HashMap<String, String> menu2 = new HashMap<String, String>();
        menu2.put("line1", "전투체육 활동 등록 및 장소 예약");
        menu2.put("line2", "이미 사람을 다 모으셨나요? 장소를 잡아드립니다.");
        hashMapMenuList.add(menu2);
        HashMap<String, String> menu3 = new HashMap<String, String>();
        menu3.put("line1", "전투체육 일지");
        menu3.put("line2", "전우님의 전투체육 참여 현황을 편리하게 볼 수 있습니다.");
        hashMapMenuList.add(menu3);
        HashMap<String, String> menu4 = new HashMap<String, String>();
        menu4.put("line1", "프로필 수정");
        menu4.put("line2", "개인 프로필 정보를 변경합니다.");
        hashMapMenuList.add(menu4);
        String[] from = {"line1", "line2"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        menuAdapter = new SimpleAdapter(this, hashMapMenuList, android.R.layout.simple_list_item_2, from, to);
        homeMenu.setAdapter(menuAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.home_menu:
                switch(position){
                    case 0:
                        Intent intent = new Intent(getApplicationContext(), ChooseSportActivity.class);
                        startActivity(intent);
                        break;
                    //TODO: 구현
                    case 1:
                        Intent intent1 = new Intent(getApplicationContext(), ReservePlaceActivity.class);
                        startActivity(intent1);
                        break;
                    //프로필 수정
                    case 3:
                        Intent intent3 = new Intent(getApplicationContext(), EditProfileActivity.class);
                        startActivity(intent3);
                        break;
                    //장소 고르기
                    default:
                        Toast.makeText(getApplicationContext(), "아직 미구현", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }
}
