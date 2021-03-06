package fr.ece.ostis.ui;

import java.util.ArrayList;

import fr.ece.ostis.R;
import fr.ece.ostis.actions.Action;
import fr.ece.ostis.actions.ActionManager;
import fr.ece.ostis.actions.BaseAction;
import fr.ece.ostis.actions.ComposedAction;
import fr.ece.ostis.lang.LanguageManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-05
 */
public class ActionsListActivity extends ConnectedActivity{

	protected LanguageManager mLanguageManager = null;
	protected ActionManager mActionManager = null;
	protected ActionArrayAdapterItem mAdapter = null;
	protected ListView mActionListView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.activity_actions_list);
		
		// Setup aciton bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Find controls
		mActionListView = (ListView) findViewById(R.id.listViewActions);
		
		// Add on click listeners
		mActionListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				if(mActionManager.getActions().get(position) instanceof ComposedAction){
					Intent intent = new Intent(ActionsListActivity.this, ActionActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("What", "Edit");
					bundle.putString("ActionId", mActionManager.getActions().get(position).getId());
					startActivity(intent);
					overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				}else{
					Toast toast = Toast.makeText(ActionsListActivity.this, getText(R.string.toast_not_composed_action), Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});
		
		// Retrieve action manager
		mLanguageManager = new LanguageManager();
		mActionManager = new ActionManager(this, mLanguageManager.getCurrentLocale());
		
		// Populate actions
		mAdapter = new ActionArrayAdapterItem(this, R.layout.list_action, mActionManager.getActions());
		mActionListView.setAdapter(mAdapter);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.actions, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
			case R.id.menu_action_add:
				Intent intent = new Intent(ActionsListActivity.this, ActionActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("What", "New");
				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onBoundToOstisService(){
		// TODO Auto-generated method stub
	}


	@Override
	protected void onBeforeUnbindFromOstisService(){
		// TODO Auto-generated method stub
	}
	

	@Override
	protected void onUnboundFromOstisService(){
		// TODO Auto-generated method stub
	}
	
	
	protected class ActionArrayAdapterItem extends ArrayAdapter<Action>{

	    Context mContext;
	    int mLayoutResourceId;
	    ArrayList<Action> mData = null;

	    
	    public ActionArrayAdapterItem(Context context, int layoutResourceId, ArrayList<Action> data){
	        super(context, layoutResourceId, data);
	        mContext = context;
	        mData = data;
	        mLayoutResourceId = layoutResourceId;
	    }

	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent){
	    	
            // Inflate the layout if not already created
	        if(convertView == null){
	            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
	            convertView = inflater.inflate(mLayoutResourceId, parent, false);
	        }

	        // action based on the position
	        Action action = mData.get(position);

	        // get the TextView and then set the text (item name) and tag (item ID) values
	        TextView textViewName = (TextView) convertView.findViewById(R.id.textViewName);
	        TextView textViewCommand = (TextView) convertView.findViewById(R.id.textViewCommand);
	        TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);
	        textViewName.setText(action.getName(mLanguageManager.getCurrentLocale()));
	        textViewCommand.setText("\" " + action.getVocalCommand(mLanguageManager.getCurrentLocale()) + " \"");
	        textViewDescription.setText(action.getDescription(mLanguageManager.getCurrentLocale()));

	        // Return view
	        return convertView;
	    }

	}

}
