package com.tinyms.waterpress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class WorkbenchActivity extends Activity {
	public static String LogKey = "WaterPress";
	private static String DB_NAME = "matchs";
	private View mainView;
	private MatchAdapter ds = null;
	private static String MATCH_RESULT_EXPRESSION = "310";
	private final List<Map<String,String>> matchs = new ArrayList<Map<String,String>>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.CheckDatabse();
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        mainView = inflater.inflate(R.layout.workbench_layout, null);
        setContentView(mainView);
        RelativeLayout bottomLayout = (RelativeLayout)findViewById(R.id.MainButtomBarLayout);
        bottomLayout.getBackground().setAlpha(200);
        ListView matchs_grid = (ListView)findViewById(R.id.listView_matchs);
        matchs_grid.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				Map<String,String> item = (Map<String,String>)matchs.get(Integer.valueOf(String.valueOf(id)));
				QueryMatchOddsDetails(item);
				StringBuffer sb_strike = new StringBuffer();
				sb_strike.append("<br/>总10场: "+item.get("last_mix")+"<br/>");
				sb_strike.append("近10场: "+item.get("last_10")+"<br/>");
				sb_strike.append("近06场: "+item.get("last_6")+"<br/>");
				sb_strike.append("近04场: "+item.get("last_4")+"<br/>");
				
				String text_strike = sb_strike.toString();
				StringBuffer sb_tip = new StringBuffer();
				sb_tip.append("实力: <font color='#336699'>"+item.get("balls_diff")+"</font>");
				sb_tip.append(" 预测: <font color='#FF0033'>"+item.get("detect_result")+"</font>");
				sb_tip.append(" 赛果: "+GetResultChineseStyle(item.get("actual_result"))+" <font color='#CC6600'>"+item.get("score")+"</font><br/>");
				text_strike += "<br/>总6场战绩: <font color='#003366'>"+item.get("last_mix_battle")+"</font>";
				text_strike += "<br/>近6场战绩: <font color='#003366'>"+item.get("last_battle")+"</font><br/><br/>";
				text_strike += sb_tip.toString();
				
				StringBuffer sb = new StringBuffer();
				sb.append(oddsValueEmptyIf("威廉","WL",item));
				sb.append(oddsValueEmptyIf("立博","LB",item));
				sb.append(oddsValueEmptyIf("韦德","WD",item));
				sb.append(oddsValueEmptyIf("贝塔","365",item));
				sb.append(oddsValueEmptyIf("必赢","Bwin",item));
				sb.append(oddsValueEmptyIf("因特","Inerwetten",item));
				sb.append(oddsValueEmptyIf("易博","YSB",item));
				sb.append(oddsValueEmptyIf("澳门","AM",item));
				//sb.append(oddsValueEmptyIf("皇冠","HG",item));
				
				
				//sb.append(oddsValueEmptyIf("十贝","10bet",item));
				sb.append("<br/>");
				sb.append(oddsChangeEmptyIf("威廉","WL",item));
				sb.append(oddsChangeEmptyIf("立博","LB",item));
				sb.append(oddsChangeEmptyIf("韦德","WD",item));
				sb.append(oddsChangeEmptyIf("贝塔","365",item));
				sb.append(oddsChangeEmptyIf("必赢","Bwin",item));
				sb.append(oddsChangeEmptyIf("因特","Inerwetten",item));
				sb.append(oddsChangeEmptyIf("易博","YSB",item));
				sb.append(oddsChangeEmptyIf("澳门","AM",item));
				//sb.append(oddsChangeEmptyIf("皇冠","HG",item));
				
				
				//sb.append(oddsChangeEmptyIf("十贝","10bet",item));
//				sb.append("<br/>");
//				sb.append(oddsModelChangeEmptyIf("威廉","WL",item));
//				sb.append(oddsModelChangeEmptyIf("立博","LB",item));
//				sb.append(oddsModelChangeEmptyIf("易博","YSB",item));
//				sb.append(oddsModelChangeEmptyIf("贝塔","365",item));
//				sb.append(oddsModelChangeEmptyIf("澳门","AM",item));
//				sb.append(oddsModelChangeEmptyIf("Iner","Inerwetten",item));
//				sb.append(oddsModelChangeEmptyIf("皇冠","HG",item));
//				sb.append(oddsModelChangeEmptyIf("韦德","WD",item));
//				sb.append(oddsModelChangeEmptyIf("Bwin","Bwin",item));
//				sb.append(oddsModelChangeEmptyIf("10bet","10bet",item));
				
				Intent intent = new Intent(WorkbenchActivity.this,DetailsActivity.class);
				intent.putExtra(DetailsActivity.KEY_STRIKE, text_strike);
				intent.putExtra(DetailsActivity.KEY_ODDS, sb.toString());
				intent.putExtra(DetailsActivity.KEY_TEAM_NAMES_TITLE, "["+item.get("evt_name")+"] "+item.get("vs_team"));
				startActivity(intent);
			}});
        ImageButton refresh = (ImageButton)findViewById(R.id.btn_310_search);
        refresh.setClickable(true);
        refresh.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				QueryHistoryData();
			}});
        ds = new MatchAdapter(this);
        ds.setMatchData(matchs);
        matchs_grid.setAdapter(ds);
        this.QueryHistoryData("310");
    }
    private void QueryHistoryData(String express){
    	MATCH_RESULT_EXPRESSION = express;
    	query_match();
		if(ds!=null){
			ds.notifyDataSetChanged();
			Nodify("更换成功.");
		}
    }
    private void QueryMatchOddsDetails(Map<String,String> item){
    	String sql = "SELECT com_name,r_3,r_1,r_0,r_3_c,r_1_c,r_0_c FROM lottery_odds WHERE battle_id=?";
    	SQLite sqlite = new SQLite(WorkbenchActivity.this,DatabasePath(),null,3);
    	SQLiteDatabase db = sqlite.getReadableDatabase();
    	Cursor c = db.rawQuery(sql, new String[]{item.get("id")});
    	c.moveToFirst();
    	while(!c.isAfterLast()){
    		String name = c.getString(0);
    		String r_3 = String.valueOf(FormatFloatWith2Bit(c.getDouble(1)));
    		String r_1 = String.valueOf(FormatFloatWith2Bit(c.getDouble(2)));
    		String r_0 = String.valueOf(FormatFloatWith2Bit(c.getDouble(3)));
    		String r_3_c = String.valueOf(FormatFloatWith2Bit(c.getDouble(4)));
    		String r_1_c = String.valueOf(FormatFloatWith2Bit(c.getDouble(5)));
    		String r_0_c = String.valueOf(FormatFloatWith2Bit(c.getDouble(6)));
    		if("WL".equals(name)){
    			item.put("Odds_WL", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_WL_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}else if("LB".equals(name)){
    			item.put("Odds_LB", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_LB_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}else if("YB".equals(name)){
    			item.put("Odds_YSB", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_YSB_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}else if("BT".equals(name)){
    			item.put("Odds_365", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_365_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}else if("AM".equals(name)){
    			item.put("Odds_AM", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_AM_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}else if("Inerwetten".equals(name)){//other
    			item.put("Odds_Inerwetten", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_Inerwetten_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}else if("HG".equals(name)){
    			item.put("Odds_HG", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_HG_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}else if("WD".equals(name)){
    			item.put("Odds_WD", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_WD_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}else if("Bwin".equals(name)){
    			item.put("Odds_Bwin", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_Bwin_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}else if("10bet".equals(name)){
    			item.put("Odds_10bet", r_3+" "+r_1+" "+r_0);
    			item.put("Odds_10bet_Change", r_3_c+" "+r_1_c+" "+r_0_c);
    		}
    		c.moveToNext();
    	}
    	c.close();
    	db.close();
    }
    private void QueryHistoryData(){
    	query_match();
		if(ds!=null){
			ds.notifyDataSetChanged();
			Nodify("更换成功.");
		}
    }
    
    private static boolean empty(String str){
    	if(str==null){
    		return true;
    	}
    	String tmp = str.trim();
    	if("".equals(tmp)){
    		return true;
    	}
    	return false;
    }
    
    private static String oddsValueEmptyIf(String comName,String comKey,Map<String,String> item){
    	if(!empty(item.get("Odds_"+comKey))){
    		return comName+": "+item.get("Odds_"+comKey)+" ~ "+item.get("Odds_"+comKey+"_Change")+"<br/>";
    	}
		return "";
	}
    
    private static String oddsChangeEmptyIf(String comName,String comKey,Map<String,String> item){
    	if(!empty(item.get("Odds_"+comKey))){
    		return comName+": "+OddsStatistics(item.get("Odds_"+comKey),item.get("Odds_"+comKey+"_Change"))+"<br/>";
    	}
		return "";
    }
    
    private static String oddsModelChangeEmptyIf(String comName,String comKey,Map<String,String> item){
    	if(!empty(item.get("Odds_"+comKey))){
    		String changeModel = OddsModelStatistics(item.get("Odds_"+comKey),item.get("Odds_"+comKey+"_Change"));
	    	if(!"".equals(changeModel)){
	    		return comName+": "+changeModel+"<br/>";
	    	}
    	}
		return "";
    }
    
    private static String OddsStatistics(String oddsStart,String oddsEnd){
    	if(oddsStart==null||oddsStart.equals("")){
    		return "";
    	}
    	StringBuffer diff = new StringBuffer();
    	float[] start_odds = OddsToFloats(oddsStart);
    	float[] end_odds = OddsToFloats(oddsEnd);
    	if((start_odds.length==3) && (end_odds.length==3)){
    		float win_diff = end_odds[0] - start_odds[0];
    		float draw_diff = end_odds[1] - start_odds[1];
    		float lost_diff = end_odds[2] - start_odds[2];
    		
    		if(win_diff>0){
    			diff.append(" <font color='red'>+"+FormatFloatWith2Bit(win_diff)+"</font>");
    		}else if(win_diff<0){
    			diff.append(" <font color='green'>"+FormatFloatWith2Bit(win_diff)+"</font>");
    		}if(win_diff==0){
    			diff.append(" +0.00");
    		}
    		
    		if(draw_diff>0){
    			diff.append(" <font color='red'>+"+FormatFloatWith2Bit(draw_diff)+"</font>");
    		}else if(draw_diff<0){
    			diff.append(" <font color='green'>"+FormatFloatWith2Bit(draw_diff)+"</font>");
    		}if(draw_diff==0){
    			diff.append(" +0.00");
    		}
    		
    		if(lost_diff>0){
    			diff.append(" <font color='red'>+"+FormatFloatWith2Bit(lost_diff)+"</font>");
    		}else if(lost_diff<0){
    			diff.append(" <font color='green'>"+FormatFloatWith2Bit(lost_diff)+"</font>");
    		}if(lost_diff==0){
    			diff.append(" +0.00");
    		}
    	}
    	return diff.toString().trim();
    }
    //两位小数
    public static String FormatFloatWith2Bit(double v){
    	DecimalFormat f = new DecimalFormat("0.00");
    	return f.format(v);
    }
    
    private static String OddsModelStatistics(String oddsStart,String oddsEnd){
    	String start = OddsModel(oddsStart);
    	String end = OddsModel(oddsEnd);
    	if(!start.equals(end)){
    		return start+" -&gt "+end;
    	}
    	return "";
    }
    
    private static float[] OddsToFloats(String odds_str){
    	float[] odds = new float[3];
    	if(odds_str!=null&&!"".equals(odds_str)){
    		String[] items = odds_str.split(" ");
    		odds[0]=Float.parseFloat(items[0]);
    		odds[1]=Float.parseFloat(items[1]);
    		odds[2]=Float.parseFloat(items[2]);
    	}
    	return odds;
    }
    
    private static String OddsModel(String odds_str){
    	StringBuffer sb = new StringBuffer();
    	Pattern pattern = Pattern.compile("\\d+(?=\\.)");
    	Matcher m = pattern.matcher(odds_str);
    	while(m.find()){
    		sb.append(m.group());
    	}
    	return sb.toString();
    }
    
    private String GetResultChineseStyle(String result){
    	if("3".equals(result)){
    		return "<font color='red'>胜</font>";
    	}else if("1".equals(result)){
    		return "<font color='green'>平</font>";
    	}else if("0".equals(result)){
    		return "<font color='blue'>负</font>";
    	}
    	return "";
    }
    private void Nodify(String msg){
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workbench, menu);
        
        MenuItem query_history_data_3 = menu.findItem(R.id.query_history_data_3);
        query_history_data_3.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				QueryHistoryData("3");
				return true;
			}});
        MenuItem query_history_data_1 = menu.findItem(R.id.query_history_data_1);
        query_history_data_1.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				QueryHistoryData("1");
				return true;
			}});
        MenuItem query_history_data_0 = menu.findItem(R.id.query_history_data_0);
        query_history_data_0.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				QueryHistoryData("0");
				return true;
			}});
        MenuItem query_history_data_3_to_0 = menu.findItem(R.id.query_history_data_3_to_0);
        query_history_data_3_to_0.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				QueryHistoryData("3_to_0");
				return true;
			}});
        MenuItem query_history_data_0_to_3 = menu.findItem(R.id.query_history_data_0_to_3);
        query_history_data_0_to_3.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				QueryHistoryData("0_to_3");
				return true;
			}});
        MenuItem query_history_data_1_to_1 = menu.findItem(R.id.query_history_data_1_to_1);
        query_history_data_1_to_1.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				QueryHistoryData("1_to_1");
				return true;
			}});
        return true;
    }
    
    private void query_match(){
    	matchs.clear();
    	SQLite sql = new SQLite(WorkbenchActivity.this,DatabasePath(),null,3);
    	SQLiteDatabase db = sql.getReadableDatabase();
    	String querySQL = "SELECT id,score,actual_result,detect_result,balls_diff,vs_team," +
    			"last_mix,last_10,last_6,last_4,last_mix_battle,last_battle,url_key,vs_date,evt_name FROM lottery_battle";
		if (!"310".equals(MATCH_RESULT_EXPRESSION)) {
			if(MATCH_RESULT_EXPRESSION.indexOf("_to_")!=-1){
				String[] exps = MATCH_RESULT_EXPRESSION.split("_to_");
				querySQL += " WHERE actual_result=" + exps[1] + " AND detect_result LIKE '%"+exps[0]+"%'";
			}
			else{
				querySQL += " WHERE actual_result=" + MATCH_RESULT_EXPRESSION;
			}
		}
    	querySQL+=" ORDER BY RANDOM() LIMIT 25";
    	Log.v(LogKey, querySQL);
    	Cursor c = db.rawQuery(querySQL, null);
    	c.moveToFirst();
    	while(!c.isAfterLast()){
    		Map<String,String> item = new HashMap<String,String>();
    		item.put("id", String.valueOf(c.getInt(0)));
    		item.put("score", c.getString(1));
    		item.put("actual_result", String.valueOf(c.getInt(2)));
    		item.put("detect_result", c.getString(3));
    		item.put("balls_diff", String.valueOf(c.getDouble(4)));
    		item.put("vs_team", c.getString(5));
    		item.put("last_mix", c.getString(6));
    		item.put("last_10", c.getString(7));
    		item.put("last_6", c.getString(8));
    		item.put("last_4", c.getString(9));
    		item.put("last_mix_battle", c.getString(10));
    		item.put("last_battle", c.getString(11));
    		item.put("url_key", c.getString(12));
    		item.put("vs_date", c.getString(13));
    		item.put("evt_name", c.getString(14));
    		
    		item.put("ItemTitle", "["+item.get("evt_name")+"]"+item.get("vs_team"));
    		StringBuffer sb_tip = new StringBuffer();
			sb_tip.append("实力: <font color='#336699'>"+item.get("balls_diff")+"</font>");
			sb_tip.append(" 预测: <font color='#FF0033'>"+item.get("detect_result")+"</font>");
			sb_tip.append(" 赛果: "+GetResultChineseStyle(item.get("actual_result"))+" <font color='#CC6600'>"+item.get("score")+"</font>");
    		item.put("ItemText", sb_tip.toString());
    		matchs.add(item);
    		c.moveToNext();
    	}
    	c.close();
    	db.close();
    }
    private String DatabasePath(){
    	File SDFile = android.os.Environment.getExternalStorageDirectory();
    	return SDFile.getAbsolutePath()  
                + File.separator + DB_NAME;
    }
    private static boolean isDebug = false;
    private void CheckDatabse(){
    	Log.v(LogKey, "Check Database..");
    	String state = android.os.Environment.getExternalStorageState();
    	if (state.equals(android.os.Environment.MEDIA_MOUNTED)) {
    		File myFile = new File(DatabasePath());
    		if (!myFile.exists()) { 
    			try {
    				InputStream is = getBaseContext().getAssets().open(DB_NAME);
    				OutputStream os = new FileOutputStream(myFile);
    				byte[] buffer = new byte[1024];
    				int length;
    				while ((length = is.read(buffer)) > 0) {
    					os.write(buffer, 0, length);
    				}
    				os.flush();
    				os.close();
    				is.close();
    				Log.v(LogKey, "Copy Database Success..");
    			} catch (Exception e) {
    				Log.v(LogKey, e.getMessage());
    			}
            }else{
            	if(isDebug){
            		myFile.delete();
            		this.CheckDatabse();
            	}
            }
    	}
    }
}
