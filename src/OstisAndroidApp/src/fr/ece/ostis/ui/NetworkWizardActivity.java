package fr.ece.ostis.ui;

import fr.ece.ostis.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

public class NetworkWizardActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.acitivity_network);
		
		// Find controls
		final RadioButton buttonMethod1 = (RadioButton) findViewById(R.id.radioButtonMethod1);
		final RadioButton buttonMethod2 = (RadioButton) findViewById(R.id.radioButtonMethod2);
		final Button buttonNext = (Button) findViewById(R.id.buttonNext);
		
		// Set on click listeners
		buttonNext.setOnClickListener(new OnClickListener(){
			
			
			@Override
			public void onClick(View v){
				setContentView(R.layout.activity_network_method2_step1);
			}
			
		});
		
		buttonMethod1.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				buttonMethod2.setChecked(!buttonMethod1.isChecked());
			}
		});
		
		buttonMethod2.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				buttonMethod1.setChecked(!buttonMethod2.isChecked());
			}
		});
		
	}
	
}
