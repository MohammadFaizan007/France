package com.SmartLightSensor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.SmartLightSensor.Fragments.AvailableDeviceFragment;
import com.SmartLightSensor.Fragments.CreateGroup;
import com.SmartLightSensor.Fragments.DashboardFragment;
import com.SmartLightSensor.Fragments.EditDeviceFragment;
import com.SmartLightSensor.Fragments.EditGroupFragment;
import com.SmartLightSensor.Fragments.NetworkFragment;
import com.SmartLightSensor.Fragments.ScanDeviceFragment;
import com.SmartLightSensor.R;
import com.SmartLightSensor.constant.Constants;

import butterknife.BindView;


public class HelperActivity extends AppCompatActivity /*implements BeaconConsumer, RangeNotifier*/ {
    Fragment fragment;
    int fragmentLoadCode;
    private static String TAG = "MyActivity";
    @BindView(R.id.title)
    TextView title_tv;
    private Fragment currentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
//
        }
        setContentView(R.layout.activity_helper);
        Intent intent=getIntent();
        String Test =  intent.getStringExtra("Unique_Key");
        String key1 =  intent.getStringExtra("first_key");
        String key2 =  intent.getStringExtra("second_key");
        String key3 =  intent.getStringExtra("third_key");
        String key4 =  intent.getStringExtra("fourth_key");
        if (intent==null)
        {
            finish();
            return;
        }
        if (intent.hasExtra(Constants.MAIN_KEY)) {
            switch (intent.getIntExtra(Constants.MAIN_KEY,0)) {
                case Constants.MY_NETWORK_CODE:
                    setTitle("Add Smart Device");
                    loadFragment(new ScanDeviceFragment());
                    break;

                case Constants.SMART_DEVICE_CODE:
                    setTitle("Smart Device");
                    loadFragment(new AvailableDeviceFragment());
//                    Toast.makeText(this, "Will be soon", Toast.LENGTH_SHORT).show();
                    break;
//
                case Constants.DASHBOARD_CODE:
                    setTitle("Dashboard");
                    loadFragment(new DashboardFragment());
                    break;
//
                case Constants.GROUP_CODE:
                    setTitle("Group");
                    loadFragment(new NetworkFragment());
                    break;
//
                case Constants.EDIT_GROUP:
                    EditGroupFragment editGroupFragment=new EditGroupFragment();
                    editGroupFragment.setGroupDetailsClass(intent.getParcelableExtra(Constants.GROUP_DETAIL_KEY));
                    setTitle("Edit Group");
                    loadFragment(editGroupFragment);
                    break;
                case Constants.EDIT_LIGHT:
                    EditDeviceFragment editDeviceFragment=new EditDeviceFragment();
                    editDeviceFragment.setDeviceData(intent.getParcelableExtra(Constants.LIGHT_DETAIL_KEY));
                    setTitle("Edit Light");
                    loadFragment(editDeviceFragment);
                    break;
                 case Constants.CREATE_GROUP:
                     setTitle("Create Group");
                     loadFragment(new CreateGroup());
                    break;
//
//                case Constants.DEMO_CODE:
//                    Toast.makeText(this, "Will be soon", Toast.LENGTH_SHORT).show();
//                    setTitle("Demo");
//
//                    break;
//
//                case Constants.ASSOCIATE:
//                    setTitle("Associate");
//                    loadFragment(new AssociateFragment());
//                    break;
//
//                case Constants.READ_ASSOCIATE:
//                    AssociateReadFragment associateReadFragment=new AssociateReadFragment();
//                    associateReadFragment.setDeviceData(intent.getParcelableExtra(Constants.LIGHT_DETAIL_KEY));
//                    setTitle("Read Status");
//                    loadFragment(associateReadFragment);
//                    break;
//
//                case Constants.ADD_ASSOCIATE:
//                    AssociateAddFragment associateAddFragment=new AssociateAddFragment();
//                    associateAddFragment.setDeviceData(intent.getParcelableExtra(Constants.LIGHT_DETAIL_KEY));
//                    setTitle("Add Status");
//                    loadFragment(associateAddFragment);
//                    break;
//
//                case Constants.READ_DETAILS:
//                    DetailsFragment detailsFragment=new DetailsFragment();
//                    detailsFragment.setDeviceData(intent.getParcelableExtra(Constants.LIGHT_DETAIL_KEY));
//                    setTitle("Light Details");
//                    loadFragment(detailsFragment);
//                    break;

            }
        }
    }
    @Override
    public void onBackPressed()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        else
            finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
//        menu.findItem(R.id.miProfile).setIcon(resizeImage(R.drawable.inferrixlogo,108,100));
        return true;
    }

//    private Drawable resizeImage(int resId, int w, int h) {
//        // load the origial Bitmap
//        Bitmap BitmapOrg = BitmapFactory.decodeResource(getResources(), resId);
//        int width = BitmapOrg.getWidth();
//        int height = BitmapOrg.getHeight();
//        int newWidth = w;
//        int newHeight = h;
//        // calculate the scale
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        // create a matrix for the manipulation
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth, scaleHeight);
//        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0,width, height, matrix, true);
//        return new BitmapDrawable(resizedBitmap);
//    }



    public void loadFragment(Fragment fragment)
    {
        String backStateName=fragment.getClass().getSimpleName();
        Log.w("LoadFragment",backStateName+" " +fragment.getClass().getName() );
        this.fragment = fragment;
        FragmentManager manager = getSupportFragmentManager();
        if ( manager.findFragmentByTag(backStateName) == null)
        {
            FragmentTransaction fragmentTransaction = manager.beginTransaction();
            fragmentTransaction.replace(R.id.main_frame_layout, fragment,backStateName);
            fragmentTransaction.addToBackStack(backStateName);
            fragmentTransaction.commitAllowingStateLoss();
//            return;
        }
        else
        {
            for (int i = manager.getBackStackEntryCount() - 1; i >=0; i--) {
                Log.w("ClassName",manager.getBackStackEntryAt(i).getName());
                if (!manager.getBackStackEntryAt(i).getName().equalsIgnoreCase(backStateName)) {
                    Log.w("ClassName",manager.getBackStackEntryAt(i).getName());
                    manager.popBackStack();
                } else {
                    manager.popBackStack();
                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame_layout, fragment, backStateName);
                    fragmentTransaction.addToBackStack(backStateName);
                    fragmentTransaction.commitAllowingStateLoss();
                    break;
                }
//                if (i==0)
            }
        }



    }




}
