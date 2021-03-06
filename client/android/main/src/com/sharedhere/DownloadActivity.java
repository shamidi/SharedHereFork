package com.sharedhere;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.sharedhere.model.SHClientServer;
import com.sharedhere.model.SHFileInfo;
import com.sharedhere.model.SHLocation;

/**
 * 
 * @author Zain
 *
 */
public class DownloadActivity extends ExpandableListActivity
{
	private SHLocation shCurrentLocation = null;
	private SHClientServer shConnection = null;
	JSONObject jsonObject = null;
	JSONArray jArray = null;	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ListContentTask task = new ListContentTask();
		task.execute();
	}	
	
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id){
		//TODO:Find a more graceful way of doing this. Its intent is to look for the "Download File" child being clicked.
		if (childPosition == 4){ 
			String filename = parent.getItemAtPosition(groupPosition).toString();
			 
			Intent i = new Intent(this, FileDialog.class);
			i.putExtra("SHLocation", shCurrentLocation);
			i.putExtra("FileName", filename);
			startActivity(i);		
		}
		return true;
	}
	
	private class ListContentTask extends AsyncTask<Void, Void, SHFileInfo[]> {
    	@Override
    	protected SHFileInfo[] doInBackground(Void... arg) {
			try {

				shCurrentLocation = (SHLocation) getIntent().getSerializableExtra("SHLocation");

				shConnection = new SHClientServer(getString(R.string.server_address));

				// Content available at current location returned from Client-Server
				// activity
				jArray = shConnection.listContent(shCurrentLocation);
							
				ArrayList<SHFileInfo> fileInformation = new ArrayList<SHFileInfo>();
				// Parsing the JSON array
				try {

					for (int i = 0; i < jArray.length(); i++) {
						jsonObject = jArray.getJSONObject(i);
						String filename = (String) jsonObject.getString("filename");
						Long size = Long.decode(jsonObject.getString("size"));
						String description = (String)jsonObject.getString("description");
						Double latitude = Double.valueOf(jsonObject.getString("latitude"));
						Double longitude = Double.valueOf(jsonObject.getString("longitude"));
								
						SHFileInfo fileInfo = new SHFileInfo(size, filename, description, latitude, longitude );
												
						fileInformation.add(fileInfo);
						Log.d("filename", fileInfo.toString());

					}
					SHFileInfo[] fnames = new SHFileInfo[fileInformation.size()];
					return fileInformation.toArray(fnames);
				
				} catch (Exception e) {
					return new SHFileInfo[0];
				}
			}
			catch (Exception e){
				return new SHFileInfo[0];
			}
    	}
    	
    	@Override
    	protected void onPostExecute(SHFileInfo[] result){
			// If no records available at this location show a message and exit activity
			if(result.length==0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.this);
				builder.setMessage("No files here!");
				builder.setPositiveButton("OK", new OnClickListener() {	
					public void onClick(DialogInterface dialog, int which) {
						DownloadActivity.this.finish();
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}else{							
				SHExpandableListAdapter expandableListAdapter = new SHExpandableListAdapter(result);
				setListAdapter(expandableListAdapter);				
			}
    	}
     }
	
	public class SHExpandableListAdapter extends BaseExpandableListAdapter {
        private String[] groups;
        private String[][] children;  
        int downloadChildElementPosition = 4;
        
        public SHExpandableListAdapter(SHFileInfo[] fileInfo){
        	groups = new String[fileInfo.length];
        	ArrayList<String[]> childDataList = new ArrayList<String[]>();//use this to build the child 2d array
        	String[] childData;
        	for (int i =0; i < groups.length; i++){
        		SHFileInfo file = fileInfo[i];
        		groups[i] = file.GetName();
        		childData = new String[5];
        		childData[0]=file.GetSize().toString() + " KB";
        		childData[1]="Desc:"+ file.GetDescription();
        		childData[2]="Latitude: " +file.GetLatitude().toString();
        		childData[3]="Longitude: " +file.GetLongitude().toString();
        		childData[4]="Download File";
        		childDataList.add(childData);
        	}
        	
        	children = childDataList.toArray(new String[fileInfo.length][5]);        	
        }
        
        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, 64);

            TextView textView = new TextView(DownloadActivity.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;
        }

        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            if (childPosition == downloadChildElementPosition) 
            	return true;
            else return false;
        }

        public boolean hasStableIds() {
            return true;
        }

    }
	
}
