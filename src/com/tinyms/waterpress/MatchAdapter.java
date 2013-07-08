/**
 * 
 */
package com.tinyms.waterpress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author tinyms
 *
 */
public class MatchAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<Map<String,String>> matchData = new ArrayList<Map<String,String>>();
	public List<Map<String, String>> getMatchData() {
		return matchData;
	}

	public void setMatchData(List<Map<String, String>> matchData) {
		this.matchData = matchData;
		inflater = LayoutInflater.from(context);
	}

	public MatchAdapter() {
	}

	public MatchAdapter(Context context) {
		super();
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		if(this.getMatchData()!=null){
			return this.getMatchData().size();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int arg0) {
		if(this.getMatchData()!=null){
			return this.getMatchData().get(arg0);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		if(view==null){
			view = this.inflater.inflate(R.layout.match_item, null);
		}
		Map<String,String> item = this.getMatchData().get(position);
		TextView title = (TextView)view.findViewById(R.id.ItemTitle);
		TextView sub_title = (TextView)view.findViewById(R.id.ItemText);
		title.setText(item.get("ItemTitle"));
		sub_title.setText(Html.fromHtml(item.get("ItemText")));
		return view;
	}

}
