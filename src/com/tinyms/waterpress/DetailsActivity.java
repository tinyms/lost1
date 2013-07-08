package com.tinyms.waterpress;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.Menu;
import android.widget.TextView;

public class DetailsActivity extends Activity {
	public static String KEY_TEAM_NAMES_TITLE = "TEAM_NAMES_TITLE";
	public static String KEY_STRIKE = "STRIKE";
	public static String KEY_ODDS = "ODDS";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		
		TextView tv_strike = (TextView)this.findViewById(R.id.textView_strike);
		TextView tv_odds = (TextView)this.findViewById(R.id.textView_odds);
		Intent intent = this.getIntent();
		this.setTitle(intent.getStringExtra(KEY_TEAM_NAMES_TITLE));
		tv_strike.setText(Html.fromHtml(intent.getStringExtra(KEY_STRIKE)));
		tv_odds.setText(Html.fromHtml(intent.getStringExtra(KEY_ODDS)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.details, menu);
		return true;
	}

}
