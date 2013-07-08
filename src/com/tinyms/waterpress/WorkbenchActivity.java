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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class WorkbenchActivity extends Activity {
	public static String LogKey = "WaterPress";
	private static String DB_PATH = "/data/data/com.tinyms.waterpress/database/";
	private static String DB_NAME = "_cache";
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
				
				StringBuffer sb_strike = new StringBuffer();
				sb_strike.append("实力: <font color='#336699'>"+item.get("Result")+"</font>");
				sb_strike.append(" 赛果: "+GetResultChineseStyle(item.get("Actual_Result"))+" <font color='#CC6600'>"+item.get("Score")+"</font>");
				sb_strike.append(" 让球: <font color='#FF0033'>"+item.get("Asia")+"</font><br/>");
				
				sb_strike.append("<br/>近10场: "+item.get("Last10TextStyle")+"<br/>");
				sb_strike.append("近06场: "+item.get("Last6TextStyle")+"<br/>");
				sb_strike.append("近04场: "+item.get("Last4TextStyle")+"<br/>");
				
				String text_strike = sb_strike.toString();
				text_strike += "<br/>近6场战绩: <font color='#003366'>"+item.get("Last4BattleHistoryDesc")+"</font><br/>";
				
				StringBuffer sb = new StringBuffer();
				sb.append(oddsValueEmptyIf("威廉","WL",item));
				sb.append(oddsValueEmptyIf("立博","LB",item));
				sb.append(oddsValueEmptyIf("易博","YSB",item));
				sb.append(oddsValueEmptyIf("贝塔","365",item));
				sb.append(oddsValueEmptyIf("澳门","AM",item));
				sb.append("<br/>");
				sb.append(oddsChangeEmptyIf("威廉","WL",item));
				sb.append(oddsChangeEmptyIf("立博","LB",item));
				sb.append(oddsChangeEmptyIf("易博","YSB",item));
				sb.append(oddsChangeEmptyIf("贝塔","365",item));
				sb.append(oddsChangeEmptyIf("澳门","AM",item));
				sb.append("<br/>");
				sb.append(oddsModelChangeEmptyIf("威廉","WL",item));
				sb.append(oddsModelChangeEmptyIf("立博","LB",item));
				sb.append(oddsModelChangeEmptyIf("易博","YSB",item));
				sb.append(oddsModelChangeEmptyIf("贝塔","365",item));
				sb.append(oddsModelChangeEmptyIf("澳门","AM",item));
				
				Intent intent = new Intent(WorkbenchActivity.this,DetailsActivity.class);
				intent.putExtra(DetailsActivity.KEY_STRIKE, text_strike);
				intent.putExtra(DetailsActivity.KEY_ODDS, sb.toString());
				intent.putExtra(DetailsActivity.KEY_TEAM_NAMES_TITLE, "["+item.get("EventName")+"] "+item.get("TeamNames"));
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
    
    private void QueryHistoryData(){
    	query_match();
		if(ds!=null){
			ds.notifyDataSetChanged();
			Nodify("更换成功.");
		}
    }
    
    private static String oddsValueEmptyIf(String comName,String comKey,Map<String,String> item){
		if(item.get("Odds_"+comKey)!=null&&!"".equals(item.get("Odds_"+comKey))){
			return comName+": "+item.get("Odds_"+comKey)+" ~ "+item.get("Odds_"+comKey+"_Change")+"<br/>";
		}
		return "";
	}
    
    private static String oddsChangeEmptyIf(String comName,String comKey,Map<String,String> item){
    	if(item.get("Odds_"+comKey)!=null&&!"".equals(item.get("Odds_"+comKey))){
			return comName+": "+OddsStatistics(item.get("Odds_"+comKey),item.get("Odds_"+comKey+"_Change"))+"<br/>";
		}
		return "";
    }
    
    private static String oddsModelChangeEmptyIf(String comName,String comKey,Map<String,String> item){
    	String changeModel = OddsModelStatistics(item.get("Odds_"+comKey),item.get("Odds_"+comKey+"_Change"));
    	if(!"".equals(changeModel)){
    		return comName+": "+changeModel+"<br/>";
    	}
		return "";
    }
    
    private static String OddsStatistics(String oddsStart,String oddsEnd){
    	StringBuffer diff = new StringBuffer();
    	float[] start_odds = OddsToFloats(oddsStart);
    	float[] end_odds = OddsToFloats(oddsEnd);
    	if((start_odds.length==3) && (end_odds.length==3)){
    		float win_diff = end_odds[0] - start_odds[0];
    		float draw_diff = end_odds[1] - start_odds[1];
    		float lost_diff = end_odds[2] - start_odds[2];
    		
    		if(win_diff>0){
    			diff.append(" <font color='red'>"+FormatFloatWith2Bit(win_diff)+"</font>");
    		}else if(win_diff<0){
    			diff.append(" <font color='green'>"+FormatFloatWith2Bit(win_diff)+"</font>");
    		}if(win_diff==0){
    			diff.append(" 0.00");
    		}
    		
    		if(draw_diff>0){
    			diff.append(" <font color='red'>"+FormatFloatWith2Bit(draw_diff)+"</font>");
    		}else if(draw_diff<0){
    			diff.append(" <font color='green'>"+FormatFloatWith2Bit(draw_diff)+"</font>");
    		}if(draw_diff==0){
    			diff.append(" 0.00");
    		}
    		
    		if(lost_diff>0){
    			diff.append(" <font color='red'>"+FormatFloatWith2Bit(lost_diff)+"</font>");
    		}else if(lost_diff<0){
    			diff.append(" <font color='green'>"+FormatFloatWith2Bit(lost_diff)+"</font>");
    		}if(lost_diff==0){
    			diff.append(" 0.00");
    		}
    	}
    	return diff.toString().trim();
    }
    //两位小数
    public static String FormatFloatWith2Bit(float v){
    	float c = (float)(Math.round(v*100))/100;
    	return String.valueOf(c);
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
        MenuItem query_history_data_top_draw = menu.findItem(R.id.query_history_data_top_draw);
        query_history_data_top_draw.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				QueryHistoryData("top_draw");
				return true;
			}});
        return true;
    }
    
    private void query_match(){
    	matchs.clear();
    	SQLite sql = new SQLite(WorkbenchActivity.this,DB_PATH+DB_NAME,null,3);
    	SQLiteDatabase db = sql.getReadableDatabase();
    	String querySQL = "SELECT ID,Score,Asia,Result,Actual_Result,TeamNames,Last10TextStyle,Last6TextStyle," +
    			"Last4TextStyle,Odds_WL,Odds_AM,Odds_LB,Odds_365,Odds_YSB," +
    			"Odds_WL_Change,Odds_AM_Change,Odds_LB_Change,Odds_365_Change,Odds_YSB_Change," +
    			"EventName,Last4BattleHistoryDesc,Top_Draw FROM match ";
		if (!"310".equals(MATCH_RESULT_EXPRESSION)) {
			if(MATCH_RESULT_EXPRESSION.indexOf("_to_")!=-1){
				String[] exps = MATCH_RESULT_EXPRESSION.split("_to_");
				querySQL += "WHERE Actual_Result=" + exps[1] + " AND Result LIKE '%"+exps[0]+"%'";
			}else if(MATCH_RESULT_EXPRESSION.equals("top_draw")){
				querySQL += "WHERE Top_Draw >= 0.4";
			}
			else{
				querySQL += "WHERE Actual_Result=" + MATCH_RESULT_EXPRESSION;
			}
		}
    	querySQL+=" ORDER BY RANDOM() LIMIT 14";
    	Log.v(LogKey, querySQL);
    	Cursor c = db.rawQuery(querySQL, null);
    	c.moveToFirst();
    	while(!c.isAfterLast()){
    		Map<String,String> item = new HashMap<String,String>();
    		item.put("ID", String.valueOf(c.getInt(0)));
    		item.put("Score", c.getString(1));
    		int rq_int = c.getInt(2);
    		String rq = String.valueOf(rq_int);
    		if(rq_int>0){
    			rq = "+"+rq;
    		}
    		item.put("Asia", rq);
    		item.put("Result", c.getString(3));
    		item.put("Actual_Result", String.valueOf(c.getInt(4)));
    		item.put("TeamNames", c.getString(5));
    		item.put("Last10TextStyle", c.getString(6));
    		item.put("Last6TextStyle", c.getString(7));
    		item.put("Last4TextStyle", c.getString(8));
    		item.put("Odds_WL", c.getString(9));
    		item.put("Odds_AM", c.getString(10));
    		item.put("Odds_LB", c.getString(11));
    		item.put("Odds_365", c.getString(12));
    		item.put("Odds_YSB", c.getString(13));
    		item.put("Odds_WL_Change", c.getString(14));
    		item.put("Odds_AM_Change", c.getString(15));
    		item.put("Odds_LB_Change", c.getString(16));
    		item.put("Odds_365_Change", c.getString(17));
    		item.put("Odds_YSB_Change", c.getString(18));
    		item.put("EventName", c.getString(19));
    		item.put("Last4BattleHistoryDesc", c.getString(20));
    		
    		String result = GetResultChineseStyle(item.get("Actual_Result"));
    		item.put("ItemTitle", "["+item.get("EventName")+"]"+item.get("TeamNames"));
    		String odds = "";
    		if(item.get("Odds_WL")!=null&&!"".equals(item.get("Odds_WL"))){
    			odds = item.get("Odds_WL")+":&lt;威廉&gt;";
    		}else if(item.get("Odds_365")!=null&&!"".equals(item.get("Odds_365"))){
    			odds = item.get("Odds_365")+":&lt;贝塔&gt;";
    		}else if(item.get("Odds_LB")!=null&&!"".equals(item.get("Odds_LB"))){
    			odds = item.get("Odds_LB")+":&lt;立博&gt;";
    		}
    		//odds = "<font color='#000000'>"+odds+"</font>";
    		item.put("ItemText", "["+result+"]"+"(<font color='#336699'>"+item.get("Result")+"</font>)"+odds);
    		matchs.add(item);
    		c.moveToNext();
    	}
    	c.close();
    	db.close();
    }
    
    private void CheckDatabse(){
    	Log.v(LogKey, "Check Database..");
    	if ((new File(DB_PATH + DB_NAME)).exists() == false) {
			File f = new File(DB_PATH);
			if (!f.exists()) {
				f.mkdir();
			}

			try {
				InputStream is = getBaseContext().getAssets().open(DB_NAME);
				OutputStream os = new FileOutputStream(DB_PATH + DB_NAME);
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
		}
    }
}
